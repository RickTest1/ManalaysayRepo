package gui;

import model.Employee;
import service.EmployeeService;
import service.JasperPayslipService;
import service.PayrollCalculator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Employee Self-Service Panel
 * Allows employees to view their personal information and generate payslips
 */
public class EmployeeSelfServicePanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(EmployeeSelfServicePanel.class.getName());

    private final Employee employee;
    private final EmployeeService employeeService;
    private final PayrollCalculator payrollCalculator;
    private JasperPayslipService jasperService;

    // Form fields
    private JTextField employeeIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField birthdayField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField positionField;
    private JTextField statusField;
    private JTextField salaryField;
    private JTextField sssField;
    private JTextField philhealthField;
    private JTextField tinField;
    private JTextField pagibigField;
    private JTextField supervisorField;

    // Payslip generation components
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<JasperPayslipService.ExportFormat> formatComboBox;
    private JButton generatePayslipButton;
    private JButton previewPayrollButton;
    private JTextArea payrollPreviewArea;

    public EmployeeSelfServicePanel(Employee employee) {
        this.employee = employee;
        this.employeeService = new EmployeeService();
        this.payrollCalculator = new PayrollCalculator();

        try {
            this.jasperService = new JasperPayslipService();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "JasperReports not available", e);
            this.jasperService = null;
        }

        initializeComponents();
        setupLayout();
        populateEmployeeData();
        setupEventHandlers();
    }

    private void initializeComponents() {
        // Personal information fields (read-only)
        employeeIdField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        birthdayField = new JTextField(20);
        addressField = new JTextField(30);
        phoneField = new JTextField(20);
        positionField = new JTextField(20);
        statusField = new JTextField(20);
        salaryField = new JTextField(20);
        sssField = new JTextField(20);
        philhealthField = new JTextField(20);
        tinField = new JTextField(20);
        pagibigField = new JTextField(20);
        supervisorField = new JTextField(20);

        // Make all fields non-editable (read-only)
        makeFieldsReadOnly();

        // Payslip generation components
        monthComboBox = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });

        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 2; year <= currentYear; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentYear);

        formatComboBox = new JComboBox<>(JasperPayslipService.ExportFormat.values());

        generatePayslipButton = new JButton("Generate Payslip");
        previewPayrollButton = new JButton("Preview Payroll Data");

        payrollPreviewArea = new JTextArea(10, 50);
        payrollPreviewArea.setEditable(false);
        payrollPreviewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private void makeFieldsReadOnly() {
        JTextField[] fields = {
                employeeIdField, firstNameField, lastNameField, birthdayField,
                addressField, phoneField, positionField, statusField, salaryField,
                sssField, philhealthField, tinField, pagibigField, supervisorField
        };

        for (JTextField field : fields) {
            field.setEditable(false);
            field.setBackground(Color.WHITE);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Personal information panel
        JPanel personalInfoPanel = createPersonalInfoPanel();

        // Payslip generation panel
        JPanel payslipPanel = createPayslipPanel();

        // Combine in a split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, personalInfoPanel, payslipPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Personal Information"));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Employee ID
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employeeIdField, gbc);

        // Name
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        // Birthday
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 1;
        formPanel.add(birthdayField, gbc);

        // Contact Information
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(addressField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        // Employment Information
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 1;
        formPanel.add(positionField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(statusField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        formPanel.add(salaryField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Supervisor:"), gbc);
        gbc.gridx = 1;
        formPanel.add(supervisorField, gbc);

        // Government IDs
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("SSS Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(sssField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("PhilHealth:"), gbc);
        gbc.gridx = 1;
        formPanel.add(philhealthField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("TIN Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(tinField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Pag-IBIG:"), gbc);
        gbc.gridx = 1;
        formPanel.add(pagibigField, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPayslipPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Payslip Generation"));

        // Controls panel
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        controlsPanel.add(new JLabel("Month:"), gbc);
        gbc.gridx = 1;
        controlsPanel.add(monthComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlsPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        controlsPanel.add(yearComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        controlsPanel.add(new JLabel("Format:"), gbc);
        gbc.gridx = 1;
        controlsPanel.add(formatComboBox, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(previewPayrollButton);
        if (jasperService != null) {
            buttonsPanel.add(generatePayslipButton);
        } else {
            JLabel warningLabel = new JLabel("JasperReports not available");
            warningLabel.setForeground(Color.RED);
            buttonsPanel.add(warningLabel);
        }

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        controlsPanel.add(buttonsPanel, gbc);

        panel.add(controlsPanel, BorderLayout.NORTH);

        // Preview area
        JScrollPane previewScrollPane = new JScrollPane(payrollPreviewArea);
        previewScrollPane.setBorder(new TitledBorder("Payroll Preview"));
        panel.add(previewScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void populateEmployeeData() {
        try {
            // Get fresh employee data with position details
            Employee freshEmployee = employeeService.getEmployeeById(employee.getId());
            if (freshEmployee == null) {
                freshEmployee = employee;
            }

            employeeIdField.setText(String.valueOf(freshEmployee.getId()));
            firstNameField.setText(freshEmployee.getFirstName() != null ? freshEmployee.getFirstName() : "");
            lastNameField.setText(freshEmployee.getLastName() != null ? freshEmployee.getLastName() : "");
            birthdayField.setText(freshEmployee.getBirthday() != null ? freshEmployee.getBirthday().toString() : "");
            addressField.setText(freshEmployee.getAddress() != null ? freshEmployee.getAddress() : "");
            phoneField.setText(freshEmployee.getPhoneNumber() != null ? freshEmployee.getPhoneNumber() : "");
            positionField.setText(freshEmployee.getPosition() != null ? freshEmployee.getPosition() : "");
            statusField.setText(freshEmployee.getStatus() != null ? freshEmployee.getStatus() : "");
            salaryField.setText(String.format("₱%.2f", freshEmployee.getBasicSalary()));
            supervisorField.setText(freshEmployee.getImmediateSupervisor() != null ? freshEmployee.getImmediateSupervisor() : "");
            sssField.setText(freshEmployee.getSssNumber() != null ? freshEmployee.getSssNumber() : "");
            philhealthField.setText(freshEmployee.getPhilhealthNumber() != null ? freshEmployee.getPhilhealthNumber() : "");
            tinField.setText(freshEmployee.getTinNumber() != null ? freshEmployee.getTinNumber() : "");
            pagibigField.setText(freshEmployee.getPagibigNumber() != null ? freshEmployee.getPagibigNumber() : "");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error populating employee data", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading employee data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupEventHandlers() {
        previewPayrollButton.addActionListener(this::handlePreviewPayroll);

        if (jasperService != null) {
            generatePayslipButton.addActionListener(this::handleGeneratePayslip);
        }
    }

    private void handlePreviewPayroll(ActionEvent e) {
        try {
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();

            LocalDate periodStart = LocalDate.of(year, month, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            // Calculate payroll data
            PayrollCalculator.PayrollData payrollData = payrollCalculator.calculatePayroll(
                    employee.getId(), periodStart, periodEnd);

            // Display the data in the preview area
            displayPayrollPreview(payrollData);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error previewing payroll", ex);
            JOptionPane.showMessageDialog(this,
                    "Error generating payroll preview: " + ex.getMessage(),
                    "Preview Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayPayrollPreview(PayrollCalculator.PayrollData data) {
        StringBuilder preview = new StringBuilder();
        preview.append("PAYROLL CALCULATION PREVIEW\n");
        preview.append("=========================================\n\n");

        preview.append(String.format("Employee: %s (ID: %d)\n", employee.getFullName(), data.getEmployeeId()));
        preview.append(String.format("Period: %s to %s\n\n",
                data.getPeriodStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                data.getPeriodEnd().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));

        preview.append("EARNINGS:\n");
        preview.append(String.format("  Monthly Rate:     ₱%,10.2f\n", data.getMonthlyRate()));
        preview.append(String.format("  Daily Rate:       ₱%,10.2f\n", data.getDailyRate()));
        preview.append(String.format("  Days Worked:      %10d\n", data.getDaysWorked()));
        preview.append(String.format("  Basic Pay:        ₱%,10.2f\n", data.getBasicPay()));
        preview.append(String.format("  Rice Subsidy:     ₱%,10.2f\n", data.getRiceSubsidy()));
        preview.append(String.format("  Phone Allowance:  ₱%,10.2f\n", data.getPhoneAllowance()));
        preview.append(String.format("  Clothing Allow.:  ₱%,10.2f\n", data.getClothingAllowance()));
        preview.append(String.format("  Total Allowances: ₱%,10.2f\n", data.getTotalAllowances()));
        preview.append(String.format("  GROSS PAY:        ₱%,10.2f\n\n", data.getGrossPay()));

        preview.append("DEDUCTIONS:\n");
        preview.append(String.format("  Late Deduction:   ₱%,10.2f\n", data.getLateDeduction()));
        preview.append(String.format("  Undertime Deduc.: ₱%,10.2f\n", data.getUndertimeDeduction()));
        preview.append(String.format("  Unpaid Leave:     ₱%,10.2f\n", data.getUnpaidLeaveDeduction()));
        preview.append(String.format("  SSS:              ₱%,10.2f\n", data.getSss()));
        preview.append(String.format("  PhilHealth:       ₱%,10.2f\n", data.getPhilhealth()));
        preview.append(String.format("  Pag-IBIG:         ₱%,10.2f\n", data.getPagibig()));
        preview.append(String.format("  Withholding Tax:  ₱%,10.2f\n", data.getTax()));
        preview.append(String.format("  Total Deductions: ₱%,10.2f\n\n", data.getTotalDeductions()));

        preview.append("SUMMARY:\n");
        preview.append(String.format("  Gross Pay:        ₱%,10.2f\n", data.getGrossPay()));
        preview.append(String.format("  Total Deductions: ₱%,10.2f\n", data.getTotalDeductions()));
        preview.append("  ").append("-".repeat(30)).append("\n");
        preview.append(String.format("  NET PAY:          ₱%,10.2f\n", data.getNetPay()));

        payrollPreviewArea.setText(preview.toString());
        payrollPreviewArea.setCaretPosition(0);
    }

    private void handleGeneratePayslip(ActionEvent e) {
        if (jasperService == null) {
            JOptionPane.showMessageDialog(this,
                    "JasperReports is not available. Cannot generate payslips.",
                    "Service Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();
            JasperPayslipService.ExportFormat format =
                    (JasperPayslipService.ExportFormat) formatComboBox.getSelectedItem();

            LocalDate periodStart = LocalDate.of(year, month, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            // Choose save location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Payslip");
            fileChooser.setSelectedFile(new File(String.format("Payslip_%s_%d_%02d.%s",
                    employee.getLastName(), year, month, format.getExtension())));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
                generatePayslipFile(periodStart, periodEnd, format, saveFile);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error generating payslip", ex);
            JOptionPane.showMessageDialog(this,
                    "Error generating payslip: " + ex.getMessage(),
                    "Generation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePayslipFile(LocalDate periodStart, LocalDate periodEnd,
                                     JasperPayslipService.ExportFormat format, File saveFile) {

        // Show progress dialog
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Generating Payslip", true);
        progressDialog.add(new JLabel("Generating payslip, please wait..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                byte[] payslipData = jasperService.generatePayslipReport(
                        employee.getId(), periodStart, periodEnd, format);

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
                    fos.write(payslipData);
                }

                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                try {
                    get(); // Check for exceptions

                    int option = JOptionPane.showConfirmDialog(
                            EmployeeSelfServicePanel.this,
                            "Payslip generated successfully!\nWould you like to open it now?",
                            "Success",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);

                    if (option == JOptionPane.YES_OPTION) {
                        Desktop.getDesktop().open(saveFile);
                    }

                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error in payslip generation", ex);
                    JOptionPane.showMessageDialog(EmployeeSelfServicePanel.this,
                            "Error generating payslip: " + ex.getMessage(),
                            "Generation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }
}