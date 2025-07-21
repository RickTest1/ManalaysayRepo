package model;

import java.sql.Time;
import java.time.LocalTime;

public abstract class Deduction extends PayrollComponent {
    private int deductionId;

    public Deduction() {}

    public Deduction(int employeeId, String type, double amount) {
        super(employeeId, type, amount);
    }

    public Deduction(int employeeId, String type, double amount, String description) {
        super(employeeId, type, amount);
        setDescription(description);
    }

    // Added missing deductionId methods
    public int getDeductionId() {
        return deductionId;
    }

    public void setDeductionId(int deductionId) {
        this.deductionId = deductionId;
        touch();
    }

    @Override
    public void calculate() {
        // Default implementation - subclasses override for specific logic
    }

    @Override
    public String getCategory() {
        return "Deduction";
    }

    @Override
    public boolean isPositiveAmount() {
        return false;
    }

    // Abstract method for deduction-specific calculation
    public abstract void calculateDeduction();

    // Factory methods for common deductions - demonstrates polymorphism
    public static Deduction createLateDeduction(int employeeId, Time arrivalTime, double hourlyRate) {
        LocalTime standard = LocalTime.of(8, 0);
        LocalTime actual = arrivalTime.toLocalTime();

        if (actual.isAfter(standard.plusMinutes(15))) { // 15-minute grace period
            long minutesLate = java.time.Duration.between(standard, actual).toMinutes();
            double amount = (minutesLate / 60.0) * hourlyRate;

            return new ConcreteDeduction(employeeId, "Late Deduction", amount,
                    minutesLate + " minutes late");
        }
        return new ConcreteDeduction(employeeId, "Late Deduction", 0.0);
    }

    public static Deduction createUndertimeDeduction(int employeeId, Time departureTime, double hourlyRate) {
        LocalTime standard = LocalTime.of(17, 0);
        LocalTime actual = departureTime.toLocalTime();

        if (actual.isBefore(standard)) {
            long minutesEarly = java.time.Duration.between(actual, standard).toMinutes();
            double amount = (minutesEarly / 60.0) * hourlyRate;

            return new ConcreteDeduction(employeeId, "Undertime Deduction", amount,
                    minutesEarly + " minutes undertime");
        }
        return new ConcreteDeduction(employeeId, "Undertime Deduction", 0.0);
    }

    // Concrete implementation for generic deductions
    private static class ConcreteDeduction extends Deduction {
        public ConcreteDeduction(int employeeId, String type, double amount) {
            super(employeeId, type, amount);
        }

        public ConcreteDeduction(int employeeId, String type, double amount, String description) {
            super(employeeId, type, amount, description);
        }

        @Override
        public void calculateDeduction() {
            // For generic deductions, the amount is already set
        }
    }
}