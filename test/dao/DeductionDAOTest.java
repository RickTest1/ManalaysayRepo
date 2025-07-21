package dao;

import model.Deduction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import util.DBConnection;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DeductionDAOTest {

    private DeductionDAO dao;
    private Connection mockConn;
    private PreparedStatement mockStmt;
    private ResultSet mockRs;

    @Before
    public void setUp() throws Exception {
        dao = new DeductionDAO();
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockStmt.executeUpdate()).thenReturn(1); // simulate success
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDeduction_nullInput_throwsException() throws Exception {
        dao.addDeduction(null);
    }

    @Test
    public void testAddDeduction_validInput() throws Exception {
        Deduction dummy = new DummyDeduction(1, "SSS", 150.00, "Monthly contribution");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(mockConn);

            dao.addDeduction(dummy);
            verify(mockStmt, times(1)).executeUpdate();
        }
    }

    @Test
    public void testGetDeductionsByEmployeeId_tableDoesNotExist_returnsEmptyList() throws Exception {
        // Simulate table does not exist
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            Connection spyConn = spy(mockConn);
            PreparedStatement infoStmt = mock(PreparedStatement.class);
            ResultSet emptyRs = mock(ResultSet.class);
            when(emptyRs.next()).thenReturn(false);
            when(infoStmt.executeQuery()).thenReturn(emptyRs);
            when(spyConn.prepareStatement(contains("information_schema.tables"))).thenReturn(infoStmt);

            db.when(DBConnection::getConnection).thenReturn(spyConn);

            List<Deduction> result = dao.getDeductionsByEmployeeId(1);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testUpdateDeduction_validUpdate() throws Exception {
        Deduction dummy = new DummyDeduction(1, "Pag-ibig", 200.0, "Loan deduction");
        dummy.setDeductionId(99);

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(mockConn);
            boolean updated = dao.updateDeduction(dummy);
            assertTrue(updated);
        }
    }

    @Test
    public void testDeleteDeduction_success() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(mockConn);
            boolean deleted = dao.deleteDeduction(55);
            assertTrue(deleted);
        }
    }

    @Test
    public void testGetTotalDeductionsByType_returnsAmount() throws Exception {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getDouble("total")).thenReturn(320.50);

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(mockConn);
            double total = dao.getTotalDeductionsByType(1, "PhilHealth");
            assertEquals(320.50, total, 0.01);
        }
    }

    @Test
    public void testGetAllDeductionTypes_returnsList() throws Exception {
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getString("type")).thenReturn("SSS", "Pag-ibig");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(mockConn);
            List<String> types = dao.getAllDeductionTypes();
            assertEquals(2, types.size());
            assertTrue(types.contains("SSS"));
            assertTrue(types.contains("Pag-ibig"));
        }
    }

    // Dummy concrete class for abstract Deduction
    private static class DummyDeduction extends Deduction {
        public DummyDeduction(int employeeId, String type, double amount, String description) {
            super(employeeId, type, amount, description);
        }

        @Override
        public void calculateDeduction() {
            // Stubbed: logic not relevant for unit tests
        }
    }
}
