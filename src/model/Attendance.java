package model;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;

public class Attendance extends BaseEntity {
    private int employeeId;
    private Date date;
    private Time logIn;
    private Time logOut;

    public Attendance() {}

    public Attendance(int employeeId, Date date, Time logIn, Time logOut) {
        this.employeeId = employeeId;
        this.date = date;
        this.logIn = logIn;
        this.logOut = logOut;
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) throw new IllegalArgumentException("Employee ID must be positive");
        this.employeeId = employeeId;
        touch();
    }

    public Date getDate() { return date; }
    public void setDate(Date date) {
        if (date == null) throw new IllegalArgumentException("Date cannot be null");
        this.date = date;
        touch();
    }

    public Time getLogIn() { return logIn; }
    public void setLogIn(Time logIn) { this.logIn = logIn; touch(); }

    public Time getLogOut() { return logOut; }
    public void setLogOut(Time logOut) {
        if (logIn != null && logOut != null && logOut.before(logIn)) {
            throw new IllegalArgumentException("Log out cannot be before log in");
        }
        this.logOut = logOut;
        touch();
    }

    // Utility methods
    public double getWorkHours() {
        if (logIn == null || logOut == null) return 0.0;
        Duration duration = Duration.between(logIn.toLocalTime(), logOut.toLocalTime());
        return duration.toMinutes() / 60.0;
    }

    public boolean isLate() {
        if (logIn == null) return false;
        return logIn.toLocalTime().isAfter(LocalTime.of(8, 0));
    }

    public boolean hasUndertime() {
        if (logOut == null) return false;
        return logOut.toLocalTime().isBefore(LocalTime.of(17, 0));
    }

    @Override
    public boolean isValid() {
        return employeeId > 0 && date != null;
    }

    @Override
    public String getDisplayName() {
        return "Attendance for Employee " + employeeId + " on " + date;
    }
}
