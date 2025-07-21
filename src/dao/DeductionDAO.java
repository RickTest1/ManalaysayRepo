package dao;

import model.Deduction;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DeductionDAO - Note: This table doesn't exist in the actual database schema
 * This DAO is included for completeness but may need to be removed or
 * the table created if deductions functionality is needed
 */
public class DeductionDAO {
    private static final Logger LOGGER = Logger.getLogger(DeductionDAO.class.getName());

    /**
     * Check if deductions table exists
     */
    private boolean deductionsTableExists() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'deductions'")) {

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking if deductions table exists", e);
            return false;
        }
    }

    /**
     * Create deductions table if it doesn't exist
     */
    private void createDeductionsTableIfNotExists() {
        if (deductionsTableExists()) {
            return;
        }

        String createTableSQL = """
            CREATE TABLE deductions (
                deduction_id INT AUTO_INCREMENT PRIMARY KEY,
                employee_id INT NOT NULL,
                type VARCHAR(50) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                description TEXT,
                deduction_date DATE DEFAULT (CURRENT_DATE),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
            )
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {

            stmt.executeUpdate();
            LOGGER.info("Created deductions table");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating deductions table", e);
            throw new RuntimeException("Failed to create deductions table", e);
        }
    }

    /**
     * Adds a deduction record to the database
     */
    public void addDeduction(Deduction deduction) throws SQLException {
        if (deduction == null) {
            throw new IllegalArgumentException("Deduction cannot be null");
        }

        createDeductionsTableIfNotExists();

        String sql = "INSERT INTO deductions (employee_id, type, amount, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, deduction.getEmployeeId());
            stmt.setString(2, deduction.getType());
            stmt.setDouble(3, deduction.getAmount());
            stmt.setString(4, deduction.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deduction.setDeductionId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("Successfully added deduction for employee: " + deduction.getEmployeeId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding deduction", e);
            throw e;
        }
    }

    /**
     * Retrieves all deductions for a specific employee
     */
    public List<Deduction> getDeductionsByEmployeeId(int employeeId) throws SQLException {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        if (!deductionsTableExists()) {
            return new ArrayList<>(); // Return empty list if table doesn't exist
        }

        String sql = "SELECT * FROM deductions WHERE employee_id = ? ORDER BY deduction_date DESC";
        List<Deduction> deductions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ConcreteDeduction d = new ConcreteDeduction(
                        rs.getInt("employee_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                d.setDeductionId(rs.getInt("deduction_id"));

                Date deductionDate = rs.getDate("deduction_date");
                if (deductionDate != null) {
                    d.setDeductionDate(deductionDate);
                }

                deductions.add(d);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving deductions for employee ID: " + employeeId, e);
            throw e;
        }

        return deductions;
    }

    /**
     * Updates an existing deduction record
     */
    public boolean updateDeduction(Deduction deduction) throws SQLException {
        if (deduction == null || deduction.getDeductionId() <= 0) {
            throw new IllegalArgumentException("Invalid deduction or deduction ID");
        }

        if (!deductionsTableExists()) {
            return false;
        }

        String sql = "UPDATE deductions SET employee_id = ?, type = ?, amount = ?, description = ? WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deduction.getEmployeeId());
            stmt.setString(2, deduction.getType());
            stmt.setDouble(3, deduction.getAmount());
            stmt.setString(4, deduction.getDescription());
            stmt.setInt(5, deduction.getDeductionId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                LOGGER.info("Successfully updated deduction ID: " + deduction.getDeductionId());
            }

            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating deduction", e);
            throw e;
        }
    }

    /**
     * Deletes a deduction record
     */
    public boolean deleteDeduction(int deductionId) throws SQLException {
        if (deductionId <= 0) {
            throw new IllegalArgumentException("Deduction ID must be positive");
        }

        if (!deductionsTableExists()) {
            return false;
        }

        String sql = "DELETE FROM deductions WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deductionId);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                LOGGER.info("Successfully deleted deduction ID: " + deductionId);
            }

            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting deduction ID: " + deductionId, e);
            throw e;
        }
    }

    /**
     * Gets a specific deduction by ID
     */
    public Deduction getDeductionById(int deductionId) throws SQLException {
        if (deductionId <= 0) {
            throw new IllegalArgumentException("Deduction ID must be positive");
        }

        if (!deductionsTableExists()) {
            return null;
        }

        String sql = "SELECT * FROM deductions WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deductionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ConcreteDeduction deduction = new ConcreteDeduction(
                        rs.getInt("employee_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                deduction.setDeductionId(rs.getInt("deduction_id"));

                Date deductionDate = rs.getDate("deduction_date");
                if (deductionDate != null) {
                    deduction.setDeductionDate(deductionDate);
                }

                return deduction;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving deduction by ID: " + deductionId, e);
            throw e;
        }

        return null;
    }

    /**
     * Gets total deductions for an employee by type
     */
    public double getTotalDeductionsByType(int employeeId, String type) throws SQLException {
        if (employeeId <= 0 || type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid employee ID or deduction type");
        }

        if (!deductionsTableExists()) {
            return 0.0;
        }

        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM deductions WHERE employee_id = ? AND type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, type.trim());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating total deductions", e);
            throw e;
        }

        return 0.0;
    }

    /**
     * Gets all deduction types
     */
    public List<String> getAllDeductionTypes() {
        if (!deductionsTableExists()) {
            return new ArrayList<>();
        }

        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT type FROM deductions ORDER BY type";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("type"));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving deduction types", e);
        }

        return types;
    }

    /**
     * Concrete implementation of the abstract Deduction class
     */
    private static class ConcreteDeduction extends Deduction {
        private Date deductionDate;

        public ConcreteDeduction() {
            super();
        }

        public ConcreteDeduction(int employeeId, String type, double amount, String description) {
            super(employeeId, type, amount, description);
        }

        @Override
        public void calculateDeduction() {
            // For generic deductions loaded from database,
            // the amount is already calculated and stored
        }

        public Date getDeductionDate() {
            return deductionDate;
        }

        public void setDeductionDate(Date deductionDate) {
            this.deductionDate = deductionDate;
        }
    }
}