package com.enaya.product_service.domain.model.collection.valueobjects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeriodeCollection {

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    private PeriodeCollection(LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.dateDebut = validateDateDebut(dateDebut);
        this.dateFin = validateDateFin(dateFin, dateDebut);
    }

    public static PeriodeCollection of(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return new PeriodeCollection(dateDebut, dateFin);
    }

    public static PeriodeCollection ofDays(LocalDateTime dateDebut, int nombreJours) {
        if (nombreJours <= 0) {
            throw new IllegalArgumentException("Number of days must be positive");
        }
        LocalDateTime dateFin = dateDebut.plusDays(nombreJours);
        return new PeriodeCollection(dateDebut, dateFin);
    }

    public static PeriodeCollection ofWeeks(LocalDateTime dateDebut, int nombreSemaines) {
        if (nombreSemaines <= 0) {
            throw new IllegalArgumentException("Number of weeks must be positive");
        }
        LocalDateTime dateFin = dateDebut.plusWeeks(nombreSemaines);
        return new PeriodeCollection(dateDebut, dateFin);
    }

    public static PeriodeCollection ofMonths(LocalDateTime dateDebut, int nombreMois) {
        if (nombreMois <= 0) {
            throw new IllegalArgumentException("Number of months must be positive");
        }
        LocalDateTime dateFin = dateDebut.plusMonths(nombreMois);
        return new PeriodeCollection(dateDebut, dateFin);
    }

    public static PeriodeCollection summer2024() {
        return of(
                LocalDateTime.of(2024, 6, 21, 0, 0),
                LocalDateTime.of(2024, 9, 22, 23, 59)
        );
    }

    public static PeriodeCollection winter2024() {
        return of(
                LocalDateTime.of(2024, 12, 21, 0, 0),
                LocalDateTime.of(2025, 3, 20, 23, 59)
        );
    }

    public static PeriodeCollection christmas2024() {
        return of(
                LocalDateTime.of(2024, 12, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
        );
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(dateDebut) && !now.isAfter(dateFin);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(dateDebut);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateFin);
    }

    public boolean contains(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }

    public boolean overlaps(PeriodeCollection other) {
        if (other == null) {
            return false;
        }
        return !this.dateFin.isBefore(other.dateDebut) && !this.dateDebut.isAfter(other.dateFin);
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }

    public long getDurationInHours() {
        return ChronoUnit.HOURS.between(dateDebut, dateFin);
    }

    public long getRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (isUpcoming()) {
            return ChronoUnit.DAYS.between(now, dateDebut);
        }
        return ChronoUnit.DAYS.between(now, dateFin);
    }

    public long getDaysUntilStart() {
        if (!isUpcoming()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), dateDebut);
    }

    public double getProgressPercentage() {
        if (isUpcoming()) {
            return 0.0;
        }
        if (isExpired()) {
            return 100.0;
        }

        LocalDateTime now = LocalDateTime.now();
        long totalDuration = ChronoUnit.MINUTES.between(dateDebut, dateFin);
        long elapsed = ChronoUnit.MINUTES.between(dateDebut, now);

        return (double) elapsed / totalDuration * 100.0;
    }

    public PeriodeCollection extend(int days) {
        return new PeriodeCollection(this.dateDebut, this.dateFin.plusDays(days));
    }

    public PeriodeCollection shorten(int days) {
        LocalDateTime newDateFin = this.dateFin.minusDays(days);
        if (newDateFin.isBefore(this.dateDebut)) {
            throw new IllegalArgumentException("Cannot shorten period: end date would be before start date");
        }
        return new PeriodeCollection(this.dateDebut, newDateFin);
    }

    @Override
    public String toString() {
        return String.format("From %s to %s", dateDebut, dateFin);
    }

    private LocalDateTime validateDateDebut(LocalDateTime dateDebut) {
        if (dateDebut == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        return dateDebut;
    }

    private LocalDateTime validateDateFin(LocalDateTime dateFin, LocalDateTime dateDebut) {
        if (dateFin == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        return dateFin;
    }
}