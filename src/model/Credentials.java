package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Credentials {
    private int employeeId;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String email; // For future enhancement
    private boolean isActive;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;

    // Constructors
    public Credentials() {
        this.isActive = true;
        this.failedLoginAttempts = 0;
    }

    public Credentials(int employeeId, String password) {
        this();
        setEmployeeId(employeeId);
        setPassword(password);
    }

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = Math.max(0, failedLoginAttempts);
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    // Utility methods
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.isActive = false; // Lock account after 3 failed attempts
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.isActive = true;
    }

    public boolean isPasswordValid(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    public boolean needsPasswordReset() {
        if (updatedAt == null) return false;
        return updatedAt.isBefore(LocalDateTime.now().minusDays(90)); // 90-day password policy
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Credentials that = (Credentials) obj;
        return employeeId == that.employeeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "employeeId=" + employeeId +
                ", isActive=" + isActive +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}