package client.ui;

import client.ChatClient;
import client.FileTransferClient;
import utils.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * TeacherWindow.java
 * 
 * GUI for teachers in EduNet.
 * 
 * FEATURES:
 * - Broadcast messages to all students
 * - Send private messages to specific students
 * - Upload lecture files (Phase 4)
 * - Send announcements (Phase 4 - UDP)
 * - View all messages
 */
public class TeacherWindow extends JFrame implements ChatClient.MessageListener {
    
    // ==================== COMPONENTS ====================
    
    private ChatClient client;
    
    // Chat area
    private JTextArea chatArea;
    private JScrollPane chatScrollPane;
    
    // Input
    private JTextField messageField;
    private JButton broadcastButton;
    private JButton privateButton;
    
    // Additional features
    private JButton fileButton;
    private JButton announcementButton;
    
    // Status
    private JLabel statusLabel;
    private JLabel userInfoLabel;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public TeacherWindow(ChatClient client) {
        this.client = client;
        
        // Register as message listener
        client.addMessageListener(this);
        
        // Set up window
        setTitle("EduNet - Teacher: " + client.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create UI
        initComponents();
        
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
        appendToChat("SYSTEM", "Welcome, Teacher " + client.getUsername() + "!");
        appendToChat("SYSTEM", "You can broadcast messages to all students.");
        appendToChat("SYSTEM", "Use 'Private Message' to send to a specific student.");
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
        
        // Bottom panel (input + buttons)
        JPanel bottomPanel = createInputPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Right panel (additional features)
        JPanel rightPanel = createFeaturesPanel();
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Create top panel with user info
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // User info
        userInfoLabel = new JLabel("👨‍🏫 Teacher: " + client.getUsername());
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 18));
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
        
        // Title
        JLabel titleLabel = new JLabel("Class Chat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatArea.setBackground(new Color(250, 250, 250));
        
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(chatScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create input panel with buttons
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        // Label
        JLabel label = new JLabel("Compose Message:");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, BorderLayout.NORTH);
        
        // Input field
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 13));
        messageField.addActionListener(e -> broadcastMessage());
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        broadcastButton = new JButton("Broadcast to All");
        broadcastButton.setFont(new Font("Arial", Font.BOLD, 12));
        broadcastButton.setBackground(new Color(52, 152, 219));
        broadcastButton.setForeground(Color.WHITE);
        broadcastButton.setFocusPainted(false);
        broadcastButton.setPreferredSize(new Dimension(160, 35));
        broadcastButton.addActionListener(e -> broadcastMessage());
        
        privateButton = new JButton("Private Message");
        privateButton.setFont(new Font("Arial", Font.PLAIN, 12));
        privateButton.setPreferredSize(new Dimension(140, 35));
        privateButton.addActionListener(e -> sendPrivateMessage());
        
        buttonPanel.add(privateButton);
        buttonPanel.add(broadcastButton);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create features panel (right side)
     */
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setPreferredSize(new Dimension(180, 0));
        
        // Title
        JLabel titleLabel = new JLabel("Features");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        // File upload button
        fileButton = createFeatureButton("Upload File", "Upload lecture files");
        fileButton.addActionListener(e -> handleFileUpload());
        panel.add(fileButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Announcement button
        announcementButton = createFeatureButton("Announcement", "Send UDP broadcast");
        announcementButton.addActionListener(e -> handleAnnouncement());
        panel.add(announcementButton);
        panel.add(Box.createVerticalStrut(10));
        
        // Clear chat button
        JButton clearButton = createFeatureButton("Clear Chat", "Clear chat history");
        clearButton.addActionListener(e -> {
            chatArea.setText("");
            appendToChat("SYSTEM", "Chat cleared.");
        });
        panel.add(clearButton);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Helper to create feature button
     */
    private JButton createFeatureButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setToolTipText(tooltip);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(150, 40));
        button.setFocusPainted(false);
        return button;
    }
    
    
    // ==================== MESSAGE HANDLING ====================
    
    /**
     * Broadcast message to all students
     */
    private void broadcastMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a message to broadcast.",
                "Empty Message",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Send to server
        client.sendBroadcast(message);
        
        // Display in chat
        appendToChat("YOU (Broadcast)", message);
        
        // Clear input
        messageField.setText("");
        messageField.requestFocus();
    }
    
    /**
     * Send private message to specific student
     */
    private void sendPrivateMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a message first.",
                "Empty Message",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Ask for recipient
        String recipient = JOptionPane.showInputDialog(
            this,
            "Enter student username:",
            "Private Message",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (recipient != null && !recipient.trim().isEmpty()) {
            // Send to server
            client.sendPrivateMessage(recipient.trim(), message);
            
            // Display in chat
            appendToChat("YOU → " + recipient, message);
            
            // Clear input
            messageField.setText("");
            messageField.requestFocus();
        }
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
            case CHAT_TO_TEACHER:
                // Message from student
                appendToChat("STUDENT: " + message.getSender(), message.getContent());
                
                // Show notification
                showNotification("New message from " + message.getSender());
                break;
                
            case CHAT_PRIVATE:
                // Private message (from another teacher or system)
                appendToChat(message.getSender() + " (Private)", message.getContent());
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
        broadcastButton.setEnabled(false);
        privateButton.setEnabled(false);
        fileButton.setEnabled(false);
        announcementButton.setEnabled(false);
        
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
     * Show system tray notification (if supported)
     */
    private void showNotification(String message) {
        // Simple visual feedback - flash title
        String originalTitle = getTitle();
        setTitle("💬 " + message);
        
        Timer timer = new Timer(2000, e -> setTitle(originalTitle));
        timer.setRepeats(false);
        timer.start();
    }
    
    
    // ==================== EXIT HANDLING ====================
    
    
    // ==================== FILE TRANSFER ====================
    
    /**
     * Handle file upload (lecture files)
     */
    private void handleFileUpload() {
        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Lecture File to Upload");
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Confirm upload
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Upload file: " + selectedFile.getName() + "?\n" +
                "Size: " + FileUtils.formatFileSize(selectedFile.length()),
                "Confirm Upload",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Show progress dialog
            JDialog progressDialog = new JDialog(this, "Uploading File", true);
            progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(this);
            progressDialog.setLayout(new BorderLayout(10, 10));
            
            JLabel statusLabel = new JLabel("Preparing upload...");
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
            
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
            
            progressDialog.add(statusLabel, BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            
            // Upload in background thread
            new Thread(() -> {
                FileTransferClient fileTransfer = new FileTransferClient(
                    client.getUsername(),
                    client.getRole()
                );
                
                boolean success = fileTransfer.uploadFile(selectedFile, (message, percentage) -> {
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
                        appendToChat("SYSTEM", "✅ File uploaded successfully: " + selectedFile.getName());
                        JOptionPane.showMessageDialog(
                            this,
                            "File uploaded successfully!",
                            "Upload Complete",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        appendToChat("SYSTEM", "❌ File upload failed");
                        JOptionPane.showMessageDialog(
                            this,
                            "File upload failed. Check server connection.",
                            "Upload Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            }).start();
            
            // Show dialog (blocks until upload completes)
            progressDialog.setVisible(true);
        }
    }
    
    
    // ==================== UDP ANNOUNCEMENTS ====================
    
    /**
     * Send UDP announcement to all students
     */
    private void handleAnnouncement() {
        // Get announcement text from user
        String announcement = JOptionPane.showInputDialog(
            this,
            "Enter announcement to broadcast to all students:",
            "Send UDP Announcement",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (announcement == null || announcement.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }
        
        announcement = announcement.trim();
        
        // Confirm sending
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Send this announcement via UDP?\n\n" +
            "\"" + announcement + "\"\n\n" +
            "This will be delivered to all connected students.",
            "Confirm UDP Announcement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Send UDP announcement in background thread
        final String finalAnnouncement = announcement;
        new Thread(() -> {
            try {
                // Create UDP socket
                java.net.DatagramSocket socket = new java.net.DatagramSocket();
                
                // Prepare message: "ANNOUNCEMENT|text"
                String message = "ANNOUNCEMENT|" + finalAnnouncement;
                byte[] data = message.getBytes();
                
                // Send to server's UDP port
                java.net.InetAddress serverAddress = java.net.InetAddress.getByName("localhost");
                java.net.DatagramPacket packet = new java.net.DatagramPacket(
                    data,
                    data.length,
                    serverAddress,
                    Constants.UDP_PORT
                );
                
                socket.send(packet);
                socket.close();
                
                // Success feedback
                SwingUtilities.invokeLater(() -> {
                    appendToChat("SYSTEM", "📢 UDP Announcement sent: " + finalAnnouncement);
                    JOptionPane.showMessageDialog(
                        this,
                        "Announcement sent successfully via UDP!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
                
                System.out.println("📢 Sent UDP announcement: " + finalAnnouncement);
                
            } catch (Exception e) {
                // Error feedback
                SwingUtilities.invokeLater(() -> {
                    appendToChat("SYSTEM", "❌ Failed to send announcement: " + e.getMessage());
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to send announcement:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
                
                System.err.println("❌ UDP announcement error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
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
            client.disconnect();
            System.exit(0);
        }
    }
}
