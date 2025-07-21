package model;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmployeeTest {

    private Employee employee;

    @Before
    public void setUp() {
        employee = new Employee();
    }

    @Test
    public void testDefaultFullNameIsUnknown() {
        assertEquals("Unknown", employee.getFullName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFirstNameEmptyThrowsException() {
        employee.setFirstName("");
    }

    @Test
    public void testSetAndGetFirstName() {
        employee.setFirstName("John");
        assertEquals("John", employee.getFirstName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLastNameEmptyThrowsException() {
        employee.setLastName("");
    }

    @Test
    public void testSetAndGetLastName() {
        employee.setLastName("Doe");
        assertEquals("Doe", employee.getLastName());
    }

    @Test
    public void testGetFullNameWithBothNames() {
        employee.setFirstName("John");
        employee.setLastName("Doe");
        assertEquals("John Doe", employee.getFullName());
    }

    @Test
    public void testGetFormattedName() {
        employee.setFirstName("John");
        employee.setLastName("Doe");
        assertEquals("Doe, John", employee.getFormattedName());
    }

    @Test
    public void testSetAndGetBirthday() {
        LocalDate birthday = LocalDate.of(2000, 1, 1);
        employee.setBirthday(birthday);
        assertEquals(birthday, employee.getBirthday());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBirthdayInFutureThrowsException() {
        employee.setBirthday(LocalDate.now().plusDays(1));
    }

    @Test
    public void testGetAgeWhenBirthdayIsNull() {
        assertEquals(0, employee.getAge());
    }

    @Test
    public void testGetAgeWhenBirthdayIsSet() {
        employee.setBirthday(LocalDate.of(2000, 1, 1));
        int expected = LocalDate.now().getYear() - 2000;
        assertEquals(expected, employee.getAge());
    }

    @Test
    public void testSalaryAndRates() {
        employee.setBasicSalary(22000);
        assertEquals(22000, employee.getBasicSalary(), 0.001);
        assertEquals(22000 / 22.0, employee.getDailyRate(), 0.001);
        assertEquals((22000 / 22.0) / 8.0, employee.getCalculatedHourlyRate(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativeSalaryThrowsException() {
        employee.setBasicSalary(-1000);
    }

    @Test
    public void testAllowancesAndTotalAllowances() {
        employee.setRiceSubsidy(1000);
        employee.setPhoneAllowance(500);
        employee.setClothingAllowance(300);
        assertEquals(1800, employee.getTotalAllowances(), 0.001);
    }

    @Test
    public void testEmploymentStatusChecks() {
        employee.setStatus("Regular");
        assertTrue(employee.isRegular());
        assertFalse(employee.isProbationary());

        employee.setStatus("Probationary");
        assertTrue(employee.isProbationary());
        assertFalse(employee.isRegular());
    }

    @Test
    public void testHasCompleteGovernmentIds() {
        employee.setSssNumber("123");
        employee.setPhilhealthNumber("456");
        employee.setTinNumber("789");
        employee.setPagibigNumber("101112");
        assertTrue(employee.hasCompleteGovernmentIds());
    }

    @Test
    public void testHasContactInfo() {
        assertFalse(employee.hasContactInfo());
        employee.setPhoneNumber("09091234567");
        assertTrue(employee.hasContactInfo());
    }

    @Test
    public void testIsValidEmployee() {
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setBasicSalary(10000);
        assertTrue(employee.isValid());
    }

    @Test
    public void testDisplayNameAndToString() {
        employee.setId(1);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        String displayName = employee.getDisplayName();
        assertTrue(displayName.contains("1 - John Doe"));

        String toString = employee.toString();
        assertTrue(toString.contains("Employee{"));
        assertTrue(toString.contains("John Doe"));
    }
}
