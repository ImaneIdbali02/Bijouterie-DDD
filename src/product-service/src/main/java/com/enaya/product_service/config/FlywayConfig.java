package com.enaya.product_service.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String flywayLocations;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean baselineOnMigrate;

    @Value("${spring.flyway.validate-on-migrate:false}")
    private boolean validateOnMigrate;

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayLocations)
                .baselineOnMigrate(baselineOnMigrate)
                .validateOnMigrate(validateOnMigrate)
                .cleanDisabled(false)
                .outOfOrder(true)
                .load();

        // Nettoyer et recréer complètement la base de données
        try {
            System.out.println("🧹 Nettoyage de la base de données...");
            flyway.clean();
            System.out.println("✅ Base de données nettoyée avec succès");
            
            // Exécuter les migrations après le nettoyage
            System.out.println("🚀 Exécution des migrations...");
            flyway.migrate();
            System.out.println("✅ Migrations exécutées avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du nettoyage/migration: " + e.getMessage());
            System.err.println("🔄 Tentative de réparation...");
            try {
                flyway.repair();
                System.out.println("✅ Réparation réussie");
                // Réessayer la migration après réparation
                flyway.migrate();
                System.out.println("✅ Migrations exécutées avec succès après réparation");
            } catch (Exception repairException) {
                System.err.println("❌ Échec de la réparation: " + repairException.getMessage());
                throw new RuntimeException("Impossible d'initialiser la base de données", repairException);
            }
        }

        return flyway;
    }
} 