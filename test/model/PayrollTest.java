package model;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.junit.Assert.*;

public class PayrollTest {

    private Payroll payroll;

    @Before
    public void setUp() {
        payroll = new Payroll(1,
                Date.valueOf("2025-07-01"),
                Date.valueOf("2025-07-15"));
        payroll.setMonthlyRate(22000); // base monthly salary
        payroll.setDaysWorked(10);
        payroll.setOvertimeHours(5);
        payroll.setOvertimePay(1000);
        payroll.setRiceSubsidy(1500);
        payroll.setPhoneAllowance(500);
        payroll.setClothingAllowance(800);
        payroll.setSss(500);
        payroll.setPhilhealth(300);
        payroll.setPagibig(200);
        payroll.setTax(1000);
        payroll.setLateDeduction(100);
        payroll.setUndertimeDeduction(200);
        payroll.setUnpaidLeaveDeduction(0);
    }

    @Test
    public void testBasicPayCalculation() {
        double expectedBasicPay = (22000 / 22.0) * 10;
        assertEquals(expectedBasicPay, payroll.getBasicPay(), 0.001);
    }

    @Test
    public void testGrossPayCalculation() {
        payroll.calculateGrossPay();
        double expected = payroll.getBasicPay() + payroll.getOvertimePay() + payroll.getTotalAllowances();
        assertEquals(expected, payroll.getGrossPay(), 0.001);
    }

    @Test
    public void testTotalDeductionsCalculation() {
        payroll.calculateTotalDeductions();
        double expected = payroll.getTotalDeductionsCalculated();
        assertEquals(expected, payroll.getTotalDeductions(), 0.001);
    }

    @Test
    public void testNetPayCalculation() {
        payroll.recalculateAll();
        double expected = payroll.getGrossPay() - payroll.getTotalDeductions();
        assertEquals(expected, payroll.getNetPay(), 0.001);
    }

    @Test
    public void testAddAndRetrieveComponents() {
        PayrollComponent allowance = new PayrollComponent(1, "Rice Subsidy", 1500) {
            @Override public void calculate() { }
            @Override public String getCategory() { return "Allowance"; }
            @Override public boolean isPositiveAmount() { return true; }
        };

        PayrollComponent deduction = new PayrollComponent(1, "Late Penalty", 200) {
            @Override public void calculate() { }
            @Override public String getCategory() { return "Deduction"; }
            @Override public boolean isPositiveAmount() { return false; }
        };

        PayrollComponent contribution = new PayrollComponent(1, "SSS Contribution", 500) {
            @Override public void calculate() { }
            @Override public String getCategory() { return "Government Contribution"; }
            @Override public boolean isPositiveAmount() { return false; }
        };

        payroll.addComponent(allowance);
        payroll.addComponent(deduction);
        payroll.addComponent(contribution);

        List<PayrollComponent> allowances = payroll.getAllowances();
        List<PayrollComponent> deductions = payroll.getDeductions();
        List<PayrollComponent> contributions = payroll.getContributions();

        assertEquals(1, allowances.size());
        assertEquals(1, deductions.size());
        assertEquals(1, contributions.size());
    }

    @Test
    public void testInvalidEmployeeIdThrowsException() {
        try {
            payroll.setEmployeeId(-1);
            fail("Expected IllegalArgumentException for invalid employee ID");
        } catch (IllegalArgumentException e) {
            assertEquals("Employee ID must be positive", e.getMessage());
        }
    }

    @Test
    public void testInvalidPeriodDatesThrowsException() {
        Payroll newPayroll = new Payroll();
        newPayroll.setEmployeeId(1);
        newPayroll.setPeriodStart(Date.valueOf("2025-07-15"));
        try {
            newPayroll.setPeriodEnd(Date.valueOf("2025-07-10"));
            fail("Expected IllegalArgumentException for invalid period end date");
        } catch (IllegalArgumentException e) {
            assertEquals("Period end cannot be before start", e.getMessage());
        }
    }

    @Test
    public void testIsValid() {
        assertTrue(payroll.isValid());
    }

    @Test
    public void testGetDisplayName() {
        String displayName = payroll.getDisplayName();
        assertTrue(displayName.contains("Payroll #"));
        assertTrue(displayName.contains("for Employee 1"));
    }

    @Test
    public void testToStringContainsKeyInfo() {
        String toString = payroll.toString();
        assertTrue(toString.contains("payrollId="));
        assertTrue(toString.contains("employeeId="));
        assertTrue(toString.contains("grossPay="));
    }
}
