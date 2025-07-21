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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Reports Panel for generating various HR and payroll reports
 */
public class ReportsPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ReportsPanel.class.getName());

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveService;
    private final PayrollCalculator payrollCalculator;

    // Report selection components
    private JComboBox<ReportType> reportTypeCombo;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JComboBox<String> departmentFilter;
    private JComboBox<String> statusFilter;

    // Report display components
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea summaryArea;

    // Action buttons
    private JButton generateReportButton;
    private JButton exportCsvButton;
    private JButton printReportButton;
    private JButton refreshDataButton;

    // Progress components
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // Report types enum
    private enum ReportType {
        EMPLOYEE_SUMMARY("Employee Summary", "List of all employees with basic information"),
        ATTENDANCE_SUMMARY("Attendance Summary", "Attendance records for selected period"),
        LEAVE_SUMMARY("Leave Summary", "Leave requests and approvals"),
        PAYROLL_SUMMARY("Payroll Summary", "Payroll calculations for selected period"),
        LATE_ATTENDANCE("Late Attendance Report", "Employees with late arrivals"),
        DEPARTMENT_HEADCOUNT("Department Headcount", "Employee count by department/position"),
        SALARY_ANALYSIS("Salary Analysis", "Salary distribution and statistics");

        private final String displayName;
        private final String description;

        ReportType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public ReportsPanel() {
        this.employeeService = new EmployeeService();
        this.attendanceService = new AttendanceService();
        this.leaveService = new LeaveRequestService();
        this.payrollCalculator = new PayrollCalculator();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadFilterData();
    }

    private void initializeComponents() {
        // Report selection components
        reportTypeCombo = new JComboBox<>(ReportType.values());

        // Default date range to current month
        LocalDate now = LocalDate.now();
        fromDateField = new JTextField(now.withDayOfMonth(1).toString(), 10);
        toDateField = new JTextField(now.toString(), 10);

        departmentFilter = new JComboBox<>(new String[]{"All Departments"});
        statusFilter = new JComboBox<>(new String[]{"All Status", "Regular", "Probationary"});

        // Report table
        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Summary area
        summaryArea = new JTextArea(6, 50);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        summaryArea.setBackground(new Color(248, 248, 248));

        // Action buttons
        generateReportButton = new JButton("Generate Report");
        exportCsvButton = new JButton("Export to CSV");
        printReportButton = new JButton("Print Report");
        refreshDataButton = new JButton("Refresh Data");

        generateReportButton.setBackground(new Color(33, 150, 243));
        generateReportButton.setForeground(Color.WHITE);
        exportCsvButton.setBackground(new Color(76, 175, 80));
        exportCsvButton.setForeground(Color.WHITE);

        // Initially disable export and print until report is generated
        exportCsvButton.setEnabled(false);
        printReportButton.setEnabled(false);

        // Progress components
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Ready to generate reports");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel - report configuration
        JPanel configPanel = createConfigurationPanel();
        add(configPanel, BorderLayout.NORTH);

        // Center - report display
        JPanel displayPanel = createDisplayPanel();
        add(displayPanel, BorderLayout.CENTER);

        // Bottom - status and actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigurationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Report Configuration"));

        // Report type and description panel
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Report Type:"));
        typePanel.add(reportTypeCombo);

        JLabel descriptionLabel = new JLabel();
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.ITALIC));
        updateDescriptionLabel(descriptionLabel);

        // Add listener to update description
        reportTypeCombo.addActionListener(e -> updateDescriptionLabel(descriptionLabel));

        typePanel.add(Box.createHorizontalStrut(20));
        typePanel.add(descriptionLabel);

        // Filters panel
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.add(new JLabel("From Date:"));
        filtersPanel.add(fromDateField);
        filtersPanel.add(new JLabel("To Date:"));
        filtersPanel.add(toDateField);
        filtersPanel.add(Box.createHorizontalStrut(10));
        filtersPanel.add(new JLabel("Department:"));
        filtersPanel.add(departmentFilter);
        filtersPanel.add(new JLabel("Status:"));
        filtersPanel.add(statusFilter);

        panel.add(typePanel, BorderLayout.NORTH);
        panel.add(filtersPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateDescriptionLabel(JLabel descriptionLabel) {
        ReportType selectedType = (ReportType) reportTypeCombo.getSelectedItem();
        if (selectedType != null) {
            descriptionLabel.setText("(" + selectedType.getDescription() + ")");
        }
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Split pane for table and summary
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Report Data"));

        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 400));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(new TitledBorder("Summary"));

        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        summaryScrollPane.setPreferredSize(new Dimension(0, 150));
        summaryPanel.add(summaryScrollPane, BorderLayout.CENTER);

        splitPane.setTopComponent(tablePanel);
        splitPane.setBottomComponent(summaryPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.7);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout());
        actionsPanel.add(generateReportButton);
        actionsPanel.add(exportCsvButton);
        actionsPanel.add(printReportButton);
        actionsPanel.add(refreshDataButton);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        panel.add(actionsPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventHandlers() {
        generateReportButton.addActionListener(this::handleGenerateReport);
        exportCsvButton.addActionListener(this::handleExportCsv);
        printReportButton.addActionListener(this::handlePrintReport);
        refreshDataButton.addActionListener(e -> loadFilterData());
    }

    private void handleGenerateReport(ActionEvent e) {
        ReportType reportType = (ReportType) reportTypeCombo.getSelectedItem();
        if (reportType == null) return;

        // Validate date range for applicable reports
        if (needsDateRange(reportType)) {
            try {
                LocalDate fromDate = LocalDate.parse(fromDateField.getText());
                LocalDate toDate = LocalDate.parse(toDateField.getText());

                if (toDate.isBefore(fromDate)) {
                    JOptionPane.showMessageDialog(this,
                            "End date cannot be before start date.",
                            "Invalid Date Range", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid date format. Please use YYYY-MM-DD format.",
                        "Date Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Generate report in background
        generateReportButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Generating report...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                generateReport(reportType);
                return null;
            }

            @Override
            protected void done() {
                generateReportButton.setEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                statusLabel.setText("Report generated successfully");
                exportCsvButton.setEnabled(true);
                printReportButton.setEnabled(true);

                try {
                    get(); // Check for exceptions
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error generating report", ex);
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                            "Error generating report: " + ex.getMessage(),
                            "Report Generation Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Report generation failed");
                }
            }
        };

        worker.execute();
    }

    private boolean needsDateRange(ReportType reportType) {
        return reportType == ReportType.ATTENDANCE_SUMMARY ||
                reportType == ReportType.PAYROLL_SUMMARY ||
                reportType == ReportType.LATE_ATTENDANCE;
    }

    private void generateReport(ReportType reportType) throws Exception {
        switch (reportType) {
            case EMPLOYEE_SUMMARY:
                generateEmployeeSummaryReport();
                break;
            case ATTENDANCE_SUMMARY:
                generateAttendanceSummaryReport();
                break;
            case LEAVE_SUMMARY:
                generateLeaveSummaryReport();
                break;
            case PAYROLL_SUMMARY:
                generatePayrollSummaryReport();
                break;
            case LATE_ATTENDANCE:
                generateLateAttendanceReport();
                break;
            case DEPARTMENT_HEADCOUNT:
                generateDepartmentHeadcountReport();
                break;
            case SALARY_ANALYSIS:
                generateSalaryAnalysisReport();
                break;
        }
    }

    private void generateEmployeeSummaryReport() throws Exception {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Loading employee data..."));

        List<Employee> employees = employeeService.getAllEmployees();

        // Apply filters
        String selectedStatus = (String) statusFilter.getSelectedItem();
        if (!"All Status".equals(selectedStatus)) {
            employees = employees.stream()
                    .filter(emp -> selectedStatus.equals(emp.getStatus()))
                    .collect(Collectors.toList());
        }

        List<Employee> finalEmployees = employees;
        SwingUtilities.invokeLater(() -> {
            // Setup table columns
            String[] columns = {"ID", "Name", "Position", "Status", "Department", "Phone", "SSS", "PhilHealth"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            // Add data
            for (Employee emp : finalEmployees) {
                Object[] row = {
                        emp.getId(),
                        emp.getFullName(),
                        emp.getPosition() != null ? emp.getPosition() : "N/A",
                        emp.getStatus() != null ? emp.getStatus() : "N/A",
                        getDepartmentFromPosition(emp.getPosition()),
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "N/A",
                        emp.getSssNumber() != null ? emp.getSssNumber() : "N/A",
                        emp.getPhilhealthNumber() != null ? emp.getPhilhealthNumber() : "N/A"
                };
                tableModel.addRow(row);
            }

            // Generate summary
            Map<String, Long> statusCounts = finalEmployees.stream()
                    .collect(Collectors.groupingBy(
                            emp -> emp.getStatus() != null ? emp.getStatus() : "Unknown",
                            Collectors.counting()));

            Map<String, Long> departmentCounts = finalEmployees.stream()
                    .collect(Collectors.groupingBy(
                            emp -> getDepartmentFromPosition(emp.getPosition()),
                            Collectors.counting()));

            StringBuilder summary = new StringBuilder();
            summary.append("EMPLOYEE SUMMARY REPORT\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");
            summary.append("Total Employees: ").append(finalEmployees.size()).append("\n\n");

            summary.append("By Status:\n");
            statusCounts.forEach((status, count) ->
                    summary.append("  ").append(status).append(": ").append(count).append("\n"));

            summary.append("\nBy Department:\n");
            departmentCounts.forEach((dept, count) ->
                    summary.append("  ").append(dept).append(": ").append(count).append("\n"));

            summaryArea.setText(summary.toString());
        });
    }

    private void generateAttendanceSummaryReport() throws Exception {
        LocalDate fromDate = LocalDate.parse(fromDateField.getText());
        LocalDate toDate = LocalDate.parse(toDateField.getText());

        SwingUtilities.invokeLater(() -> statusLabel.setText("Loading attendance data..."));

        List<Employee> employees = employeeService.getAllEmployees();

        SwingUtilities.invokeLater(() -> {
            String[] columns = {"Employee ID", "Name", "Days Present", "Total Hours", "Late Days", "Undertime Days"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            int totalEmployees = employees.size();
            int totalDaysPresent = 0;
            double totalHours = 0.0;
            int totalLateDays = 0;
            int totalUndertimeDays = 0;

            for (Employee emp : employees) {
                List<Attendance> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(
                        emp.getId(), fromDate, toDate);

                int daysPresent = 0;
                double hours = 0.0;
                int lateDays = 0;
                int undertimeDays = 0;

                for (Attendance att : attendanceList) {
                    if (att.getLogIn() != null) {
                        daysPresent++;
                        hours += att.getWorkHours();

                        if (att.isLate()) lateDays++;
                        if (att.hasUndertime()) undertimeDays++;
                    }
                }

                Object[] row = {
                        emp.getId(),
                        emp.getFullName(),
                        daysPresent,
                        String.format("%.2f", hours),
                        lateDays,
                        undertimeDays
                };
                tableModel.addRow(row);

                totalDaysPresent += daysPresent;
                totalHours += hours;
                totalLateDays += lateDays;
                totalUndertimeDays += undertimeDays;
            }

            StringBuilder summary = new StringBuilder();
            summary.append("ATTENDANCE SUMMARY REPORT\n");
            summary.append("Period: ").append(fromDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .append(" to ").append(toDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Total Employees: ").append(totalEmployees).append("\n");
            summary.append("Total Days Present: ").append(totalDaysPresent).append("\n");
            summary.append("Total Working Hours: ").append(String.format("%.2f", totalHours)).append("\n");
            summary.append("Total Late Instances: ").append(totalLateDays).append("\n");
            summary.append("Total Undertime Instances: ").append(totalUndertimeDays).append("\n\n");

            if (totalEmployees > 0) {
                summary.append("Average Days Present per Employee: ").append(String.format("%.1f", (double) totalDaysPresent / totalEmployees)).append("\n");
                summary.append("Average Hours per Employee: ").append(String.format("%.2f", totalHours / totalEmployees)).append("\n");
            }

            summaryArea.setText(summary.toString());
        });
    }

    private void generateLeaveSummaryReport() throws Exception {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Loading leave data..."));

        List<LeaveRequest> allLeaves = leaveService.getLeaveRequestsByStatus("Pending");
        allLeaves.addAll(leaveService.getLeaveRequestsByStatus("Approved"));
        allLeaves.addAll(leaveService.getLeaveRequestsByStatus("Rejected"));

        SwingUtilities.invokeLater(() -> {
            String[] columns = {"Employee ID", "Employee Name", "Leave Type", "Start Date", "End Date", "Days", "Status"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            for (LeaveRequest leave : allLeaves) {
                String employeeName = getEmployeeName(leave.getEmployeeId());

                Object[] row = {
                        leave.getEmployeeId(),
                        employeeName,
                        leave.getLeaveType(),
                        leave.getStartDateAsLocalDate().toString(),
                        leave.getEndDateAsLocalDate().toString(),
                        leave.getLeaveDays(),
                        leave.getStatus()
                };
                tableModel.addRow(row);
            }

            // Generate summary
            Map<String, Long> statusCounts = allLeaves.stream()
                    .collect(Collectors.groupingBy(LeaveRequest::getStatus, Collectors.counting()));

            Map<String, Long> typeCounts = allLeaves.stream()
                    .collect(Collectors.groupingBy(LeaveRequest::getLeaveType, Collectors.counting()));

            long totalLeaveDays = allLeaves.stream()
                    .filter(LeaveRequest::isApproved)
                    .mapToLong(LeaveRequest::getLeaveDays)
                    .sum();

            StringBuilder summary = new StringBuilder();
            summary.append("LEAVE SUMMARY REPORT\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Total Leave Requests: ").append(allLeaves.size()).append("\n");
            summary.append("Total Approved Leave Days: ").append(totalLeaveDays).append("\n\n");

            summary.append("By Status:\n");
            statusCounts.forEach((status, count) ->
                    summary.append("  ").append(status).append(": ").append(count).append("\n"));

            summary.append("\nBy Type:\n");
            typeCounts.forEach((type, count) ->
                    summary.append("  ").append(type).append(": ").append(count).append("\n"));

            summaryArea.setText(summary.toString());
        });
    }

    private void generatePayrollSummaryReport() throws Exception {
        LocalDate fromDate = LocalDate.parse(fromDateField.getText());
        LocalDate toDate = LocalDate.parse(toDateField.getText());

        SwingUtilities.invokeLater(() -> statusLabel.setText("Calculating payroll data..."));

        List<Employee> employees = employeeService.getAllEmployees();

        SwingUtilities.invokeLater(() -> {
            String[] columns = {"Employee ID", "Name", "Basic Pay", "Gross Pay", "Deductions", "Net Pay"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            double totalBasicPay = 0.0;
            double totalGrossPay = 0.0;
            double totalDeductions = 0.0;
            double totalNetPay = 0.0;
            int processedEmployees = 0;

            for (Employee emp : employees) {
                try {
                    PayrollCalculator.PayrollData payrollData = payrollCalculator.calculatePayroll(
                            emp.getId(), fromDate, toDate);

                    Object[] row = {
                            emp.getId(),
                            emp.getFullName(),
                            String.format("₱%,.2f", payrollData.getBasicPay()),
                            String.format("₱%,.2f", payrollData.getGrossPay()),
                            String.format("₱%,.2f", payrollData.getTotalDeductions()),
                            String.format("₱%,.2f", payrollData.getNetPay())
                    };
                    tableModel.addRow(row);

                    totalBasicPay += payrollData.getBasicPay();
                    totalGrossPay += payrollData.getGrossPay();
                    totalDeductions += payrollData.getTotalDeductions();
                    totalNetPay += payrollData.getNetPay();
                    processedEmployees++;

                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error calculating payroll for employee " + emp.getId(), ex);

                    Object[] row = {
                            emp.getId(),
                            emp.getFullName(),
                            "Error",
                            "Error",
                            "Error",
                            "Error"
                    };
                    tableModel.addRow(row);
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("PAYROLL SUMMARY REPORT\n");
            summary.append("Period: ").append(fromDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .append(" to ").append(toDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Total Employees Processed: ").append(processedEmployees).append("\n");
            summary.append("Total Basic Pay: ").append(String.format("₱%,.2f", totalBasicPay)).append("\n");
            summary.append("Total Gross Pay: ").append(String.format("₱%,.2f", totalGrossPay)).append("\n");
            summary.append("Total Deductions: ").append(String.format("₱%,.2f", totalDeductions)).append("\n");
            summary.append("Total Net Pay: ").append(String.format("₱%,.2f", totalNetPay)).append("\n\n");

            if (processedEmployees > 0) {
                summary.append("Average Net Pay: ").append(String.format("₱%,.2f", totalNetPay / processedEmployees)).append("\n");
            }

            summaryArea.setText(summary.toString());
        });
    }

    private void generateLateAttendanceReport() throws Exception {
        LocalDate fromDate = LocalDate.parse(fromDateField.getText());
        LocalDate toDate = LocalDate.parse(toDateField.getText());

        SwingUtilities.invokeLater(() -> statusLabel.setText("Analyzing late attendance..."));

        List<Employee> employees = employeeService.getAllEmployees();

        SwingUtilities.invokeLater(() -> {
            String[] columns = {"Employee ID", "Name", "Position", "Late Days", "Total Days", "Late Percentage"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            int totalLateInstances = 0;
            int totalEmployeesWithLates = 0;

            for (Employee emp : employees) {
                List<Attendance> attendanceList = attendanceService.getAttendanceByEmployeeAndDateRange(
                        emp.getId(), fromDate, toDate);

                int lateDays = 0;
                int totalDays = 0;

                for (Attendance att : attendanceList) {
                    if (att.getLogIn() != null) {
                        totalDays++;
                        if (att.isLate()) {
                            lateDays++;
                        }
                    }
                }

                if (lateDays > 0) {
                    double latePercentage = totalDays > 0 ? (lateDays * 100.0 / totalDays) : 0.0;

                    Object[] row = {
                            emp.getId(),
                            emp.getFullName(),
                            emp.getPosition(),
                            lateDays,
                            totalDays,
                            String.format("%.1f%%", latePercentage)
                    };
                    tableModel.addRow(row);

                    totalLateInstances += lateDays;
                    totalEmployeesWithLates++;
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("LATE ATTENDANCE REPORT\n");
            summary.append("Period: ").append(fromDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .append(" to ").append(toDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Employees with Late Arrivals: ").append(totalEmployeesWithLates).append("\n");
            summary.append("Total Late Instances: ").append(totalLateInstances).append("\n");

            if (totalEmployeesWithLates > 0) {
                summary.append("Average Late Days per Employee: ").append(String.format("%.1f", (double) totalLateInstances / totalEmployeesWithLates)).append("\n");
            }

            summaryArea.setText(summary.toString());
        });
    }

    private void generateDepartmentHeadcountReport() throws Exception {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Analyzing department headcount..."));

        List<Employee> employees = employeeService.getAllEmployees();

        SwingUtilities.invokeLater(() -> {
            Map<String, List<Employee>> departmentMap = employees.stream()
                    .collect(Collectors.groupingBy(emp -> getDepartmentFromPosition(emp.getPosition())));

            String[] columns = {"Department", "Regular", "Probationary", "Total"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            for (Map.Entry<String, List<Employee>> entry : departmentMap.entrySet()) {
                String department = entry.getKey();
                List<Employee> deptEmployees = entry.getValue();

                long regular = deptEmployees.stream().filter(emp -> "Regular".equals(emp.getStatus())).count();
                long probationary = deptEmployees.stream().filter(emp -> "Probationary".equals(emp.getStatus())).count();

                Object[] row = {
                        department,
                        regular,
                        probationary,
                        deptEmployees.size()
                };
                tableModel.addRow(row);
            }

            StringBuilder summary = new StringBuilder();
            summary.append("DEPARTMENT HEADCOUNT REPORT\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Total Employees: ").append(employees.size()).append("\n");
            summary.append("Total Departments: ").append(departmentMap.size()).append("\n\n");

            summary.append("Department Breakdown:\n");
            departmentMap.forEach((dept, empList) ->
                    summary.append("  ").append(dept).append(": ").append(empList.size()).append(" employees\n"));

            summaryArea.setText(summary.toString());
        });
    }

    private void generateSalaryAnalysisReport() throws Exception {
        SwingUtilities.invokeLater(() -> statusLabel.setText("Analyzing salary data..."));

        List<Employee> employees = employeeService.getAllEmployees();

        SwingUtilities.invokeLater(() -> {
            String[] columns = {"Position", "Count", "Min Salary", "Max Salary", "Avg Salary"};
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            Map<String, List<Employee>> positionMap = employees.stream()
                    .collect(Collectors.groupingBy(emp -> emp.getPosition() != null ? emp.getPosition() : "Unknown"));

            double totalSalaries = 0.0;
            double minSalary = Double.MAX_VALUE;
            double maxSalary = Double.MIN_VALUE;

            for (Map.Entry<String, List<Employee>> entry : positionMap.entrySet()) {
                String position = entry.getKey();
                List<Employee> positionEmployees = entry.getValue();

                double posMin = positionEmployees.stream().mapToDouble(Employee::getBasicSalary).min().orElse(0.0);
                double posMax = positionEmployees.stream().mapToDouble(Employee::getBasicSalary).max().orElse(0.0);
                double posAvg = positionEmployees.stream().mapToDouble(Employee::getBasicSalary).average().orElse(0.0);

                Object[] row = {
                        position,
                        positionEmployees.size(),
                        String.format("₱%,.2f", posMin),
                        String.format("₱%,.2f", posMax),
                        String.format("₱%,.2f", posAvg)
                };
                tableModel.addRow(row);

                totalSalaries += positionEmployees.stream().mapToDouble(Employee::getBasicSalary).sum();
                minSalary = Math.min(minSalary, posMin);
                maxSalary = Math.max(maxSalary, posMax);
            }

            double avgSalary = employees.size() > 0 ? totalSalaries / employees.size() : 0.0;

            StringBuilder summary = new StringBuilder();
            summary.append("SALARY ANALYSIS REPORT\n");
            summary.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n\n");

            summary.append("Total Employees: ").append(employees.size()).append("\n");
            summary.append("Total Monthly Payroll: ").append(String.format("₱%,.2f", totalSalaries)).append("\n");
            summary.append("Average Salary: ").append(String.format("₱%,.2f", avgSalary)).append("\n");
            summary.append("Minimum Salary: ").append(String.format("₱%,.2f", minSalary)).append("\n");
            summary.append("Maximum Salary: ").append(String.format("₱%,.2f", maxSalary)).append("\n\n");

            summary.append("Annual Payroll Cost: ").append(String.format("₱%,.2f", totalSalaries * 12)).append("\n");

            summaryArea.setText(summary.toString());
        });
    }

    private void handleExportCsv(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data to export. Please generate a report first.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("report_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                exportToCsv(fileChooser.getSelectedFile());
                JOptionPane.showMessageDialog(this,
                        "Report exported successfully!",
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error exporting to CSV", ex);
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write headers
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.write(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");

            // Write data
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    String stringValue = value != null ? value.toString() : "";

                    // Escape commas and quotes
                    if (stringValue.contains(",") || stringValue.contains("\"")) {
                        stringValue = "\"" + stringValue.replace("\"", "\"\"") + "\"";
                    }

                    writer.write(stringValue);
                    if (col < tableModel.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        }
    }

    private void handlePrintReport(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data to print. Please generate a report first.",
                    "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            reportTable.print();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error printing report", ex);
            JOptionPane.showMessageDialog(this,
                    "Error printing report: " + ex.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFilterData() {
        try {
            // Load unique positions/departments for filtering
            List<Employee> employees = employeeService.getAllEmployees();

            java.util.Set<String> departments = employees.stream()
                    .map(emp -> getDepartmentFromPosition(emp.getPosition()))
                    .collect(Collectors.toSet());

            departmentFilter.removeAllItems();
            departmentFilter.addItem("All Departments");
            departments.forEach(departmentFilter::addItem);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading filter data", e);
        }
    }

    private String getDepartmentFromPosition(String position) {
        if (position == null) return "Unknown";

        String pos = position.toLowerCase();
        if (pos.contains("hr") || pos.contains("human resource")) return "Human Resources";
        if (pos.contains("accounting") || pos.contains("payroll")) return "Accounting";
        if (pos.contains("marketing") || pos.contains("sales")) return "Marketing";
        if (pos.contains("it") || pos.contains("operations")) return "IT Operations";
        if (pos.contains("ceo") || pos.contains("executive")) return "Executive";
        if (pos.contains("chief")) return "Executive";

        return "General";
    }

    private String getEmployeeName(int employeeId) {
        try {
            Employee emp = employeeService.getEmployeeById(employeeId);
            return emp != null ? emp.getFullName() : "Unknown Employee";
        } catch (Exception e) {
            return "Employee " + employeeId;
        }
    }
}