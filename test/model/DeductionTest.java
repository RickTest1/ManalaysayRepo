package model;

import org.junit.Test;
import java.sql.Time;
import java.time.LocalTime;

import static org.junit.Assert.*;

public class DeductionTest {

    @Test
    public void testGetAndSetDeductionId() {
        Deduction d = new DeductionImpl();
        d.setDeductionId(10);
        assertEquals(10, d.getDeductionId());
    }

    @Test
    public void testGetCategory() {
        Deduction d = new DeductionImpl();
        assertEquals("Deduction", d.getCategory());
    }

    @Test
    public void testIsPositiveAmount() {
        Deduction d = new DeductionImpl();
        assertFalse(d.isPositiveAmount());
    }

    @Test
    public void testCreateLateDeduction_NoDeductionWithinGracePeriod() {
        Time arrivalTime = Time.valueOf(LocalTime.of(8, 10)); // within 15 mins
        Deduction d = Deduction.createLateDeduction(1, arrivalTime, 100);
        assertEquals(0.0, d.getAmount(), 0.001);
        assertEquals("Deduction", d.getCategory());
    }

    @Test
    public void testCreateLateDeduction_WithDeduction() {
        Time arrivalTime = Time.valueOf(LocalTime.of(8, 30)); // 30 mins late
        Deduction d = Deduction.createLateDeduction(1, arrivalTime, 120);
        assertTrue(d.getAmount() > 0);
        assertEquals("Deduction", d.getCategory());
    }

    @Test
    public void testCreateUndertimeDeduction_NoDeductionOnOrAfter5PM() {
        Time departureTime = Time.valueOf(LocalTime.of(17, 0)); // exactly 5 PM
        Deduction d = Deduction.createUndertimeDeduction(1, departureTime, 100);
        assertEquals(0.0, d.getAmount(), 0.001);
    }

    @Test
    public void testCreateUndertimeDeduction_WithDeduction() {
        Time departureTime = Time.valueOf(LocalTime.of(16, 0)); // 1 hour early
        Deduction d = Deduction.createUndertimeDeduction(1, departureTime, 200);
        assertTrue(d.getAmount() > 0);
    }

    // Concrete implementation for testing
    private static class DeductionImpl extends Deduction {
        public DeductionImpl() { super(1, "Test Deduction", 0.0); }

        @Override
        public void calculateDeduction() {
            // No additional calculation for testing
        }
    }
}
