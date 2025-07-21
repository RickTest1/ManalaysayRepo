package service;

import dao.LeaveRequestDAO;
import dao.EmployeeDAO;
import model.LeaveRequest;
import model.Employee;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for leave request management operations
 */
public class LeaveRequestService {
    private static final Logger LOGGER = Logger.getLogger(LeaveRequestService.class.getName());

    private final LeaveRequestDAO leaveRequestDAO;
    private final EmployeeDAO employeeDAO;

    public LeaveRequestService() {
        this.leaveRequestDAO = new LeaveRequestDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * Submit a new leave request
     */
    public boolean submitLeaveRequest(int employeeId, String leaveType, LocalDate startDate, LocalDate endDate) {
        try {
            // Validate employee exists
            if (!employeeDAO.employeeExists(employeeId)) {
                LOGGER.warning("Employee not found for leave request: " + employeeId);
                return false;
            }

            // Check for overlapping leave requests
            if (leaveRequestDAO.hasOverlappingLeave(employeeId, startDate, endDate, null)) {
                LOGGER.warning("Overlapping leave request detected for employee: " + employeeId);
                return false;
            }

            // Create leave request
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setEmployeeId(employeeId);
            leaveRequest.setLeaveType(leaveType);
            leaveRequest.setStartDate(Date.valueOf(startDate));
            leaveRequest.setEndDate(Date.valueOf(endDate));
            leaveRequest.setStatus(LeaveRequest.STATUS_PENDING);

            int leaveId = leaveRequestDAO.insertLeaveRequest(leaveRequest);
            boolean success = leaveId > 0;

            if (success) {
                LOGGER.info("Leave request submitted for employee " + employeeId +
                        " from " + startDate + " to " + endDate);
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error submitting leave request for employee: " + employeeId, e);
            return false;
        }
    }

    /**
     * Approve or reject leave request
     */
    public boolean processLeaveRequest(int leaveId, String status) {
        try {
            if (!LeaveRequest.STATUS_APPROVED.equals(status) && !LeaveRequest.STATUS_REJECTED.equals(status)) {
                LOGGER.warning("Invalid status for leave request processing: " + status);
                return false;
            }

            boolean updated = leaveRequestDAO.updateLeaveStatus(leaveId, status);
            if (updated) {
                LOGGER.info("Leave request " + leaveId + " processed with status: " + status);
            }

            return updated;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing leave request: " + leaveId, e);
            return false;
        }
    }

    /**
     * Get leave requests for employee
     */
    public List<LeaveRequest> getLeaveRequestsByEmployee(int employeeId) {
        try {
            return leaveRequestDAO.getLeaveRequestsByEmployeeId(employeeId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving leave requests for employee: " + employeeId, e);
            throw new RuntimeException("Failed to retrieve leave requests", e);
        }
    }

    /**
     * Get leave requests by status
     */
    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        try {
            return leaveRequestDAO.getLeaveRequestsByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving leave requests by status: " + status, e);
            throw new RuntimeException("Failed to retrieve leave requests", e);
        }
    }

    /**
     * Get pending leave requests
     */
    public List<LeaveRequest> getPendingLeaveRequests() {
        return getLeaveRequestsByStatus(LeaveRequest.STATUS_PENDING);
    }

    /**
     * Get approved leave requests for employee in date range
     */
    public List<LeaveRequest> getApprovedLeavesByEmployeeAndDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            return leaveRequestDAO.getApprovedLeavesByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving approved leaves for employee: " + employeeId, e);
            throw new RuntimeException("Failed to retrieve approved leaves", e);
        }
    }

    /**
     * Get leave request by ID
     */
    public Optional<LeaveRequest> getLeaveRequestById(int leaveId) {
        try {
            return leaveRequestDAO.getLeaveRequestById(leaveId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving leave request: " + leaveId, e);
            return Optional.empty();
        }
    }

    /**
     * Update leave request
     */
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        try {
            if (!leaveRequest.isValid()) {
                LOGGER.warning("Invalid leave request data provided for update");
                return false;
            }

            // Check for overlapping leave requests (excluding current request)
            if (leaveRequestDAO.hasOverlappingLeave(
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getStartDateAsLocalDate(),
                    leaveRequest.getEndDateAsLocalDate(),
                    leaveRequest.getLeaveId())) {
                LOGGER.warning("Overlapping leave request detected for employee: " + leaveRequest.getEmployeeId());
                return false;
            }

            boolean updated = leaveRequestDAO.updateLeaveRequest(leaveRequest);
            if (updated) {
                LOGGER.info("Leave request updated successfully: " + leaveRequest.getLeaveId());
            }

            return updated;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating leave request: " + leaveRequest.getLeaveId(), e);
            return false;
        }
    }

    /**
     * Delete leave request
     */
    public boolean deleteLeaveRequest(int leaveId) {
        try {
            boolean deleted = leaveRequestDAO.deleteLeaveRequest(leaveId);
            if (deleted) {
                LOGGER.info("Leave request deleted successfully: " + leaveId);
            }

            return deleted;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting leave request: " + leaveId, e);
            return false;
        }
    }
}