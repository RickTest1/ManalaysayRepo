package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for Allowance class and its subclasses
 * Demonstrates proper unit testing with JUnit assertions
 */
public class AllowanceTest {

    private Allowance allowance;

    @Before
    public void setUp() {
        allowance = new Allowance(1, "Test Allowance", 1000.0);
    }

    @Test
    public void testAllowanceCreation() {
        assertEquals(1, allowance.getEmployeeId());
        assertEquals("Test Allowance", allowance.getType());
        assertEquals(1000.0, allowance.getAmount(), 0.01);
    }

    @Test
    public void testGetCategory() {
        assertEquals("Allowance", allowance.getCategory());
    }

    @Test
    public void testIsPositiveAmount() {
        assertTrue("Allowances should be positive amounts", allowance.isPositiveAmount());
    }

    @Test
    public void testCalculateMethod() {
        // Test that calculate method doesn't throw exception
        allowance.calculate();
        // Amount should remain the same for base allowance
        assertEquals(1000.0, allowance.getAmount(), 0.01);
    }

    @Test
    public void testCreateRiceSubsidy() {
        Allowance riceSubsidy = Allowance.createRiceSubsidy(1, 1500.0);
        
        assertNotNull("Rice subsidy should not be null", riceSubsidy);
        assertEquals("Rice Subsidy", riceSubsidy.getType());
        assertEquals(1500.0, riceSubsidy.getAmount(), 0.01);
        assertTrue("Should be RiceSubsidyAllowance instance", 
                riceSubsidy instanceof Allowance.RiceSubsidyAllowance);
    }

    @Test
    public void testCreateRiceSubsidyWithMaxLimit() {
        Allowance riceSubsidy = Allowance.createRiceSubsidy(1, 3000.0);
        
        // Should be capped at 2000.0
        assertEquals(2000.0, riceSubsidy.getAmount(), 0.01);
    }

    @Test
    public void testCreatePhoneAllowance() {
        Allowance phoneAllowance = Allowance.createPhoneAllowance(1, 2500.0);
        
        assertNotNull("Phone allowance should not be null", phoneAllowance);
        assertEquals("Phone Allowance", phoneAllowance.getType());
        assertEquals(2500.0, phoneAllowance.getAmount(), 0.01);
        assertTrue("Should be PhoneAllowance instance", 
                phoneAllowance instanceof Allowance.PhoneAllowance);
    }

    @Test
    public void testCreatePhoneAllowanceWithMaxLimit() {
        Allowance phoneAllowance = Allowance.createPhoneAllowance(1, 5000.0);
        
        // Should be capped at 3000.0
        assertEquals(3000.0, phoneAllowance.getAmount(), 0.01);
    }

    @Test
    public void testCreateClothingAllowance() {
        Allowance clothingAllowance = Allowance.createClothingAllowance(1, 1200.0);
        
        assertNotNull("Clothing allowance should not be null", clothingAllowance);
        assertEquals("Clothing Allowance", clothingAllowance.getType());
        assertEquals(1200.0, clothingAllowance.getAmount(), 0.01);
        assertTrue("Should be ClothingAllowance instance", 
                clothingAllowance instanceof Allowance.ClothingAllowance);
    }

    @Test
    public void testCreateClothingAllowanceWithMaxLimit() {
        Allowance clothingAllowance = Allowance.createClothingAllowance(1, 2000.0);
        
        // Should be capped at 1500.0
        assertEquals(1500.0, clothingAllowance.getAmount(), 0.01);
    }

    @Test
    public void testRiceSubsidyCalculateMethod() {
        Allowance.RiceSubsidyAllowance riceSubsidy = 
                new Allowance.RiceSubsidyAllowance(1, 2500.0);
        
        riceSubsidy.calculate();
        
        // Should be capped at maximum
        assertEquals(2000.0, riceSubsidy.getAmount(), 0.01);
    }

    @Test
    public void testPhoneAllowanceCalculateMethod() {
        Allowance.PhoneAllowance phoneAllowance = 
                new Allowance.PhoneAllowance(1, 4000.0);
        
        phoneAllowance.calculate();
        
        // Should be capped at maximum
        assertEquals(3000.0, phoneAllowance.getAmount(), 0.01);
    }

    @Test
    public void testClothingAllowanceCalculateMethod() {
        Allowance.ClothingAllowance clothingAllowance = 
                new Allowance.ClothingAllowance(1, 2000.0);
        
        clothingAllowance.calculate();
        
        // Should be capped at maximum
        assertEquals(1500.0, clothingAllowance.getAmount(), 0.01);
    }

    @Test
    public void testPolymorphism() {
        // Test that different allowance types can be treated as base Allowance
        Allowance rice = Allowance.createRiceSubsidy(1, 1500.0);
        Allowance phone = Allowance.createPhoneAllowance(1, 1000.0);
        Allowance clothing = Allowance.createClothingAllowance(1, 800.0);
        
        Allowance[] allowances = {rice, phone, clothing};
        
        for (Allowance a : allowances) {
            assertTrue("All should be positive amounts", a.isPositiveAmount());
            assertEquals("All should be allowance category", "Allowance", a.getCategory());
            assertNotNull("All should have display names", a.getDisplayName());
        }
    }

    @Test
    public void testInheritanceFromPayrollComponent() {
        assertTrue("Allowance should extend PayrollComponent", 
                allowance instanceof PayrollComponent);
        assertTrue("Allowance should implement Calculable", 
                allowance instanceof Calculable);
    }
}