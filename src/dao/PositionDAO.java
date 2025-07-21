package dao;

import util.DBConnection;
import model.Position;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO for the positions table matching the actual database schema
 */
public class PositionDAO {
    private static final Logger LOGGER = Logger.getLogger(PositionDAO.class.getName());

    /**
     * Get all positions
     */
    public List<Position> getAllPositions() {
        List<Position> positions = new ArrayList<>();
        String query = "SELECT * FROM positions ORDER BY position_title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Position position = mapResultSetToPosition(rs);
                positions.add(position);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all positions", ex);
            throw new RuntimeException("Failed to fetch positions", ex);
        }

        return positions;
    }

    /**
     * Get position by ID
     */
    public Position getPositionById(int positionId) {
        if (positionId <= 0) {
            throw new IllegalArgumentException("Position ID must be positive");
        }

        String query = "SELECT * FROM positions WHERE position_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPosition(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching position with ID: " + positionId, ex);
            throw new RuntimeException("Failed to fetch position", ex);
        }

        return null;
    }

    /**
     * Get position by title
     */
    public Position getPositionByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Position title cannot be null or empty");
        }

        String query = "SELECT * FROM positions WHERE position_title = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPosition(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching position with title: " + title, ex);
            throw new RuntimeException("Failed to fetch position", ex);
        }

        return null;
    }

    /**
     * Insert a new position
     */
    public int insertPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (position.getPositionName() == null || position.getPositionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Position title is required");
        }

        // Check for duplicate position title
        if (getPositionByTitle(position.getPositionName()) != null) {
            throw new IllegalArgumentException("Position title already exists: " + position.getPositionName());
        }

        String sql = "INSERT INTO positions (position_title, basic_salary, rice_subsidy, phone_allowance, " +
                "clothing_allowance, gross_semi_monthly_rate, hourly_rate) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, position.getPositionName().trim());
            stmt.setDouble(2, position.getMonthlySalary()); // Using monthlySalary for basic_salary
            stmt.setDouble(3, position.getRiceSubsidy());
            stmt.setDouble(4, position.getPhoneAllowance());
            stmt.setDouble(5, position.getClothingAllowance());
            stmt.setDouble(6, position.getGrossSemiMonthlyRate());
            stmt.setDouble(7, position.getHourlyRate());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating position failed, no rows affected");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    position.setPositionId(generatedId);
                    LOGGER.info("Successfully inserted position: " + position.getPositionName());
                    return generatedId;
                } else {
                    throw new SQLException("Creating position failed, no ID obtained");
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting position: " + position.getPositionName(), ex);
            throw new RuntimeException("Failed to insert position: " + ex.getMessage(), ex);
        }
    }

    /**
     * Update an existing position
     */
    public boolean updatePosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (position.getPositionId() <= 0) {
            throw new IllegalArgumentException("Position ID must be positive");
        }
        if (position.getPositionName() == null || position.getPositionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Position title is required");
        }

        String sql = "UPDATE positions SET position_title = ?, basic_salary = ?, rice_subsidy = ?, " +
                "phone_allowance = ?, clothing_allowance = ?, gross_semi_monthly_rate = ?, hourly_rate = ? " +
                "WHERE position_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, position.getPositionName().trim());
            stmt.setDouble(2, position.getMonthlySalary());
            stmt.setDouble(3, position.getRiceSubsidy());
            stmt.setDouble(4, position.getPhoneAllowance());
            stmt.setDouble(5, position.getClothingAllowance());
            stmt.setDouble(6, position.getGrossSemiMonthlyRate());
            stmt.setDouble(7, position.getHourlyRate());
            stmt.setInt(8, position.getPositionId());

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                LOGGER.info("Successfully updated position: " + position.getPositionName());
            } else {
                LOGGER.warning("No position found with ID: " + position.getPositionId());
            }

            return updated;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating position with ID: " + position.getPositionId(), ex);
            throw new RuntimeException("Failed to update position: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete a position
     */
    public boolean deletePosition(int positionId) {
        if (positionId <= 0) {
            throw new IllegalArgumentException("Position ID must be positive");
        }

        // Check if position is being used by any employees
        if (isPositionInUse(positionId)) {
            throw new RuntimeException("Cannot delete position: it is being used by employees");
        }

        String sql = "DELETE FROM positions WHERE position_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, positionId);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                LOGGER.info("Successfully deleted position with ID: " + positionId);
            } else {
                LOGGER.warning("No position found with ID: " + positionId);
            }

            return deleted;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting position with ID: " + positionId, ex);
            throw new RuntimeException("Failed to delete position: " + ex.getMessage(), ex);
        }
    }

    /**
     * Check if a position exists
     */
    public boolean positionExists(int positionId) {
        if (positionId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM positions WHERE position_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if position exists: " + positionId, ex);
            return false;
        }
    }

    /**
     * Check if a position is being used by any employees
     */
    public boolean isPositionInUse(int positionId) {
        if (positionId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM employees WHERE position_id = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if position is in use: " + positionId, ex);
            return true; // Return true to be safe and prevent deletion
        }
    }

    /**
     * Get positions within a salary range
     */
    public List<Position> getPositionsBySalaryRange(double minSalary, double maxSalary) {
        if (minSalary < 0 || maxSalary < 0) {
            throw new IllegalArgumentException("Salary values cannot be negative");
        }
        if (minSalary > maxSalary) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
        }

        List<Position> positions = new ArrayList<>();
        String query = "SELECT * FROM positions WHERE basic_salary >= ? AND basic_salary <= ? ORDER BY basic_salary";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, minSalary);
            stmt.setDouble(2, maxSalary);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Position position = mapResultSetToPosition(rs);
                positions.add(position);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching positions by salary range: " + minSalary + " - " + maxSalary, ex);
            throw new RuntimeException("Failed to fetch positions by salary range", ex);
        }

        return positions;
    }

    /**
     * Get count of employees per position
     */
    public Map<Integer, Integer> getEmployeeCountByPosition() {
        Map<Integer, Integer> employeeCounts = new HashMap<>();
        String query = "SELECT p.position_id, p.position_title, COUNT(e.employee_id) as employee_count " +
                "FROM positions p LEFT JOIN employees e ON p.position_id = e.position_id " +
                "GROUP BY p.position_id, p.position_title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int positionId = rs.getInt("position_id");
                int count = rs.getInt("employee_count");
                employeeCounts.put(positionId, count);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by position", ex);
            throw new RuntimeException("Failed to get employee count by position", ex);
        }

        return employeeCounts;
    }

    /**
     * Map ResultSet to Position object
     */
    private Position mapResultSetToPosition(ResultSet rs) throws SQLException {
        Position position = new Position();
        position.setPositionId(rs.getInt("position_id"));
        position.setPositionName(rs.getString("position_title"));
        position.setMonthlySalary(rs.getDouble("basic_salary"));

        // Additional fields from the database schema
        position.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        position.setPhoneAllowance(rs.getDouble("phone_allowance"));
        position.setClothingAllowance(rs.getDouble("clothing_allowance"));
        position.setGrossSemiMonthlyRate(rs.getDouble("gross_semi_monthly_rate"));
        position.setHourlyRate(rs.getDouble("hourly_rate"));

        return position;
    }
}