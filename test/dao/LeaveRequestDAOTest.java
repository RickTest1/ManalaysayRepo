package dao;

import model.LeaveRequest;
import org.junit.*;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class LeaveRequestDAOTest {

    private static Connection connection;
    private LeaveRequestDAO dao;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        // Create in-memory H2 database
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        DBConnection.setTestConnection(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE leave_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "employee_id INT NOT NULL, " +
                    "leave_type VARCHAR(50), " +
                    "start_date DATE, " +
                    "end_date DATE, " +
                    "status VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");
        }
    }

    @AfterClass
    public static void tearDownDatabase() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE leave_requests");
        }
        connection.close();
    }

    @Before
    public void setUp() {
        dao = new LeaveRequestDAO();
    }

    @After
    public void cleanUp() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM leave_requests");
        }
    }

    @Test
    public void testInsertAndGetLeaveRequests() {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(101);
        lr.setLeaveType("Vacation");
        lr.setStartDate(Date.valueOf(LocalDate.of(2025, 7, 21)));
        lr.setEndDate(Date.valueOf(LocalDate.of(2025, 7, 25)));
        lr.setStatus("Pending");

        int generatedId = dao.insertLeaveRequest(lr);
        assertTrue(generatedId > 0);

        List<LeaveRequest> leaves = dao.getLeaveRequestsByEmployeeId(101);
        assertEquals(1, leaves.size());
        assertEquals("Vacation", leaves.get(0).getLeaveType());
    }

    @Test
    public void testUpdateLeaveStatus() {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployeeId(102);
        lr.setLeaveType("Sick");
        lr.setStartDate(Date.valueOf(LocalDate.of(2025, 7, 22)));
        lr.setEndDate(Date.valueOf(LocalDate.of(2025, 7, 23)));
        lr.setStatus("Pending");
        int leaveId = dao.insertLeaveRequest(lr);

        boolean updated = dao.updateLeaveStatus(leaveId, "Approved");
        assertTrue(updated);

        List<LeaveRequest> approvedLeaves = dao.getApprovedLeavesByEmployeeId(102);
        assertEquals(1, approvedLeaves.size());
        assertEquals("Approved", approvedLeaves.get(0).getStatus());
    }
}
