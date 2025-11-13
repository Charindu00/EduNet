package client.ui;

import client.ChatClient;
import client.FileTransferClient;
import client.UDPAnnouncementListener;
import utils.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * StudentWindow.java
 * 
 * GUI for students in EduNet.
 * 
 * FEATURES:
 * - View broadcast messages from teacher
 * - Send messages to teacher
 * - Receive announcements
 * - View connected users
 * - Clean, organized layout
 */
public class StudentWindow extends JFrame implements ChatClient.MessageListener {
    
    // ==================== COMPONENTS ====================
    
    private ChatClient client;
    private UDPAnnouncementListener udpListener;
    
    // Chat area
    private JTextArea chatArea;
    private JScrollPane chatScrollPane;
    
    // Input
    private JTextField messageField;
    private JButton sendButton;
    
    // Status
    private JLabel statusLabel;
    private JLabel userInfoLabel;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public StudentWindow(ChatClient client) {
        this.client = client;
        
        // Register as message listener
        client.addMessageListener(this);
        
        // Set up window
        setTitle("EduNet - Student: " + client.getUsername());
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create UI
        initComponents();
        
        // Start UDP listener for announcements
        startUDPListener();
        
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Show window
        setVisible(true);
        
        // Welcome message
        appendToChat("SYSTEM", "Welcome to EduNet, " + client.getUsername() + "!");
        appendToChat("SYSTEM", "You can send messages to your teacher using the text field below.");
        appendToChat("SYSTEM", "=" + "=".repeat(60) + "\n");
    }
    
    
    // ==================== UI INITIALIZATION ====================
    
    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        
        // Top panel (user info + status)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel (chat area)
        JPanel centerPanel = createChatPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Right panel (features)
        JPanel rightPanel = createFeaturesPanel();
        add(rightPanel, BorderLayout.EAST);
        
        // Bottom panel (input)
        JPanel bottomPanel = createInputPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create top panel with user info
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // User info
        userInfoLabel = new JLabel("Student: " + client.getUsername());
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userInfoLabel.setForeground(Color.WHITE);
        
        // Status
        statusLabel = new JLabel("Connected [ONLINE]");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(46, 204, 113));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(userInfoLabel, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create chat display area
     */
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(chatScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create input panel
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        // Label
        JLabel label = new JLabel("Message to Teacher:");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, BorderLayout.NORTH);
        
        // Input field
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 13));
        messageField.addActionListener(e -> sendMessage());
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(52, 152, 219));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create features panel (right side)
     */
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Features")
        ));
        panel.setPreferredSize(new Dimension(180, 0));
        
        // Download Files button
        JButton downloadButton = new JButton("<html><center>Download<br>Files</center></html>");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 12));
        downloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        downloadButton.setMaximumSize(new Dimension(150, 60));
        downloadButton.setBackground(new Color(52, 152, 219));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.addActionListener(e -> handleFileDownload());
        
        panel.add(Box.createVerticalStrut(10));
        panel.add(downloadButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Info label
        JLabel infoLabel = new JLabel("<html><center>Browse and download<br>lectures and assignments</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    
    // ==================== MESSAGE HANDLING ====================
    
    /**
     * Send message to teacher
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        // Send to server
        client.sendMessageToTeacher(message);
        
        // Display in chat (as sent by me)
        appendToChat("YOU → TEACHER", message);
        
        // Clear input
        messageField.setText("");
        messageField.requestFocus();
    }
    
    /**
     * Append message to chat area
     */
    private void appendToChat(String sender, String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        
        chatArea.append(String.format("[%s] %s: %s\n", timestamp, sender, message));
        
        // Auto-scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    
    // ==================== MESSAGE LISTENER IMPLEMENTATION ====================
    
    @Override
    public void onMessageReceived(Message message) {
        // Handle different message types
        switch (message.getType()) {
            case CHAT_BROADCAST:
                // Teacher broadcast
                appendToChat("TEACHER (Broadcast)", message.getContent());
                break;
                
            case CHAT_PRIVATE:
                // Private message from teacher
                appendToChat("TEACHER (Private)", message.getContent());
                break;
                
            case ANNOUNCEMENT:
                // UDP announcement
                showAnnouncement(message.getContent());
                break;
                
            case DISCONNECT:
                // Someone disconnected
                if (!message.getSender().equals(client.getUsername())) {
                    appendToChat("SYSTEM", message.getContent());
                }
                break;
                
            case SERVER_SHUTDOWN:
                // Server is shutting down
                appendToChat("SYSTEM", "⚠️ " + message.getContent());
                JOptionPane.showMessageDialog(
                    this,
                    message.getContent(),
                    "Server Shutdown",
                    JOptionPane.WARNING_MESSAGE
                );
                break;
                
            default:
                // Other messages
                appendToChat(message.getSender(), message.getContent());
        }
    }
    
    @Override
    public void onConnectionLost(String reason) {
        // Update status
        statusLabel.setText("Disconnected [OFFLINE]");
        statusLabel.setForeground(Color.RED);
        
        // Disable input
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        // Show error
        appendToChat("SYSTEM", "❌ Connection lost: " + reason);
        
        JOptionPane.showMessageDialog(
            this,
            "Connection to server was lost.\n" + reason,
            "Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show announcement in popup
     */
    private void showAnnouncement(String announcement) {
        appendToChat("📢 ANNOUNCEMENT", announcement);
        
        // Also show as popup
        JOptionPane.showMessageDialog(
            this,
            announcement,
            "📢 Announcement from Teacher",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    
    // ==================== UDP LISTENER ====================
    
    /**
     * Start UDP listener for receiving announcements
     */
    private void startUDPListener() {
        udpListener = new UDPAnnouncementListener(new UDPAnnouncementListener.AnnouncementCallback() {
            @Override
            public void onAnnouncementReceived(String announcement) {
                // Handle announcement on UI thread
                SwingUtilities.invokeLater(() -> {
                    showAnnouncement(announcement);
                });
            }
            
            @Override
            public void onRegistrationConfirmed(String message) {
                // Registration successful
                SwingUtilities.invokeLater(() -> {
                    appendToChat("SYSTEM", "✅ " + message);
                });
            }
            
            @Override
            public void onError(String error) {
                // Handle error
                SwingUtilities.invokeLater(() -> {
                    appendToChat("SYSTEM", "⚠️  UDP Error: " + error);
                });
            }
        });
        
        udpListener.start();
    }
    
    
    // ==================== FILE DOWNLOAD ====================
    
    /**
     * Handle file download - browse and download lectures/assignments
     */
    private void handleFileDownload() {
        // Get available files from server directories
        File lecturesDir = new File("data/files/lectures");
        File assignmentsDir = new File("data/files/assignments");
        
        java.util.List<String> availableFiles = new java.util.ArrayList<>();
        
        // Add lectures
        if (lecturesDir.exists() && lecturesDir.isDirectory()) {
            File[] lectures = lecturesDir.listFiles();
            if (lectures != null) {
                for (File file : lectures) {
                    if (file.isFile()) {
                        availableFiles.add("📚 Lecture: " + file.getName());
                    }
                }
            }
        }
        
        // Add assignments
        if (assignmentsDir.exists() && assignmentsDir.isDirectory()) {
            File[] assignments = assignmentsDir.listFiles();
            if (assignments != null) {
                for (File file : assignments) {
                    if (file.isFile()) {
                        availableFiles.add("📝 Assignment: " + file.getName());
                    }
                }
            }
        }
        
        // Check if any files available
        if (availableFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No files available for download yet.\nYour teacher hasn't uploaded any materials.",
                "No Files Available",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        // Show file selection dialog
        String[] fileArray = availableFiles.toArray(new String[0]);
        String selectedFile = (String) JOptionPane.showInputDialog(
            this,
            "Select a file to download:",
            "Available Files",
            JOptionPane.QUESTION_MESSAGE,
            null,
            fileArray,
            fileArray[0]
        );
        
        if (selectedFile == null) {
            return; // User cancelled
        }
        
        // Extract filename (remove prefix)
        String filename = selectedFile.substring(selectedFile.indexOf(": ") + 2);
        
        // Choose download location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File As");
        fileChooser.setSelectedFile(new File(filename));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // User cancelled
        }
        
        File destination = fileChooser.getSelectedFile();
        
        // Confirm download
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Download file: " + filename + "?\n" +
            "Save to: " + destination.getAbsolutePath(),
            "Confirm Download",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Downloading File", true);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setLayout(new BorderLayout(10, 10));
        
        JLabel statusLabel = new JLabel("Preparing download...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
        
        progressDialog.add(statusLabel, BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        
        // Download in background thread
        new Thread(() -> {
            FileTransferClient fileTransfer = new FileTransferClient(
                client.getUsername(),
                client.getRole()
            );
            
            boolean success = fileTransfer.downloadFile(filename, destination, (message, percentage) -> {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(message);
                    if (percentage >= 0) {
                        progressBar.setValue(percentage);
                    }
                });
            });
            
            // Close dialog and show result
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                
                if (success) {
                    appendToChat("SYSTEM", "✅ File downloaded successfully: " + filename);
                    JOptionPane.showMessageDialog(
                        this,
                        "File downloaded successfully!\nSaved to: " + destination.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    appendToChat("SYSTEM", "❌ File download failed");
                    JOptionPane.showMessageDialog(
                        this,
                        "File download failed. The file may not be available.",
                        "Download Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });
        }).start();
        
        // Show dialog (blocks until download completes)
        progressDialog.setVisible(true);
    }
    
    
    // ==================== EXIT HANDLING ====================
    
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Stop UDP listener
            if (udpListener != null) {
                udpListener.stop();
            }
            
            client.disconnect();
            System.exit(0);
        }
    }
}
