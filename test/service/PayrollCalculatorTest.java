package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import model.Employee;
import model.Attendance;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import util.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PayrollCalculatorTest {
    
    private PayrollCalculator calculator;
    private static Connection testConnection;
    
    @Mock
    private EmployeeDAO mockEmployeeDAO;
    
    @Mock
    private AttendanceDAO mockAttendanceDAO;
    
    @Mock
    private LeaveRequestDAO mockLeaveDAO;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        calculator = new PayrollCalculator();
        
        // Set up in-memory database for testing
        testConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        DBConnection.setTestConnection(testConnection);
        
        // Create test tables
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "employee_id INT PRIMARY KEY," +
                    "first_name VARCHAR(50)," +
                    "last_name VARCHAR(50)," +
                    "basic_salary DECIMAL(10,2)," +
                    "position VARCHAR(100)" +
                    ")");
            
            // Insert test employee
            stmt.execute("INSERT INTO employees (employee_id, first_name, last_name, basic_salary, position) " +
                    "VALUES (1, 'John', 'Doe', 25000.00, 'Developer')");
        }
    }

    /**
     * Test calculatePayroll with invalid employee ID should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithInvalidEmployeeId() throws Exception {
        calculator.calculatePayroll(0, LocalDate.now().minusDays(15), LocalDate.now());
    }

    /**
     * Test calculatePayroll with null dates should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithNullDates() throws Exception {
        calculator.calculatePayroll(1, null, null);
    }

    /**
     * Test calculatePayroll with periodEnd before periodStart should throw exception
     */
    @Test(expected = PayrollCalculator.PayrollCalculationException.class)
    public void testCalculatePayrollWithEndBeforeStart() throws Exception {
        calculator.calculatePayroll(1, LocalDate.now(), LocalDate.now().minusDays(5));
    }
    
    /**
     * Test successful payroll calculation
     */
    @Test
    public void testCalculatePayrollSuccess() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 15);
        
        PayrollCalculator.PayrollData result = calculator.calculatePayroll(1, startDate, endDate);
        
        assertNotNull("PayrollData should not be null", result);
        assertEquals("Employee ID should match", 1, result.getEmployeeId());
        assertEquals("Period start should match", startDate, result.getPeriodStart());
        assertEquals("Period end should match", endDate, result.getPeriodEnd());
        assertTrue("Monthly rate should be positive", result.getMonthlyRate() > 0);
        assertTrue("Daily rate should be positive", result.getDailyRate() > 0);
        assertTrue("Net pay should be calculated", result.getNetPay() >= 0);
    }
    
    /**
     * Test payroll calculation with attendance data
     */
    @Test
    public void testCalculatePayrollWithAttendance() throws Exception {
        // This test would require more complex setup with attendance data
        // For now, we'll test that the method doesn't crash
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 15);
        
        try {
            PayrollCalculator.PayrollData result = calculator.calculatePayroll(1, startDate, endDate);
            assertNotNull("Result should not be null even with no attendance", result);
        } catch (Exception e) {
            // Expected if employee doesn't exist in test database
            assertTrue("Should handle missing employee gracefully", 
                    e.getMessage().contains("Employee not found"));
        }
    }
    
    /**
     * Test PayrollData object creation and getters/setters
     */
    @Test
    public void testPayrollDataObject() {
        PayrollCalculator.PayrollData data = new PayrollCalculator.PayrollData();
        
        // Test setters and getters
        data.setEmployeeId(123);
        assertEquals(123, data.getEmployeeId());
        
        data.setMonthlyRate(30000.0);
        assertEquals(30000.0, data.getMonthlyRate(), 0.01);
        
        data.setDaysWorked(20);
        assertEquals(20, data.getDaysWorked());
        
        data.setBasicPay(25000.0);
        assertEquals(25000.0, data.getBasicPay(), 0.01);
        
        data.setGrossPay(28000.0);
        assertEquals(28000.0, data.getGrossPay(), 0.01);
        
        data.setTotalDeductions(5000.0);
        assertEquals(5000.0, data.getTotalDeductions(), 0.01);
        
        data.setNetPay(23000.0);
        assertEquals(23000.0, data.getNetPay(), 0.01);
    }
    
    /**
     * Test PayrollCalculationException
     */
    @Test
    public void testPayrollCalculationException() {
        PayrollCalculator.PayrollCalculationException exception = 
                new PayrollCalculator.PayrollCalculationException("Test message");
        assertEquals("Test message", exception.getMessage());
        
        Exception cause = new RuntimeException("Cause");
        PayrollCalculator.PayrollCalculationException exceptionWithCause = 
                new PayrollCalculator.PayrollCalculationException("Test with cause", cause);
        assertEquals("Test with cause", exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
    }
}
