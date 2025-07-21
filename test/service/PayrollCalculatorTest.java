package service;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class PayrollCalculatorTest {

    /**
     * Test calculatePayroll with invalid employee ID should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithInvalidEmployeeId() throws Exception {
        PayrollCalculator calculator = new PayrollCalculator();
        calculator.calculatePayroll(0, LocalDate.now().minusDays(15), LocalDate.now());
    }

    /**
     * Test calculatePayroll with null dates should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithNullDates() throws Exception {
        PayrollCalculator calculator = new PayrollCalculator();
        calculator.calculatePayroll(1, null, null);
    }

    /**
     * Test calculatePayroll with periodEnd before periodStart should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithEndBeforeStart() throws Exception {
        PayrollCalculator calculator = new PayrollCalculator();
        calculator.calculatePayroll(1, LocalDate.now(), LocalDate.now().minusDays(5));
    }
}
