package model;

/**
 * Allowance class demonstrating inheritance and polymorphism
 * Extends PayrollComponent and implements specific allowance behavior
 */
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
        return new RiceSubsidyAllowance(employeeId, Math.min(amount, 2000.0));
    }

    public static Allowance createPhoneAllowance(int employeeId, double amount) {
        return new PhoneAllowance(employeeId, Math.min(amount, 3000.0));
    }

    public static Allowance createClothingAllowance(int employeeId, double amount) {
        return new ClothingAllowance(employeeId, Math.min(amount, 1500.0));
    }
    
    /**
     * Specific allowance implementations demonstrating inheritance
     */
    public static class RiceSubsidyAllowance extends Allowance {
        private static final double MAX_AMOUNT = 2000.0;
        
        public RiceSubsidyAllowance(int employeeId, double amount) {
            super(employeeId, "Rice Subsidy", amount);
        }
        
        @Override
        public void calculate() {
            // Ensure rice subsidy doesn't exceed maximum
            if (getAmount() > MAX_AMOUNT) {
                setAmount(MAX_AMOUNT);
            }
        }
    }
    
    public static class PhoneAllowance extends Allowance {
        private static final double MAX_AMOUNT = 3000.0;
        
        public PhoneAllowance(int employeeId, double amount) {
            super(employeeId, "Phone Allowance", amount);
        }
        
        @Override
        public void calculate() {
            // Ensure phone allowance doesn't exceed maximum
            if (getAmount() > MAX_AMOUNT) {
                setAmount(MAX_AMOUNT);
            }
        }
    }
    
    public static class ClothingAllowance extends Allowance {
        private static final double MAX_AMOUNT = 1500.0;
        
        public ClothingAllowance(int employeeId, double amount) {
            super(employeeId, "Clothing Allowance", amount);
        }
        
        @Override
        public void calculate() {
            // Ensure clothing allowance doesn't exceed maximum
            if (getAmount() > MAX_AMOUNT) {
                setAmount(MAX_AMOUNT);
            }
        }
    }
}