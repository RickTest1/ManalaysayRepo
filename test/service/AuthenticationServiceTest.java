package service;

import model.Employee;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for AuthenticationService
 */
public class AuthenticationServiceTest {

    /**
     * Test authentication with invalid credentials (empty password and ID)
     */
    @Test
    public void testAuthenticateWithInvalidCredentials() {
        AuthenticationService instance = new AuthenticationService();
        AuthenticationService.AuthenticationResult result = instance.authenticate(0, "");
        assertFalse("Authentication should fail with invalid credentials", result.isSuccessful());
        assertEquals("Invalid credentials provided", result.getMessage());
    }

    /**
     * Test hasCredentials with an invalid employee ID
     * (assuming no credentials exist for ID 0)
     */
    @Test
    public void testHasCredentialsWithInvalidId() {
        AuthenticationService instance = new AuthenticationService();
        boolean hasCreds = instance.hasCredentials(0);
        assertFalse("No credentials should exist for employee ID 0", hasCreds);
    }

    /**
     * Test changePassword with incorrect current password
     * (expected to fail since ID and password are invalid)
     */
    @Test
    public void testChangePasswordWithWrongCurrentPassword() {
        AuthenticationService instance = new AuthenticationService();
        boolean result = instance.changePassword(0, "wrongpass", "newpass");
        assertFalse("Password change should fail with incorrect current password", result);
    }

    /**
     * Basic smoke test for constructor and non-null dependencies
     */
    @Test
    public void testServiceInitialization() {
        AuthenticationService instance = new AuthenticationService();
        assertNotNull("AuthenticationService should be created successfully", instance);
    }
}
