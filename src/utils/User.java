package utils;

import java.io.Serializable;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User.java
 * 
 * Represents a user in the EduNet system.
 * 
 * USAGE:
 * - Server maintains a list of active User objects (connected clients)
 * - Client stores its own User object after successful login
 * - Admin dashboard displays User information
 * 
 * WHY SERIALIZABLE?
 * - Can be sent in Message.data field (e.g., user lists to admin)
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== FIELDS ====================
    
    private String username;                // Unique identifier
    private String password;                // Stored in plain text (for simplicity - not secure!)
    private Constants.UserRole role;        // TEACHER, STUDENT, or ADMIN
    private String ipAddress;               // Client's IP (from socket)
    private int port;                       // Client's port
    private LocalDateTime loginTime;        // When did they connect?
    private boolean isOnline;               // Currently connected?
    
    // Transient = don't serialize this field (can't send Socket over network)
    private transient Socket socket;        // Network connection (server-side only)
    
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Constructor for creating user from credentials file
     * Used during authentication
     */
    public User(String username, String password, Constants.UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isOnline = false;
        this.loginTime = null;
        this.ipAddress = null;
        this.port = 0;
        this.socket = null;
    }
    
    /**
     * Constructor for active connected user (server-side)
     */
    public User(String username, String password, Constants.UserRole role, Socket socket) {
        this(username, password, role);
        this.socket = socket;
        this.isOnline = true;
        this.loginTime = LocalDateTime.now();
        
        if (socket != null) {
            this.ipAddress = socket.getInetAddress().getHostAddress();
            this.port = socket.getPort();
        }
    }
    
    /**
     * Copy constructor (for sending user info without socket)
     */
    public User(User other) {
        this.username = other.username;
        this.password = other.password;
        this.role = other.role;
        this.ipAddress = other.ipAddress;
        this.port = other.port;
        this.loginTime = other.loginTime;
        this.isOnline = other.isOnline;
        // Note: socket is NOT copied (transient)
    }
    
    
    // ==================== GETTERS & SETTERS ====================
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Constants.UserRole getRole() {
        return role;
    }
    
    public void setRole(Constants.UserRole role) {
        this.role = role;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public void setSocket(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            this.ipAddress = socket.getInetAddress().getHostAddress();
            this.port = socket.getPort();
        }
    }
    
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get formatted connection time (how long they've been online)
     */
    public String getConnectionDuration() {
        if (loginTime == null) {
            return "N/A";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(loginTime, now).toMinutes();
        
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
    
    /**
     * Get formatted login time as string
     */
    public String getLoginTimeFormatted() {
        if (loginTime == null) {
            return "N/A";
        }
        return loginTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    /**
     * Check if user has teacher privileges
     */
    public boolean isTeacher() {
        return role == Constants.UserRole.TEACHER;
    }
    
    /**
     * Check if user has student role
     */
    public boolean isStudent() {
        return role == Constants.UserRole.STUDENT;
    }
    
    /**
     * Check if user has admin privileges
     */
    public boolean isAdmin() {
        return role == Constants.UserRole.ADMIN;
    }
    
    /**
     * Validate password
     */
    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    /**
     * Mark user as disconnected
     */
    public void disconnect() {
        this.isOnline = false;
        if (this.socket != null && !this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get connection status as emoji
     */
    public String getStatusIcon() {
        return isOnline ? "[ONLINE]" : "[OFFLINE]";
    }
    
    /**
     * Get role as icon
     */
    public String getRoleIcon() {
        switch (role) {
            case TEACHER:
                return "[T]";
            case STUDENT:
                return "[S]";
            case ADMIN:
                return "[A]";
            default:
                return "[U]";
        }
    }
    
    
    // ==================== OVERRIDE METHODS ====================
    
    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        return String.format("User[%s, role=%s, online=%s, ip=%s]", 
                            username, role, isOnline, ipAddress);
    }
    
    /**
     * Display format for UI (admin dashboard)
     */
    public String toDisplayString() {
        return String.format("%s %s (%s) - %s - Connected: %s", 
                            getStatusIcon(), 
                            getRoleIcon(), 
                            username, 
                            ipAddress != null ? ipAddress : "N/A",
                            getConnectionDuration());
    }
    
    /**
     * Format for saving to file
     * Format: username:password:role
     */
    public String toFileFormat() {
        return String.format("%s:%s:%s", username, password, role);
    }
    
    /**
     * Parse user from file line
     * Format: username:password:role
     */
    public static User fromFileFormat(String line) {
        String[] parts = line.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid user format: " + line);
        }
        
        String username = parts[0].trim();
        String password = parts[1].trim();
        Constants.UserRole role = Constants.UserRole.valueOf(parts[2].trim().toUpperCase());
        
        return new User(username, password, role);
    }
    
    /**
     * Compare users by username
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
