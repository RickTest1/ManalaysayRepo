package model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GovernmentContributionTest {

    private GovernmentContribution contribution;

    @Before
    public void setUp() {
        contribution = new GovernmentContribution();
    }

    @Test
    public void testContributionRateGetSet() {
        contribution.setContributionRate(0.05);
        assertEquals(0.05, contribution.getContributionRate(), 0.001);
    }

    @Test
    public void testBaseSalaryGetSet() {
        contribution.setBaseSalary(20000);
        assertEquals(20000, contribution.getBaseSalary(), 0.001);
    }

    @Test
    public void testCalculateAmount() {
        contribution.setBaseSalary(20000);
        contribution.setContributionRate(0.05);
        contribution.calculate();
        // Amount should be baseSalary * rate
        assertEquals(1000, contribution.getAmount(), 0.001);
    }

    @Test
    public void testCategoryAlwaysGovernmentContribution() {
        assertEquals("Government Contribution", contribution.getCategory());
    }

    @Test
    public void testIsPositiveAmountAlwaysFalse() {
        assertFalse(contribution.isPositiveAmount());
    }

    @Test
    public void testCreateSSS() {
        GovernmentContribution sss = GovernmentContribution.createSSS(1, 6000);
        assertEquals("SSS", sss.getType());
        assertTrue(sss.getBaseSalary() > 0);
        assertTrue(sss.getAmount() > 0);
        assertTrue(sss.getDescription().contains("Social Security System"));
    }

    @Test
    public void testCreatePhilHealth() {
        GovernmentContribution philhealth = GovernmentContribution.createPhilHealth(1, 15000);
        assertEquals("PhilHealth", philhealth.getType());
        assertEquals(15000, philhealth.getBaseSalary(), 0.001);
        assertTrue(philhealth.getAmount() > 0);
        assertTrue(philhealth.getDescription().contains("Philippine Health Insurance"));
    }

    @Test
    public void testCreatePagIBIG() {
        GovernmentContribution pagibig = GovernmentContribution.createPagIBIG(1, 4000);
        assertEquals("Pag-IBIG", pagibig.getType());
        assertTrue(pagibig.getBaseSalary() > 0);
        assertTrue(pagibig.getAmount() > 0);
        assertTrue(pagibig.getDescription().contains("Home Development Mutual Fund"));
    }

    @Test
    public void testSSSStaticHelpers() {
        double employerShare = GovernmentContribution.getSSSEmployerContribution(6000);
        double salaryCredit = GovernmentContribution.getSSSSalaryCredit(6000);
        assertTrue(employerShare > 0);
        assertTrue(salaryCredit > 0);
    }

    @Test
    public void testPhilHealthStaticHelpers() {
        double employerShare = GovernmentContribution.getPhilHealthEmployerContribution(15000);
        double monthlyPremium = GovernmentContribution.getPhilHealthMonthlyPremium(15000);
        assertTrue(employerShare > 0);
        assertTrue(monthlyPremium > 0);
    }

    @Test
    public void testPagIBIGStaticHelpers() {
        double employerShare = GovernmentContribution.getPagIBIGEmployerContribution(4000);
        double employeeRate = GovernmentContribution.getPagIBIGEmployeeRate(4000);
        double employerRate = GovernmentContribution.getPagIBIGEmployerRate(4000);
        assertTrue(employerShare > 0);
        assertTrue(employeeRate > 0);
        assertTrue(employerRate > 0);
    }
}
