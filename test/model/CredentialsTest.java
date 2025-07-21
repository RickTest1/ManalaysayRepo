package model;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class CredentialsTest {

    @Test
    public void testSetAndGetEmployeeId() {
        Credentials c = new Credentials();
        c.setEmployeeId(1);
        assertEquals(1, c.getEmployeeId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmployeeIdInvalid() {
        Credentials c = new Credentials();
        c.setEmployeeId(0);
    }

    @Test
    public void testSetAndGetPassword() {
        Credentials c = new Credentials();
        c.setPassword("password123");
        assertTrue(c.isPasswordValid("password123"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPasswordTooShort() {
        Credentials c = new Credentials();
        c.setPassword("short");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPasswordEmpty() {
        Credentials c = new Credentials();
        c.setPassword("");
    }

    @Test
    public void testSetAndGetDates() {
        Credentials c = new Credentials();
        LocalDateTime now = LocalDateTime.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now.minusDays(1));
        assertEquals(now, c.getCreatedAt());
        assertEquals(now.minusDays(1), c.getUpdatedAt());
    }

    @Test
    public void testSetAndGetEmail() {
        Credentials c = new Credentials();
        c.setEmail("test@example.com");
        assertEquals("test@example.com", c.getEmail());
    }

    @Test
    public void testActiveStatus() {
        Credentials c = new Credentials();
        assertTrue(c.isActive());
        c.setActive(false);
        assertFalse(c.isActive());
    }

    @Test
    public void testFailedLoginAttemptsAndLock() {
        Credentials c = new Credentials();
        assertEquals(0, c.getFailedLoginAttempts());
        c.incrementFailedLoginAttempts();
        c.incrementFailedLoginAttempts();
        c.incrementFailedLoginAttempts();
        assertEquals(3, c.getFailedLoginAttempts());
        assertFalse(c.isActive()); // should lock after 3 failed attempts
    }

    @Test
    public void testResetFailedLoginAttempts() {
        Credentials c = new Credentials();
        c.incrementFailedLoginAttempts();
        c.incrementFailedLoginAttempts();
        c.incrementFailedLoginAttempts();
        assertFalse(c.isActive());
        c.resetFailedLoginAttempts();
        assertEquals(0, c.getFailedLoginAttempts());
        assertTrue(c.isActive());
    }

    @Test
    public void testSetAndGetLastLoginAt() {
        Credentials c = new Credentials();
        LocalDateTime lastLogin = LocalDateTime.now();
        c.setLastLoginAt(lastLogin);
        assertEquals(lastLogin, c.getLastLoginAt());
    }

    @Test
    public void testNeedsPasswordReset() {
        Credentials c = new Credentials();
        c.setUpdatedAt(LocalDateTime.now().minusDays(100));
        assertTrue(c.needsPasswordReset());
        c.setUpdatedAt(LocalDateTime.now());
        assertFalse(c.needsPasswordReset());
    }

    @Test
    public void testEqualsAndHashCode() {
        Credentials c1 = new Credentials();
        c1.setEmployeeId(1);
        Credentials c2 = new Credentials();
        c2.setEmployeeId(1);
        Credentials c3 = new Credentials();
        c3.setEmployeeId(2);

        assertTrue(c1.equals(c2));
        assertFalse(c1.equals(c3));
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testToStringContainsImportantInfo() {
        Credentials c = new Credentials();
        c.setEmployeeId(1);
        String toString = c.toString();
        assertTrue(toString.contains("employeeId=1"));
    }
}
