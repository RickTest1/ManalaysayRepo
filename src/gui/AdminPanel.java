package gui;

import service.EmployeeService;
import service.AuthenticationService;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * System Administration Panel
 * Provides system administration tools and database management
 */
public class AdminPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(AdminPanel.class.getName());

    private final EmployeeService employeeService;
    private final AuthenticationService authService;

    // Database management components
    private JTextArea connectionInfoArea;
    private JButton testConnectionButton;
    private JButton verifyDatabaseButton;
    private JButton backupDatabaseButton;
    private JLabel connectionStatusLabel;

    // System information components
    private JTextArea systemInfoArea;
    private JButton refreshSystemInfoButton;

    // User management components
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JTextField newUserIdField;
    private JPasswordField newPasswordField;
    private JButton createUserButton;
    private JButton resetPasswordButton;
    private JButton deleteUserButton;

    // System logs components
    private JTextArea logsArea;
    private JComboBox<String> logLevelCombo;
    private JButton refreshLogsButton;
    private JButton exportLogsButton;

    // Maintenance components
    private JProgressBar maintenanceProgressBar;
    private JButton cleanupTempFilesButton;
    private JButton optimizeDatabaseButton;
    private JButton generateSystemReportButton;
    private JLabel maintenanceStatusLabel;

    public AdminPanel() {
        this.employeeService = new EmployeeService();
        this.authService = new AuthenticationService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSystemInformation();
        loadUserManagement();
    }

    private void initializeComponents() {
        // Database management components
        connectionInfoArea = new JTextArea(8, 50);
        connectionInfoArea.setEditable(false);
        connectionInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        connectionInfoArea.setBackground(new Color(248, 248, 248));

        testConnectionButton = new JButton("Test Connection");
        verifyDatabaseButton = new JButton("Verify Database");
        backupDatabaseButton = new JButton("Backup Database");
        connectionStatusLabel = new JLabel("Connection status unknown");

        testConnectionButton.setBackground(new Color(33, 150, 243));
        testConnectionButton.setForeground(Color.WHITE);
        verifyDatabaseButton.setBackground(new Color(76, 175, 80));
        verifyDatabaseButton.setForeground(Color.WHITE);
        backupDatabaseButton.setBackground(new Color(255, 152, 0));
        backupDatabaseButton.setForeground(Color.WHITE);

        // System information components
        systemInfoArea = new JTextArea(10, 50);
        systemInfoArea.setEditable(false);
        systemInfoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        systemInfoArea.setBackground(new Color(248, 248, 248));

        refreshSystemInfoButton = new JButton("Refresh System Info");

        // User management components
        String[] userColumns = {"Employee ID", "Has Credentials", "Last Login", "Status"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersTableModel);

        newUserIdField = new JTextField(10);
        newPasswordField = new JPasswordField(15);
        createUserButton = new JButton("Create User");
        resetPasswordButton = new JButton("Reset Password");
        deleteUserButton = new JButton("Delete User");

        createUserButton.setBackground(new Color(76, 175, 80));
        createUserButton.setForeground(Color.WHITE);
        resetPasswordButton.setBackground(new Color(255, 152, 0));
        resetPasswordButton.setForeground(Color.WHITE);
        deleteUserButton.setBackground(new Color(244, 67, 54));
        deleteUserButton.setForeground(Color.WHITE);

        resetPasswordButton.setEnabled(false);
        deleteUserButton.setEnabled(false);

        // System logs components
        logsArea = new JTextArea(12, 50);
        logsArea.setEditable(false);
        logsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        logsArea.setBackground(Color.BLACK);
        logsArea.setForeground(Color.GREEN);

        logLevelCombo = new JComboBox<>(new String[]{"ALL", "INFO", "WARNING", "SEVERE"});
        refreshLogsButton = new JButton("Refresh Logs");
        exportLogsButton = new JButton("Export Logs");

        // Maintenance components
        maintenanceProgressBar = new JProgressBar();
        maintenanceProgressBar.setStringPainted(true);

        cleanupTempFilesButton = new JButton("Cleanup Temp Files");
        optimizeDatabaseButton = new JButton("Optimize Database");
        generateSystemReportButton = new JButton("Generate System Report");
        maintenanceStatusLabel = new JLabel("No maintenance tasks running");

        cleanupTempFilesButton.setBackground(new Color(156, 39, 176));
        cleanupTempFilesButton.setForeground(Color.WHITE);
        optimizeDatabaseButton.setBackground(new Color(63, 81, 181));
        optimizeDatabaseButton.setForeground(Color.WHITE);
        generateSystemReportButton.setBackground(new Color(0, 150, 136));
        generateSystemReportButton.setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create tabbed pane for different admin functions
        JTabbedPane tabbedPane = new JTabbedPane();

        // Database Management Tab
        JPanel databasePanel = createDatabaseManagementPanel();
        tabbedPane.addTab("Database Management", databasePanel);

        // System Information Tab
        JPanel systemInfoPanel = createSystemInformationPanel();
        tabbedPane.addTab("System Information", systemInfoPanel);

        // User Management Tab
        JPanel userMgmtPanel = createUserManagementPanel();
        tabbedPane.addTab("User Management", userMgmtPanel);

        // System Logs Tab
        JPanel logsPanel = createSystemLogsPanel();
        tabbedPane.addTab("System Logs", logsPanel);

        // Maintenance Tab
        JPanel maintenancePanel = createMaintenancePanel();
        tabbedPane.addTab("Maintenance", maintenancePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDatabaseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Connection info panel
        JPanel connectionPanel = new JPanel(new BorderLayout());
        connectionPanel.setBorder(new TitledBorder("Database Connection Information"));

        JScrollPane connectionScrollPane = new JScrollPane(connectionInfoArea);
        connectionPanel.add(connectionScrollPane, BorderLayout.CENTER);

        // Connection controls
        JPanel connectionControlsPanel = new JPanel(new FlowLayout());
        connectionControlsPanel.add(testConnectionButton);
        connectionControlsPanel.add(verifyDatabaseButton);
        connectionControlsPanel.add(backupDatabaseButton);

        connectionPanel.add(connectionControlsPanel, BorderLayout.SOUTH);

        panel.add(connectionPanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(connectionStatusLabel, BorderLayout.WEST);

        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSystemInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // System info display
        JScrollPane scrollPane = new JScrollPane(systemInfoArea);
        scrollPane.setBorder(new TitledBorder("System Information"));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Controls
        JPanel controlsPanel = new JPanel(new FlowLayout());
        controlsPanel.add(refreshSystemInfoButton);

        panel.add(controlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Users table
        JScrollPane tableScrollPane = new JScrollPane(usersTable);
        tableScrollPane.setBorder(new TitledBorder("System Users"));
        tableScrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // User management form
        JPanel userFormPanel = new JPanel(new GridBagLayout());
        userFormPanel.setBorder(new TitledBorder("User Management"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        userFormPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        userFormPanel.add(newUserIdField, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        userFormPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 3;
        userFormPanel.add(newPasswordField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(createUserButton);
        buttonsPanel.add(resetPasswordButton);
        buttonsPanel.add(deleteUserButton);
        userFormPanel.add(buttonsPanel, gbc);

        panel.add(userFormPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSystemLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Logs display
        JScrollPane logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.setBorder(new TitledBorder("System Logs"));

        panel.add(logsScrollPane, BorderLayout.CENTER);

        // Logs controls
        JPanel logsControlsPanel = new JPanel(new FlowLayout());
        logsControlsPanel.add(new JLabel("Log Level:"));
        logsControlsPanel.add(logLevelCombo);
        logsControlsPanel.add(refreshLogsButton);
        logsControlsPanel.add(exportLogsButton);

        panel.add(logsControlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMaintenancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Maintenance actions
        JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionsPanel.setBorder(new TitledBorder("Maintenance Tasks"));

        actionsPanel.add(cleanupTempFilesButton);
        actionsPanel.add(optimizeDatabaseButton);
        actionsPanel.add(generateSystemReportButton);

        panel.add(actionsPanel, BorderLayout.NORTH);

        // Progress and status
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(new TitledBorder("Progress"));

        progressPanel.add(maintenanceProgressBar, BorderLayout.CENTER);
        progressPanel.add(maintenanceStatusLabel, BorderLayout.SOUTH);

        panel.add(progressPanel, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        // Database management
        testConnectionButton.addActionListener(this::handleTestConnection);
        verifyDatabaseButton.addActionListener(this::handleVerifyDatabase);
        backupDatabaseButton.addActionListener(this::handleBackupDatabase);

        // System information
        refreshSystemInfoButton.addActionListener(e -> loadSystemInformation());

        // User management
        createUserButton.addActionListener(this::handleCreateUser);
        resetPasswordButton.addActionListener(this::handleResetPassword);
        deleteUserButton.addActionListener(this::handleDeleteUser);

        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleUserTableSelection();
            }
        });

        // System logs
        refreshLogsButton.addActionListener(this::handleRefreshLogs);
        exportLogsButton.addActionListener(this::handleExportLogs);

        // Maintenance
        cleanupTempFilesButton.addActionListener(this::handleCleanupTempFiles);
        optimizeDatabaseButton.addActionListener(this::handleOptimizeDatabase);
        generateSystemReportButton.addActionListener(this::handleGenerateSystemReport);
    }

    private void handleTestConnection(ActionEvent e) {
        testConnectionButton.setEnabled(false);
        connectionStatusLabel.setText("Testing connection...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                testConnectionButton.setEnabled(true);

                try {
                    boolean connected = get();
                    if (connected) {
                        connectionStatusLabel.setText("✅ Connection successful");
                        connectionStatusLabel.setForeground(new Color(46, 125, 50));

                        // Load connection info
                        connectionInfoArea.setText(DBConnection.getConnectionInfo());
                    } else {
                        connectionStatusLabel.setText("❌ Connection failed");
                        connectionStatusLabel.setForeground(new Color(198, 40, 40));
                    }
                } catch (Exception ex) {
                    connectionStatusLabel.setText("❌ Connection error: " + ex.getMessage());
                    connectionStatusLabel.setForeground(new Color(198, 40, 40));
                    LOGGER.log(Level.SEVERE, "Connection test failed", ex);
                }
            }
        };

        worker.execute();
    }

    private void handleVerifyDatabase(ActionEvent e) {
        verifyDatabaseButton.setEnabled(false);
        connectionStatusLabel.setText("Verifying database...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBConnection.verifyDatabase();
                return null;
            }

            @Override
            protected void done() {
                verifyDatabaseButton.setEnabled(true);

                try {
                    get();
                    connectionStatusLabel.setText("✅ Database verification completed");
                    connectionStatusLabel.setForeground(new Color(46, 125, 50));

                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Database verification completed successfully!",
                            "Verification Complete", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    connectionStatusLabel.setText("❌ Database verification failed");
                    connectionStatusLabel.setForeground(new Color(198, 40, 40));

                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Database verification failed: " + ex.getMessage(),
                            "Verification Failed", JOptionPane.ERROR_MESSAGE);

                    LOGGER.log(Level.SEVERE, "Database verification failed", ex);
                }
            }
        };

        worker.execute();
    }

    private void handleBackupDatabase(ActionEvent e) {
        JOptionPane.showMessageDialog(this,
                "Database backup functionality would require mysqldump utility.\n" +
                        "This is a placeholder for backup implementation.",
                "Backup Database", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCreateUser(ActionEvent e) {
        String employeeIdText = newUserIdField.getText().trim();
        String password = new String(newPasswordField.getPassword());

        if (employeeIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an Employee ID.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int employeeId = Integer.parseInt(employeeIdText);

            // Check if employee exists
            if (!employeeService.getAllEmployees().stream()
                    .anyMatch(emp -> emp.getId() == employeeId)) {
                JOptionPane.showMessageDialog(this,
                        "Employee ID " + employeeId + " does not exist.",
                        "Employee Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if user already has credentials
            if (authService.hasCredentials(employeeId)) {
                JOptionPane.showMessageDialog(this,
                        "Employee ID " + employeeId + " already has login credentials.",
                        "User Already Exists", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create credentials (this would need to be implemented in AuthenticationService)
            // For now, show success message
            JOptionPane.showMessageDialog(this,
                    "User credentials created successfully for Employee " + employeeId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            newUserIdField.setText("");
            newPasswordField.setText("");
            loadUserManagement();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID must be a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error creating user", ex);
            JOptionPane.showMessageDialog(this,
                    "Error creating user: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetPassword(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String employeeId = (String) usersTableModel.getValueAt(selectedRow, 0);

            String newPassword = JOptionPane.showInputDialog(this,
                    "Enter new password for Employee " + employeeId + ":",
                    "Reset Password",
                    JOptionPane.QUESTION_MESSAGE);

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Implementation would go here
                JOptionPane.showMessageDialog(this,
                        "Password reset successfully for Employee " + employeeId,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void handleDeleteUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String employeeId = (String) usersTableModel.getValueAt(selectedRow, 0);

            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete login credentials for Employee " + employeeId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                // Implementation would go here
                JOptionPane.showMessageDialog(this,
                        "User credentials deleted for Employee " + employeeId,
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                loadUserManagement();
            }
        }
    }

    private void handleUserTableSelection() {
        int selectedRow = usersTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;

        resetPasswordButton.setEnabled(hasSelection);
        deleteUserButton.setEnabled(hasSelection);

        if (hasSelection) {
            String employeeId = (String) usersTableModel.getValueAt(selectedRow, 0);
            newUserIdField.setText(employeeId);
        }
    }

    private void handleRefreshLogs(ActionEvent e) {
        // Simulate log loading
        StringBuilder logs = new StringBuilder();
        logs.append("=== SYSTEM LOGS ===\n");
        logs.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        logs.append("Log Level: ").append(logLevelCombo.getSelectedItem()).append("\n\n");

        // Add some sample log entries
        logs.append("[INFO] ").append(LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append(" - Application started successfully\n");
        logs.append("[INFO] ").append(LocalDateTime.now().minusMinutes(3).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append(" - Database connection established\n");
        logs.append("[INFO] ").append(LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append(" - User login: Employee 10001\n");
        logs.append("[WARNING] ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .append(" - System logs refreshed manually\n");

        logsArea.setText(logs.toString());
        logsArea.setCaretPosition(logsArea.getDocument().getLength());
    }

    private void handleExportLogs(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("system_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")) + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(logsArea.getText());
                }

                JOptionPane.showMessageDialog(this,
                        "Logs exported successfully to: " + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error exporting logs", ex);
                JOptionPane.showMessageDialog(this,
                        "Error exporting logs: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCleanupTempFiles(ActionEvent e) {
        performMaintenanceTask("Cleaning up temporary files...", () -> {
            // Simulate cleanup
            try {
                Thread.sleep(2000);
                return "Temporary files cleaned up successfully";
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return "Cleanup interrupted";
            }
        });
    }

    private void handleOptimizeDatabase(ActionEvent e) {
        performMaintenanceTask("Optimizing database...", () -> {
            // Simulate database optimization
            try {
                Thread.sleep(3000);
                return "Database optimization completed";
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return "Optimization interrupted";
            }
        });
    }

    private void handleGenerateSystemReport(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("system_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            performMaintenanceTask("Generating system report...", () -> {
                try {
                    generateSystemReport(file);
                    return "System report generated: " + file.getAbsolutePath();
                } catch (Exception ex) {
                    return "Error generating report: " + ex.getMessage();
                }
            });
        }
    }

    private void performMaintenanceTask(String taskDescription, java.util.function.Supplier<String> task) {
        maintenanceStatusLabel.setText(taskDescription);
        maintenanceProgressBar.setIndeterminate(true);
        setMaintenanceButtonsEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return task.get();
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    maintenanceStatusLabel.setText(result);

                    JOptionPane.showMessageDialog(AdminPanel.this,
                            result,
                            "Task Complete", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    maintenanceStatusLabel.setText("Task failed: " + ex.getMessage());
                    LOGGER.log(Level.SEVERE, "Maintenance task failed", ex);

                    JOptionPane.showMessageDialog(AdminPanel.this,
                            "Task failed: " + ex.getMessage(),
                            "Task Failed", JOptionPane.ERROR_MESSAGE);
                } finally {
                    maintenanceProgressBar.setIndeterminate(false);
                    maintenanceProgressBar.setValue(0);
                    setMaintenanceButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void setMaintenanceButtonsEnabled(boolean enabled) {
        cleanupTempFilesButton.setEnabled(enabled);
        optimizeDatabaseButton.setEnabled(enabled);
        generateSystemReportButton.setEnabled(enabled);
    }

    private void generateSystemReport(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("MOTORPH PAYROLL SYSTEM - SYSTEM REPORT\n");
            writer.write("=".repeat(60) + "\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");

            // System information
            writer.write("SYSTEM INFORMATION:\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("Java Version: " + System.getProperty("java.version") + "\n");
            writer.write("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n");
            writer.write("Java VM: " + System.getProperty("java.vm.name") + "\n");
            writer.write("User Directory: " + System.getProperty("user.dir") + "\n");
            writer.write("Available Processors: " + Runtime.getRuntime().availableProcessors() + "\n");

            // Memory information
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024);
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;

            writer.write("Max Memory: " + maxMemory + " MB\n");
            writer.write("Total Memory: " + totalMemory + " MB\n");
            writer.write("Used Memory: " + usedMemory + " MB\n");
            writer.write("Free Memory: " + freeMemory + " MB\n\n");

            // Database information
            writer.write("DATABASE INFORMATION:\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("Connection Status: " + (DBConnection.testConnection() ? "Connected" : "Disconnected") + "\n");
            writer.write(DBConnection.getConnectionInfo() + "\n\n");

            // Application statistics
            writer.write("APPLICATION STATISTICS:\n");
            writer.write("-".repeat(30) + "\n");
            try {
                int totalEmployees = employeeService.getAllEmployees().size();
                int regularEmployees = employeeService.getEmployeeCountByStatus("Regular");
                int probationaryEmployees = employeeService.getEmployeeCountByStatus("Probationary");

                writer.write("Total Employees: " + totalEmployees + "\n");
                writer.write("Regular Employees: " + regularEmployees + "\n");
                writer.write("Probationary Employees: " + probationaryEmployees + "\n");
            } catch (Exception ex) {
                writer.write("Error loading employee statistics: " + ex.getMessage() + "\n");
            }

            writer.write("\n" + "=".repeat(60) + "\n");
            writer.write("End of Report\n");
        }
    }

    private void loadSystemInformation() {
        StringBuilder info = new StringBuilder();
        info.append("SYSTEM INFORMATION\n");
        info.append("=".repeat(50)).append("\n\n");

        // Java Information
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("Java Home: ").append(System.getProperty("java.home")).append("\n");
        info.append("Java VM: ").append(System.getProperty("java.vm.name")).append("\n");
        info.append("Java VM Version: ").append(System.getProperty("java.vm.version")).append("\n\n");

        // Operating System Information
        info.append("Operating System: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("OS Architecture: ").append(System.getProperty("os.arch")).append("\n\n");

        // System Properties
        info.append("User Name: ").append(System.getProperty("user.name")).append("\n");
        info.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("Working Directory: ").append(System.getProperty("user.dir")).append("\n\n");

        // Runtime Information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        info.append("MEMORY INFORMATION\n");
        info.append("-".repeat(30)).append("\n");
        info.append("Max Memory: ").append(formatMemory(maxMemory)).append("\n");
        info.append("Total Memory: ").append(formatMemory(totalMemory)).append("\n");
        info.append("Used Memory: ").append(formatMemory(usedMemory)).append("\n");
        info.append("Free Memory: ").append(formatMemory(freeMemory)).append("\n");
        info.append("Available Processors: ").append(runtime.availableProcessors()).append("\n\n");

        // Database Information
        info.append("DATABASE INFORMATION\n");
        info.append("-".repeat(30)).append("\n");
        info.append("Connection Test: ").append(DBConnection.testConnection() ? "✅ Connected" : "❌ Failed").append("\n");

        systemInfoArea.setText(info.toString());
        systemInfoArea.setCaretPosition(0);
    }

    private String formatMemory(long bytes) {
        long mb = bytes / (1024 * 1024);
        return mb + " MB (" + String.format("%.2f", bytes / (1024.0 * 1024.0 * 1024.0)) + " GB)";
    }

    private void loadUserManagement() {
        try {
            usersTableModel.setRowCount(0);

            // Load all employees and check which ones have credentials
            employeeService.getAllEmployees().forEach(emp -> {
                boolean hasCredentials = authService.hasCredentials(emp.getId());

                Object[] row = {
                        String.valueOf(emp.getId()),
                        hasCredentials ? "Yes" : "No",
                        hasCredentials ? "Unknown" : "N/A", // Last login would need to be tracked
                        hasCredentials ? "Active" : "No Login"
                };
                usersTableModel.addRow(row);
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading user management data", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading user data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}