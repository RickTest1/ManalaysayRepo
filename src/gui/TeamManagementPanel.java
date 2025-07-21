package gui;

import model.Employee;
import model.Attendance;
import model.LeaveRequest;
import service.EmployeeService;
import service.AttendanceService;
import service.LeaveRequestService;
import service.PayrollCalculator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Team Management Panel for Managers and Team Leaders
 * Allows viewing and managing their team members' information
 */
public class TeamManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(TeamManagementPanel.class.getName());

    private final Employee manager;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveService;
    private final PayrollCalculator payrollCalculator;

    // Team overview components
    private JTable teamTable;
    private DefaultTableModel teamTableModel;
    private JLabel teamSizeLabel;
    private JLabel presentTodayLabel;
    private JLabel onLeaveLabel;

    // Team attendance components
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JTextField attendanceDateField;
    private JButton viewAttendanceButton;

    // Team leave requests components
    private JTable leaveRequestsTable;
    private DefaultTableModel leaveRequestsTableModel;
    private JButton approveLeaveButton;
    private JButton rejectLeaveButton;

    // Team performance components
    private JTextArea performanceSummaryArea;
    private JComboBox<String> performancePeriodCombo;
    private JButton generatePerformanceButton;

    // Quick actions
    private JButton refreshDataButton;
    private JButton teamReportButton;

    private List<Employee> teamMembers;
    private LeaveRequest selectedLeaveRequest;

    public TeamManagementPanel(Employee manager) {
        this.manager = manager;
        this.employeeService = new EmployeeService();
        this.attendanceService = new AttendanceService();
        this.leaveService = new LeaveRequestService();
        this.payrollCalculator = new PayrollCalculator();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTeamData();
    }

    private void initializeComponents() {
        // Team overview table
        String[] teamColumns = {"ID", "Name", "Position", "Status", "Phone", "Today's Status"};
        teamTableModel = new DefaultTableModel(teamColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        teamTable = new JTable(teamTableModel);
        teamTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        // Summary labels
        teamSizeLabel = new JLabel("Team Size: 0");
        presentTodayLabel = new JLabel("Present Today: 0");
        onLeaveLabel = new JLabel("On Leave: 0");

        // Attendance table
        String[] attendanceColumns = {"Employee", "Date", "Log In", "Log Out", "Hours", "Status"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        attendanceTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        attendanceDateField = new JTextField(LocalDate.now().toString(), 10);
        viewAttendanceButton = new JButton("View Attendance");

        // Leave requests table
        String[] leaveColumns = {"Employee", "Type", "Start Date", "End Date", "Days", "Status", "Submitted"};
        leaveRequestsTableModel = new DefaultTableModel(leaveColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveRequestsTable = new JTable(leaveRequestsTableModel);
        leaveRequestsTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        approveLeaveButton = new JButton("Approve");
        rejectLeaveButton = new JButton("Reject");

        approveLeaveButton.setBackground(new Color(46, 125, 50));
        approveLeaveButton.setForeground(Color.WHITE);
        rejectLeaveButton.setBackground(new Color(198, 40, 40));
        rejectLeaveButton.setForeground(Color.WHITE);

        approveLeaveButton.setEnabled(false);
        rejectLeaveButton.setEnabled(false);

        // Performance summary
        performanceSummaryArea = new JTextArea(8, 40);
        performanceSummaryArea.setEditable(false);
        performanceSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        performanceSummaryArea.setBackground(new Color(248, 248, 248));

        performancePeriodCombo = new JComboBox<>(new String[]{
                "Current Month", "Last Month", "Last 3 Months", "Current Year"
        });

        generatePerformanceButton = new JButton("Generate Performance Summary");

        // Quick actions
        refreshDataButton = new JButton("Refresh Data");
        teamReportButton = new JButton("Generate Team Report");

        refreshDataButton.setBackground(new Color(33, 150, 243));
        refreshDataButton.setForeground(Color.WHITE);
        teamReportButton.setBackground(new Color(76, 175, 80));
        teamReportButton.setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content - tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Team Overview Tab
        JPanel overviewPanel = createTeamOverviewPanel();
        tabbedPane.addTab("Team Overview", overviewPanel);

        // Attendance Tab
        JPanel attendancePanel = createAttendancePanel();
        tabbedPane.addTab("Team Attendance", attendancePanel);

        // Leave Requests Tab
        JPanel leavePanel = createLeaveRequestsPanel();
        tabbedPane.addTab("Leave Requests", leavePanel);

        // Performance Tab
        JPanel performancePanel = createPerformancePanel();
        tabbedPane.addTab("Team Performance", performancePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Team Management - " + manager.getFullName()));

        // Manager info and summary
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Manager: " + manager.getFullName() +
                " (" + (manager.getPosition() != null ? manager.getPosition() : "Unknown Position") + ")"));
        infoPanel.add(Box.createHorizontalStrut(30));
        infoPanel.add(teamSizeLabel);
        infoPanel.add(Box.createHorizontalStrut(15));
        infoPanel.add(presentTodayLabel);
        infoPanel.add(Box.createHorizontalStrut(15));
        infoPanel.add(onLeaveLabel);

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.add(refreshDataButton);
        actionsPanel.add(teamReportButton);

        panel.add(infoPanel, BorderLayout.WEST);
        panel.add(actionsPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTeamOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(teamTable);
        scrollPane.setBorder(new TitledBorder("Team Members"));
        scrollPane.setPreferredSize(new Dimension(0, 400));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Quick stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setBorder(new TitledBorder("Quick Statistics"));

        // We'll populate these dynamically
        JLabel regularEmployeesLabel = new JLabel("Regular Employees: 0");
        JLabel probationaryEmployeesLabel = new JLabel("Probationary: 0");
        JLabel avgAttendanceLabel = new JLabel("Avg Attendance: 0%");
        JLabel pendingLeavesLabel = new JLabel("Pending Leaves: 0");

        statsPanel.add(regularEmployeesLabel);
        statsPanel.add(probationaryEmployeesLabel);
        statsPanel.add(avgAttendanceLabel);
        statsPanel.add(pendingLeavesLabel);

        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBorder(new TitledBorder("Attendance Controls"));

        controlsPanel.add(new JLabel("Date:"));
        controlsPanel.add(attendanceDateField);
        controlsPanel.add(viewAttendanceButton);

        // Set default to today
        viewAttendanceButton.doClick();

        panel.add(controlsPanel, BorderLayout.NORTH);

        // Attendance table
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new TitledBorder("Team Attendance"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLeaveRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Leave requests table
        JScrollPane scrollPane = new JScrollPane(leaveRequestsTable);
        scrollPane.setBorder(new TitledBorder("Pending Leave Requests"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setBorder(new TitledBorder("Actions"));

        buttonsPanel.add(new JLabel("Select a request to:"));
        buttonsPanel.add(approveLeaveButton);
        buttonsPanel.add(rejectLeaveButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBorder(new TitledBorder("Performance Analysis"));

        controlsPanel.add(new JLabel("Period:"));
        controlsPanel.add(performancePeriodCombo);
        controlsPanel.add(generatePerformanceButton);

        panel.add(controlsPanel, BorderLayout.NORTH);

        // Performance summary
        JScrollPane scrollPane = new JScrollPane(performanceSummaryArea);
        scrollPane.setBorder(new TitledBorder("Team Performance Summary"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        refreshDataButton.addActionListener(e -> {
            loadTeamData();
            loadTeamAttendance();
            loadTeamLeaveRequests();
        });

        teamReportButton.addActionListener(this::handleGenerateTeamReport);
        viewAttendanceButton.addActionListener(this::handleViewAttendance);
        generatePerformanceButton.addActionListener(this::handleGeneratePerformance);

        approveLeaveButton.addActionListener(this::handleApproveLeave);
        rejectLeaveButton.addActionListener(this::handleRejectLeave);

        // Table selection for leave requests
        leaveRequestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleLeaveRequestSelection();
            }
        });
    }

    private void handleViewAttendance(ActionEvent e) {
        loadTeamAttendance();
    }

    private void handleGeneratePerformance(ActionEvent e) {
        generatePerformanceSummary();
    }

    private void handleApproveLeave(ActionEvent e) {
        if (selectedLeaveRequest == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request to approve.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to approve this leave request?",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            boolean success = leaveService.processLeaveRequest(
                    selectedLeaveRequest.getLeaveId(), LeaveRequest.STATUS_APPROVED);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Leave request approved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTeamLeaveRequests();
                selectedLeaveRequest = null;
                approveLeaveButton.setEnabled(false);
                rejectLeaveButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to approve leave request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRejectLeave(ActionEvent e) {
        if (selectedLeaveRequest == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request to reject.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reject this leave request?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            boolean success = leaveService.processLeaveRequest(
                    selectedLeaveRequest.getLeaveId(), LeaveRequest.STATUS_REJECTED);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Leave request rejected successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTeamLeaveRequests();
                selectedLeaveRequest = null;
                approveLeaveButton.setEnabled(false);
                rejectLeaveButton.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to reject leave request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLeaveRequestSelection() {
        int selectedRow = leaveRequestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Get the employee name and find the corresponding leave request
            String employeeName = (String) leaveRequestsTableModel.getValueAt(selectedRow, 0);
            String startDate = (String) leaveRequestsTableModel.getValueAt(selectedRow, 2);

            // Find the leave request (this is a simplified approach)
            selectedLeaveRequest = findLeaveRequest(employeeName, startDate);

            if (selectedLeaveRequest != null && selectedLeaveRequest.isPending()) {
                approveLeaveButton.setEnabled(true);
                rejectLeaveButton.setEnabled(true);
            } else {
                approveLeaveButton.setEnabled(false);
                rejectLeaveButton.setEnabled(false);
            }
        } else {
            selectedLeaveRequest = null;
            approveLeaveButton.setEnabled(false);
            rejectLeaveButton.setEnabled(false);
        }
    }

    private LeaveRequest findLeaveRequest(String employeeName, String startDate) {
        try {
            // Get all pending leave requests and find the matching one
            List<LeaveRequest> pendingRequests = leaveService.getPendingLeaveRequests();

            for (LeaveRequest request : pendingRequests) {
                Employee emp = getEmployeeById(request.getEmployeeId());
                if (emp != null && emp.getFullName().equals(employeeName) &&
                        request.getStartDateAsLocalDate().toString().equals(startDate)) {
                    return request;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error finding leave request", ex);
        }
        return null;
    }

    private void handleGenerateTeamReport(ActionEvent e) {
        if (teamMembers == null || teamMembers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No team members found to generate report.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create team report dialog
        createTeamReportDialog();
    }

    private void createTeamReportDialog() {
        JDialog reportDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Team Report - " + manager.getFullName(), true);

        JTextArea reportArea = new JTextArea(25, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        StringBuilder report = new StringBuilder();
        report.append("TEAM MANAGEMENT REPORT\n");
        report.append("=".repeat(60)).append("\n");
        report.append("Manager: ").append(manager.getFullName()).append("\n");
        report.append("Position: ").append(manager.getPosition() != null ? manager.getPosition() : "Unknown").append("\n");
        report.append("Report Date: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
        report.append("=".repeat(60)).append("\n\n");

        // Team composition
        report.append("TEAM COMPOSITION:\n");
        report.append("-".repeat(30)).append("\n");
        report.append("Total Team Members: ").append(teamMembers.size()).append("\n");

        long regularCount = teamMembers.stream().filter(emp -> "Regular".equals(emp.getStatus())).count();
        long probationaryCount = teamMembers.stream().filter(emp -> "Probationary".equals(emp.getStatus())).count();

        report.append("Regular Employees: ").append(regularCount).append("\n");
        report.append("Probationary Employees: ").append(probationaryCount).append("\n\n");

        // Team members list
        report.append("TEAM MEMBERS:\n");
        report.append("-".repeat(30)).append("\n");
        for (Employee emp : teamMembers) {
            report.append(String.format("%-5d %-25s %-20s %s\n",
                    emp.getId(),
                    emp.getFullName(),
                    emp.getPosition() != null ? emp.getPosition() : "Unknown",
                    emp.getStatus() != null ? emp.getStatus() : "Unknown"));
        }

        // Attendance summary for today
        report.append("\nTODAY'S ATTENDANCE:\n");
        report.append("-".repeat(30)).append("\n");

        LocalDate today = LocalDate.now();
        int presentToday = 0;
        int lateToday = 0;

        for (Employee emp : teamMembers) {
            try {
                Attendance todayAttendance = attendanceService.getAttendanceByDate(emp.getId(), today);
                if (todayAttendance != null && todayAttendance.getLogIn() != null) {
                    presentToday++;
                    if (todayAttendance.isLate()) {
                        lateToday++;
                    }
                }
            } catch (Exception ex) {
                // Continue with other employees
            }
        }

        report.append("Present Today: ").append(presentToday).append(" / ").append(teamMembers.size()).append("\n");
        report.append("Late Arrivals: ").append(lateToday).append("\n");
        report.append("Attendance Rate: ").append(
                teamMembers.size() > 0 ? String.format("%.1f%%", (presentToday * 100.0) / teamMembers.size()) : "0%").append("\n\n");

        // Pending leave requests
        try {
            List<LeaveRequest> pendingLeaves = leaveService.getPendingLeaveRequests();
            long teamPendingLeaves = pendingLeaves.stream()
                    .filter(leave -> teamMembers.stream().anyMatch(emp -> emp.getId() == leave.getEmployeeId()))
                    .count();

            report.append("PENDING LEAVE REQUESTS: ").append(teamPendingLeaves).append("\n");
            report.append("-".repeat(30)).append("\n");

            if (teamPendingLeaves > 0) {
                for (LeaveRequest leave : pendingLeaves) {
                    Employee emp = getEmployeeById(leave.getEmployeeId());
                    if (emp != null && teamMembers.contains(emp)) {
                        report.append(String.format("%-25s %-15s %s to %s (%d days)\n",
                                emp.getFullName(),
                                leave.getLeaveType(),
                                leave.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                                leave.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("MMM dd")),
                                leave.getLeaveDays()));
                    }
                }
            } else {
                report.append("No pending leave requests.\n");
            }
        } catch (Exception ex) {
            report.append("Error loading leave requests.\n");
        }

        report.append("\n").append("=".repeat(60)).append("\n");
        report.append("Report generated by MotorPH Payroll System\n");

        reportArea.setText(report.toString());
        reportArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        reportDialog.add(scrollPane);

        reportDialog.setSize(800, 600);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setVisible(true);
    }

    private void loadTeamData() {
        try {
            // Load all employees and filter by supervisor
            List<Employee> allEmployees = employeeService.getAllEmployees();

            teamMembers = allEmployees.stream()
                    .filter(emp -> manager.getFormattedName().equals(emp.getImmediateSupervisor()))
                    .collect(Collectors.toList());

            // Update team table
            teamTableModel.setRowCount(0);

            LocalDate today = LocalDate.now();
            int presentToday = 0;
            int onLeave = 0;

            for (Employee emp : teamMembers) {
                String todayStatus = getTodayStatus(emp.getId(), today);
                if ("Present".equals(todayStatus) || "Late".equals(todayStatus)) {
                    presentToday++;
                } else if ("On Leave".equals(todayStatus)) {
                    onLeave++;
                }

                Object[] row = {
                        emp.getId(),
                        emp.getFullName(),
                        emp.getPosition() != null ? emp.getPosition() : "Unknown",
                        emp.getStatus() != null ? emp.getStatus() : "Unknown",
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "N/A",
                        todayStatus
                };
                teamTableModel.addRow(row);
            }

            // Update summary labels
            teamSizeLabel.setText("Team Size: " + teamMembers.size());
            presentTodayLabel.setText("Present Today: " + presentToday);
            onLeaveLabel.setText("On Leave: " + onLeave);

            // Load other data
            loadTeamAttendance();
            loadTeamLeaveRequests();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team data", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading team data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getTodayStatus(int employeeId, LocalDate date) {
        try {
            // Check if on approved leave
            List<LeaveRequest> approvedLeaves = leaveService.getApprovedLeavesByEmployeeAndDateRange(
                    employeeId, date, date);
            if (!approvedLeaves.isEmpty()) {
                return "On Leave";
            }

            // Check attendance
            Attendance attendance = attendanceService.getAttendanceByDate(employeeId, date);
            if (attendance == null || attendance.getLogIn() == null) {
                return "Absent";
            }

            if (attendance.isLate()) {
                return "Late";
            }

            return "Present";

        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void loadTeamAttendance() {
        try {
            LocalDate selectedDate = LocalDate.parse(attendanceDateField.getText());

            attendanceTableModel.setRowCount(0);

            for (Employee emp : teamMembers) {
                Attendance attendance = attendanceService.getAttendanceByDate(emp.getId(), selectedDate);

                String logIn = "N/A";
                String logOut = "N/A";
                String hours = "0.00";
                String status = "Absent";

                if (attendance != null && attendance.getLogIn() != null) {
                    logIn = attendance.getLogIn().toString();
                    logOut = attendance.getLogOut() != null ? attendance.getLogOut().toString() : "Not logged out";
                    hours = String.format("%.2f", attendance.getWorkHours());

                    if (attendance.isLate()) {
                        status = "Late";
                    } else if (attendance.hasUndertime() && attendance.getLogOut() != null) {
                        status = "Undertime";
                    } else {
                        status = "Normal";
                    }
                } else {
                    // Check if on leave
                    List<LeaveRequest> leaves = leaveService.getApprovedLeavesByEmployeeAndDateRange(
                            emp.getId(), selectedDate, selectedDate);
                    if (!leaves.isEmpty()) {
                        status = "On Leave";
                    }
                }

                Object[] row = {
                        emp.getFullName(),
                        selectedDate.toString(),
                        logIn,
                        logOut,
                        hours,
                        status
                };
                attendanceTableModel.addRow(row);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team attendance", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading team attendance: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTeamLeaveRequests() {
        try {
            List<LeaveRequest> pendingRequests = leaveService.getPendingLeaveRequests();

            leaveRequestsTableModel.setRowCount(0);

            for (LeaveRequest request : pendingRequests) {
                // Check if this request is from a team member
                Employee requestEmployee = getEmployeeById(request.getEmployeeId());
                if (requestEmployee != null && teamMembers.contains(requestEmployee)) {
                    Object[] row = {
                            requestEmployee.getFullName(),
                            request.getLeaveType(),
                            request.getStartDateAsLocalDate().toString(),
                            request.getEndDateAsLocalDate().toString(),
                            request.getLeaveDays(),
                            request.getStatus(),
                            request.getCreatedAt() != null ?
                                    request.getCreatedAt().toLocalDate().toString() : "Unknown"
                    };
                    leaveRequestsTableModel.addRow(row);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team leave requests", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading leave requests: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePerformanceSummary() {
        try {
            String selectedPeriod = (String) performancePeriodCombo.getSelectedItem();
            LocalDate[] dateRange = getDateRangeForPeriod(selectedPeriod);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            StringBuilder summary = new StringBuilder();
            summary.append("TEAM PERFORMANCE SUMMARY\n");
            summary.append("=".repeat(50)).append("\n");
            summary.append("Manager: ").append(manager.getFullName()).append("\n");
            summary.append("Period: ").append(startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .append(" to ").append(endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            summary.append("=".repeat(50)).append("\n\n");

            if (teamMembers.isEmpty()) {
                summary.append("No team members found.\n");
                performanceSummaryArea.setText(summary.toString());
                return;
            }

            // Attendance performance
            summary.append("ATTENDANCE PERFORMANCE:\n");
            summary.append("-".repeat(30)).append("\n");

            int totalWorkingDays = getWorkingDaysBetween(startDate, endDate);
            int totalPossibleDays = teamMembers.size() * totalWorkingDays;
            int totalActualDays = 0;
            int totalLateDays = 0;
            int totalUndertimeDays = 0;
            double totalHours = 0.0;

            for (Employee emp : teamMembers) {
                List<Attendance> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(
                        emp.getId(), startDate, endDate);

                int empDays = 0;
                int empLateDays = 0;
                int empUndertimeDays = 0;
                double empHours = 0.0;

                for (Attendance att : attendanceList) {
                    if (att.getLogIn() != null) {
                        empDays++;
                        empHours += att.getWorkHours();

                        if (att.isLate()) empLateDays++;
                        if (att.hasUndertime()) empUndertimeDays++;
                    }
                }

                totalActualDays += empDays;
                totalLateDays += empLateDays;
                totalUndertimeDays += empUndertimeDays;
                totalHours += empHours;
            }

            double attendanceRate = totalPossibleDays > 0 ? (totalActualDays * 100.0) / totalPossibleDays : 0.0;
            double avgHoursPerEmployee = teamMembers.size() > 0 ? totalHours / teamMembers.size() : 0.0;

            summary.append("Total Working Days in Period: ").append(totalWorkingDays).append("\n");
            summary.append("Team Attendance Rate: ").append(String.format("%.1f%%", attendanceRate)).append("\n");
            summary.append("Total Days Present: ").append(totalActualDays).append(" / ").append(totalPossibleDays).append("\n");
            summary.append("Total Late Instances: ").append(totalLateDays).append("\n");
            summary.append("Total Undertime Instances: ").append(totalUndertimeDays).append("\n");
            summary.append("Average Hours per Employee: ").append(String.format("%.2f", avgHoursPerEmployee)).append("\n\n");

            // Leave utilization
            summary.append("LEAVE UTILIZATION:\n");
            summary.append("-".repeat(30)).append("\n");

            int totalLeaveRequests = 0;
            int approvedLeaves = 0;
            long totalLeaveDays = 0;

            for (Employee emp : teamMembers) {
                List<LeaveRequest> empLeaves = leaveService.getLeaveRequestsByEmployee(emp.getId());

                for (LeaveRequest leave : empLeaves) {
                    if (!leave.getStartDateAsLocalDate().isBefore(startDate) &&
                            !leave.getEndDateAsLocalDate().isAfter(endDate)) {
                        totalLeaveRequests++;
                        if (leave.isApproved()) {
                            approvedLeaves++;
                            totalLeaveDays += leave.getLeaveDays();
                        }
                    }
                }
            }

            summary.append("Total Leave Requests: ").append(totalLeaveRequests).append("\n");
            summary.append("Approved Leaves: ").append(approvedLeaves).append("\n");
            summary.append("Total Leave Days Used: ").append(totalLeaveDays).append("\n");
            summary.append("Average Leave Days per Employee: ")
                    .append(teamMembers.size() > 0 ? String.format("%.1f", (double) totalLeaveDays / teamMembers.size()) : "0")
                    .append("\n\n");

            // Individual performance summary
            summary.append("INDIVIDUAL PERFORMANCE:\n");
            summary.append("-".repeat(30)).append("\n");
            summary.append(String.format("%-25s %8s %8s %6s %6s\n", "Employee", "Present", "Hours", "Late", "Leave"));
            summary.append("-".repeat(60)).append("\n");

            for (Employee emp : teamMembers) {
                List<Attendance> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(
                        emp.getId(), startDate, endDate);

                int empDays = (int) attendanceList.stream().filter(att -> att.getLogIn() != null).count();
                double empHours = attendanceList.stream().mapToDouble(Attendance::getWorkHours).sum();
                int empLateDays = (int) attendanceList.stream().filter(Attendance::isLate).count();

                List<LeaveRequest> empLeaves = leaveService.getApprovedLeavesByEmployeeAndDateRange(
                        emp.getId(), startDate, endDate);
                long empLeaveDays = empLeaves.stream().mapToLong(LeaveRequest::getLeaveDays).sum();

                summary.append(String.format("%-25s %8d %8.1f %6d %6d\n",
                        emp.getFullName().length() > 25 ? emp.getFullName().substring(0, 22) + "..." : emp.getFullName(),
                        empDays, empHours, empLateDays, empLeaveDays));
            }

            performanceSummaryArea.setText(summary.toString());
            performanceSummaryArea.setCaretPosition(0);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating performance summary", e);
            JOptionPane.showMessageDialog(this,
                    "Error generating performance summary: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate[] getDateRangeForPeriod(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start, end;

        switch (period) {
            case "Current Month":
                start = now.withDayOfMonth(1);
                end = now;
                break;
            case "Last Month":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());
                break;
            case "Last 3 Months":
                start = now.minusMonths(3).withDayOfMonth(1);
                end = now;
                break;
            case "Current Year":
                start = now.withDayOfYear(1);
                end = now;
                break;
            default:
                start = now.withDayOfMonth(1);
                end = now;
                break;
        }

        return new LocalDate[]{start, end};
    }

    private int getWorkingDaysBetween(LocalDate start, LocalDate end) {
        // Simple calculation - excluding weekends
        int workingDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (current.getDayOfWeek().getValue() < 6) { // Monday to Friday
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    private Employee getEmployeeById(int employeeId) {
        try {
            return employeeService.getEmployeeById(employeeId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Custom cell renderer for status columns
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                String status = value.toString();
                switch (status) {
                    case "Present":
                    case "Normal":
                    case "Approved":
                        c.setBackground(new Color(235, 255, 235)); // Light green
                        break;
                    case "Late":
                    case "Undertime":
                    case "Pending":
                        c.setBackground(new Color(255, 243, 224)); // Light orange
                        break;
                    case "Absent":
                    case "Rejected":
                        c.setBackground(new Color(255, 235, 235)); // Light red
                        break;
                    case "On Leave":
                        c.setBackground(new Color(224, 235, 255)); // Light blue
                        break;
                    default:
                        c.setBackground(Color.WHITE);
                        break;
                }
            }

            return c;
        }
    }
}