package client.ui;

import client.ChatClient;
import utils.Constants;
import javax.swing.*;
import java.awt.*;

/**
 * LoginWindow.java
 * 
 * The entry point GUI for EduNet.
 * Users enter credentials and select their role.
 * 
 * FEATURES:
 * - Clean login form
 * - Role selection (Teacher/Student/Admin)
 * - Connection status indicator
 * - Input validation
 * - Opens appropriate window based on role after login
 */
public class LoginWindow extends JFrame {
    
    // ==================== COMPONENTS ====================
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    private ChatClient client;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public LoginWindow() {
        // Initialize client
        client = new ChatClient();
        
        // Set up window
        setTitle(Constants.APP_NAME + " - Login");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center on screen
        setResizable(false);
        
        // Create UI
        initComponents();
        
        // Show window
        setVisible(true);
    }
    
    
    // ==================== UI INITIALIZATION ====================
    
    private void initComponents() {
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(245, 245, 250));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Login form
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Create header with title and logo
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 250));
        
        // Title
        JLabel titleLabel = new JLabel("EduNet");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Educational Communication Platform");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        return panel;
    }
    
    /**
     * Create login form
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(passwordField, gbc);
        
        // Role
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(roleLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] roles = {"TEACHER", "STUDENT", "ADMIN"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        roleComboBox.setSelectedIndex(1);  // Default to STUDENT
        panel.add(roleComboBox, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        statusLabel = new JLabel("Enter credentials to login");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, gbc);
        
        // Progress bar (hidden by default)
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 5, 5, 5);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        panel.add(progressBar, gbc);
        
        // Enter key triggers login
        passwordField.addActionListener(e -> handleLogin());
        
        return panel;
    }
    
    /**
     * Create button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(new Color(245, 245, 250));
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        
        // Exit button
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 16));
        exitButton.setPreferredSize(new Dimension(120, 40));
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));
        
        panel.add(loginButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handle login button click
     */
    private void handleLogin() {
        // Get input
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String roleStr = (String) roleComboBox.getSelectedItem();
        
        // Validate
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        
        // Disable UI during login
        setUIEnabled(false);
        showStatus("Connecting to server...", Color.BLUE);
        progressBar.setVisible(true);
        
        // Perform login in background thread (don't freeze UI)
        new Thread(() -> {
            try {
                // Step 1: Connect
                if (!client.connect()) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Failed to connect to server. Is it running?");
                        setUIEnabled(true);
                        progressBar.setVisible(false);
                    });
                    return;
                }
                
                SwingUtilities.invokeLater(() -> 
                    showStatus("Authenticating...", Color.BLUE)
                );
                
                // Step 2: Login
                Constants.UserRole role = Constants.UserRole.valueOf(roleStr);
                boolean success = client.login(username, password, role);
                
                // Step 3: Handle result
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    
                    if (success) {
                        handleSuccessfulLogin(role);
                    } else {
                        showError("Login failed. Check your credentials.");
                        setUIEnabled(true);
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Error: " + e.getMessage());
                    setUIEnabled(true);
                    progressBar.setVisible(false);
                });
            }
        }).start();
    }
    
    /**
     * Handle successful login - open appropriate window
     */
    private void handleSuccessfulLogin(Constants.UserRole role) {
        showStatus("Login successful! Opening window...", new Color(34, 139, 34));
        
        // Wait a moment to show success message
        Timer timer = new Timer(500, e -> {
            // Hide login window
            setVisible(false);
            
            // Open appropriate window based on role
            switch (role) {
                case TEACHER:
                    new TeacherWindow(client);
                    break;
                    
                case STUDENT:
                    new StudentWindow(client);
                    break;
                    
                case ADMIN:
                    new AdminDashboard(client);
                    break;
            }
            
            // Dispose login window
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    
    // ==================== UI HELPERS ====================
    
    private void setUIEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        roleComboBox.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        exitButton.setEnabled(enabled);
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    private void showError(String message) {
        showStatus(message, Color.RED);
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    
    // ==================== MAIN METHOD ====================
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default if system L&F fails
        }
        
        // Create login window on EDT
        SwingUtilities.invokeLater(() -> new LoginWindow());
    }
}
