package gui;

import model.Employee;
import service.EmployeeService;
import service.PayrollCalculator;
import service.JasperPayslipService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Payroll Management Panel for HR users
 * Allows bulk generation of payslips and payroll calculations
 */
public class PayrollManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PayrollManagementPanel.class.getName());

    private final EmployeeService employeeService;
    private final PayrollCalculator payrollCalculator;
    private JasperPayslipService jasperService;

    // Employee selection components
    private JTable employeeTable;
    private DefaultTableModel employeeTableModel;
    private JButton selectAllButton;
    private JButton selectNoneButton;
    private JTextField searchField;

    // Payroll period components
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;

    // Generation options
    private JComboBox<JasperPayslipService.ExportFormat> formatComboBox;
    private JCheckBox createFolderCheckBox;
    private JTextField outputDirectoryField;
    private JButton browseDirectoryButton;

    // Action buttons
    private JButton calculatePayrollButton;
    private JButton generatePayslipsButton;
    private JButton previewSampleButton;

    // Progress components
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea logArea;

    // Results table
    private JTable resultsTable;
    private DefaultTableModel resultsTableModel;

    public PayrollManagementPanel() {
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
        setupEventHandlers();
        loadEmployees();
    }

    private void initializeComponents() {
        // Employee selection table
        String[] employeeColumns = {"Select", "ID", "Name", "Position", "Status"};
        employeeTableModel = new DefaultTableModel(employeeColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only checkbox column is editable
            }
        };

        employeeTable = new JTable(employeeTableModel);
        employeeTable.getColumnModel().getColumn(0).setMaxWidth(60);
        employeeTable.getColumnModel().getColumn(1).setMaxWidth(80);

        selectAllButton = new JButton("Select All");
        selectNoneButton = new JButton("Select None");
        searchField = new JTextField(20);

        // Payroll period
        monthComboBox = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        });

        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 1; year <= currentYear; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentYear);

        // Set default to current month
        int currentMonth = LocalDate.now().getMonthValue() - 1;
        monthComboBox.setSelectedIndex(currentMonth);

        // Generation options
        formatComboBox = new JComboBox<>(JasperPayslipService.ExportFormat.values());
        createFolderCheckBox = new JCheckBox("Create dated folder", true);
        outputDirectoryField = new JTextField(30);
        outputDirectoryField.setText(System.getProperty("user.home") + File.separator + "Payslips");
        browseDirectoryButton = new JButton("Browse...");

        // Action buttons
        calculatePayrollButton = new JButton("Calculate Payroll");
        generatePayslipsButton = new JButton("Generate Payslips");
        previewSampleButton = new JButton("Preview Sample");

        calculatePayrollButton.setBackground(new Color(33, 150, 243));
        calculatePayrollButton.setForeground(Color.WHITE);
        generatePayslipsButton.setBackground(new Color(76, 175, 80));
        generatePayslipsButton.setForeground(Color.WHITE);

        if (jasperService == null) {
            generatePayslipsButton.setEnabled(false);
            previewSampleButton.setEnabled(false);
        }

        // Progress components
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Ready");
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        // Results table
        String[] resultColumns = {"Employee ID", "Name", "Basic Pay", "Gross Pay", "Deductions", "Net Pay", "Status"};
        resultsTableModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(resultsTableModel);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create tabbed pane for better organization
        JTabbedPane tabbedPane = new JTabbedPane();

        // Employee Selection Tab
        JPanel selectionPanel = createEmployeeSelectionPanel();
        tabbedPane.addTab("Employee Selection", selectionPanel);

        // Payroll Generation Tab
        JPanel generationPanel = createPayrollGenerationPanel();
        tabbedPane.addTab("Payroll Generation", generationPanel);

        // Results Tab
        JPanel resultsPanel = createResultsPanel();
        tabbedPane.addTab("Results", resultsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Status panel at bottom
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createEmployeeSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Top panel - search and selection controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(selectAllButton);
        topPanel.add(selectNoneButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // Center - employee table
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(new TitledBorder("Select Employees for Payroll Processing"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPayrollGenerationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Period selection panel
        JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodPanel.setBorder(new TitledBorder("Payroll Period"));

        periodPanel.add(new JLabel("Month:"));
        periodPanel.add(monthComboBox);
        periodPanel.add(Box.createHorizontalStrut(10));
        periodPanel.add(new JLabel("Year:"));
        periodPanel.add(yearComboBox);

        // Output options panel
        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(new TitledBorder("Output Options"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        outputPanel.add(new JLabel("Format:"), gbc);
        gbc.gridx = 1;
        outputPanel.add(formatComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        outputPanel.add(new JLabel("Output Directory:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        outputPanel.add(outputDirectoryField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        outputPanel.add(browseDirectoryButton, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        outputPanel.add(createFolderCheckBox, gbc);

        // Action buttons panel
        JPanel actionsPanel = new JPanel(new FlowLayout());
        actionsPanel.setBorder(new TitledBorder("Actions"));

        actionsPanel.add(calculatePayrollButton);
        actionsPanel.add(previewSampleButton);
        actionsPanel.add(generatePayslipsButton);

        // Combine panels
        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(periodPanel, BorderLayout.NORTH);
        combinedPanel.add(outputPanel, BorderLayout.CENTER);
        combinedPanel.add(actionsPanel, BorderLayout.SOUTH);

        panel.add(combinedPanel, BorderLayout.NORTH);

        // Log area
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(new TitledBorder("Processing Log"));
        panel.add(logScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(new TitledBorder("Payroll Calculation Results"));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        // Employee selection
        selectAllButton.addActionListener(this::handleSelectAll);
        selectNoneButton.addActionListener(this::handleSelectNone);

        // Search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterEmployees(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterEmployees(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterEmployees(); }
        });

        // Directory browsing
        browseDirectoryButton.addActionListener(this::handleBrowseDirectory);

        // Actions
        calculatePayrollButton.addActionListener(this::handleCalculatePayroll);
        generatePayslipsButton.addActionListener(this::handleGeneratePayslips);
        previewSampleButton.addActionListener(this::handlePreviewSample);
    }

    private void handleSelectAll(ActionEvent e) {
        for (int i = 0; i < employeeTableModel.getRowCount(); i++) {
            employeeTableModel.setValueAt(true, i, 0);
        }
    }

    private void handleSelectNone(ActionEvent e) {
        for (int i = 0; i < employeeTableModel.getRowCount(); i++) {
            employeeTableModel.setValueAt(false, i, 0);
        }
    }

    private void filterEmployees() {
        // This is a simple implementation - you might want to use TableRowSorter for better filtering
        String searchText = searchField.getText().toLowerCase().trim();
        // For now, just reload and show message if search is not empty
        if (!searchText.isEmpty()) {
            statusLabel.setText("Filtering not fully implemented - showing all employees");
        } else {
            statusLabel.setText("Ready");
        }
    }

    private void handleBrowseDirectory(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(outputDirectoryField.getText()));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            outputDirectoryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void handleCalculatePayroll(ActionEvent e) {
        List<Integer> selectedEmployeeIds = getSelectedEmployeeIds();

        if (selectedEmployeeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one employee for payroll calculation.",
                    "No Employees Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear previous results
        resultsTableModel.setRowCount(0);
        logArea.setText("");

        // Set up progress
        progressBar.setMaximum(selectedEmployeeIds.size());
        progressBar.setValue(0);
        statusLabel.setText("Calculating payroll...");

        // Disable buttons during processing
        setButtonsEnabled(false);

        // Process in background
        CompletableFuture.runAsync(() -> {
            calculatePayrollForEmployees(selectedEmployeeIds);
        });
    }

    private void calculatePayrollForEmployees(List<Integer> employeeIds) {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();

        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        int processed = 0;

        for (int employeeId : employeeIds) {
            try {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Calculating payroll for Employee " + employeeId + "...\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });

                PayrollCalculator.PayrollData payrollData = payrollCalculator.calculatePayroll(
                        employeeId, periodStart, periodEnd);

                // Get employee name
                Employee employee = employeeService.getEmployeeById(employeeId);
                String employeeName = employee != null ? employee.getFullName() : "Unknown";

                // Add to results table
                SwingUtilities.invokeLater(() -> {
                    Object[] rowData = {
                            employeeId,
                            employeeName,
                            String.format("₱%,.2f", payrollData.getBasicPay()),
                            String.format("₱%,.2f", payrollData.getGrossPay()),
                            String.format("₱%,.2f", payrollData.getTotalDeductions()),
                            String.format("₱%,.2f", payrollData.getNetPay()),
                            "Success"
                    };
                    resultsTableModel.addRow(rowData);

                    logArea.append("✓ Completed for Employee " + employeeId +
                            " - Net Pay: ₱" + String.format("%,.2f", payrollData.getNetPay()) + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error calculating payroll for employee " + employeeId, ex);

                SwingUtilities.invokeLater(() -> {
                    Object[] rowData = {
                            employeeId, "Unknown", "N/A", "N/A", "N/A", "N/A", "Error: " + ex.getMessage()
                    };
                    resultsTableModel.addRow(rowData);

                    logArea.append("✗ Error for Employee " + employeeId + ": " + ex.getMessage() + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }

            processed++;
            final int finalProcessed = processed;
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(finalProcessed);
                statusLabel.setText(String.format("Processed %d of %d employees", finalProcessed, employeeIds.size()));
            });
        }

        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progressBar.getMaximum());
            statusLabel.setText("Payroll calculation completed");
            setButtonsEnabled(true);

            logArea.append("\n" + "=".repeat(50) + "\n");
            logArea.append("Payroll calculation completed!\n");
            logArea.append("Processed: " + employeeIds.size() + " employees\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void handleGeneratePayslips(ActionEvent e) {
        if (jasperService == null) {
            JOptionPane.showMessageDialog(this,
                    "JasperReports is not available. Cannot generate payslips.",
                    "Service Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Integer> selectedEmployeeIds = getSelectedEmployeeIds();

        if (selectedEmployeeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one employee for payslip generation.",
                    "No Employees Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate output directory
        String outputDir = outputDirectoryField.getText().trim();
        if (outputDir.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please specify an output directory.",
                    "No Output Directory", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Output directory does not exist. Create it?",
                    "Create Directory",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                if (!outputDirectory.mkdirs()) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to create output directory.",
                            "Directory Creation Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }

        // Clear log and set up progress
        logArea.setText("");
        progressBar.setMaximum(selectedEmployeeIds.size());
        progressBar.setValue(0);
        statusLabel.setText("Generating payslips...");
        setButtonsEnabled(false);

        // Process in background
        CompletableFuture.runAsync(() -> {
            generatePayslipsForEmployees(selectedEmployeeIds, outputDirectory);
        });
    }

    private void generatePayslipsForEmployees(List<Integer> employeeIds, File outputDirectory) {
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (Integer) yearComboBox.getSelectedItem();
        JasperPayslipService.ExportFormat format =
                (JasperPayslipService.ExportFormat) formatComboBox.getSelectedItem();

        LocalDate periodStart = LocalDate.of(year, month, 1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        // Create dated folder if requested
        File actualOutputDir;
        if (createFolderCheckBox.isSelected()) {
            String folderName = String.format("Payslips_%s_%d",
                    monthComboBox.getSelectedItem().toString(), year);
            actualOutputDir = new File(outputDirectory, folderName);
            if (!actualOutputDir.exists()) {
                actualOutputDir.mkdirs();
            }
        } else {
            actualOutputDir = outputDirectory;
        }

        int processed = 0;
        int successful = 0;

        for (int employeeId : employeeIds) {
            try {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Generating payslip for Employee " + employeeId + "...\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });

                File payslipFile = jasperService.generatePayslipToFile(
                        employeeId, periodStart, periodEnd, format, actualOutputDir.getAbsolutePath());

                successful++;

                SwingUtilities.invokeLater(() -> {
                    logArea.append("✓ Generated: " + payslipFile.getName() + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error generating payslip for employee " + employeeId, ex);

                SwingUtilities.invokeLater(() -> {
                    logArea.append("✗ Error for Employee " + employeeId + ": " + ex.getMessage() + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }

            processed++;
            final int finalProcessed = processed;
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(finalProcessed);
                statusLabel.setText(String.format("Generated %d of %d payslips", finalProcessed, employeeIds.size()));
            });
        }

        final int finalSuccessful = successful;
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progressBar.getMaximum());
            statusLabel.setText("Payslip generation completed");
            setButtonsEnabled(true);

            logArea.append("\n" + "=".repeat(50) + "\n");
            logArea.append("Payslip generation completed!\n");
            logArea.append("Total processed: " + employeeIds.size() + "\n");
            logArea.append("Successful: " + finalSuccessful + "\n");
            logArea.append("Failed: " + (employeeIds.size() - finalSuccessful) + "\n");
            logArea.append("Output directory: " + actualOutputDir.getAbsolutePath() + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // Show completion dialog
            if (finalSuccessful > 0) {
                int option = JOptionPane.showConfirmDialog(
                        PayrollManagementPanel.this,
                        String.format("Generated %d payslips successfully!\nWould you like to open the output folder?",
                                finalSuccessful),
                        "Generation Complete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    try {
                        Desktop.getDesktop().open(actualOutputDir);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Could not open output directory", ex);
                    }
                }
            }
        });
    }

    private void handlePreviewSample(ActionEvent e) {
        List<Integer> selectedEmployeeIds = getSelectedEmployeeIds();

        if (selectedEmployeeIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one employee to preview.",
                    "No Employees Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use the first selected employee for preview
        int employeeId = selectedEmployeeIds.get(0);

        try {
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = (Integer) yearComboBox.getSelectedItem();

            LocalDate periodStart = LocalDate.of(year, month, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            // Calculate payroll data first
            PayrollCalculator.PayrollData payrollData = payrollCalculator.calculatePayroll(
                    employeeId, periodStart, periodEnd);

            // Create preview dialog
            showPayrollPreviewDialog(employeeId, payrollData);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error generating preview", ex);
            JOptionPane.showMessageDialog(this,
                    "Error generating preview: " + ex.getMessage(),
                    "Preview Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPayrollPreviewDialog(int employeeId, PayrollCalculator.PayrollData data) {
        JDialog previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Payroll Preview - Employee " + employeeId, true);

        JTextArea previewArea = new JTextArea(20, 60);
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        StringBuilder preview = new StringBuilder();
        preview.append("PAYROLL PREVIEW\n");
        preview.append("===============================================\n\n");

        preview.append(String.format("Employee ID: %d\n", employeeId));
        preview.append(String.format("Period: %s to %s\n\n",
                data.getPeriodStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                data.getPeriodEnd().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));

        preview.append("EARNINGS:\n");
        preview.append(String.format("  Basic Pay:        ₱%,10.2f\n", data.getBasicPay()));
        preview.append(String.format("  Rice Subsidy:     ₱%,10.2f\n", data.getRiceSubsidy()));
        preview.append(String.format("  Phone Allowance:  ₱%,10.2f\n", data.getPhoneAllowance()));
        preview.append(String.format("  Clothing Allow.:  ₱%,10.2f\n", data.getClothingAllowance()));
        preview.append("  " + "-".repeat(30) + "\n");
        preview.append(String.format("  GROSS PAY:        ₱%,10.2f\n\n", data.getGrossPay()));

        preview.append("DEDUCTIONS:\n");
        preview.append(String.format("  Late Deduction:   ₱%,10.2f\n", data.getLateDeduction()));
        preview.append(String.format("  Undertime Deduc.: ₱%,10.2f\n", data.getUndertimeDeduction()));
        preview.append(String.format("  SSS:              ₱%,10.2f\n", data.getSss()));
        preview.append(String.format("  PhilHealth:       ₱%,10.2f\n", data.getPhilhealth()));
        preview.append(String.format("  Pag-IBIG:         ₱%,10.2f\n", data.getPagibig()));
        preview.append(String.format("  Withholding Tax:  ₱%,10.2f\n", data.getTax()));
        preview.append("  " + "-".repeat(30) + "\n");
        preview.append(String.format("  TOTAL DEDUCTIONS: ₱%,10.2f\n\n", data.getTotalDeductions()));

        preview.append("SUMMARY:\n");
        preview.append(String.format("  Gross Pay:        ₱%,10.2f\n", data.getGrossPay()));
        preview.append(String.format("  Total Deductions: ₱%,10.2f\n", data.getTotalDeductions()));
        preview.append("  " + "=".repeat(30) + "\n");
        preview.append(String.format("  NET PAY:          ₱%,10.2f\n", data.getNetPay()));

        previewArea.setText(preview.toString());
        previewArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        previewDialog.add(scrollPane);

        previewDialog.setSize(500, 600);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setVisible(true);
    }

    private List<Integer> getSelectedEmployeeIds() {
        List<Integer> selectedIds = new ArrayList<>();

        for (int i = 0; i < employeeTableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) employeeTableModel.getValueAt(i, 0);
            if (selected != null && selected) {
                String idStr = (String) employeeTableModel.getValueAt(i, 1);
                try {
                    selectedIds.add(Integer.parseInt(idStr));
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid employee ID: " + idStr);
                }
            }
        }

        return selectedIds;
    }

    private void setButtonsEnabled(boolean enabled) {
        calculatePayrollButton.setEnabled(enabled);
        generatePayslipsButton.setEnabled(enabled && jasperService != null);
        previewSampleButton.setEnabled(enabled && jasperService != null);
        selectAllButton.setEnabled(enabled);
        selectNoneButton.setEnabled(enabled);
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();

            // Clear existing data
            employeeTableModel.setRowCount(0);

            // Add employee data to table
            for (Employee emp : employees) {
                Object[] rowData = {
                        false, // Not selected by default
                        String.valueOf(emp.getId()),
                        emp.getFullName(),
                        emp.getPosition() != null ? emp.getPosition() : "Unknown",
                        emp.getStatus() != null ? emp.getStatus() : "Unknown"
                };
                employeeTableModel.addRow(rowData);
            }

            statusLabel.setText("Loaded " + employees.size() + " employees");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading employees", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading employees: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}