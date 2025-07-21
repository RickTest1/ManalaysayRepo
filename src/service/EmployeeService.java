package service;

import dao.EmployeeDAO;
import dao.CredentialsDAO;
import dao.PositionDAO;
import model.Employee;
import model.Position;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for employee management operations
 */
public class EmployeeService {
    private static final Logger LOGGER = Logger.getLogger(EmployeeService.class.getName());

    private final EmployeeDAO employeeDAO;
    private final CredentialsDAO credentialsDAO;
    private final PositionDAO positionDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
        this.credentialsDAO = new CredentialsDAO();
        this.positionDAO = new PositionDAO();
    }

    /**
     * Get all employees with position details
     */
    public List<Employee> getAllEmployees() {
        try {
            return employeeDAO.getAllEmployees();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all employees", e);
            throw new RuntimeException("Failed to retrieve employees", e);
        }
    }

    /**
     * Get employee by ID with position details
     */
    public Employee getEmployeeById(int employeeId) {
        try {
            return employeeDAO.getEmployeeWithPositionDetails(employeeId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving employee: " + employeeId, e);
            return null;
        }
    }

    /**
     * Add new employee with credentials
     */
    public boolean addEmployee(Employee employee, String password) {
        try {
            // Validate employee data
            if (!employee.isValid()) {
                LOGGER.warning("Invalid employee data provided");
                return false;
            }

            // Check if employee ID already exists
            if (employeeDAO.employeeExists(employee.getId())) {
                LOGGER.warning("Employee ID already exists: " + employee.getId());
                return false;
            }

            // Insert employee
            boolean employeeAdded = employeeDAO.insertEmployee(employee);
            if (!employeeAdded) {
                LOGGER.warning("Failed to insert employee: " + employee.getId());
                return false;
            }

            // Create credentials if password provided
            if (password != null && !password.trim().isEmpty()) {
                boolean credentialsCreated = credentialsDAO.createCredentials(employee.getId(), password);
                if (!credentialsCreated) {
                    LOGGER.warning("Failed to create credentials for employee: " + employee.getId());
                    // Consider rollback here in a transactional environment
                }
            }

            LOGGER.info("Employee added successfully: " + employee.getId());
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding employee: " + employee.getId(), e);
            return false;
        }
    }

    /**
     * Update employee information
     */
    public boolean updateEmployee(Employee employee) {
        try {
            if (!employee.isValid()) {
                LOGGER.warning("Invalid employee data provided for update");
                return false;
            }

            boolean updated = employeeDAO.updateEmployee(employee);
            if (updated) {
                LOGGER.info("Employee updated successfully: " + employee.getId());
            } else {
                LOGGER.warning("Employee update failed: " + employee.getId());
            }

            return updated;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating employee: " + employee.getId(), e);
            return false;
        }
    }

    /**
     * Delete employee and associated credentials
     */
    public boolean deleteEmployee(int employeeId) {
        try {
            // Delete credentials first (if they exist)
            if (credentialsDAO.credentialsExist(employeeId)) {
                credentialsDAO.deleteCredentials(employeeId);
            }

            // Delete employee
            boolean deleted = employeeDAO.deleteEmployee(employeeId);
            if (deleted) {
                LOGGER.info("Employee deleted successfully: " + employeeId);
            } else {
                LOGGER.warning("Employee deletion failed: " + employeeId);
            }

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Get employees by status
     */
    public List<Employee> getEmployeesByStatus(String status) {
        try {
            return employeeDAO.getEmployeesByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving employees by status: " + status, e);
            throw new RuntimeException("Failed to retrieve employees by status", e);
        }
    }

    /**
     * Get all positions
     */
    public List<Position> getAllPositions() {
        try {
            return positionDAO.getAllPositions();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving positions", e);
            throw new RuntimeException("Failed to retrieve positions", e);
        }
    }

    /**
     * Get employee count by status
     */
    public int getEmployeeCountByStatus(String status) {
        try {
            return employeeDAO.getEmployeeCountByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by status: " + status, e);
            return 0;
        }
    }
}
