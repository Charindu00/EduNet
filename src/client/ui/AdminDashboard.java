package client.ui;

import client.ChatClient;
import utils.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AdminDashboard.java
 * 
 * Administrative control panel for EduNet.
 * 
 * FEATURES:
 * - Real-time view of connected users
 * - Server statistics (uptime, messages, files)
 * - Kick users from server
 * - Broadcast admin messages
 * - View server logs
 * - Monitor system health
 * 
 * KEY CONCEPTS:
 * - JTable for tabular data display
 * - Timer for auto-refresh
 * - Admin privileges and controls
 * - Real-time monitoring
 */
public class AdminDashboard extends JFrame implements ChatClient.MessageListener {
    
    // ==================== COMPONENTS ====================
    
    private ChatClient client;
    
    // User table
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;
    
    // Statistics
    private JLabel uptimeLabel;
    private JLabel usersOnlineLabel;
    private JLabel messagesSentLabel;
    private JLabel filesTransferredLabel;
    private JLabel announcementsSentLabel;
    
    // Chat/Log area
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    
    // Control buttons
    private JButton kickButton;
    private JButton broadcastButton;
    private JButton refreshButton;
    private JButton viewLogsButton;
    
    // Status
    private JLabel statusLabel;
    private JLabel adminInfoLabel;
    
    // Auto-refresh timer
    private Timer refreshTimer;
    
    // Statistics tracking
    private int messageCount = 0;
    private int fileCount = 0;
    private int announcementCount = 0;
    private LocalDateTime serverStartTime;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public AdminDashboard(ChatClient client) {
        this.client = client;
        this.serverStartTime = LocalDateTime.now();
        
        // Register as message listener
        client.addMessageListener(this);
        
        // Set up window
        setTitle("EduNet - Admin Dashboard: " + client.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create UI
        initComponents();
        
        // Start auto-refresh
        startAutoRefresh();
        
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Show window
        setVisible(true);
        
        // Initial load
        appendToLog("SYSTEM", "Admin dashboard initialized");
        appendToLog("SYSTEM", "Welcome, Administrator " + client.getUsername() + "!");
        appendToLog("SYSTEM", "=" + "=".repeat(60) + "\n");
        
        // Load initial data
        refreshUserList();
    }
    
    
    // ==================== UI INITIALIZATION ====================
    
    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        
        // Top panel (admin info + status)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel (users table + logs)
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Right panel (statistics + controls)
        JPanel rightPanel = createRightPanel();
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Create top panel with admin info
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(231, 76, 60));  // Admin red color
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Admin info
        adminInfoLabel = new JLabel("👨‍💼 Administrator: " + client.getUsername());
        adminInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        adminInfoLabel.setForeground(Color.WHITE);
        
        // Status
        statusLabel = new JLabel("Connected [ADMIN]");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(46, 204, 113));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(adminInfoLabel, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create center panel with users table and logs
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Split: Top = Users Table, Bottom = Logs
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // Users table panel
        JPanel usersPanel = createUsersTablePanel();
        splitPane.setTopComponent(usersPanel);
        
        // Logs panel
        JPanel logsPanel = createLogsPanel();
        splitPane.setBottomComponent(logsPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create users table panel
     */
    private JPanel createUsersTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Connected Users"));
        
        // Table columns
        String[] columns = {"Username", "Role", "IP Address", "Connected At", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Read-only table
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(120);  // Username
        userTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Role
        userTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // IP
        userTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Time
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Status
        
        tableScrollPane = new JScrollPane(userTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create logs panel
     */
    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(5, 5, 5, 5));
        
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create right panel with statistics and controls
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setPreferredSize(new Dimension(250, 0));
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Controls panel
        JPanel controlsPanel = createControlsPanel();
        panel.add(controlsPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Create statistics panel
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Server Statistics"));
        
        // Uptime
        uptimeLabel = new JLabel("Uptime: 0m");
        uptimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(uptimeLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Users online
        usersOnlineLabel = new JLabel("Users Online: 0");
        usersOnlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usersOnlineLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Messages sent
        messagesSentLabel = new JLabel("Messages: 0");
        messagesSentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(messagesSentLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Files transferred
        filesTransferredLabel = new JLabel("Files: 0");
        filesTransferredLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(filesTransferredLabel);
        panel.add(Box.createVerticalStrut(5));
        
        // Announcements
        announcementsSentLabel = new JLabel("Announcements: 0");
        announcementsSentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(announcementsSentLabel);
        
        return panel;
    }
    
    /**
     * Create controls panel
     */
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Admin Controls"));
        
        // Refresh button
        refreshButton = createControlButton("🔄 Refresh Users", "Reload user list");
        refreshButton.addActionListener(e -> refreshUserList());
        panel.add(refreshButton);
        panel.add(Box.createVerticalStrut(8));
        
        // Kick user button
        kickButton = createControlButton("⚠️  Kick User", "Disconnect selected user");
        kickButton.addActionListener(e -> kickSelectedUser());
        panel.add(kickButton);
        panel.add(Box.createVerticalStrut(8));
        
        // Broadcast button
        broadcastButton = createControlButton("📢 Broadcast", "Send admin message");
        broadcastButton.addActionListener(e -> broadcastAdminMessage());
        panel.add(broadcastButton);
        panel.add(Box.createVerticalStrut(8));
        
        // View logs button
        viewLogsButton = createControlButton("📋 View Logs", "Open server logs");
        viewLogsButton.addActionListener(e -> viewServerLogs());
        panel.add(viewLogsButton);
        panel.add(Box.createVerticalStrut(8));
        
        // Clear log button
        JButton clearLogButton = createControlButton("🗑️ Clear Log", "Clear activity log");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogButton);
        
        return panel;
    }
    
    /**
     * Create control button
     */
    private JButton createControlButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 35));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }
    
    
    // ==================== AUTO-REFRESH ====================
    
    /**
     * Start auto-refresh timer (every 5 seconds)
     */
    private void startAutoRefresh() {
        refreshTimer = new Timer(5000, e -> {
            refreshUserList();
            updateStatistics();
        });
        refreshTimer.start();
    }
    
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Refresh connected users list
     */
    private void refreshUserList() {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // TODO: Get actual connected users from server
        // For now, add mock data
        addMockUsers();
        
        // Update count
        usersOnlineLabel.setText("Users Online: " + tableModel.getRowCount());
    }
    
    /**
     * Add mock users for demonstration
     * TODO: Replace with real server data
     */
    private void addMockUsers() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        
        // Add some sample users
        tableModel.addRow(new Object[]{"teacher1", "TEACHER", "127.0.0.1", timestamp, "ACTIVE"});
        tableModel.addRow(new Object[]{"student1", "STUDENT", "127.0.0.1", timestamp, "ACTIVE"});
        tableModel.addRow(new Object[]{"student2", "STUDENT", "127.0.0.1", timestamp, "ACTIVE"});
    }
    
    /**
     * Kick selected user
     */
    private void kickSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a user to kick.",
                "No User Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String role = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Confirm kick
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Kick user: " + username + " (" + role + ")?\n\nThis will disconnect them immediately.",
            "Confirm Kick User",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Send kick command to server
            appendToLog("ADMIN", "Kicked user: " + username);
            JOptionPane.showMessageDialog(
                this,
                "User " + username + " has been kicked.",
                "User Kicked",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Refresh list
            refreshUserList();
        }
    }
    
    
    // ==================== ADMIN ACTIONS ====================
    
    /**
     * Broadcast admin message to all users
     */
    private void broadcastAdminMessage() {
        String message = JOptionPane.showInputDialog(
            this,
            "Enter admin broadcast message:",
            "Admin Broadcast",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (message != null && !message.trim().isEmpty()) {
            // Send as admin broadcast
            client.sendBroadcast("[ADMIN] " + message);
            appendToLog("ADMIN", "Broadcast: " + message);
        }
    }
    
    /**
     * View server logs
     */
    private void viewServerLogs() {
        // TODO: Implement log viewer
        JOptionPane.showMessageDialog(
            this,
            "Server logs viewer will be implemented here.\n" +
            "For now, check: data/chat_logs.txt",
            "Server Logs",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    
    // ==================== STATISTICS ====================
    
    /**
     * Update statistics display
     */
    private void updateStatistics() {
        // Calculate uptime
        long minutes = java.time.Duration.between(serverStartTime, LocalDateTime.now()).toMinutes();
        if (minutes < 60) {
            uptimeLabel.setText("Uptime: " + minutes + "m");
        } else {
            long hours = minutes / 60;
            long mins = minutes % 60;
            uptimeLabel.setText("Uptime: " + hours + "h " + mins + "m");
        }
        
        // Update counters
        messagesSentLabel.setText("Messages: " + messageCount);
        filesTransferredLabel.setText("Files: " + fileCount);
        announcementsSentLabel.setText("Announcements: " + announcementCount);
    }
    
    
    // ==================== LOGGING ====================
    
    /**
     * Append message to log area
     */
    private void appendToLog(String source, String message) {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        
        logArea.append(String.format("[%s] %s: %s\n", timestamp, source, message));
        
        // Auto-scroll to bottom
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    
    // ==================== MESSAGE LISTENER IMPLEMENTATION ====================
    
    @Override
    public void onMessageReceived(Message message) {
        // Track message
        messageCount++;
        
        // Log message
        String logMsg = String.format("%s -> %s: %s",
            message.getSender(),
            message.getRecipient(),
            message.getContent().substring(0, Math.min(50, message.getContent().length()))
        );
        appendToLog("MESSAGE", logMsg);
        
        // Handle specific message types
        switch (message.getType()) {
            case FILE_NOTIFICATION:
                fileCount++;
                break;
                
            case ANNOUNCEMENT:
                announcementCount++;
                break;
                
            case DISCONNECT:
                appendToLog("SYSTEM", message.getContent());
                refreshUserList();
                break;
                
            case SERVER_SHUTDOWN:
                appendToLog("SYSTEM", "⚠️  " + message.getContent());
                break;
                
            default:
                // Handle all other message types
                break;
        }
        
        updateStatistics();
    }
    
    @Override
    public void onConnectionLost(String reason) {
        // Update status
        statusLabel.setText("Disconnected [OFFLINE]");
        statusLabel.setForeground(Color.RED);
        
        // Disable controls
        kickButton.setEnabled(false);
        broadcastButton.setEnabled(false);
        refreshButton.setEnabled(false);
        
        // Stop auto-refresh
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        
        // Show error
        appendToLog("SYSTEM", "❌ Connection lost: " + reason);
        
        JOptionPane.showMessageDialog(
            this,
            "Connection to server was lost.\n" + reason,
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    
    // ==================== EXIT HANDLING ====================
    
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit the admin dashboard?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            if (refreshTimer != null) {
                refreshTimer.stop();
            }
            client.disconnect();
            System.exit(0);
        }
    }
}
