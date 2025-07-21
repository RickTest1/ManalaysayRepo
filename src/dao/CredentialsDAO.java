package dao;

import util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CredentialsDAO {
    private static final Logger logger = Logger.getLogger(CredentialsDAO.class.getName());

    /**
     * Authenticate user - matches actual database schema
     */
    public boolean authenticateUser(int employeeId, String password) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        String query = "SELECT employee_id FROM credentials WHERE employee_id = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, password); // In real app, this should be hashed

            try (ResultSet rs = stmt.executeQuery()) {
                boolean authenticated = rs.next();
                if (authenticated) {
                    logger.info("Authentication successful for employee: " + employeeId);
                } else {
                    logger.warning("Authentication failed for employee: " + employeeId);
                }
                return authenticated;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error authenticating user: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Update password for an employee
     */
    public boolean updatePassword(int employeeId, String newPassword) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        String query = "UPDATE credentials SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword); // In real app, this should be hashed
            stmt.setInt(2, employeeId);

            int result = stmt.executeUpdate();

            if (result > 0) {
                logger.info("Password updated successfully for employee: " + employeeId);
                return true;
            } else {
                logger.warning("No credentials found for employee: " + employeeId);
                return false;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating password for employee: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Create new credentials for an employee
     */
    public boolean createCredentials(int employeeId, String password) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        String query = "INSERT INTO credentials (employee_id, password_hash) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, password); // In real app, this should be hashed

            int result = stmt.executeUpdate();

            if (result > 0) {
                logger.info("Successfully created credentials for employee: " + employeeId);
                return true;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error creating credentials for employee: " + employeeId, ex);

            if (ex.getErrorCode() == 1062) { // Duplicate entry
                throw new RuntimeException("Credentials already exist for employee: " + employeeId, ex);
            }
            if (ex.getErrorCode() == 1452) { // Foreign key constraint
                throw new RuntimeException("Employee ID " + employeeId + " does not exist", ex);
            }
        }

        return false;
    }

    /**
     * Check if credentials exist for an employee
     */
    public boolean credentialsExist(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String query = "SELECT 1 FROM credentials WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking credentials existence for employee: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Delete credentials for an employee
     */
    public boolean deleteCredentials(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String query = "DELETE FROM credentials WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                logger.info("Credentials deleted for employee: " + employeeId);
                return true;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting credentials for employee: " + employeeId, ex);
        }

        return false;
    }

    /**
     * Get password hash for an employee (for administrative purposes)
     */
    public String getPasswordHash(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String query = "SELECT password_hash FROM credentials WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving password hash for employee: " + employeeId, ex);
        }

        return null;
    }

    /**
     * Get credentials creation date
     */
    public LocalDateTime getCredentialsCreatedAt(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String query = "SELECT created_at FROM credentials WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp created = rs.getTimestamp("created_at");
                    return created != null ? created.toLocalDateTime() : null;
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving credentials creation date for employee: " + employeeId, ex);
        }

        return null;
    }

    /**
     * Get credentials last update date
     */
    public LocalDateTime getCredentialsUpdatedAt(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String query = "SELECT updated_at FROM credentials WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp updated = rs.getTimestamp("updated_at");
                    return updated != null ? updated.toLocalDateTime() : null;
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving credentials update date for employee: " + employeeId, ex);
        }

        return null;
    }

    /**
     * Get count of all credentials (for administrative purposes)
     */
    public int getCredentialsCount() {
        String query = "SELECT COUNT(*) FROM credentials";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error getting credentials count", ex);
        }

        return 0;
    }
}