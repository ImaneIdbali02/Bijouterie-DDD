package com.enaya.service.auth.infrastructure.persistence;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.domain.valueobjects.AuthenticationAttempt;
import com.enaya.service.auth.domain.valueobjects.SessionToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.enaya.service.auth.infrastructure.persistence.AuthenticationMapper.*;

@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostgresAuthenticationRepository implements AuthenticationRepository {

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    // ==================== JPA/Hibernate Operations ====================

    @Override
    public Authentication findById(UUID id) {
        AuthenticationEntity entity = entityManager.find(AuthenticationEntity.class, id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public Authentication findByUsername(String username) {
        try {
            TypedQuery<AuthenticationEntity> query = entityManager.createQuery(
                    "SELECT a FROM AuthenticationEntity a WHERE LOWER(a.username) = LOWER(:username)",
                    AuthenticationEntity.class);
            query.setParameter("username", username);
            return toDomain(query.getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Authentication findByEmail(String email) {
        try {
            TypedQuery<AuthenticationEntity> query = entityManager.createQuery(
                    "SELECT a FROM AuthenticationEntity a WHERE LOWER(a.email) = LOWER(:email)",
                    AuthenticationEntity.class);
            query.setParameter("email", email);
            return toDomain(query.getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Authentication findByProviderAndProviderId(Authentication.AuthProvider provider, String providerId) {
        try {
            TypedQuery<AuthenticationEntity> query = entityManager.createQuery(
                    "SELECT a FROM AuthenticationEntity a WHERE a.provider = :provider AND a.providerUserId = :providerId",
                    AuthenticationEntity.class);
            query.setParameter("provider", provider);
            query.setParameter("providerId", providerId);
            return toDomain(query.getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Authentication save(Authentication authentication) {
        AuthenticationEntity entity = toEntity(authentication);
        
        // Check if entity exists in database
        AuthenticationEntity existingEntity = null;
        if (entity.getId() != null) {
            existingEntity = entityManager.find(AuthenticationEntity.class, entity.getId());
        }
        
        if (existingEntity == null) {
            // New entity - create a new one without ID
            AuthenticationEntity newEntity = new AuthenticationEntity();
            newEntity.setClientId(UUID.randomUUID());
            newEntity.setUsername(entity.getUsername());
            newEntity.setEmail(entity.getEmail());
            newEntity.setPasswordHash(entity.getPasswordHash());
            newEntity.setProvider(entity.getProvider());
            newEntity.setProviderUserId(entity.getProviderUserId());
            newEntity.setEnabled(entity.isEnabled());
            newEntity.setLocked(entity.isLocked());
            newEntity.setCreatedAt(LocalDateTime.now());
            newEntity.setUpdatedAt(LocalDateTime.now());
            newEntity.setVersion(0L);

            entityManager.persist(newEntity);
            entityManager.flush();
            return toDomain(newEntity);
        } else {
            // Existing entity - use merge
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setVersion(existingEntity.getVersion());
            AuthenticationEntity saved = entityManager.merge(entity);
            entityManager.flush();
            return toDomain(saved);
        }
    }

    @Override
    public void delete(Authentication authentication) {
        AuthenticationEntity entity = entityManager.find(AuthenticationEntity.class, authentication.getId());
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    // ==================== JDBC Template Operations for Sessions and Logs ====================

    public SessionToken saveSession(SessionToken sessionToken) {
        String sql = """
            
                INSERT INTO session_tokens (token, expires_at, authentication_id, ip_address, user_agent, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (token) DO UPDATE SET
                expires_at = EXCLUDED.expires_at,
                ip_address = EXCLUDED.ip_address,
                user_agent = EXCLUDED.user_agent
            """;

        jdbcTemplate.update(sql,
                sessionToken.token(),
                Timestamp.valueOf(sessionToken.dateExpiration()),
                sessionToken.userId(),
                sessionToken.ipAddress(),
                sessionToken.userAgent(),
                Timestamp.valueOf(LocalDateTime.now()));

        return sessionToken;
    }

    public SessionToken findSessionByToken(UUID token) {
        String sql = "SELECT * FROM session_tokens WHERE token = ? AND expires_at > NOW()";

        try {
            return jdbcTemplate.queryForObject(sql, sessionTokenRowMapper(), token);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteSession(UUID token) {
        String sql = "DELETE FROM session_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }

    public void deleteExpiredSessions() {
        String sql = "DELETE FROM session_tokens WHERE expires_at <= NOW()";
        int deleted = jdbcTemplate.update(sql);
        log.info("Deleted {} expired sessions", deleted);
    }

    public void deleteAllUserSessions(UUID userId) {
        String sql = "DELETE FROM session_tokens WHERE authentication_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void logAuthenticationAttempt(AuthenticationAttempt attempt) {
        String sql =
                """
            INSERT INTO authentication_attempts (date, ip_address, success, username, failure_reason, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                Timestamp.valueOf(attempt.getDate()),
                attempt.getIp(),
                attempt.isSuccess(),
                attempt.getUsername(),
                attempt.getFailureReason(),
                Timestamp.valueOf(LocalDateTime.now()));

        log.info(attempt.toLogString());
    }

    public List<AuthenticationAttempt> getFailedAttempts(String ipOrUsername, LocalDateTime since) {
                String sql = """
            SELECT * FROM authent
                pts 
            WHERE (ip_address = ?
                OR userna
                        AND success =
                false 
            AND
                date >= ?
            ORDER BY date DESC
            """;

        return jdbcTemplate.query(sql, authenticationAttemptRowMapper(),
                ipOrUsername, ipOrUsername, Timestamp.valueOf(since));
    }

    public int countFailedAttempts(String
                ipOrUsername, LocalDateTime since) {
        String sql = """
            SELECT COUNT(*) FROM
                authentication_attem
                    WHERE (ip_address = ?
                OR username = ?) 
            AND success = false 
            AND date >= ?
            """;

        return jdbcTemplate.queryForObject(sql, Integer.class,
                ipOrUsername, ipOrUsername, Timestamp.valueOf(since));
    }

    // ==================== Admin Operations ====================

    public Authentication createAdminUser(String username, String email, String passwordHash) {
        Authentication admin = Authentication.builder()
                .id(UUID.randomUUID())
                .clientId(UUID.randomUUID()) // ou un client ID spécifique pour l'admin
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .provider(Authentication.AuthProvider.LOCAL)
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return save(admin);
    }

    // ==================== Row Mappers ====================

    private RowMapper<SessionToken> sessionTokenRowMapper() {
        return new RowMapper<SessionToken>() {
            @Override
            public SessionToken mapRow(ResultSet rs, int rowNum) throws SQLException {
                return SessionToken.builder()
                        .token(UUID.fromString(rs.getString("token")))
                        .dateExpiration(rs.getTimestamp("expires_at").toLocalDateTime())
                        .userId(UUID.fromString(rs.getString("authentication_id")))
                        .ipAddress(rs.getString("ip_address"))
                        .userAgent(rs.getString("user_agent"))
                        .build();
            }
        };
    }



    private RowMapper<AuthenticationAttempt> authenticationAttemptRowMapper() {
        return (rs, rowNum) -> AuthenticationAttempt.of(
                rs.getTimestamp("date").toLocalDateTime(),
                rs.getString("ip_address"),
                rs.getBoolean("success"),
                rs.getString("username"),
                rs.getString("failure_reason")
        );
    }

    @Override
    public Authentication findByPasswordResetToken(String token) {
        try {
            TypedQuery<AuthenticationEntity> query = entityManager.createQuery(
                    "SELECT a FROM AuthenticationEntity a WHERE a.passwordResetToken = :token",
                    AuthenticationEntity.class);
            query.setParameter("token", token);
            return toDomain(query.getSingleResult());
        } catch (NoResultException e) {
            return null;
        }
    }
}




