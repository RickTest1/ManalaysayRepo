package model;

import java.sql.Date;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LeaveRequestTest {

    private LeaveRequest leaveRequest;
    private Date startDate;
    private Date endDate;

    @Before
    public void setUp() {
        startDate = Date.valueOf(LocalDate.of(2025, 7, 1));
        endDate = Date.valueOf(LocalDate.of(2025, 7, 5));
        leaveRequest = new LeaveRequest(1001, startDate, endDate, LeaveRequest.ANNUAL_LEAVE);
        leaveRequest.setLeaveId(1);
    }

    @Test
    public void testGetAndSetLeaveId() {
        leaveRequest.setLeaveId(10);
        assertEquals(10, leaveRequest.getLeaveId());
    }

    @Test
    public void testGetAndSetEmployeeId() {
        leaveRequest.setEmployeeId(2002);
        assertEquals(2002, leaveRequest.getEmployeeId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmployeeIdInvalid() {
        leaveRequest.setEmployeeId(0);
    }

    @Test
    public void testGetAndSetStartDate() {
        Date newStart = Date.valueOf("2025-08-01");
        leaveRequest.setStartDate(newStart);
        assertEquals(newStart, leaveRequest.getStartDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartDateNull() {
        leaveRequest.setStartDate(null);
    }

    @Test
    public void testGetAndSetEndDate() {
        Date newEnd = Date.valueOf("2025-08-10");
        leaveRequest.setEndDate(newEnd);
        assertEquals(newEnd, leaveRequest.getEndDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndDateBeforeStartDate() {
        leaveRequest.setEndDate(Date.valueOf("2025-06-30"));
    }

    @Test
    public void testGetAndSetLeaveType() {
        leaveRequest.setLeaveType("Sick");
        assertEquals("Sick", leaveRequest.getLeaveType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLeaveTypeInvalid() {
        leaveRequest.setLeaveType("   ");
    }

    @Test
    public void testGetAndSetStatus() {
        leaveRequest.setStatus(LeaveRequest.STATUS_APPROVED);
        assertEquals(LeaveRequest.STATUS_APPROVED, leaveRequest.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStatusInvalid() {
        leaveRequest.setStatus("");
    }

    @Test
    public void testGetLeaveDays() {
        assertEquals(5, leaveRequest.getLeaveDays()); // July 1 to July 5 inclusive
    }

    @Test
    public void testIsApprovedPendingRejected() {
        leaveRequest.setStatus(LeaveRequest.STATUS_APPROVED);
        assertTrue(leaveRequest.isApproved());
        leaveRequest.setStatus(LeaveRequest.STATUS_PENDING);
        assertTrue(leaveRequest.isPending());
        leaveRequest.setStatus(LeaveRequest.STATUS_REJECTED);
        assertTrue(leaveRequest.isRejected());
    }

    @Test
    public void testGetStartAndEndDateAsLocalDate() {
        assertEquals(LocalDate.of(2025, 7, 1), leaveRequest.getStartDateAsLocalDate());
        assertEquals(LocalDate.of(2025, 7, 5), leaveRequest.getEndDateAsLocalDate());
    }

    @Test
    public void testIsValidLeaveType() {
        assertTrue(leaveRequest.isValidLeaveType());
        LeaveRequest lr = new LeaveRequest();
        assertFalse(lr.isValidLeaveType());
    }

    @Test
    public void testOverlaps() {
        Date checkStart = Date.valueOf("2025-07-03");
        Date checkEnd = Date.valueOf("2025-07-04");
        assertTrue(leaveRequest.overlaps(checkStart, checkEnd));

        Date nonOverlapStart = Date.valueOf("2025-07-06");
        Date nonOverlapEnd = Date.valueOf("2025-07-07");
        assertFalse(leaveRequest.overlaps(nonOverlapStart, nonOverlapEnd));
    }

    @Test
    public void testIsValid() {
        assertTrue(leaveRequest.isValid());

        LeaveRequest invalid = new LeaveRequest();
        assertFalse(invalid.isValid());
    }

    @Test
    public void testGetDisplayName() {
        String displayName = leaveRequest.getDisplayName();
        assertTrue(displayName.contains("Leave Request #1"));
        assertTrue(displayName.contains(LeaveRequest.ANNUAL_LEAVE));
    }

    @Test
    public void testEqualsAndHashCode() {
        LeaveRequest other = new LeaveRequest(1001, startDate, endDate, LeaveRequest.ANNUAL_LEAVE);
        other.setLeaveId(1);
        assertTrue(leaveRequest.equals(other));
        assertEquals(leaveRequest.hashCode(), other.hashCode());
    }

    @Test
    public void testToString() {
        String str = leaveRequest.toString();
        assertTrue(str.contains("leaveId=1"));
        assertTrue(str.contains("employeeId=1001"));
    }
}
