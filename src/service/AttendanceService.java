package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import model.Attendance;
import model.Employee;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for attendance management operations
 */
public class AttendanceService {
    private static final Logger LOGGER = Logger.getLogger(AttendanceService.class.getName());

     private final EmployeeDAO employeeDAO;
    public dao.AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Record employee log in
     */
    public boolean recordLogIn(int employeeId, LocalDate date, Time logInTime) {
        try {
            // Validate employee exists
            if (!employeeDAO.employeeExists(employeeId)) {
                LOGGER.warning("Employee not found for log in: " + employeeId);
                return false;
            }

            // Check if attendance already exists for this date
            if (attendanceDAO.attendanceExistsForDate(employeeId, date)) {
                LOGGER.warning("Attendance already recorded for employee " + employeeId + " on " + date);
                return false;
            }

            // Create attendance record
            Attendance attendance = new Attendance();
            attendance.setEmployeeId(employeeId);
            attendance.setDate(Date.valueOf(date));
            attendance.setLogIn(logInTime);

            int attendanceId = attendanceDAO.insertAttendance(attendance);
            boolean success = attendanceId > 0;

            if (success) {
                LOGGER.info("Log in recorded for employee " + employeeId + " at " + logInTime);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording log in for employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Record employee log out
     */
    public boolean recordLogOut(int employeeId, LocalDate date, Time logOutTime) {
        try {
            // Get existing attendance record
            Attendance attendance = attendanceDAO.getAttendanceByEmployeeAndDate(employeeId, date);
            if (attendance == null) {
                LOGGER.warning("No attendance record found for employee " + employeeId + " on " + date);
                return false;
            }

            // Update with log out time
            attendance.setLogOut(logOutTime);

            boolean success = attendanceDAO.updateAttendance(attendance);
            if (success) {
                LOGGER.info("Log out recorded for employee " + employeeId + " at " + logOutTime);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording log out for employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Get attendance records for employee
     */
    public List<Attendance> getAttendanceByEmployee(int employeeId) {
        try {
            return attendanceDAO.getAttendanceByEmployeeId(employeeId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving attendance for employee: " + employeeId, e);
            throw new RuntimeException("Failed to retrieve attendance records", e);
        }
    }

    /**
     * Get attendance records for employee in date range
     */
    public List<Attendance> getAttendanceByEmployeeAndDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            return attendanceDAO.getAttendanceByEmployeeIdBetweenDates(employeeId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving attendance for employee: " + employeeId, e);
            throw new RuntimeException("Failed to retrieve attendance records", e);
        }
    }

    /**
     * Get attendance for specific date
     */
    public Attendance getAttendanceByDate(int employeeId, LocalDate date) {
        try {
            return attendanceDAO.getAttendanceByEmployeeAndDate(employeeId, date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving attendance for employee: " + employeeId + " on " + date, e);
            return null;
        }
    }

    /**
     * Count attendance days in period
     */
    public int countAttendanceDays(int employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            return attendanceDAO.countAttendanceDays(employeeId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting attendance days for employee: " + employeeId, e);
            return 0;
        }
    }

    /**
     * Update attendance record
     */
    public boolean updateAttendance(Attendance attendance) {
        try {
            if (!attendance.isValid()) {
                LOGGER.warning("Invalid attendance data provided for update");
                return false;
            }

            boolean updated = attendanceDAO.updateAttendance(attendance);
            if (updated) {
                LOGGER.info("Attendance updated successfully: " + attendance.getId());
            }

            return updated;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance: " + attendance.getId(), e);
            return false;
        }
    }

    /**
     * Delete attendance record
     */
    public boolean deleteAttendance(int attendanceId) {
        try {
            boolean deleted = attendanceDAO.deleteAttendance(attendanceId);
            if (deleted) {
                LOGGER.info("Attendance deleted successfully: " + attendanceId);
            }

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance: " + attendanceId, e);
            return false;
        }
    }
}
