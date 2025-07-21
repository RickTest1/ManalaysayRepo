package model;

import java.time.LocalDate;

public class Employee extends BaseEntity {
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String address;
    private String phoneNumber;
    private String position;
    private int positionId;
    private String status; // Regular, Probationary
    private double basicSalary;
    private String immediateSupervisor;

    // Government IDs
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;

    // Additional fields for position details (from view)
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSemiMonthlyRate;
    private double hourlyRate;

    // Constructors
    public Employee() {}

    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Employee(int employeeId, String firstName, String lastName) {
        setId(employeeId);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and Setters with validation
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        this.firstName = firstName.trim();
        touch();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        this.lastName = lastName.trim();
        touch();
    }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be in the future");
        }
        this.birthday = birthday;
        touch();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = address;
        touch();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        touch();
    }

    public String getPosition() { return position; }
    public void setPosition(String position) {
        this.position = position;
        touch();
    }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) {
        this.positionId = positionId;
        touch();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        touch();
    }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) {
        if (basicSalary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.basicSalary = basicSalary;
        touch();
    }

    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) {
        this.immediateSupervisor = immediateSupervisor;
        touch();
    }

    // Government ID getters/setters
    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }

    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }

    // Additional fields from position (populated from view)
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
        touch();
    }

    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
        touch();
    }

    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) {
        this.clothingAllowance = clothingAllowance;
        touch();
    }

    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) {
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        touch();
    }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
        touch();
    }

    // Utility methods
    public String getFullName() {
        if (firstName == null && lastName == null) return "Unknown";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public String getFormattedName() {
        if (firstName == null && lastName == null) return "Unknown";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return lastName + ", " + firstName;
    }

    public int getAge() {
        return birthday != null ? LocalDate.now().getYear() - birthday.getYear() : 0;
    }

    public double getDailyRate() {
        return basicSalary / 22.0; // 22 working days
    }

    public double getCalculatedHourlyRate() {
        return getDailyRate() / 8.0; // 8 hours per day
    }

    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public boolean isRegular() {
        return "Regular".equalsIgnoreCase(status);
    }

    public boolean isProbationary() {
        return "Probationary".equalsIgnoreCase(status);
    }

    public boolean hasCompleteGovernmentIds() {
        return sssNumber != null && !sssNumber.trim().isEmpty() &&
                philhealthNumber != null && !philhealthNumber.trim().isEmpty() &&
                tinNumber != null && !tinNumber.trim().isEmpty() &&
                pagibigNumber != null && !pagibigNumber.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return (phoneNumber != null && !phoneNumber.trim().isEmpty()) ||
                (address != null && !address.trim().isEmpty());
    }

    @Override
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                basicSalary >= 0;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getFullName();
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + getId() +
                ", name='" + getFullName() + '\'' +
                ", position='" + position + '\'' +
                ", status='" + status + '\'' +
                ", basicSalary=" + basicSalary +
                '}';
    }
}