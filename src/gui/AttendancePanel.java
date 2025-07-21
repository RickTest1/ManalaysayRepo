package gui;

import model.Employee;
import model.Attendance;
import service.AttendanceService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Attendance Management Panel
 * Allows employees to view their attendance and HR to manage attendance records
 */
public class AttendancePanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(AttendancePanel.class.getName());

    private final Employee employee;
    private final AttendanceService attendanceService;
    private final boolean isHRUser;

    // Table components
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    // Quick log components
    private JButton logInButton;
    private JButton logOutButton;
    private JLabel currentStatusLabel;

    // Manual entry components
    private JTextField dateField;
    private JTextField logInTimeField;
    private JTextField logOutTimeField;
    private JButton addAttendanceButton;
    private JButton updateAttendanceButton;
    private JButton deleteAttendanceButton;

    // Filter components
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton filterButton;
    private JButton refreshButton;

    // Summary components
    private JLabel totalDaysLabel;
    private JLabel workingHoursLabel;
    private JLabel lateCountLabel;
    private JLabel undertimeCountLabel;

    private Attendance selectedAttendance;

    public AttendancePanel(Employee employee) {
        this.employee = employee;
        this.attendanceService = new AttendanceService();
        this.isHRUser = checkIfHRUser(employee);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAttendanceData();
        updateCurrentStatus();
    }

    private boolean checkIfHRUser(Employee emp) {
        String position = emp.getPosition();
        return position != null && (
                position.toLowerCase().contains("hr") ||
                        position.toLowerCase().contains("human resource") ||
                        position.toLowerCase().contains("chief") ||
                        position.toLowerCase().contains("ceo") ||
                        position.toLowerCase().contains("manager")
        );
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {
                "Date", "Log In", "Log Out", "Work Hours", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return String.class;
                    case 1: case 2: return String.class;
                    case 3: return String.class;
                    case 4: return String.class;
                    default: return Object.class;
                }
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for status column
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        // Quick log components
        logInButton = new JButton("Log In Now");
        logOutButton = new JButton("Log Out Now");
        currentStatusLabel = new JLabel("Status: Not logged in today");

        logInButton.setBackground(new Color(46, 125, 50));
        logInButton.setForeground(Color.WHITE);
        logOutButton.setBackground(new Color(198, 40, 40));
        logOutButton.setForeground(Color.WHITE);

        // Manual entry components
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().toString());
        dateField.setToolTipText("Format: YYYY-MM-DD");

        logInTimeField = new JTextField(8);
        logInTimeField.setToolTipText("Format: HH:MM");

        logOutTimeField = new JTextField(8);
        logOutTimeField.setToolTipText("Format: HH:MM");

        addAttendanceButton = new JButton("Add");
        updateAttendanceButton = new JButton("Update");
        deleteAttendanceButton = new JButton("Delete");

        updateAttendanceButton.setEnabled(false);
        deleteAttendanceButton.setEnabled(false);

        // Only enable manual entry for HR users
        if (!isHRUser) {
            addAttendanceButton.setEnabled(false);
            updateAttendanceButton.setEnabled(false);
            deleteAttendanceButton.setEnabled(false);
        }

        // Filter components
        fromDateField = new JTextField(10);
        toDateField = new JTextField(10);

        // Default to current month
        LocalDate now = LocalDate.now();
        fromDateField.setText(now.withDayOfMonth(1).toString());
        toDateField.setText(now.toString());

        filterButton = new JButton("Filter");
        refreshButton = new JButton("Refresh");

        // Summary labels
        totalDaysLabel = new JLabel("Total Days: 0");
        workingHoursLabel = new JLabel("Total Hours: 0.00");
        lateCountLabel = new JLabel("Late Days: 0");
        undertimeCountLabel = new JLabel("Undertime Days: 0");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel - quick actions and filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center - table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom - manual entry and summary
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Quick log panel
        JPanel quickLogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickLogPanel.setBorder(new TitledBorder("Quick Actions"));

        quickLogPanel.add(currentStatusLabel);
        quickLogPanel.add(Box.createHorizontalStrut(20));
        quickLogPanel.add(logInButton);
        quickLogPanel.add(logOutButton);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBorder(new TitledBorder("Filter"));

        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromDateField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toDateField);
        filterPanel.add(filterButton);
        filterPanel.add(refreshButton);

        panel.add(quickLogPanel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Attendance Records"));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Manual entry panel (only for HR)
        if (isHRUser) {
            JPanel manualEntryPanel = createManualEntryPanel();
            panel.add(manualEntryPanel, BorderLayout.NORTH);
        }

        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createManualEntryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Manual Entry (HR Only)"));

        panel.add(new JLabel("Date:"));
        panel.add(dateField);
        panel.add(new JLabel("Log In:"));
        panel.add(logInTimeField);
        panel.add(new JLabel("Log Out:"));
        panel.add(logOutTimeField);

        panel.add(Box.createHorizontalStrut(20));
        panel.add(addAttendanceButton);
        panel.add(updateAttendanceButton);
        panel.add(deleteAttendanceButton);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(new TitledBorder("Summary"));

        panel.add(totalDaysLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(workingHoursLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(lateCountLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(undertimeCountLabel);

        return panel;
    }

    private void setupEventHandlers() {
        // Quick log buttons
        logInButton.addActionListener(this::handleQuickLogIn);
        logOutButton.addActionListener(this::handleQuickLogOut);

        // Manual entry buttons
        addAttendanceButton.addActionListener(this::handleAddAttendance);
        updateAttendanceButton.addActionListener(this::handleUpdateAttendance);
        deleteAttendanceButton.addActionListener(this::handleDeleteAttendance);

        // Filter and refresh
        filterButton.addActionListener(this::handleFilter);
        refreshButton.addActionListener(e -> loadAttendanceData());

        // Table selection
        attendanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });
    }

    private void handleQuickLogIn(ActionEvent e) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        boolean success = attendanceService.recordLogIn(employee.getId(), today, Time.valueOf(now));

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Logged in successfully at " + now.format(DateTimeFormatter.ofPattern("HH:mm")),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadAttendanceData();
            updateCurrentStatus();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to record log in. You may have already logged in today.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleQuickLogOut(ActionEvent e) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        boolean success = attendanceService.recordLogOut(employee.getId(), today, Time.valueOf(now));

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Logged out successfully at " + now.format(DateTimeFormatter.ofPattern("HH:mm")),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadAttendanceData();
            updateCurrentStatus();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to record log out. You may not have logged in today yet.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddAttendance(ActionEvent e) {
        try {
            Attendance attendance = createAttendanceFromForm();

            int attendanceId = attendanceService.attendanceDAO.insertAttendance(attendance);

            if (attendanceId > 0) {
                JOptionPane.showMessageDialog(this,
                        "Attendance record added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadAttendanceData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add attendance record.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error adding attendance", ex);
            JOptionPane.showMessageDialog(this,
                    "Error adding attendance: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateAttendance(ActionEvent e) {
        if (selectedAttendance == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an attendance record to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Attendance attendance = createAttendanceFromForm();
            attendance.setId(selectedAttendance.getId());

            boolean success = attendanceService.updateAttendance(attendance);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Attendance record updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                loadAttendanceData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update attendance record.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error updating attendance", ex);
            JOptionPane.showMessageDialog(this,
                    "Error updating attendance: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteAttendance(ActionEvent e) {
        if (selectedAttendance == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an attendance record to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this attendance record?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            boolean success = attendanceService.deleteAttendance(selectedAttendance.getId());

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Attendance record deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadAttendanceData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete attendance record.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleFilter(ActionEvent e) {
        loadAttendanceData();
    }

    private void handleTableSelection() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow >= 0 && isHRUser) {
            // Get attendance data from table
            String dateStr = (String) tableModel.getValueAt(selectedRow, 0);
            String logInStr = (String) tableModel.getValueAt(selectedRow, 1);
            String logOutStr = (String) tableModel.getValueAt(selectedRow, 2);

            dateField.setText(dateStr);
            logInTimeField.setText(logInStr);
            logOutTimeField.setText(logOutStr.equals("--") ? "" : logOutStr);

            updateAttendanceButton.setEnabled(true);
            deleteAttendanceButton.setEnabled(true);

            // Find the selected attendance object
            try {
                LocalDate date = LocalDate.parse(dateStr);
                selectedAttendance = attendanceService.getAttendanceByDate(employee.getId(), date);
            } catch (Exception ex) {
                selectedAttendance = null;
            }
        } else {
            updateAttendanceButton.setEnabled(false);
            deleteAttendanceButton.setEnabled(false);
            selectedAttendance = null;
        }
    }

    private Attendance createAttendanceFromForm() throws IllegalArgumentException {
        String dateText = dateField.getText().trim();
        String logInText = logInTimeField.getText().trim();
        String logOutText = logOutTimeField.getText().trim();

        if (dateText.isEmpty()) {
            throw new IllegalArgumentException("Date is required");
        }
        if (logInText.isEmpty()) {
            throw new IllegalArgumentException("Log in time is required");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }

        LocalTime logIn;
        try {
            logIn = LocalTime.parse(logInText);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid log in time format. Use HH:MM");
        }

        LocalTime logOut = null;
        if (!logOutText.isEmpty()) {
            try {
                logOut = LocalTime.parse(logOutText);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid log out time format. Use HH:MM");
            }
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employee.getId());
        attendance.setDate(java.sql.Date.valueOf(date));
        attendance.setLogIn(Time.valueOf(logIn));
        if (logOut != null) {
            attendance.setLogOut(Time.valueOf(logOut));
        }

        return attendance;
    }

    private void clearForm() {
        dateField.setText(LocalDate.now().toString());
        logInTimeField.setText("");
        logOutTimeField.setText("");
        selectedAttendance = null;
        updateAttendanceButton.setEnabled(false);
        deleteAttendanceButton.setEnabled(false);
        attendanceTable.clearSelection();
    }

    private void loadAttendanceData() {
        try {
            LocalDate fromDate = LocalDate.parse(fromDateField.getText());
            LocalDate toDate = LocalDate.parse(toDateField.getText());

            List<Attendance> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(
                    employee.getId(), fromDate, toDate);

            // Clear existing data
            tableModel.setRowCount(0);

            int totalDays = 0;
            double totalHours = 0.0;
            int lateDays = 0;
            int undertimeDays = 0;

            // Add attendance data to table
            for (Attendance att : attendanceList) {
                if (att.getLogIn() != null) {
                    totalDays++;

                    String date = att.getDate().toLocalDate().toString();
                    String logIn = att.getLogIn().toString();
                    String logOut = att.getLogOut() != null ? att.getLogOut().toString() : "--";

                    double workHours = att.getWorkHours();
                    totalHours += workHours;

                    String workHoursStr = String.format("%.2f", workHours);

                    String status = "";
                    if (att.isLate()) {
                        status += "Late ";
                        lateDays++;
                    }
                    if (att.hasUndertime() && att.getLogOut() != null) {
                        status += "Undertime ";
                        undertimeDays++;
                    }
                    if (status.isEmpty()) {
                        status = "Normal";
                    }

                    Object[] rowData = {date, logIn, logOut, workHoursStr, status.trim()};
                    tableModel.addRow(rowData);
                }
            }

            // Update summary
            totalDaysLabel.setText("Total Days: " + totalDays);
            workingHoursLabel.setText(String.format("Total Hours: %.2f", totalHours));
            lateCountLabel.setText("Late Days: " + lateDays);
            undertimeCountLabel.setText("Undertime Days: " + undertimeDays);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance data", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading attendance data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCurrentStatus() {
        try {
            LocalDate today = LocalDate.now();
            Attendance todayAttendance = attendanceService.getAttendanceByDate(employee.getId(), today);

            if (todayAttendance == null) {
                currentStatusLabel.setText("Status: Not logged in today");
                logInButton.setEnabled(true);
                logOutButton.setEnabled(false);
            } else if (todayAttendance.getLogIn() != null && todayAttendance.getLogOut() == null) {
                currentStatusLabel.setText("Status: Logged in at " +
                        todayAttendance.getLogIn().toString());
                logInButton.setEnabled(false);
                logOutButton.setEnabled(true);
            } else if (todayAttendance.getLogOut() != null) {
                currentStatusLabel.setText("Status: Completed (" +
                        todayAttendance.getLogIn().toString() + " - " +
                        todayAttendance.getLogOut().toString() + ")");
                logInButton.setEnabled(false);
                logOutButton.setEnabled(false);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating current status", e);
            currentStatusLabel.setText("Status: Unknown");
        }
    }

    /**
     * Custom cell renderer for status column
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                String status = value.toString();
                if (status.contains("Late") || status.contains("Undertime")) {
                    c.setBackground(new Color(255, 235, 235)); // Light red
                } else if (status.equals("Normal")) {
                    c.setBackground(new Color(235, 255, 235)); // Light green
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            return c;
        }
    }
}