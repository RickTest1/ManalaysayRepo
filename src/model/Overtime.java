package model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing employee overtime records
 * @author rejoice
 */
public class Overtime {
    private int overtimeId;
    private int employeeId;
    private Date date;
    private double hours;
    private String reason;
    private boolean approved;

    // Constructors
    public Overtime() {}

    public Overtime(int employeeId, Date date, double hours) {
        setEmployeeId(employeeId);
        setDate(date);
        setHours(hours);
        this.approved = false; // Default to not approved
    }

    public Overtime(int employeeId, Date date, double hours, String reason) {
        this(employeeId, date, hours);
        this.reason = reason;
    }

    // Getters and Setters with validation
    public int getOvertimeId() {
        return overtimeId;
    }

    public void setOvertimeId(int overtimeId) {
        this.overtimeId = overtimeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative");
        }
        if (hours > 24) {
            throw new IllegalArgumentException("Overtime hours cannot exceed 24 hours in a day");
        }
        this.hours = hours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason != null ? reason.trim() : null;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    // Utility methods
    public LocalDate getDateAsLocalDate() {
        return date != null ? date.toLocalDate() : null;
    }

    public void setDateFromLocalDate(LocalDate localDate) {
        if (localDate != null) {
            this.date = Date.valueOf(localDate);
        }
    }

    public double calculateOvertimePay(double hourlyRate, double overtimeMultiplier) {
        if (hourlyRate < 0 || overtimeMultiplier < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        return hours * hourlyRate * overtimeMultiplier;
    }

    public boolean isValidOvertimeHours() {
        return hours > 0 && hours <= 12; // Reasonable overtime limit
    }

    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public String getFormattedHours() {
        return String.format("%.2f", hours);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Overtime overtime = (Overtime) obj;
        return overtimeId == overtime.overtimeId &&
                employeeId == overtime.employeeId &&
                Objects.equals(date, overtime.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overtimeId, employeeId, date);
    }

    @Override
    public String toString() {
        return "Overtime{" +
                "overtimeId=" + overtimeId +
                ", employeeId=" + employeeId +
                ", date=" + date +
                ", hours=" + getFormattedHours() +
                ", reason='" + reason + '\'' +
                ", approved=" + approved +
                '}';
    }
}