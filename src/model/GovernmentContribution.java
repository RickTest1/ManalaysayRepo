package model;

public class GovernmentContribution extends PayrollComponent {
    protected double contributionRate;
    protected double baseSalary;

    public GovernmentContribution() {}

    public GovernmentContribution(int employeeId, String type, double baseSalary, double rate) {
        super(employeeId, type, 0); // Amount calculated later
        this.baseSalary = baseSalary;
        this.contributionRate = rate;
        calculate();
    }

    public double getContributionRate() { return contributionRate; }
    public void setContributionRate(double rate) { this.contributionRate = rate; touch(); }

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double salary) { this.baseSalary = salary; touch(); }

    @Override
    public void calculate() {
        setAmount(baseSalary * contributionRate);
    }

    @Override
    public String getCategory() {
        return "Government Contribution";
    }

    @Override
    public boolean isPositiveAmount() {
        return false;
    }

    // SSS Contribution Table Data
    private static final double[][] SSS_TABLE = {
            // {minSalary, maxSalary, salaryCredit, employeeContrib, employerContrib}
            {0.00, 5249.99, 5000.00, 250.00, 500.00},
            {5250.00, 5749.99, 5500.00, 275.00, 550.00},
            {5750.00, 6249.99, 6000.00, 300.00, 600.00},
            {6250.00, 6749.99, 6500.00, 325.00, 650.00},
            {6750.00, 7249.99, 7000.00, 350.00, 700.00},
            {7250.00, 7749.99, 7500.00, 375.00, 750.00},
            {7750.00, 8249.99, 8000.00, 400.00, 800.00},
            {8250.00, 8749.99, 8500.00, 425.00, 850.00},
            {8750.00, 9249.99, 9000.00, 450.00, 900.00},
            {9250.00, 9749.99, 9500.00, 475.00, 950.00},
            {9750.00, 10249.99, 10000.00, 500.00, 1000.00},
            {10250.00, 10749.99, 10500.00, 525.00, 1050.00},
            {10750.00, 11249.99, 11000.00, 550.00, 1100.00},
            {11250.00, 11749.99, 11500.00, 575.00, 1150.00},
            {11750.00, 12249.99, 12000.00, 600.00, 1200.00},
            {12250.00, 12749.99, 12500.00, 625.00, 1250.00},
            {12750.00, 13249.99, 13000.00, 650.00, 1300.00},
            {13250.00, 13749.99, 13500.00, 675.00, 1350.00},
            {13750.00, 14249.99, 14000.00, 700.00, 1400.00},
            {14250.00, 14749.99, 14500.00, 725.00, 1450.00},
            {14750.00, 15249.99, 15000.00, 750.00, 1500.00},
            {15250.00, 15749.99, 15500.00, 775.00, 1550.00},
            {15750.00, 16249.99, 16000.00, 800.00, 1600.00},
            {16250.00, 16749.99, 16500.00, 825.00, 1650.00},
            {16750.00, 17249.99, 17000.00, 850.00, 1700.00},
            {17250.00, 17749.99, 17500.00, 875.00, 1750.00},
            {17750.00, 18249.99, 18000.00, 900.00, 1800.00},
            {18250.00, 18749.99, 18500.00, 925.00, 1850.00},
            {18750.00, 19249.99, 19000.00, 950.00, 1900.00},
            {19250.00, 19749.99, 19500.00, 975.00, 1950.00},
            {19750.00, 20249.99, 20000.00, 1000.00, 2000.00},
            {20250.00, 20749.99, 20500.00, 1025.00, 2050.00},
            {20750.00, 21249.99, 21000.00, 1050.00, 2100.00},
            {21250.00, 21749.99, 21500.00, 1075.00, 2150.00},
            {21750.00, 22249.99, 22000.00, 1100.00, 2200.00},
            {22250.00, 22749.99, 22500.00, 1125.00, 2250.00},
            {22750.00, 23249.99, 23000.00, 1150.00, 2300.00},
            {23250.00, 23749.99, 23500.00, 1175.00, 2350.00},
            {23750.00, 24249.99, 24000.00, 1200.00, 2400.00},
            {24250.00, 24749.99, 24500.00, 1225.00, 2450.00},
            {24750.00, 25249.99, 25000.00, 1250.00, 2500.00},
            {25250.00, 25749.99, 25500.00, 1275.00, 2550.00},
            {25750.00, 26249.99, 26000.00, 1300.00, 2600.00},
            {26250.00, 26749.99, 26500.00, 1325.00, 2650.00},
            {26750.00, 27249.99, 27000.00, 1350.00, 2700.00},
            {27250.00, 27749.99, 27500.00, 1375.00, 2750.00},
            {27750.00, 28249.99, 28000.00, 1400.00, 2800.00},
            {28250.00, 28749.99, 28500.00, 1425.00, 2850.00},
            {28750.00, 29249.99, 29000.00, 1450.00, 2900.00},
            {29250.00, 29749.99, 29500.00, 1475.00, 2950.00},
            {29750.00, 30249.99, 30000.00, 1500.00, 3000.00},
            {30250.00, 30749.99, 30500.00, 1525.00, 3050.00},
            {30750.00, 31249.99, 31000.00, 1550.00, 3100.00},
            {31250.00, 31749.99, 31500.00, 1575.00, 3150.00},
            {31750.00, 32249.99, 32000.00, 1600.00, 3200.00},
            {32250.00, 32749.99, 32500.00, 1625.00, 3250.00},
            {32750.00, 33249.99, 33000.00, 1650.00, 3300.00},
            {33250.00, 33749.99, 33500.00, 1675.00, 3350.00},
            {33750.00, 34249.99, 34000.00, 1700.00, 3400.00},
            {34250.00, 34749.99, 34500.00, 1725.00, 3450.00},
            {34750.00, Double.MAX_VALUE, 35000.00, 1750.00, 3500.00} // Over 34,750
    };

    /**
     * Calculate SSS contribution based on salary bracket
     */
    private static SSSContribution calculateSSSFromTable(double salary) {
        for (double[] bracket : SSS_TABLE) {
            if (salary >= bracket[0] && salary <= bracket[1]) {
                return new SSSContribution(bracket[2], bracket[3], bracket[4]);
            }
        }
        // Default to highest bracket if not found
        double[] highestBracket = SSS_TABLE[SSS_TABLE.length - 1];
        return new SSSContribution(highestBracket[2], highestBracket[3], highestBracket[4]);
    }

    // Factory methods for specific contributions
    public static GovernmentContribution createSSS(int employeeId, double salary) {
        SSSContribution sssData = calculateSSSFromTable(salary);
        GovernmentContribution sss = new GovernmentContribution();
        sss.setEmployeeId(employeeId);
        sss.setType("SSS");
        sss.setBaseSalary(sssData.salaryCredit);
        sss.setAmount(sssData.employeeContribution);
        sss.setDescription("Social Security System contribution - Salary Credit: ₱" +
                String.format("%.2f", sssData.salaryCredit));
        return sss;
    }

    // PhilHealth Contribution Table Data
    private static final double[][] PHILHEALTH_TABLE = {
            // {minSalary, maxSalary, monthlyPremium, employeeShare, employerShare}
            {0.00, 10000.00, 500.00, 250.00, 250.00},
            {10000.01, 99999.99, 2500.00, 1250.00, 1250.00},
            {100000.00, Double.MAX_VALUE, 5000.00, 2500.00, 2500.00}
    };

    /**
     * Calculate PhilHealth contribution based on salary bracket
     */
    private static PhilHealthContribution calculatePhilHealthFromTable(double salary) {
        for (double[] bracket : PHILHEALTH_TABLE) {
            if (salary >= bracket[0] && salary <= bracket[1]) {
                return new PhilHealthContribution(bracket[2], bracket[3], bracket[4]);
            }
        }
        // Default to highest bracket if not found
        double[] highestBracket = PHILHEALTH_TABLE[PHILHEALTH_TABLE.length - 1];
        return new PhilHealthContribution(highestBracket[2], highestBracket[3], highestBracket[4]);
    }

    public static GovernmentContribution createPhilHealth(int employeeId, double salary) {
        PhilHealthContribution philHealthData = calculatePhilHealthFromTable(salary);

        GovernmentContribution philHealth = new GovernmentContribution();
        philHealth.setEmployeeId(employeeId);
        philHealth.setType("PhilHealth");
        philHealth.setBaseSalary(salary);
        philHealth.setAmount(philHealthData.employeeContribution);
        philHealth.setDescription("Philippine Health Insurance Corporation contribution - Monthly Premium: ₱" +
                String.format("%.2f", philHealthData.monthlyPremium));
        return philHealth;
    }

    // Pag-IBIG Contribution Table Data
    private static final double[][] PAGIBIG_TABLE = {
            // {minSalary, maxSalary, employeeRate, employerRate}
            {1000.00, 1500.00, 0.01, 0.02}, // 1% employee, 2% employer
            {1500.01, Double.MAX_VALUE, 0.02, 0.02} // 2% employee, 2% employer
    };

    private static final double PAGIBIG_MAX_CONTRIBUTORY_SALARY = 5000.00;

    /**
     * Calculate Pag-IBIG contribution based on salary bracket
     */
    private static PagIBIGContribution calculatePagIBIGFromTable(double salary) {
        // Use minimum of actual salary or max contributory salary
        double contributorySalary = Math.min(salary, PAGIBIG_MAX_CONTRIBUTORY_SALARY);

        // Find the appropriate bracket
        for (double[] bracket : PAGIBIG_TABLE) {
            if (contributorySalary >= bracket[0] && contributorySalary <= bracket[1]) {
                double employeeContribution = contributorySalary * bracket[2];
                double employerContribution = contributorySalary * bracket[3];
                return new PagIBIGContribution(contributorySalary, employeeContribution, employerContribution, bracket[2], bracket[3]);
            }
        }

        // If salary is below 1,000, use the first bracket rates but with actual salary
        if (contributorySalary < 1000.00) {
            double[] firstBracket = PAGIBIG_TABLE[0];
            double employeeContribution = contributorySalary * firstBracket[2];
            double employerContribution = contributorySalary * firstBracket[3];
            return new PagIBIGContribution(contributorySalary, employeeContribution, employerContribution, firstBracket[2], firstBracket[3]);
        }

        // Default to highest bracket if not found
        double[] highestBracket = PAGIBIG_TABLE[PAGIBIG_TABLE.length - 1];
        double employeeContribution = contributorySalary * highestBracket[2];
        double employerContribution = contributorySalary * highestBracket[3];
        return new PagIBIGContribution(contributorySalary, employeeContribution, employerContribution, highestBracket[2], highestBracket[3]);
    }

    public static GovernmentContribution createPagIBIG(int employeeId, double salary) {
        PagIBIGContribution pagibigData = calculatePagIBIGFromTable(salary);

        GovernmentContribution pagibig = new GovernmentContribution();
        pagibig.setEmployeeId(employeeId);
        pagibig.setType("Pag-IBIG");
        pagibig.setBaseSalary(pagibigData.contributorySalary);
        pagibig.setAmount(pagibigData.employeeContribution);
        pagibig.setDescription("Home Development Mutual Fund contribution - Rate: " +
                String.format("%.1f%%", pagibigData.employeeRate * 100) +
                " (Max contributory: ₱" + String.format("%.2f", PAGIBIG_MAX_CONTRIBUTORY_SALARY) + ")");
        return pagibig;
    }

    // Helper class to hold SSS calculation results
    private static class SSSContribution {
        final double salaryCredit;
        final double employeeContribution;
        final double employerContribution;

        SSSContribution(double salaryCredit, double employeeContribution, double employerContribution) {
            this.salaryCredit = salaryCredit;
            this.employeeContribution = employeeContribution;
            this.employerContribution = employerContribution;
        }
    }

    // Helper class to hold PhilHealth calculation results
    private static class PhilHealthContribution {
        final double monthlyPremium;
        final double employeeContribution;
        final double employerContribution;

        PhilHealthContribution(double monthlyPremium, double employeeContribution, double employerContribution) {
            this.monthlyPremium = monthlyPremium;
            this.employeeContribution = employeeContribution;
            this.employerContribution = employerContribution;
        }
    }

    // Helper class to hold Pag-IBIG calculation results
    private static class PagIBIGContribution {
        final double contributorySalary;
        final double employeeContribution;
        final double employerContribution;
        final double employeeRate;
        final double employerRate;

        PagIBIGContribution(double contributorySalary, double employeeContribution, double employerContribution, double employeeRate, double employerRate) {
            this.contributorySalary = contributorySalary;
            this.employeeContribution = employeeContribution;
            this.employerContribution = employerContribution;
            this.employeeRate = employeeRate;
            this.employerRate = employerRate;
        }
    }

    /**
     * Get the employer contribution amount for SSS (for informational purposes)
     */
    public static double getSSSEmployerContribution(double salary) {
        SSSContribution sssData = calculateSSSFromTable(salary);
        return sssData.employerContribution;
    }

    /**
     * Get the salary credit for SSS (for informational purposes)
     */
    public static double getSSSSalaryCredit(double salary) {
        SSSContribution sssData = calculateSSSFromTable(salary);
        return sssData.salaryCredit;
    }

    /**
     * Get the employer contribution amount for PhilHealth (for informational purposes)
     */
    public static double getPhilHealthEmployerContribution(double salary) {
        PhilHealthContribution philHealthData = calculatePhilHealthFromTable(salary);
        return philHealthData.employerContribution;
    }

    /**
     * Get the monthly premium for PhilHealth (for informational purposes)
     */
    public static double getPhilHealthMonthlyPremium(double salary) {
        PhilHealthContribution philHealthData = calculatePhilHealthFromTable(salary);
        return philHealthData.monthlyPremium;
    }

    /**
     * Get the employer contribution amount for Pag-IBIG (for informational purposes)
     */
    public static double getPagIBIGEmployerContribution(double salary) {
        PagIBIGContribution pagibigData = calculatePagIBIGFromTable(salary);
        return pagibigData.employerContribution;
    }

    /**
     * Get the employee contribution rate for Pag-IBIG (for informational purposes)
     */
    public static double getPagIBIGEmployeeRate(double salary) {
        PagIBIGContribution pagibigData = calculatePagIBIGFromTable(salary);
        return pagibigData.employeeRate;
    }

    /**
     * Get the employer contribution rate for Pag-IBIG (for informational purposes)
     */
    public static double getPagIBIGEmployerRate(double salary) {
        PagIBIGContribution pagibigData = calculatePagIBIGFromTable(salary);
        return pagibigData.employerRate;
    }
}