package model;

import java.util.ArrayList;
import java.util.List;

public class PayrollComponentFactory {

    public enum ComponentType {
        RICE_SUBSIDY, PHONE_ALLOWANCE, CLOTHING_ALLOWANCE,
        LATE_DEDUCTION, UNDERTIME_DEDUCTION,
        SSS, PHILHEALTH, PAGIBIG
    }

    // Factory method demonstrating polymorphism - returns different concrete types as PayrollComponent
    public static PayrollComponent create(ComponentType type, int employeeId, Object... params) {
        switch (type) {
            case RICE_SUBSIDY:
                return Allowance.createRiceSubsidy(employeeId, (Double) params[0]);
            case PHONE_ALLOWANCE:
                return Allowance.createPhoneAllowance(employeeId, (Double) params[0]);
            case CLOTHING_ALLOWANCE:
                return Allowance.createClothingAllowance(employeeId, (Double) params[0]);
            case SSS:
                return GovernmentContribution.createSSS(employeeId, (Double) params[0]);
            case PHILHEALTH:
                return GovernmentContribution.createPhilHealth(employeeId, (Double) params[0]);
            case PAGIBIG:
                return GovernmentContribution.createPagIBIG(employeeId, (Double) params[0]);
            default:
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }

    // Convenience methods
    public static List<PayrollComponent> createStandardAllowances(int employeeId, double rice, double phone, double clothing) {
        List<PayrollComponent> allowances = new ArrayList<>();
        allowances.add(create(ComponentType.RICE_SUBSIDY, employeeId, rice));
        allowances.add(create(ComponentType.PHONE_ALLOWANCE, employeeId, phone));
        allowances.add(create(ComponentType.CLOTHING_ALLOWANCE, employeeId, clothing));
        return allowances;
    }

    public static List<PayrollComponent> createStandardContributions(int employeeId, double salary) {
        List<PayrollComponent> contributions = new ArrayList<>();
        contributions.add(create(ComponentType.SSS, employeeId, salary));
        contributions.add(create(ComponentType.PHILHEALTH, employeeId, salary));
        contributions.add(create(ComponentType.PAGIBIG, employeeId, salary));
        return contributions;
    }
}