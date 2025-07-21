package model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Payroll extends BaseEntity {
    private int payrollId;
    private int employeeId;
    private Date periodStart;
    private Date periodEnd;
    private double monthlyRate;
    private int daysWorked;
    private double overtimeHours;
    private double grossPay;
    private double totalDeductions;
    private double netPay;
    private double grossEarnings;

    // Deduction details
    private double lateDeduction;
    private double undertimeDeduction;
    private double unpaidLeaveDeduction;

    // Additional pay details
    private double overtimePay;

    // Allowances
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;

    // Government contributions
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;

    // Components for detailed breakdown (for backward compatibility)
    private List<PayrollComponent> components;

    public Payroll() {
        super();
        this.components = new ArrayList<>();
    }

    public Payroll(int employeeId, Date periodStart, Date periodEnd) {
        this();
        this.employeeId = employeeId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // Getters and Setters
    public int getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(int payrollId) {
        this.payrollId = payrollId;
        touch();
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) throw new IllegalArgumentException("Employee ID must be positive");
        this.employeeId = employeeId;
        touch();
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        if (periodStart == null) throw new IllegalArgumentException("Period start cannot be null");
        this.periodStart = periodStart;
        touch();
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        if (periodEnd == null) throw new IllegalArgumentException("Period end cannot be null");
        if (periodStart != null && periodEnd.before(periodStart)) {
            throw new IllegalArgumentException("Period end cannot be before start");
        }
        this.periodEnd = periodEnd;
        touch();
    }

    public double getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(double monthlyRate) {
        if (monthlyRate < 0) throw new IllegalArgumentException("Monthly rate cannot be negative");
        this.monthlyRate = monthlyRate;
        touch();
    }

    public int getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(int daysWorked) {
        if (daysWorked < 0) throw new IllegalArgumentException("Days worked cannot be negative");
        this.daysWorked = daysWorked;
        touch();
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        if (overtimeHours < 0) throw new IllegalArgumentException("Overtime hours cannot be negative");
        this.overtimeHours = overtimeHours;
        touch();
    }

    public double getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(double grossPay) {
        if (grossPay < 0) throw new IllegalArgumentException("Gross pay cannot be negative");
        this.grossPay = grossPay;
        touch();
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        if (totalDeductions < 0) throw new IllegalArgumentException("Total deductions cannot be negative");
        this.totalDeductions = totalDeductions;
        touch();
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double netPay) {
        this.netPay = netPay;
        touch();
    }

    public double getGrossEarnings() {
        return grossEarnings;
    }

    public void setGrossEarnings(double grossEarnings) {
        if (grossEarnings < 0) throw new IllegalArgumentException("Gross earnings cannot be negative");
        this.grossEarnings = grossEarnings;
        touch();
    }

    // Deduction getters/setters
    public double getLateDeduction() {
        return lateDeduction;
    }

    public void setLateDeduction(double lateDeduction) {
        if (lateDeduction < 0) throw new IllegalArgumentException("Late deduction cannot be negative");
        this.lateDeduction = lateDeduction;
        touch();
    }

    public double getUndertimeDeduction() {
        return undertimeDeduction;
    }

    public void setUndertimeDeduction(double undertimeDeduction) {
        if (undertimeDeduction < 0) throw new IllegalArgumentException("Undertime deduction cannot be negative");
        this.undertimeDeduction = undertimeDeduction;
        touch();
    }

    public double getUnpaidLeaveDeduction() {
        return unpaidLeaveDeduction;
    }

    public void setUnpaidLeaveDeduction(double unpaidLeaveDeduction) {
        if (unpaidLeaveDeduction < 0) throw new IllegalArgumentException("Unpaid leave deduction cannot be negative");
        this.unpaidLeaveDeduction = unpaidLeaveDeduction;
        touch();
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(double overtimePay) {
        if (overtimePay < 0) throw new IllegalArgumentException("Overtime pay cannot be negative");
        this.overtimePay = overtimePay;
        touch();
    }

    // Allowance getters/setters
    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        if (riceSubsidy < 0) throw new IllegalArgumentException("Rice subsidy cannot be negative");
        this.riceSubsidy = riceSubsidy;
        touch();
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        if (phoneAllowance < 0) throw new IllegalArgumentException("Phone allowance cannot be negative");
        this.phoneAllowance = phoneAllowance;
        touch();
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) throw new IllegalArgumentException("Clothing allowance cannot be negative");
        this.clothingAllowance = clothingAllowance;
        touch();
    }

    // Government contribution getters/setters
    public double getSss() {
        return sss;
    }

    public void setSss(double sss) {
        if (sss < 0) throw new IllegalArgumentException("SSS contribution cannot be negative");
        this.sss = sss;
        touch();
    }

    public double getPhilhealth() {
        return philhealth;
    }

    public void setPhilhealth(double philhealth) {
        if (philhealth < 0) throw new IllegalArgumentException("PhilHealth contribution cannot be negative");
        this.philhealth = philhealth;
        touch();
    }

    public double getPagibig() {
        return pagibig;
    }

    public void setPagibig(double pagibig) {
        if (pagibig < 0) throw new IllegalArgumentException("Pag-IBIG contribution cannot be negative");
        this.pagibig = pagibig;
        touch();
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        if (tax < 0) throw new IllegalArgumentException("Tax cannot be negative");
        this.tax = tax;
        touch();
    }

    // Component-based methods (for backward compatibility)
    public List<PayrollComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public void addComponent(PayrollComponent component) {
        if (component.getEmployeeId() != this.employeeId) {
            throw new IllegalArgumentException("Component employee ID must match payroll");
        }
        components.add(component);
        touch();
    }

    public void removeComponent(PayrollComponent component) {
        components.remove(component);
        touch();
    }

    // Utility methods
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getTotalGovernmentContributions() {
        return sss + philhealth + pagibig;
    }

    public double getTotalDeductionsCalculated() {
        return lateDeduction + undertimeDeduction + unpaidLeaveDeduction +
                sss + philhealth + pagibig + tax;
    }

    public double getBasicPay() {
        return (monthlyRate / 22.0) * daysWorked; // 22 working days per month
    }

    public void calculateNetPay() {
        this.netPay = grossPay - totalDeductions;
        touch();
    }

    public void calculateGrossPay() {
        this.grossPay = getBasicPay() + overtimePay + getTotalAllowances();
        touch();
    }

    public void calculateTotalDeductions() {
        this.totalDeductions = getTotalDeductionsCalculated();
        touch();
    }

    public void recalculateAll() {
        calculateGrossPay();
        calculateTotalDeductions();
        calculateNetPay();
    }

    // Get components by category - demonstrates polymorphism
    public List<PayrollComponent> getAllowances() {
        return components.stream()
                .filter(c -> "Allowance".equals(c.getCategory()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<PayrollComponent> getDeductions() {
        return components.stream()
                .filter(c -> "Deduction".equals(c.getCategory()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<PayrollComponent> getContributions() {
        return components.stream()
                .filter(c -> "Government Contribution".equals(c.getCategory()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isValid() {
        return employeeId > 0 && periodStart != null && periodEnd != null && monthlyRate >= 0;
    }

    @Override
    public String getDisplayName() {
        return "Payroll #" + payrollId + " for Employee " + employeeId +
                " (" + periodStart + " to " + periodEnd + ")";
    }

    @Override
    public String toString() {
        return "Payroll{" +
                "payrollId=" + payrollId +
                ", employeeId=" + employeeId +
                ", period=" + periodStart + " to " + periodEnd +
                ", grossPay=" + grossPay +
                ", totalDeductions=" + totalDeductions +
                ", netPay=" + netPay +
                '}';
    }
}