package dao;

import util.DBConnection;
import model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AttendanceDAO {
    private static final Logger logger = Logger.getLogger(AttendanceDAO.class.getName());

    public List<Attendance> getAttendanceByEmployeeId(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY attendance_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = mapResultSetToAttendance(rs);
                    list.add(a);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee ID: " + empId, ex);
            throw new RuntimeException("Failed to retrieve attendance records", ex);
        }

        return list;
    }

    public List<Attendance> getAttendanceByEmployeeIdBetweenDates(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }

        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND attendance_date >= ? AND attendance_date <= ? ORDER BY attendance_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = mapResultSetToAttendance(rs);
                    list.add(a);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee ID: " + employeeId +
                    " between dates: " + periodStart + " and " + periodEnd, ex);
            throw new RuntimeException("Failed to retrieve attendance records", ex);
        }

        return list;
    }

    public int insertAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        if (attendance.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive, got: " + attendance.getEmployeeId());
        }
        if (attendance.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (attendance.getLogIn() == null) {
            throw new IllegalArgumentException("Log in time cannot be null");
        }

        // Validate date is not in the future
        LocalDate today = LocalDate.now();
        LocalDate attendanceDate = attendance.getDate().toLocalDate();
        if (attendanceDate.isAfter(today)) {
            throw new IllegalArgumentException("Attendance date cannot be in the future: " + attendanceDate);
        }

        // Validate log times
        if (attendance.getLogOut() != null) {
            LocalTime logIn = attendance.getLogIn().toLocalTime();
            LocalTime logOut = attendance.getLogOut().toLocalTime();

            if (logOut.isBefore(logIn)) {
                throw new IllegalArgumentException("Log out time (" + logOut +
                        ") cannot be before log in time (" + logIn + ")");
            }
        }

        // Check if employee exists
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {

            checkStmt.setInt(1, attendance.getEmployeeId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Employee ID " + attendance.getEmployeeId() +
                            " does not exist in the system");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error validating employee existence for ID: " + attendance.getEmployeeId(), e);
            throw new RuntimeException("Error validating employee: " + e.getMessage(), e);
        }

        // Check for duplicate attendance on the same date
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement duplicateStmt = conn.prepareStatement(
                     "SELECT id FROM attendance WHERE employee_id = ? AND attendance_date = ?")) {

            duplicateStmt.setInt(1, attendance.getEmployeeId());
            duplicateStmt.setDate(2, attendance.getDate());

            try (ResultSet rs = duplicateStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Attendance record already exists for employee " +
                            attendance.getEmployeeId() + " on date " + attendance.getDate());
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking for duplicate attendance", e);
            throw new RuntimeException("Error checking duplicate attendance: " + e.getMessage(), e);
        }

        // Insert attendance record
        String query = "INSERT INTO attendance (employee_id, attendance_date, log_in, log_out) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setDate(2, attendance.getDate());
            stmt.setTime(3, attendance.getLogIn());
            stmt.setTime(4, attendance.getLogOut());

            logger.info(String.format("Inserting attendance record for employee %d on %s: Log in: %s, Log out: %s",
                    attendance.getEmployeeId(), attendance.getDate(),
                    attendance.getLogIn(), attendance.getLogOut()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating attendance failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    attendance.setId(generatedId);

                    logger.info(String.format("Successfully inserted attendance record with ID: %d for employee %d",
                            generatedId, attendance.getEmployeeId()));

                    return generatedId;
                } else {
                    throw new SQLException("Creating attendance failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting attendance record", ex);
            throw new RuntimeException("Failed to insert attendance record: " + ex.getMessage(), ex);
        }
    }

    public boolean updateAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        if (attendance.getId() <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "UPDATE attendance SET employee_id = ?, attendance_date = ?, log_in = ?, log_out = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setDate(2, attendance.getDate());
            stmt.setTime(3, attendance.getLogIn());
            stmt.setTime(4, attendance.getLogOut());
            stmt.setInt(5, attendance.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating attendance record", ex);
            throw new RuntimeException("Failed to update attendance record", ex);
        }
    }

    public boolean deleteAttendance(int attendanceId) {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "DELETE FROM attendance WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendanceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting attendance record", ex);
            throw new RuntimeException("Failed to delete attendance record", ex);
        }
    }

    public Attendance getAttendanceById(int attendanceId) {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "SELECT * FROM attendance WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance by ID: " + attendanceId, ex);
            throw new RuntimeException("Failed to retrieve attendance record", ex);
        }

        return null;
    }

    public boolean attendanceExistsForDate(int employeeId, LocalDate attendanceDate) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (attendanceDate == null) {
            throw new IllegalArgumentException("Attendance date cannot be null");
        }

        String query = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND attendance_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(attendanceDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking attendance existence for employee " + employeeId + " on " + attendanceDate, ex);
            throw new RuntimeException("Failed to check attendance existence", ex);
        }

        return false;
    }

    public int countAttendanceDays(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }

        String query = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND attendance_date >= ? AND attendance_date <= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error counting attendance days for employee " + employeeId +
                    " between " + periodStart + " and " + periodEnd, ex);
            throw new RuntimeException("Failed to count attendance days", ex);
        }

        return 0;
    }

    public Attendance getAttendanceByEmployeeAndDate(int employeeId, LocalDate attendanceDate) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (attendanceDate == null) {
            throw new IllegalArgumentException("Attendance date cannot be null");
        }

        String query = "SELECT * FROM attendance WHERE employee_id = ? AND attendance_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(attendanceDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee " + employeeId + " on " + attendanceDate, ex);
            throw new RuntimeException("Failed to retrieve attendance record", ex);
        }

        return null;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id"));
        attendance.setEmployeeId(rs.getInt("employee_id"));
        attendance.setDate(rs.getDate("attendance_date"));
        attendance.setLogIn(rs.getTime("log_in"));
        attendance.setLogOut(rs.getTime("log_out"));

        // Handle timestamps if they exist
        try {
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) {
                attendance.setCreatedAt(created.toLocalDateTime());
            }
        } catch (SQLException e) {
            // created_at column might not exist in all queries
        }

        try {
            Timestamp updated = rs.getTimestamp("updated_at");
            if (updated != null) {
                attendance.setUpdatedAt(updated.toLocalDateTime());
            }
        } catch (SQLException e) {
            // updated_at column might not exist in all queries
        }

        return attendance;
    }
}