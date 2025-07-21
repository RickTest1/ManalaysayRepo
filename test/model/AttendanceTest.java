package model;

import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class AttendanceTest {

    @Test
    public void testGetAndSetEmployeeId() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(1);
        assertEquals(1, attendance.getEmployeeId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmployeeIdInvalid() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(0); // should throw
    }

    @Test
    public void testGetAndSetDate() {
        Attendance attendance = new Attendance();
        Date today = Date.valueOf(LocalDate.now());
        attendance.setDate(today);
        assertEquals(today, attendance.getDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateNull() {
        Attendance attendance = new Attendance();
        attendance.setDate(null); // should throw
    }

    @Test
    public void testGetAndSetLogInOut() {
        Attendance attendance = new Attendance();
        Time logIn = Time.valueOf(LocalTime.of(8, 0));
        Time logOut = Time.valueOf(LocalTime.of(17, 0));

        attendance.setLogIn(logIn);
        attendance.setLogOut(logOut);

        assertEquals(logIn, attendance.getLogIn());
        assertEquals(logOut, attendance.getLogOut());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLogOutBeforeLogIn() {
        Attendance attendance = new Attendance();
        attendance.setLogIn(Time.valueOf(LocalTime.of(9, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(8, 0))); // should throw
    }

    @Test
    public void testGetWorkHours() {
        Attendance attendance = new Attendance();
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 0)));
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
        assertEquals(9.0, attendance.getWorkHours(), 0.01);
    }

    @Test
    public void testGetWorkHoursWithNullTimes() {
        Attendance attendance = new Attendance();
        assertEquals(0.0, attendance.getWorkHours(), 0.0);
    }

    @Test
    public void testIsLate() {
        Attendance attendance = new Attendance();
        attendance.setLogIn(Time.valueOf(LocalTime.of(8, 30)));
        assertTrue(attendance.isLate());
    }

    @Test
    public void testIsNotLate() {
        Attendance attendance = new Attendance();
        attendance.setLogIn(Time.valueOf(LocalTime.of(7, 59)));
        assertFalse(attendance.isLate());
    }

    @Test
    public void testHasUndertime() {
        Attendance attendance = new Attendance();
        attendance.setLogOut(Time.valueOf(LocalTime.of(16, 59)));
        assertTrue(attendance.hasUndertime());
    }

    @Test
    public void testHasNoUndertime() {
        Attendance attendance = new Attendance();
        attendance.setLogOut(Time.valueOf(LocalTime.of(17, 0)));
        assertFalse(attendance.hasUndertime());
    }

    @Test
    public void testIsValid() {
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(1);
        attendance.setDate(Date.valueOf(LocalDate.now()));
        assertTrue(attendance.isValid());
    }

    @Test
    public void testIsNotValid() {
        Attendance attendance = new Attendance();
        assertFalse(attendance.isValid());
    }

    @Test
    public void testGetDisplayName() {
        Date date = Date.valueOf(LocalDate.of(2025, 7, 20));
        Attendance attendance = new Attendance(1, date, null, null);
        assertEquals("Attendance for Employee 1 on 2025-07-20", attendance.getDisplayName());
    }
}
