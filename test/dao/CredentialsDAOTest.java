package dao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CredentialsDAOTest {

    private CredentialsDAO dao;
    private Connection mockConn;
    private PreparedStatement mockStmt;
    private ResultSet mockRs;

    @Before
    public void setUp() throws Exception {
        dao = new CredentialsDAO();
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        when(mockStmt.executeUpdate()).thenReturn(1); // default success
    }

    @Test(expected = IllegalArgumentException.class)
    public void authenticateUser_invalidId() {
        dao.authenticateUser(0, "password");
    }

    @Test(expected = IllegalArgumentException.class)
    public void authenticateUser_emptyPassword() {
        dao.authenticateUser(1, "");
    }

    @Test
    public void authenticateUser_valid() throws Exception {
        when(mockRs.next()).thenReturn(true);
        try (MockedStatic<DBConnection> mocked = Mockito.mockStatic(DBConnection.class)) {
            mocked.when(DBConnection::getConnection).thenReturn(mockConn);

            boolean result = dao.authenticateUser(1, "password");
            assertTrue(result);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void updatePassword_invalidId() {
        dao.updatePassword(0, "newPass");
    }

    @Test
    public void updatePassword_success() throws Exception {
        try (MockedStatic<DBConnection> mocked = Mockito.mockStatic(DBConnection.class)) {
            mocked.when(DBConnection::getConnection).thenReturn(mockConn);

            boolean result = dao.updatePassword(1, "newPass");
            assertTrue(result);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCredentials_invalidPassword() {
        dao.createCredentials(1, "");
    }

    @Test
    public void createCredentials_success() throws Exception {
        try (MockedStatic<DBConnection> mocked = Mockito.mockStatic(DBConnection.class)) {
            mocked.when(DBConnection::getConnection).thenReturn(mockConn);

            boolean result = dao.createCredentials(1, "password");
            assertTrue(result);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void credentialsExist_invalidId() {
        dao.credentialsExist(0);
    }

    @Test
    public void credentialsExist_true() throws Exception {
        when(mockRs.next()).thenReturn(true);
        try (MockedStatic<DBConnection> mocked = Mockito.mockStatic(DBConnection.class)) {
            mocked.when(DBConnection::getConnection).thenReturn(mockConn);

            boolean exists = dao.credentialsExist(1);
            assertTrue(exists);
        }
    }
}
