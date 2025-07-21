package model;

import java.sql.Date;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OvertimeTest {

    private Overtime overtime;
    private Date date;

    @Before
    public void setUp() {
        date = Date.valueOf(LocalDate.of(2025, 7, 20));
        overtime = new Overtime(1001, date, 5.5, "Project deadline");
        overtime.setOvertimeId(1);
    }

    @Test
    public void testGetAndSetOvertimeId() {
        overtime.setOvertimeId(10);
        assertEquals(10, overtime.getOvertimeId());
    }

    @Test
    public void testGetAndSetEmployeeId() {
        overtime.setEmployeeId(2002);
        assertEquals(2002, overtime.getEmployeeId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmployeeIdInvalid() {
        overtime.setEmployeeId(0);
    }

    @Test
    public void testGetAndSetDate() {
        Date newDate = Date.valueOf("2025-08-01");
        overtime.setDate(newDate);
        assertEquals(newDate, overtime.getDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDateNull() {
        overtime.setDate(null);
    }

    @Test
    public void testGetAndSetHours() {
        overtime.setHours(8);
        assertEquals(8, overtime.getHours(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHoursNegative() {
        overtime.setHours(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHoursExceedLimit() {
        overtime.setHours(25);
    }

    @Test
    public void testGetAndSetReason() {
        overtime.setReason("  System upgrade  ");
        assertEquals("System upgrade", overtime.getReason());
    }

    @Test
    public void testIsApprovedAndSetApproved() {
        assertFalse(overtime.isApproved());
        overtime.setApproved(true);
        assertTrue(overtime.isApproved());
    }

    @Test
    public void testGetDateAsLocalDateAndSetDateFromLocalDate() {
        LocalDate localDate = overtime.getDateAsLocalDate();
        assertEquals(LocalDate.of(2025, 7, 20), localDate);

        Overtime newOvertime = new Overtime();
        newOvertime.setDateFromLocalDate(LocalDate.of(2025, 8, 15));
        assertEquals(Date.valueOf("2025-08-15"), newOvertime.getDate());
    }

    @Test
    public void testCalculateOvertimePay() {
        double pay = overtime.calculateOvertimePay(100, 1.5);
        assertEquals(5.5 * 100 * 1.5, pay, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateOvertimePayWithNegativeRate() {
        overtime.calculateOvertimePay(-100, 1.5);
    }

    @Test
    public void testIsValidOvertimeHours() {
        assertTrue(overtime.isValidOvertimeHours());
        overtime.setHours(0);
        assertFalse(overtime.isValidOvertimeHours());
    }

    @Test
    public void testHasReason() {
        assertTrue(overtime.hasReason());
        overtime.setReason("   ");
        assertFalse(overtime.hasReason());
    }

    @Test
    public void testGetFormattedHours() {
        assertEquals("5.50", overtime.getFormattedHours());
    }

    @Test
    public void testEqualsAndHashCode() {
        Overtime other = new Overtime(1001, date, 5.5, "Project deadline");
        other.setOvertimeId(1);
        assertTrue(overtime.equals(other));
        assertEquals(overtime.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsWithDifferentObject() {
        assertFalse(overtime.equals(new Object()));
    }

    @Test
    public void testToString() {
        String str = overtime.toString();
        assertTrue(str.contains("overtimeId=1"));
        assertTrue(str.contains("employeeId=1001"));
        assertTrue(str.contains("5.50"));
    }
}
