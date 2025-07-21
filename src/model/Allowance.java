package model;

public class Allowance extends PayrollComponent {

    public Allowance() {}

    public Allowance(int employeeId, String type, double amount) {
        super(employeeId, type, amount);
    }

    @Override
    public void calculate() {
        // Default implementation - amount is fixed
        // Subclasses can override for specific calculation logic
    }

    @Override
    public String getCategory() {
        return "Allowance";
    }

    @Override
    public boolean isPositiveAmount() {
        return true;
    }

    // Factory methods for common allowances
    public static Allowance createRiceSubsidy(int employeeId, double amount) {
        return new Allowance(employeeId, "Rice Subsidy", Math.min(amount, 2000.0));
    }

    public static Allowance createPhoneAllowance(int employeeId, double amount) {
        return new Allowance(employeeId, "Phone Allowance", Math.min(amount, 3000.0));
    }

    public static Allowance createClothingAllowance(int employeeId, double amount) {
        return new Allowance(employeeId, "Clothing Allowance", Math.min(amount, 1500.0));
    }
}