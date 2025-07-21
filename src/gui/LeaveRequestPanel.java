package gui;

import model.Employee;
import model.LeaveRequest;
import service.LeaveRequestService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Leave Request Management Panel
 * Allows employees to submit leave requests and managers/HR to approve them
 */
public class LeaveRequestPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(LeaveRequestPanel.class.getName());

    private final Employee employee;
    private final LeaveRequestService leaveService;
    private final boolean isApprover;

    // Table components
    private JTable leaveTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;

    // Form components for new requests
    private JComboBox<String> leaveTypeCombo;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea reasonArea;
    private JButton submitRequestButton;

    // Approval components (for managers/HR)
    private JButton approveButton;
    private JButton rejectButton;
    private JTextArea approvalNotesArea;

    // Summary components
    private JLabel totalRequestsLabel;
    private JLabel pendingRequestsLabel;
    private JLabel approvedRequestsLabel;
    private JLabel rejectedRequestsLabel;

    private LeaveRequest selectedLeaveRequest;

    public LeaveRequestPanel(Employee employee) {
        this.employee = employee;
        this.leaveService = new LeaveRequestService();
        this.isApprover = checkIfApprover(employee);

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadLeaveRequests();
        updateSummary();
    }

    private boolean checkIfApprover(Employee emp) {
        String position = emp.getPosition();
        return position != null && (
                position.toLowerCase().contains("hr") ||
                        position.toLowerCase().contains("human resource") ||
                        position.toLowerCase().contains("manager") ||
                        position.toLowerCase().contains("head") ||
                        position.toLowerCase().contains("chief") ||
                        position.toLowerCase().contains("ceo") ||
                        position.toLowerCase().contains("leader")
        );
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {
                "ID", "Employee", "Leave Type", "Start Date", "End Date", "Days", "Status", "Submitted"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: case 5: return Integer.class;
                    default: return String.class;
                }
            }
        };

        leaveTable = new JTable(tableModel);
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for status column
        leaveTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        // Status filter
        statusFilterCombo = new JComboBox<>(new String[]{
                "All", "Pending", "Approved", "Rejected"
        });

        // Form components
        leaveTypeCombo = new JComboBox<>(new String[]{
                LeaveRequest.ANNUAL_LEAVE,
                LeaveRequest.SICK_LEAVE,
                LeaveRequest.EMERGENCY_LEAVE,
                LeaveRequest.MATERNITY_LEAVE,
                LeaveRequest.PATERNITY_LEAVE,
                "Personal Leave",
                "Vacation Leave",
                "Unpaid Leave"
        });

        startDateField = new JTextField(10);
        startDateField.setToolTipText("Format: YYYY-MM-DD");

        endDateField = new JTextField(10);
        endDateField.setToolTipText("Format: YYYY-MM-DD");

        reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        submitRequestButton = new JButton("Submit Request");

        // Approval components (only for approvers)
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");
        approvalNotesArea = new JTextArea(2, 30);
        approvalNotesArea.setLineWrap(true);
        approvalNotesArea.setWrapStyleWord(true);

        approveButton.setBackground(new Color(46, 125, 50));
        approveButton.setForeground(Color.WHITE);
        rejectButton.setBackground(new Color(198, 40, 40));
        rejectButton.setForeground(Color.WHITE);

        if (!isApprover) {
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
        } else {
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }

        // Summary labels
        totalRequestsLabel = new JLabel("Total: 0");
        pendingRequestsLabel = new JLabel("Pending: 0");
        approvedRequestsLabel = new JLabel("Approved: 0");
        rejectedRequestsLabel = new JLabel("Rejected: 0");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel - filters and summary
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center - table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom - forms
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(new TitledBorder("Filters"));

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadLeaveRequests();
            updateSummary();
        });
        filterPanel.add(refreshButton);

        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBorder(new TitledBorder("Summary"));

        summaryPanel.add(totalRequestsLabel);
        summaryPanel.add(Box.createHorizontalStrut(15));
        summaryPanel.add(pendingRequestsLabel);
        summaryPanel.add(Box.createHorizontalStrut(15));
        summaryPanel.add(approvedRequestsLabel);
        summaryPanel.add(Box.createHorizontalStrut(15));
        summaryPanel.add(rejectedRequestsLabel);

        panel.add(filterPanel, BorderLayout.WEST);
        panel.add(summaryPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Leave Requests"));

        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create tabbed pane for different forms
        JTabbedPane tabbedPane = new JTabbedPane();

        // New request tab
        JPanel newRequestPanel = createNewRequestPanel();
        tabbedPane.addTab("Submit New Request", newRequestPanel);

        // Approval tab (only for approvers)
        if (isApprover) {
            JPanel approvalPanel = createApprovalPanel();
            tabbedPane.addTab("Approve/Reject Requests", approvalPanel);
        }

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNewRequestPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Leave Type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Leave Type:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(leaveTypeCombo, gbc);

        // Start Date
        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Start Date:*"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(startDateField, gbc);

        // End Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("End Date:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(endDateField, gbc);

        // Calculate days button
        JButton calculateDaysButton = new JButton("Calculate Days");
        calculateDaysButton.addActionListener(this::calculateLeaveDays);
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(calculateDaysButton, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        panel.add(reasonScrollPane, gbc);

        // Submit button
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitRequestButton, gbc);

        // Required fields note
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.WEST;
        JLabel noteLabel = new JLabel("* Required fields");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC));
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, gbc);

        return panel;
    }

    private JPanel createApprovalPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Instructions
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel instructionLabel = new JLabel("Select a leave request from the table above to approve or reject");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.ITALIC));
        panel.add(instructionLabel, gbc);

        // Approval notes
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Approval Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.5;
        JScrollPane notesScrollPane = new JScrollPane(approvalNotesArea);
        panel.add(notesScrollPane, gbc);

        // Buttons
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(approveButton, gbc);

        gbc.gridx = 2;
        panel.add(rejectButton, gbc);

        return panel;
    }

    private void setupEventHandlers() {
        // Status filter
        statusFilterCombo.addActionListener(e -> loadLeaveRequests());

        // Submit request
        submitRequestButton.addActionListener(this::handleSubmitRequest);

        // Approval buttons
        approveButton.addActionListener(this::handleApproveRequest);
        rejectButton.addActionListener(this::handleRejectRequest);

        // Table selection
        leaveTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });
    }

    private void calculateLeaveDays(ActionEvent e) {
        try {
            String startDateText = startDateField.getText().trim();
            String endDateText = endDateField.getText().trim();

            if (startDateText.isEmpty() || endDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both start and end dates.",
                        "Missing Dates", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this,
                        "End date cannot be before start date.",
                        "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

            JOptionPane.showMessageDialog(this,
                    String.format("Total leave days: %d", days),
                    "Leave Days Calculation", JOptionPane.INFORMATION_MESSAGE);

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD format.",
                    "Date Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSubmitRequest(ActionEvent e) {
        try {
            // Validate form
            String leaveType = (String) leaveTypeCombo.getSelectedItem();
            String startDateText = startDateField.getText().trim();
            String endDateText = endDateField.getText().trim();

            if (startDateText.isEmpty() || endDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both start and end dates.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this,
                        "End date cannot be before start date.",
                        "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Submit leave request
            boolean success = leaveService.submitLeaveRequest(
                    employee.getId(), leaveType, startDate, endDate);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Leave request submitted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear form
                clearNewRequestForm();
                loadLeaveRequests();
                updateSummary();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit leave request. You may have overlapping leave dates.",
                        "Submission Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD format.",
                    "Date Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error submitting leave request", ex);
            JOptionPane.showMessageDialog(this,
                    "Error submitting leave request: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleApproveRequest(ActionEvent e) {
        if (selectedLeaveRequest == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request to approve.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!selectedLeaveRequest.isPending()) {
            JOptionPane.showMessageDialog(this,
                    "Only pending requests can be approved.",
                    "Invalid Status", JOptionPane.WARNING_MESSAGE);
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

                loadLeaveRequests();
                updateSummary();
                clearApprovalForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to approve leave request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRejectRequest(ActionEvent e) {
        if (selectedLeaveRequest == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request to reject.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!selectedLeaveRequest.isPending()) {
            JOptionPane.showMessageDialog(this,
                    "Only pending requests can be rejected.",
                    "Invalid Status", JOptionPane.WARNING_MESSAGE);
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

                loadLeaveRequests();
                updateSummary();
                clearApprovalForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to reject leave request.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleTableSelection() {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow >= 0) {
            int leaveId = (Integer) tableModel.getValueAt(selectedRow, 0);

            Optional<LeaveRequest> optionalLeave = leaveService.getLeaveRequestById(leaveId);
            if (optionalLeave.isPresent()) {
                selectedLeaveRequest = optionalLeave.get();

                if (isApprover && selectedLeaveRequest.isPending()) {
                    approveButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                } else {
                    approveButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                }
            }
        } else {
            selectedLeaveRequest = null;
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }
    }

    private void clearNewRequestForm() {
        leaveTypeCombo.setSelectedIndex(0);
        startDateField.setText("");
        endDateField.setText("");
        reasonArea.setText("");
    }

    private void clearApprovalForm() {
        approvalNotesArea.setText("");
        selectedLeaveRequest = null;
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        leaveTable.clearSelection();
    }

    private void loadLeaveRequests() {
        try {
            String statusFilter = (String) statusFilterCombo.getSelectedItem();
            List<LeaveRequest> requests;

            if (isApprover && !"All".equals(statusFilter)) {
                // HR/Managers can see requests by status
                requests = leaveService.getLeaveRequestsByStatus(statusFilter);
            } else if (isApprover && "All".equals(statusFilter)) {
                // HR/Managers can see all requests (you might want to add this method)
                requests = leaveService.getPendingLeaveRequests(); // For now, show pending
                // Add approved and rejected
                requests.addAll(leaveService.getLeaveRequestsByStatus("Approved"));
                requests.addAll(leaveService.getLeaveRequestsByStatus("Rejected"));
            } else {
                // Regular employees see only their requests
                requests = leaveService.getLeaveRequestsByEmployee(employee.getId());

                // Filter by status if needed
                if (!"All".equals(statusFilter)) {
                    requests.removeIf(req -> !req.getStatus().equals(statusFilter));
                }
            }

            // Clear existing data
            tableModel.setRowCount(0);

            // Add leave request data to table
            for (LeaveRequest request : requests) {
                Object[] rowData = {
                        request.getLeaveId(),
                        getEmployeeName(request.getEmployeeId()),
                        request.getLeaveType(),
                        request.getStartDateAsLocalDate().toString(),
                        request.getEndDateAsLocalDate().toString(),
                        request.getLeaveDays(),
                        request.getStatus(),
                        request.getCreatedAt() != null ?
                                request.getCreatedAt().toLocalDate().toString() : "Unknown"
                };
                tableModel.addRow(rowData);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading leave requests", e);
            JOptionPane.showMessageDialog(this,
                    "Error loading leave requests: " + e.getMessage(),
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getEmployeeName(int employeeId) {
        if (employeeId == employee.getId()) {
            return employee.getFullName() + " (You)";
        }

        // For HR users, try to get the actual employee name
        if (isApprover) {
            try {
                // You might want to add a method to get employee name by ID
                return "Employee " + employeeId;
            } catch (Exception e) {
                return "Employee " + employeeId;
            }
        }

        return "Employee " + employeeId;
    }

    private void updateSummary() {
        try {
            List<LeaveRequest> allRequests;

            if (isApprover) {
                // For approvers, count all requests in the system
                allRequests = leaveService.getPendingLeaveRequests();
                allRequests.addAll(leaveService.getLeaveRequestsByStatus("Approved"));
                allRequests.addAll(leaveService.getLeaveRequestsByStatus("Rejected"));
            } else {
                // For regular employees, count only their requests
                allRequests = leaveService.getLeaveRequestsByEmployee(employee.getId());
            }

            int total = allRequests.size();
            int pending = (int) allRequests.stream().filter(LeaveRequest::isPending).count();
            int approved = (int) allRequests.stream().filter(LeaveRequest::isApproved).count();
            int rejected = (int) allRequests.stream().filter(LeaveRequest::isRejected).count();

            totalRequestsLabel.setText("Total: " + total);
            pendingRequestsLabel.setText("Pending: " + pending);
            approvedRequestsLabel.setText("Approved: " + approved);
            rejectedRequestsLabel.setText("Rejected: " + rejected);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating summary", e);
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
                switch (status) {
                    case "Pending":
                        c.setBackground(new Color(255, 243, 224)); // Light orange
                        break;
                    case "Approved":
                        c.setBackground(new Color(235, 255, 235)); // Light green
                        break;
                    case "Rejected":
                        c.setBackground(new Color(255, 235, 235)); // Light red
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