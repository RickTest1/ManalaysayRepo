package model;

import java.util.Objects;

/**
 * Position model matching the actual database schema
 */
public class Position {
    private int positionId;
    private String positionName;
    private double monthlySalary; // Maps to basic_salary in database
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSemiMonthlyRate;
    private double hourlyRate;

    // Constructors
    public Position() {}

    public Position(String positionName, double monthlySalary) {
        this.positionName = positionName;
        this.monthlySalary = monthlySalary;
    }

    public Position(int positionId, String positionName, double monthlySalary) {
        this.positionId = positionId;
        this.positionName = positionName;
        this.monthlySalary = monthlySalary;
    }

    // Getters and Setters
    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        if (positionName != null && positionName.trim().length() > 100) {
            throw new IllegalArgumentException("Position name cannot exceed 100 characters");
        }
        this.positionName = positionName != null ? positionName.trim() : null;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        if (monthlySalary < 0) {
            throw new IllegalArgumentException("Monthly salary cannot be negative");
        }
        this.monthlySalary = monthlySalary;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        if (riceSubsidy < 0) {
            throw new IllegalArgumentException("Rice subsidy cannot be negative");
        }
        this.riceSubsidy = riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        if (phoneAllowance < 0) {
            throw new IllegalArgumentException("Phone allowance cannot be negative");
        }
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) {
            throw new IllegalArgumentException("Clothing allowance cannot be negative");
        }
        this.clothingAllowance = clothingAllowance;
    }

    public double getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) {
        if (grossSemiMonthlyRate < 0) {
            throw new IllegalArgumentException("Gross semi-monthly rate cannot be negative");
        }
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.hourlyRate = hourlyRate;
    }

    // Utility methods
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getDailyRate() {
        return monthlySalary / 22.0; // 22 working days per month
    }

    public double getCalculatedHourlyRate() {
        return getDailyRate() / 8.0; // 8 hours per day
    }

    public boolean isValid() {
        return positionName != null && !positionName.trim().isEmpty() && monthlySalary >= 0;
    }

    public String getDisplayName() {
        return positionName + " (₱" + String.format("%.2f", monthlySalary) + ")";
    }

    // Auto-calculate rates based on monthly salary
    public void calculateRates() {
        this.grossSemiMonthlyRate = monthlySalary / 2.0;
        this.hourlyRate = getCalculatedHourlyRate();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return positionId == position.positionId &&
                Objects.equals(positionName, position.positionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionId, positionName);
    }

    @Override
    public String toString() {
        return "Position{" +
                "positionId=" + positionId +
                ", positionName='" + positionName + '\'' +
                ", monthlySalary=" + monthlySalary +
                ", riceSubsidy=" + riceSubsidy +
                ", phoneAllowance=" + phoneAllowance +
                ", clothingAllowance=" + clothingAllowance +
                ", grossSemiMonthlyRate=" + grossSemiMonthlyRate +
                ", hourlyRate=" + hourlyRate +
                '}';
    }
}