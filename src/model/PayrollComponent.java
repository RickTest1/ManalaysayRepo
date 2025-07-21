package model;

import java.time.LocalDate;

public abstract class PayrollComponent extends BaseEntity {
    protected int employeeId;
    protected String type;
    protected double amount;
    protected String description;
    protected LocalDate effectiveDate;

    public PayrollComponent() {
        this.effectiveDate = LocalDate.now();
    }

    public PayrollComponent(int employeeId, String type, double amount) {
        this();
        this.employeeId = employeeId;
        this.type = type;
        this.amount = amount;
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) throw new IllegalArgumentException("Employee ID must be positive");
        this.employeeId = employeeId;
        touch();
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        this.type = type.trim();
        touch();
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.amount = amount;
        touch();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; touch(); }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; touch(); }

    // Abstract methods for polymorphism
    public abstract void calculate();
    public abstract String getCategory(); // "Allowance", "Deduction", "Contribution"
    public abstract boolean isPositiveAmount(); // true for earnings, false for deductions

    // Common utility methods
    public String getFormattedAmount() {
        String prefix = isPositiveAmount() ? "+" : "-";
        return prefix + "₱" + String.format("%.2f", amount);
    }

    @Override
    public boolean isValid() {
        return employeeId > 0 && type != null && !type.trim().isEmpty() && amount >= 0;
    }

    @Override
    public String getDisplayName() {
        return type + " (" + getFormattedAmount() + ")";
    }
}