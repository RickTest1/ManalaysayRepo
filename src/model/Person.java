package model;

import java.time.LocalDate;

/**
 * Abstract base class for all person-related entities
 * Demonstrates abstraction in OOP
 */
public abstract class Person extends BaseEntity {
    protected String firstName;
    protected String lastName;
    protected LocalDate birthday;
    protected String address;
    protected String phoneNumber;

    public Person() {}

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Abstract methods that subclasses must implement
    public abstract String getRole();
    public abstract boolean hasRequiredDocuments();

    // Common methods for all persons
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

    // Common utility methods
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

    public boolean hasContactInfo() {
        return (phoneNumber != null && !phoneNumber.trim().isEmpty()) ||
                (address != null && !address.trim().isEmpty());
    }
}