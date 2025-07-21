package service;

import model.Employee;
import model.Position;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for EmployeeService
 */
public class EmployeeServiceTest {

    /**
     * Test getAllEmployees returns non-null list
     */
    @Test
    public void testGetAllEmployees() {
        EmployeeService service = new EmployeeService();
        List<Employee> employees = service.getAllEmployees();
        assertNotNull("Employee list should not be null", employees);
    }

    /**
     * Test getEmployeeById with invalid ID returns null
     */
    @Test
    public void testGetEmployeeByIdInvalid() {
        EmployeeService service = new EmployeeService();
        Employee employee = service.getEmployeeById(-1);
        assertNull("Employee should be null for invalid ID", employee);
    }

    /**
     * Test deleteEmployee with invalid ID returns false
     */
    @Test
    public void testDeleteEmployeeInvalid() {
        EmployeeService service = new EmployeeService();
        boolean deleted = service.deleteEmployee(-1);
        assertFalse("Delete should fail for invalid ID", deleted);
    }

    /**
     * Test getAllPositions returns non-null list
     */
    @Test
    public void testGetAllPositions() {
        EmployeeService service = new EmployeeService();
        List<Position> positions = service.getAllPositions();
        assertNotNull("Positions list should not be null", positions);
    }

    /**
     * Test getEmployeeCountByStatus with empty status returns 0 or greater
     */
    @Test
    public void testGetEmployeeCountByStatus() {
        EmployeeService service = new EmployeeService();
        int count = service.getEmployeeCountByStatus("");
        assertTrue("Employee count should not be negative", count >= 0);
    }
}
