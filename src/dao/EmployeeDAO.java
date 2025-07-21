package dao;

import util.DBConnection;
import model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all employees", ex);
            throw new RuntimeException("Failed to fetch employees", ex);
        }

        return employees;
    }

    public Employee getEmployeeById(int employeeId) {
        String query = "SELECT * FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch employee", ex);
        }

        return null;
    }

    /**
     * Get employee with position details using the view
     */
    public Employee getEmployeeWithPositionDetails(int employeeId) {
        String query = "SELECT * FROM v_employee_details WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapViewResultSetToEmployee(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employee details with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch employee details", ex);
        }

        return null;
    }

    /**
     * Enhanced insertEmployee method matching the actual database schema
     */
    public boolean insertEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        // Validate required fields
        if (e.getId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (e.getFirstName() == null || e.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (e.getLastName() == null || e.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        // Validate status enum
        if (e.getStatus() != null && !e.getStatus().trim().isEmpty()) {
            String status = e.getStatus().trim();
            if (!status.equals("Regular") && !status.equals("Probationary")) {
                throw new IllegalArgumentException("Status must be either 'Regular' or 'Probationary'");
            }
        }

        // Check for duplicate employee ID
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {

            checkStmt.setInt(1, e.getId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Employee ID " + e.getId() + " already exists");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking for duplicate employee ID: " + e.getId(), ex);
            throw new RuntimeException("Error checking for duplicate employee ID: " + ex.getMessage(), ex);
        }

        // SQL matching actual database schema
        String sql = "INSERT INTO employees (employee_id, last_name, first_name, birthday, address, " +
                "phone_number, sss_number, philhealth_number, tin_number, pagibig_number, " +
                "status, position_id, supervisor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getId());
            stmt.setString(2, e.getLastName() != null ? e.getLastName().trim() : null);
            stmt.setString(3, e.getFirstName() != null ? e.getFirstName().trim() : null);
            stmt.setDate(4, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(5, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(6, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(7, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(8, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(9, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(10, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(11, e.getStatus() != null ? e.getStatus().trim() : "Regular");

            // For position_id, we need to get this from the position string or set a default
            // This assumes you have a way to map position names to IDs
            stmt.setInt(12, getPositionId(e.getPosition()));

            // For supervisor_id, we need to convert supervisor name to ID or set null
            stmt.setObject(13, getSupervisorId(e.getImmediateSupervisor()), java.sql.Types.INTEGER);

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully inserted employee: " + e.getId() + " - " + e.getFullName());
                return true;
            } else {
                LOGGER.warning("No rows affected when inserting employee: " + e.getId());
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting employee: " + e.getId(), ex);
            throw new RuntimeException("Failed to insert employee: " + ex.getMessage(), ex);
        }
    }

    public boolean updateEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (e.getId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "UPDATE employees SET last_name=?, first_name=?, birthday=?, address=?, " +
                "phone_number=?, sss_number=?, philhealth_number=?, tin_number=?, " +
                "pagibig_number=?, status=?, position_id=?, supervisor_id=? WHERE employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getLastName() != null ? e.getLastName().trim() : null);
            stmt.setString(2, e.getFirstName() != null ? e.getFirstName().trim() : null);
            stmt.setDate(3, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(4, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(5, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(6, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(7, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(8, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(9, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(10, e.getStatus() != null ? e.getStatus().trim() : "Regular");
            stmt.setInt(11, getPositionId(e.getPosition()));
            stmt.setObject(12, getSupervisorId(e.getImmediateSupervisor()), java.sql.Types.INTEGER);
            stmt.setInt(13, e.getId());

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully updated employee: " + e.getId() + " - " + e.getFullName());
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + e.getId() + " for update");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating employee with ID: " + e.getId(), ex);
            throw new RuntimeException("Failed to update employee: " + ex.getMessage(), ex);
        }
    }

    public boolean deleteEmployee(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully deleted employee with ID: " + employeeId);
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + employeeId + " for deletion");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to delete employee: " + ex.getMessage(), ex);
        }
    }

    public List<Employee> getEmployeesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE status = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by status: " + status, ex);
            throw new RuntimeException("Failed to fetch employees by status", ex);
        }

        return employees;
    }

    public List<Employee> getEmployeesByPositionId(int positionId) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE position_id = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by position ID: " + positionId, ex);
            throw new RuntimeException("Failed to fetch employees by position", ex);
        }

        return employees;
    }

    public List<Employee> getEmployeesBySupervisor(int supervisorId) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE supervisor_id = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by supervisor ID: " + supervisorId, ex);
            throw new RuntimeException("Failed to fetch employees by supervisor", ex);
        }

        return employees;
    }

    /**
     * Enhanced mapResultSetToEmployee matching actual database schema
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("employee_id"));
        e.setLastName(rs.getString("last_name"));
        e.setFirstName(rs.getString("first_name"));

        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            e.setBirthday(birthday.toLocalDate());
        }

        e.setAddress(rs.getString("address"));
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setSssNumber(rs.getString("sss_number"));
        e.setPhilhealthNumber(rs.getString("philhealth_number"));
        e.setTinNumber(rs.getString("tin_number"));
        e.setPagibigNumber(rs.getString("pagibig_number"));
        e.setStatus(rs.getString("status"));

        // Get position name from position_id
        int positionId = rs.getInt("position_id");
        e.setPosition(getPositionName(positionId));

        // Get supervisor name from supervisor_id
        Integer supervisorId = rs.getObject("supervisor_id", Integer.class);
        if (supervisorId != null) {
            e.setImmediateSupervisor(getSupervisorName(supervisorId));
        }

        // Handle timestamps
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            e.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            e.setUpdatedAt(updated.toLocalDateTime());
        }

        return e;
    }

    /**
     * Map view result set to Employee with position details
     */
    private Employee mapViewResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("employee_id"));
        e.setLastName(rs.getString("last_name"));
        e.setFirstName(rs.getString("first_name"));

        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            e.setBirthday(birthday.toLocalDate());
        }

        e.setAddress(rs.getString("address"));
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setSssNumber(rs.getString("sss_number"));
        e.setPhilhealthNumber(rs.getString("philhealth_number"));
        e.setTinNumber(rs.getString("tin_number"));
        e.setPagibigNumber(rs.getString("pagibig_number"));
        e.setStatus(rs.getString("status"));
        e.setPosition(rs.getString("position_title"));
        e.setImmediateSupervisor(rs.getString("supervisor_name"));

        // Set basic salary from position
        e.setBasicSalary(rs.getDouble("basic_salary"));

        return e;
    }

    // Helper methods to convert between position names and IDs
    private int getPositionId(String positionName) {
        if (positionName == null) return 1; // Default position

        String query = "SELECT position_id FROM positions WHERE position_title = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, positionName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("position_id");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting position ID for: " + positionName, ex);
        }

        return 1; // Default position ID if not found
    }

    private String getPositionName(int positionId) {
        String query = "SELECT position_title FROM positions WHERE position_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("position_title");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting position name for ID: " + positionId, ex);
        }

        return "Unknown Position";
    }

    private Integer getSupervisorId(String supervisorName) {
        if (supervisorName == null || supervisorName.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT employee_id FROM employees WHERE CONCAT(last_name, ', ', first_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, supervisorName.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("employee_id");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting supervisor ID for: " + supervisorName, ex);
        }

        return null;
    }

    private String getSupervisorName(int supervisorId) {
        String query = "SELECT CONCAT(last_name, ', ', first_name) as full_name FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supervisorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("full_name");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting supervisor name for ID: " + supervisorId, ex);
        }

        return "Unknown Supervisor";
    }

    /**
     * Utility method to check if an employee exists
     */
    public boolean employeeExists(int employeeId) {
        if (employeeId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if employee exists: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Get employee count by status
     */
    public int getEmployeeCountByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String query = "SELECT COUNT(*) FROM employees WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by status: " + status, ex);
            throw new RuntimeException("Failed to get employee count", ex);
        }

        return 0;
    }
}