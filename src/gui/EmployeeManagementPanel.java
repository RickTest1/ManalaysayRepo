package gui;

import model.Employee;
import model.Position;
import service.EmployeeService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Employee Management Panel for HR users
 * Provides CRUD operations for employee records
 */
public class EmployeeManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(EmployeeManagementPanel.class.getName());

    private final EmployeeService employeeService;

    // Table components
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    // Form components
    private JTextField employeeIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField birthdayField;
    private JTextField addressField;
    private JTextField phoneField;
    private JComboBox<Position> positionComboBox;
    private JComboBox<String> statusComboBox;
    private JComboBox<Employee> supervisorComboBox;
    private JTextField sssField;
    private JTextField philhealthField;
    private JTextField tinField;
    private JTextField pagibigField;
    private JPasswordField passwordField;

    // Action buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton refreshButton;

    // Current selected employee
    private Employee selectedEmployee;

    public EmployeeManagementPanel() {
        this.employeeService = new EmployeeService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadEmployeeData();
        loadPositions();
        loadPotentialSupervisors();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {
                "ID", "Last Name", "First Name", "Position", "Status",
                "Phone", "SSS", "PhilHealth", "TIN", "Pag-IBIG"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setAutoCreateRowSorter(true);

        sorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(sorter);

        searchField = new JTextField(20);

        // Form fields
        employeeIdField = new JTextField(15);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        birthdayField = new JTextField(15);
        birthdayField.setToolTipText("Format: YYYY-MM-DD");
        addressField = new JTextField(30);
        phoneField = new JTextField(15);

        positionComboBox = new JComboBox<>();
        statusComboBox = new JComboBox<>(new String[]{"Regular", "Probationary"});
        supervisorComboBox = new JComboBox<>();

        sssField = new JTextField(15);
        philhealthField = new JTextField(15);
        tinField = new JTextField(15);
        pagibigField = new JTextField(15);
        passwordField = new JPasswordField(15);

        // Buttons
        addButton = new JButton("Add Employee");
        updateButton = new JButton("Update Employee");
        deleteButton = new JButton("Delete Employee");
        clearButton = new JButton("Clear Form");
        refreshButton = new JButton("Refresh");

        // Initially disable update and delete buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel - search
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Center - split pane with table and form
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createTablePanel());
        splitPane.setRightComponent(createFormPanel());
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Search Employees"));

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Employee List"));

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Employee Details"));

        // Form fields panel
        JPanel formFieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Employee ID
        gbc.gridx = 0; gbc.gridy = row;
        formFieldsPanel.add(new JLabel("Employee ID:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(employeeIdField, gbc);

        // First Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("First Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(firstNameField, gbc);

        // Last Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Last Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(lastNameField, gbc);

        // Birthday
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(birthdayField, gbc);

        // Address
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(addressField, gbc);

        // Phone
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(phoneField, gbc);

        // Position
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Position:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(positionComboBox, gbc);

        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Status:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(statusComboBox, gbc);

        // Supervisor
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Supervisor:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(supervisorComboBox, gbc);

        // Government IDs
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("SSS Number:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(sssField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("PhilHealth:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(philhealthField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("TIN Number:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(tinField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Pag-IBIG:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(pagibigField, gbc);

        // Password (for new employees)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formFieldsPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formFieldsPanel.add(passwordField, gbc);

        JScrollPane formScrollPane = new JScrollPane(formFieldsPanel);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(formScrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(clearButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        // Required fields note
        JLabel noteLabel = new JLabel("* Required fields");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC));
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, BorderLayout.NORTH);

        return panel;
    }

    private void setupEventHandlers() {
        // Search functionality
        searchField.addActionListener(e -> filterTable());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        // Table selection
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        // Double-click to edit
        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleTableSelection();
                }
            }
        });

        // Button actions
        addButton.addActionListener(this::handleAddEmployee);
        updateButton.addActionListener(this::handleUpdateEmployee);
        deleteButton.addActionListener(this::handleDeleteEmployee);
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            loadEmployeeData();
            loadPotentialSupervisors();
        });
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void handleTableSelection() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
            int employeeId = (Integer) tableModel.getValueAt(modelRow, 0);

            selectedEmployee = employeeService.getEmployeeById(employeeId);
            if (selectedEmployee != null) {
                populateForm(selectedEmployee);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        } else {
            selectedEmployee = null;
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void populateForm(Employee employee) {
        employeeIdField.setText(String.valueOf(employee.getId()));
        firstNameField.setText(employee.getFirstName() != null ? employee.getFirstName() : "");
        lastNameField.setText(employee.getLastName() != null ? employee.getLastName() : "");
        birthdayField.setText(employee.getBirthday() != null ? employee.getBirthday().toString() : "");
        addressField.setText(employee.getAddress() != null ? employee.getAddress() : "");
        phoneField.setText(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");

        // Set position
        String employeePosition = employee.getPosition();
        for (int i = 0; i < positionComboBox.getItemCount(); i++) {
            Position pos = positionComboBox.getItemAt(i);
            if (pos.getPositionName().equals(employeePosition)) {
                positionComboBox.setSelectedIndex(i);
                break;
            }
        }

        statusComboBox.setSelectedItem(employee.getStatus() != null ? employee.getStatus() : "Regular");

        // Set supervisor
        String supervisorName = employee.getImmediateSupervisor();
        for (int i = 0; i < supervisorComboBox.getItemCount(); i++) {
            Employee supervisor = supervisorComboBox.getItemAt(i);
            if (supervisor != null && supervisor.getFormattedName().equals(supervisorName)) {
                supervisorComboBox.setSelectedIndex(i);
                break;
            }
        }

        sssField.setText(employee.getSssNumber() != null ? employee.getSssNumber() : "");
        philhealthField.setText(employee.getPhilhealthNumber() != null ? employee.getPhilhealthNumber() : "");
        tinField.setText(employee.getTinNumber() != null ? employee.getTinNumber() : "");
        pagibigField.setText(employee.getPagibigNumber() != null ? employee.getPagibigNumber() : "");
        passwordField.setText(""); // Don't populate password for security
    }

    private void clearForm() {
        employeeIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        birthdayField.setText("");
        addressField.setText("");
        phoneField.setText("");
        positionComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);
        supervisorComboBox.setSelectedIndex(0);
        sssField.setText("");
        philhealthField.setText("");
        tinField.setText("");
        pagibigField.setText("");
        passwordField.setText("");

        selectedEmployee = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        employeeTable.clearSelection();
    }

    private void handleAddEmployee(ActionEvent e) {
        try {
            Employee newEmployee = createEmployeeFromForm();
            String password = new String(passwordField.getPassword());

            if (password.isEmpty()) {
                password = "password1234"; // Default password
            }

            boolean success = employeeService.addEmployee(newEmployee, password);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Employee added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadEmployeeData();
                loadPotentialSupervisors();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add employee. Please check the data and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error adding employee", ex);
            JOptionPane.showMessageDialog(this,
                    "Error adding employee: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateEmployee(ActionEvent e) {
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Employee updatedEmployee = createEmployeeFromForm();
            updatedEmployee.setId(selectedEmployee.getId());

            boolean success = employeeService.updateEmployee(updatedEmployee);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Employee updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                loadEmployeeData();
                loadPotentialSupervisors();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update employee. Please check the data and try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error updating employee", ex);
            JOptionPane.showMessageDialog(this,
                    "Error updating employee: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteEmployee(ActionEvent e) {
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete employee " + selectedEmployee.getFullName() + "?\n" +
                        "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeService.deleteEmployee(selectedEmployee.getId());

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Employee deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    clearForm();
                    loadEmployeeData();
                    loadPotentialSupervisors();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete employee.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error deleting employee", ex);
                JOptionPane.showMessageDialog(this,
                        "Error deleting employee: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Employee createEmployeeFromForm() throws IllegalArgumentException {
        // Validate required fields
        String employeeIdText = employeeIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (employeeIdText.isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (firstName.isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (positionComboBox.getSelectedItem() == null) {
            throw new IllegalArgumentException("Position is required");
        }

        // Parse employee ID
        int employeeId;
        try {
            employeeId = Integer.parseInt(employeeIdText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Employee ID must be a valid number");
        }

        // Create employee object
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);

        // Parse birthday if provided
        String birthdayText = birthdayField.getText().trim();
        if (!birthdayText.isEmpty()) {
            try {
                LocalDate birthday = LocalDate.parse(birthdayText);
                employee.setBirthday(birthday);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid birthday format. Use YYYY-MM-DD");
            }
        }

        employee.setAddress(addressField.getText().trim());
        employee.setPhoneNumber(phoneField.getText().trim());

        Position selectedPosition = (Position) positionComboBox.getSelectedItem();
        employee.setPosition(selectedPosition.getPositionName());
        employee.setPositionId(selectedPosition.getPositionId());
        employee.setBasicSalary(selectedPosition.getMonthlySalary());

        employee.setStatus((String) statusComboBox.getSelectedItem());

        Employee selectedSupervisor = (Employee) supervisorComboBox.getSelectedItem();
        if (selectedSupervisor != null) {
            employee.setImmediateSupervisor(selectedSupervisor.getFormattedName());
        }

        employee.setSssNumber(sssField.getText().trim());
        employee.setPhilhealthNumber(philhealthField.getText().trim());
        employee.setTinNumber(tinField.getText().trim());
        employee.setPagibigNumber(pagibigField.getText().trim());

        return employee;
    }

    private void loadEmployeeData() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();

            // Clear existing data
            tableModel.setRowCount(0);

            // Add employee data to table
            for (Employee emp : employees) {
                Object[] rowData = {
                        emp.getId(),
                        emp.getLastName() != null ? emp.getLastName() : "",
                        emp.getFirstName() != null ? emp.getFirstName() : "",
                        emp.getPosition() != null ? emp.getPosition() : "",
                        emp.getStatus() != null ? emp.getStatus() : "",
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "",
                        emp.getSssNumber() != null ? emp.getSssNumber() : "",
                        emp.getPhilhealthNumber() != null ? emp.getPhilhealthNumber() : "",
                        emp.getTinNumber() != null ? emp.getTinNumber() : "",
                        emp.getPagibigNumber() != null ? emp.getPagibigNumber() : ""
                };
                tableModel.addRow(rowData);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading employee data", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading employee data: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPositions() {
        try {
            List<Position> positions = employeeService.getAllPositions();

            positionComboBox.removeAllItems();
            for (Position position : positions) {
                positionComboBox.addItem(position);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading positions", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading positions: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPotentialSupervisors() {
        try {
            List<Employee> allEmployees = employeeService.getAllEmployees();

            supervisorComboBox.removeAllItems();
            supervisorComboBox.addItem(null); // No supervisor option

            for (Employee emp : allEmployees) {
                String position = emp.getPosition();
                if (position != null && (
                        position.toLowerCase().contains("manager") ||
                                position.toLowerCase().contains("head") ||
                                position.toLowerCase().contains("chief") ||
                                position.toLowerCase().contains("leader") ||
                                position.toLowerCase().contains("ceo") ||
                                position.toLowerCase().contains("coo") ||
                                position.toLowerCase().contains("cfo") ||
                                position.toLowerCase().contains("cmo")
                )) {
                    supervisorComboBox.addItem(emp);
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading potential supervisors", e);
        }
    }
}