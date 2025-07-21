package model;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

/**
 * JUnit tests for abstract Person class
 */
public class PersonTest {

    private TestPerson person;

    @Before
    public void setUp() {
        person = new TestPerson("John", "Doe");
    }

    @Test
    public void testPersonCreation() {
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
    }

    @Test
    public void testGetFullName() {
        assertEquals("John Doe", person.getFullName());
    }

    @Test
    public void testGetFormattedName() {
        assertEquals("Doe, John", person.getFormattedName());
    }

    @Test
    public void testSetBirthday() {
        LocalDate birthday = LocalDate.of(1990, 1, 1);
        person.setBirthday(birthday);
        assertEquals(birthday, person.getBirthday());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBirthdayInFuture() {
        person.setBirthday(LocalDate.now().plusDays(1));
    }

    @Test
    public void testGetAge() {
        person.setBirthday(LocalDate.of(2000, 1, 1));
        int expectedAge = LocalDate.now().getYear() - 2000;
        assertEquals(expectedAge, person.getAge());
    }

    @Test
    public void testHasContactInfo() {
        assertFalse(person.hasContactInfo());
        
        person.setPhoneNumber("123-456-7890");
        assertTrue(person.hasContactInfo());
        
        person.setPhoneNumber(null);
        person.setAddress("123 Main St");
        assertTrue(person.hasContactInfo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFirstNameEmpty() {
        person.setFirstName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLastNameEmpty() {
        person.setLastName("");
    }

    @Test
    public void testAbstractMethods() {
        // Test that abstract methods are implemented
        assertEquals("Test Role", person.getRole());
        assertTrue(person.hasRequiredDocuments());
    }

    // Concrete implementation for testing abstract Person class
    private static class TestPerson extends Person {
        public TestPerson(String firstName, String lastName) {
            super(firstName, lastName);
        }

        @Override
        public String getRole() {
            return "Test Role";
        }

        @Override
        public boolean hasRequiredDocuments() {
            return true;
        }

        @Override
        public boolean isValid() {
            return firstName != null && lastName != null;
        }

        @Override
        public String getDisplayName() {
            return getFullName();
        }
    }
}