package service;

import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import model.LeaveRequest;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LeaveRequestServiceTest {

    private LeaveRequestService leaveRequestService;
    private FakeEmployeeDAO fakeEmployeeDAO;
    private FakeLeaveRequestDAO fakeLeaveRequestDAO;

    @Before
    public void setUp() throws Exception {
        fakeEmployeeDAO = new FakeEmployeeDAO();
        fakeLeaveRequestDAO = new FakeLeaveRequestDAO();

        leaveRequestService = new LeaveRequestService();

        // Inject fakes using reflection
        var empDaoField = LeaveRequestService.class.getDeclaredField("employeeDAO");
        empDaoField.setAccessible(true);
        empDaoField.set(leaveRequestService, fakeEmployeeDAO);

        var leaveDaoField = LeaveRequestService.class.getDeclaredField("leaveRequestDAO");
        leaveDaoField.setAccessible(true);
        leaveDaoField.set(leaveRequestService, fakeLeaveRequestDAO);
    }

    @Test
    public void testSubmitLeaveRequest_Success() {
        fakeEmployeeDAO.exists = true;

        boolean result = leaveRequestService.submitLeaveRequest(1, "Vacation",
                LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5));

        assertTrue(result);
        assertEquals(1, fakeLeaveRequestDAO.inserted.size());
    }

    @Test
    public void testSubmitLeaveRequest_FailsForNonExistentEmployee() {
        fakeEmployeeDAO.exists = false;

        boolean result = leaveRequestService.submitLeaveRequest(99, "Sick",
                LocalDate.now(), LocalDate.now().plusDays(1));

        assertFalse(result);
        assertTrue(fakeLeaveRequestDAO.inserted.isEmpty());
    }

    // Fake DAO implementations
    private static class FakeEmployeeDAO extends EmployeeDAO {
        boolean exists = true;

        @Override
        public boolean employeeExists(int employeeId) {
            return exists;
        }
    }

    private static class FakeLeaveRequestDAO extends LeaveRequestDAO {
        List<LeaveRequest> inserted = new ArrayList<>();

        @Override
        public boolean hasOverlappingLeave(int employeeId, LocalDate start, LocalDate end, Integer excludeId) {
            return false;
        }

        @Override
        public int insertLeaveRequest(LeaveRequest leaveRequest) {
            inserted.add(leaveRequest);
            return 1001; // Pretend insert succeeded
        }
    }
}
