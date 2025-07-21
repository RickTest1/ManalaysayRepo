package service;

import dao.CredentialsDAO;
import dao.EmployeeDAO;
import model.Employee;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for handling user authentication
 */
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    private final CredentialsDAO credentialsDAO;
    private final EmployeeDAO employeeDAO;

    public AuthenticationService() {
        this.credentialsDAO = new CredentialsDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Authenticate user credentials
     */
    public AuthenticationResult authenticate(int employeeId, String password) {
        try {
            if (employeeId <= 0 || password == null || password.trim().isEmpty()) {
                return new AuthenticationResult(false, "Invalid credentials provided");
            }

            // Check credentials
            boolean isValid = credentialsDAO.authenticateUser(employeeId, password);

            if (isValid) {
                // Get employee details
                Employee employee = employeeDAO.getEmployeeById(employeeId);
                if (employee != null) {
                    LOGGER.info("Authentication successful for employee: " + employeeId);
                    return new AuthenticationResult(true, "Authentication successful", employee);
                } else {
                    LOGGER.warning("Employee not found after successful authentication: " + employeeId);
                    return new AuthenticationResult(false, "Employee record not found");
                }
            } else {
                LOGGER.warning("Authentication failed for employee: " + employeeId);
                return new AuthenticationResult(false, "Invalid employee ID or password");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Authentication error for employee: " + employeeId, e);
            return new AuthenticationResult(false, "Authentication system error");
        }
    }

    /**
     * Change password for an employee
     */
    public boolean changePassword(int employeeId, String currentPassword, String newPassword) {
        try {
            // Verify current password
            if (!credentialsDAO.authenticateUser(employeeId, currentPassword)) {
                return false;
            }

            // Update password
            return credentialsDAO.updatePassword(employeeId, newPassword);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password for employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Check if employee has credentials
     */
    public boolean hasCredentials(int employeeId) {
        try {
            return credentialsDAO.credentialsExist(employeeId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking credentials for employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Authentication result container
     */
    public static class AuthenticationResult {
        private final boolean successful;
        private final String message;
        private final Employee employee;

        public AuthenticationResult(boolean successful, String message) {
            this(successful, message, null);
        }

        public AuthenticationResult(boolean successful, String message, Employee employee) {
            this.successful = successful;
            this.message = message;
            this.employee = employee;
        }

        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
        public Employee getEmployee() { return employee; }
    }
}