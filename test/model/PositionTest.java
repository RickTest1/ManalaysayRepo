package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Position class.
 */
public class PositionTest {

    private Position position;

    @Before
    public void setUp() {
        position = new Position();
    }

    @Test
    public void testDefaultValues() {
        assertEquals(0, position.getPositionId());
        assertNull(position.getPositionName());
        assertEquals(0.0, position.getMonthlySalary(), 0.001);
        assertEquals(0.0, position.getRiceSubsidy(), 0.001);
        assertEquals(0.0, position.getPhoneAllowance(), 0.001);
        assertEquals(0.0, position.getClothingAllowance(), 0.001);
        assertEquals(0.0, position.getGrossSemiMonthlyRate(), 0.001);
        assertEquals(0.0, position.getHourlyRate(), 0.001);
    }

    @Test
    public void testSetAndGetPositionId() {
        position.setPositionId(101);
        assertEquals(101, position.getPositionId());
    }

    @Test
    public void testSetAndGetPositionName() {
        position.setPositionName("Software Engineer");
        assertEquals("Software Engineer", position.getPositionName());
    }

    @Test
    public void testSetPositionNameTrimsWhitespace() {
        position.setPositionName("  Manager  ");
        assertEquals("Manager", position.getPositionName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPositionNameTooLongThrowsException() {
        String longName = new String(new char[101]).replace("\0", "A");
        position.setPositionName(longName);
    }

    @Test
    public void testSetAndGetMonthlySalary() {
        position.setMonthlySalary(50000.0);
        assertEquals(50000.0, position.getMonthlySalary(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativeMonthlySalaryThrowsException() {
        position.setMonthlySalary(-1000.0);
    }

    @Test
    public void testAllowancesSetAndGet() {
        position.setRiceSubsidy(1500.0);
        position.setPhoneAllowance(1000.0);
        position.setClothingAllowance(800.0);

        assertEquals(1500.0, position.getRiceSubsidy(), 0.001);
        assertEquals(1000.0, position.getPhoneAllowance(), 0.001);
        assertEquals(800.0, position.getClothingAllowance(), 0.001);
        assertEquals(3300.0, position.getTotalAllowances(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeRiceSubsidyThrowsException() {
        position.setRiceSubsidy(-500.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePhoneAllowanceThrowsException() {
        position.setPhoneAllowance(-300.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeClothingAllowanceThrowsException() {
        position.setClothingAllowance(-200.0);
    }

    @Test
    public void testSetAndGetGrossSemiMonthlyRate() {
        position.setGrossSemiMonthlyRate(25000.0);
        assertEquals(25000.0, position.getGrossSemiMonthlyRate(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeGrossSemiMonthlyRateThrowsException() {
        position.setGrossSemiMonthlyRate(-25000.0);
    }

    @Test
    public void testSetAndGetHourlyRate() {
        position.setHourlyRate(300.0);
        assertEquals(300.0, position.getHourlyRate(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeHourlyRateThrowsException() {
        position.setHourlyRate(-100.0);
    }

    @Test
    public void testDailyAndHourlyRateCalculation() {
        position.setMonthlySalary(44000.0);
        assertEquals(2000.0, position.getDailyRate(), 0.001);
        assertEquals(250.0, position.getCalculatedHourlyRate(), 0.001);
    }

    @Test
    public void testCalculateRatesUpdatesFields() {
        position.setMonthlySalary(44000.0);
        position.calculateRates();
        assertEquals(22000.0, position.getGrossSemiMonthlyRate(), 0.001);
        assertEquals(250.0, position.getHourlyRate(), 0.001);
    }

    @Test
    public void testIsValid() {
        position.setPositionName("Developer");
        position.setMonthlySalary(30000.0);
        assertTrue(position.isValid());
    }

    @Test
    public void testIsNotValidWithEmptyName() {
        position.setPositionName("   ");
        position.setMonthlySalary(30000.0);
        assertFalse(position.isValid());
    }

    @Test
    public void testDisplayName() {
        position.setPositionName("Tester");
        position.setMonthlySalary(20000.0);
        assertEquals("Tester (₱20000.00)", position.getDisplayName());
    }

    @Test
    public void testEqualsAndHashCode() {
        Position p1 = new Position(1, "Manager", 50000);
        Position p2 = new Position(1, "Manager", 50000);
        Position p3 = new Position(2, "Developer", 60000);

        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("NotAPosition"));
    }

    @Test
    public void testToStringContainsFields() {
        position.setPositionId(10);
        position.setPositionName("Analyst");
        position.setMonthlySalary(35000.0);
        position.setRiceSubsidy(1000.0);
        position.setPhoneAllowance(500.0);
        position.setClothingAllowance(800.0);
        position.calculateRates();

        String output = position.toString();
        assertTrue(output.contains("Analyst"));
        assertTrue(output.contains("35000.0"));
        assertTrue(output.contains("1000.0"));
    }
}
