package com.enaya.product_service.domain.model.collection.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PeriodCollection {

    @Column(name = "period_start_date")
    LocalDateTime startDate;
    
    @Column(name = "period_end_date")
    LocalDateTime endDate;

    private PeriodCollection(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = validateStartDate(startDate);
        this.endDate = validateEndDate(endDate, startDate);
    }

    public static PeriodCollection of(LocalDateTime startDate, LocalDateTime endDate) {
        return new PeriodCollection(startDate, endDate);
    }

    public static PeriodCollection ofDays(LocalDateTime startDate, int numberOfDays) {
        if (numberOfDays <= 0) {
            throw new IllegalArgumentException("Number of days must be positive");
        }
        LocalDateTime endDate = startDate.plusDays(numberOfDays);
        return new PeriodCollection(startDate, endDate);
    }

    public static PeriodCollection ofWeeks(LocalDateTime startDate, int numberOfWeeks) {
        if (numberOfWeeks <= 0) {
            throw new IllegalArgumentException("Number of weeks must be positive");
        }
        LocalDateTime endDate = startDate.plusWeeks(numberOfWeeks);
        return new PeriodCollection(startDate, endDate);
    }

    public static PeriodCollection ofMonths(LocalDateTime startDate, int numberOfMonths) {
        if (numberOfMonths <= 0) {
            throw new IllegalArgumentException("Number of months must be positive");
        }
        LocalDateTime endDate = startDate.plusMonths(numberOfMonths);
        return new PeriodCollection(startDate, endDate);
    }

    public static PeriodCollection summer2025() {
        return of(
                LocalDateTime.of(2025, 6, 21, 0, 0),
                LocalDateTime.of(2025, 9, 22, 23, 59)
        );
    }

    public static PeriodCollection winter2025() {
        return of(
                LocalDateTime.of(2025, 12, 21, 0, 0),
                LocalDateTime.of(2026, 3, 20, 23, 59)
        );
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean isUpcoming() {
        return LocalDateTime.now().isBefore(startDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean contains(LocalDateTime date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(PeriodCollection other) {
        if (other == null) {
            return false;
        }
        return !this.endDate.isBefore(other.startDate) && !this.startDate.isAfter(other.endDate);
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public long getDurationInHours() {
        return ChronoUnit.HOURS.between(startDate, endDate);
    }

    public long getRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (isUpcoming()) {
            return ChronoUnit.DAYS.between(now, startDate);
        }
        return ChronoUnit.DAYS.between(now, endDate);
    }

    public long getDaysUntilStart() {
        if (!isUpcoming()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), startDate);
    }

    public double getProgressPercentage() {
        if (isUpcoming()) {
            return 0.0;
        }
        if (isExpired()) {
            return 100.0;
        }

        LocalDateTime now = LocalDateTime.now();
        long totalDuration = ChronoUnit.MINUTES.between(startDate, endDate);
        long elapsed = ChronoUnit.MINUTES.between(startDate, now);

        return (double) elapsed / totalDuration * 100.0;
    }

    public PeriodCollection extend(int days) {
        return new PeriodCollection(this.startDate, this.endDate.plusDays(days));
    }

    public PeriodCollection shorten(int days) {
        LocalDateTime newEndDate = this.endDate.minusDays(days);
        if (newEndDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Cannot shorten period: end date would be before start date");
        }
        return new PeriodCollection(this.startDate, newEndDate);
    }

    @Override
    public String toString() {
        return String.format("From %s to %s", startDate, endDate);
    }

    private LocalDateTime validateStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        return startDate;
    }

    private LocalDateTime validateEndDate(LocalDateTime endDate, LocalDateTime startDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        return endDate;
    }
}
