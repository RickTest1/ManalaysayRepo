package dao;

import model.Attendance;
import org.junit.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class AttendanceDAOTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetAttendanceByEmployeeId_invalidId() {
        AttendanceDAO dao = new AttendanceDAO();
        dao.getAttendanceByEmployeeId(0); // Should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAttendanceByEmployeeIdBetweenDates_invalidDates() {
        AttendanceDAO dao = new AttendanceDAO();
        dao.getAttendanceByEmployeeIdBetweenDates(1, LocalDate.now(), LocalDate.now().minusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertAttendance_nullAttendance() {
        AttendanceDAO dao = new AttendanceDAO();
        dao.insertAttendance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteAttendance_invalidId() {
        AttendanceDAO dao = new AttendanceDAO();
        dao.deleteAttendance(0); // Should throw exception
    }
}
