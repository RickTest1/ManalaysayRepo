package gui;

import service.*;
import model.Employee;
import util.DBConnection;

import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main Application class for MotorPH Payroll System
 * Entry point for the GUI application
 */
public class MotorPHPayrollApp {
    private static final Logger LOGGER = Logger.getLogger(MotorPHPayrollApp.class.getName());

    public static void main(String[] args) {
        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set system look and feel", e);
        }

        // Set up logging
        setupLogging();

        // Test database connection before starting GUI
        if (!testDatabaseConnection()) {
            showDatabaseErrorDialog();
            return;
        }

        // Start the application
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void setupLogging() {
        // Configure logging to show in console for development
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
    }

    private static boolean testDatabaseConnection() {
        try {
            LOGGER.info("Testing database connection...");
            return DBConnection.testConnection();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }

    private static void showDatabaseErrorDialog() {
        String message = """
            Database Connection Failed!
            
            Please ensure:
            1. MySQL server is running
            2. Database 'aoopdatabase_payroll' exists
            3. Username/password are correct
            4. Run the provided SQL setup script
            
            Check the console for detailed error information.
            """;

        JOptionPane.showMessageDialog(null, message,
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

/**
 * Login Frame - Entry point for user authentication
 */
class LoginFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private final AuthenticationService authService;
    private JTextField employeeIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        configureFrame();
    }

    private void initializeComponents() {
        employeeIdField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");

        // Set default button
        getRootPane().setDefaultButton(loginButton);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Login form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(47, 59, 79)); // Dark blue background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("MotorPH Payroll System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Employee Login Portal", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);

        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(subtitleLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Employee ID:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(employeeIdField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        loginButton.setPreferredSize(new Dimension(100, 35));
        exitButton.setPreferredSize(new Dimension(100, 35));

        panel.add(loginButton);
        panel.add(exitButton);

        return panel;
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(this::handleLogin);
        exitButton.addActionListener(e -> System.exit(0));

        // Enter key in password field triggers login
        passwordField.addActionListener(this::handleLogin);
    }

    private void handleLogin(ActionEvent e) {
        String employeeIdText = employeeIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (employeeIdText.isEmpty() || password.isEmpty()) {
            showErrorMessage("Please enter both Employee ID and Password.");
            return;
        }

        try {
            int employeeId = Integer.parseInt(employeeIdText);
            performLogin(employeeId, password);
        } catch (NumberFormatException ex) {
            showErrorMessage("Employee ID must be a valid number.");
            employeeIdField.selectAll();
            employeeIdField.requestFocus();
        }
    }

    private void performLogin(int employeeId, String password) {
        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);

        // Perform authentication in background thread
        SwingWorker<AuthenticationService.AuthenticationResult, Void> worker =
                new SwingWorker<AuthenticationService.AuthenticationResult, Void>() {

                    @Override
                    protected AuthenticationService.AuthenticationResult doInBackground() {
                        return authService.authenticate(employeeId, password);
                    }

                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                        loginButton.setEnabled(true);

                        try {
                            AuthenticationService.AuthenticationResult result = get();
                            handleAuthenticationResult(result);
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Authentication error", ex);
                            showErrorMessage("Authentication failed: " + ex.getMessage());
                        }
                    }
                };

        worker.execute();
    }

    private void handleAuthenticationResult(AuthenticationService.AuthenticationResult result) {
        if (result.isSuccessful()) {
            LOGGER.info("Login successful for employee: " + result.getEmployee().getId());

            // Hide login frame and show main application
            setVisible(false);
            new MainFrame(result.getEmployee()).setVisible(true);
            dispose();
        } else {
            showErrorMessage("Login Failed: " + result.getMessage());
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private void configureFrame() {
        setTitle("MotorPH Payroll System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Center on screen

        // Set icon if available
        try {
            // You can add an icon here if you have one
            // setIconImage(ImageIO.read(getClass().getResource("/images/motorph_icon.png")));
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }
    }
}

/**
 * Main Application Frame - Shows different panels based on user role
 */
class MainFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final Employee loggedInEmployee;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public MainFrame(Employee employee) {
        this.loggedInEmployee = employee;
        initializeComponents();
        setupLayout();
        configureFrame();

        LOGGER.info("Main application opened for: " + employee.getFullName());
    }

    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        statusLabel = new JLabel("Welcome, " + loggedInEmployee.getFullName());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main content - tabbed pane
        setupTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(47, 59, 79));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("MotorPH Payroll System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(logoutButton, BorderLayout.EAST);

        return panel;
    }

    private void setupTabbedPane() {
        // All employees get self-service panel
        tabbedPane.addTab("My Information", new EmployeeSelfServicePanel(loggedInEmployee));
        tabbedPane.addTab("My Attendance", new AttendancePanel(loggedInEmployee));
        tabbedPane.addTab("My Leave Requests", new LeaveRequestPanel(loggedInEmployee));

        // Check if user has HR privileges (simplified check - you might want more sophisticated role management)
        if (isHRUser(loggedInEmployee)) {
            tabbedPane.addTab("Employee Management", new EmployeeManagementPanel());
            tabbedPane.addTab("Payroll Management", new PayrollManagementPanel());
            tabbedPane.addTab("Reports", new ReportsPanel());
            tabbedPane.addTab("System Administration", new AdminPanel());
        }

        // Managers can view their team's information
        if (isManager(loggedInEmployee)) {
            tabbedPane.addTab("Team Management", new TeamManagementPanel(loggedInEmployee));
        }
    }

    private boolean isHRUser(Employee employee) {
        String position = employee.getPosition();
        return position != null && (
                position.toLowerCase().contains("hr") ||
                        position.toLowerCase().contains("human resource") ||
                        position.toLowerCase().contains("chief") ||
                        position.toLowerCase().contains("ceo") ||
                        position.toLowerCase().contains("payroll")
        );
    }

    private boolean isManager(Employee employee) {
        String position = employee.getPosition();
        return position != null && (
                position.toLowerCase().contains("manager") ||
                        position.toLowerCase().contains("head") ||
                        position.toLowerCase().contains("chief") ||
                        position.toLowerCase().contains("leader")
        );
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        panel.setBackground(Color.LIGHT_GRAY);

        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(timeLabel, BorderLayout.EAST);

        return panel;
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            LOGGER.info("User logged out: " + loggedInEmployee.getFullName());
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void configureFrame() {
        setTitle("MotorPH Payroll System - " + loggedInEmployee.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
    }
}