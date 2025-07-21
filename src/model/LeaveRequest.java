package model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

public class LeaveRequest extends BaseEntity {
    private int leaveId;
    private int employeeId;
    private Date startDate;
    private Date endDate;
    private String leaveType;
    private String status;

    // Common leave types
    public static final String ANNUAL_LEAVE = "Annual";
    public static final String SICK_LEAVE = "Sick";
    public static final String EMERGENCY_LEAVE = "Emergency";
    public static final String MATERNITY_LEAVE = "Maternity";
    public static final String PATERNITY_LEAVE = "Paternity";

    // Status constants
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";

    // Constructors
    public LeaveRequest() {
        super();
    }

    public LeaveRequest(int employeeId, Date startDate, Date endDate, String leaveType) {
        super();
        setEmployeeId(employeeId);
        setStartDate(startDate);
        setEndDate(endDate);
        setLeaveType(leaveType);
        this.status = STATUS_PENDING; // Default status
    }

    // Getters and Setters with validation
    public int getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(int leaveId) {
        this.leaveId = leaveId;
        touch();
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
        touch();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        this.startDate = startDate;
        touch();
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate != null && endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.endDate = endDate;
        touch();
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        if (leaveType == null || leaveType.trim().isEmpty()) {
            throw new IllegalArgumentException("Leave type cannot be null or empty");
        }
        this.leaveType = leaveType.trim();
        touch();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        this.status = status.trim();
        touch();
    }

    // Utility methods
    public long getLeaveDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return (diffInMillis / (1000 * 60 * 60 * 24)) + 1; // +1 to include both start and end dates
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(status);
    }

    public LocalDate getStartDateAsLocalDate() {
        return startDate != null ? startDate.toLocalDate() : null;
    }

    public LocalDate getEndDateAsLocalDate() {
        return endDate != null ? endDate.toLocalDate() : null;
    }

    public boolean isValidLeaveType() {
        return leaveType != null && !leaveType.trim().isEmpty();
    }

    public boolean overlaps(Date checkStart, Date checkEnd) {
        if (startDate == null || endDate == null || checkStart == null || checkEnd == null) {
            return false;
        }
        return !(endDate.before(checkStart) || startDate.after(checkEnd));
    }

    @Override
    public boolean isValid() {
        return employeeId > 0 &&
                startDate != null &&
                endDate != null &&
                leaveType != null && !leaveType.trim().isEmpty() &&
                status != null && !status.trim().isEmpty();
    }

    @Override
    public String getDisplayName() {
        return String.format("Leave Request #%d - %s (%s to %s)",
                leaveId, leaveType, startDate, endDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LeaveRequest that = (LeaveRequest) obj;
        return leaveId == that.leaveId &&
                employeeId == that.employeeId &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaveId, employeeId, startDate, endDate);
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId=" + leaveId +
                ", employeeId=" + employeeId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", leaveType='" + leaveType + '\'' +
                ", status='" + status + '\'' +
                ", days=" + getLeaveDays() +
                '}';
    }
}