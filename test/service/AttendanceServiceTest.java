package service;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import model.Attendance;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AttendanceService
 */
public class AttendanceServiceTest {

    /**
     * Test recordLogIn with invalid employee ID (should fail)
     */
    @Test
    public void testRecordLogInWithInvalidEmployee() {
        AttendanceService service = new AttendanceService();
        boolean result = service.recordLogIn(0, LocalDate.now(), Time.valueOf("08:00:00"));
        assertFalse("Log in should fail for invalid employee ID", result);
    }

    /**
     * Test recordLogOut when no attendance record exists
     */
    @Test
    public void testRecordLogOutWithoutExistingRecord() {
        AttendanceService service = new AttendanceService();
        boolean result = service.recordLogOut(0, LocalDate.now(), Time.valueOf("17:00:00"));
        assertFalse("Log out should fail if no attendance record exists", result);
    }

    /**
     * Test getAttendanceByEmployee returns non-null list (may be empty)
     */
    @Test
    public void testGetAttendanceByEmployee() {
        AttendanceService service = new AttendanceService();
        List<Attendance> records = service.getAttendanceByEmployee(0);
        assertNotNull("Attendance list should not be null", records);
    }

    /**
     * Test deleteAttendance with invalid ID (should fail)
     */
    @Test
    public void testDeleteAttendanceInvalidId() {
        AttendanceService service = new AttendanceService();
        boolean deleted = service.deleteAttendance(-1);
        assertFalse("Delete should fail for invalid attendance ID", deleted);
    }
}
