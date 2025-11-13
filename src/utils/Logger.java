package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger.java
 * 
 * Centralized logging system for EduNet.
 * Writes all important events to chat_logs.txt.
 * 
 * THREAD-SAFE:
 * - Multiple ClientHandler threads may log simultaneously
 * - Uses ReentrantLock to prevent file corruption
 * 
 * LOG TYPES:
 * - INFO: General events (login, logout)
 * - CHAT: Message logs
 * - FILE: File transfer logs
 * - ERROR: Errors and exceptions
 * - ADMIN: Admin actions
 */
public class Logger {
    
    // Single lock for all logging operations
    private static final ReentrantLock lock = new ReentrantLock();
    
    // Date formatter for timestamps
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    // ==================== LOG LEVELS ====================
    
    public enum LogLevel {
        INFO,    // General information
        CHAT,    // Chat messages
        FILE,    // File operations
        ERROR,   // Errors and exceptions
        ADMIN,   // Admin actions
        DEBUG    // Debug information
    }
    
    
    // ==================== PUBLIC LOGGING METHODS ====================
    
    /**
     * Log a general info message
     * Example: "User teacher1 connected from 192.168.1.5"
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * Log a chat message
     * Example: "teacher1 -> ALL: Hello class!"
     */
    public static void chat(String sender, String recipient, String message) {
        String logMessage = String.format("%s -> %s: %s", sender, recipient, message);
        log(LogLevel.CHAT, logMessage);
    }
    
    /**
     * Log a chat message from Message object
     */
    public static void chat(Message message) {
        log(LogLevel.CHAT, message.getLogFormat());
    }
    
    /**
     * Log a file operation
     * Example: "teacher1 uploaded lecture_notes.pdf (2.5 MB)"
     */
    public static void file(String message) {
        log(LogLevel.FILE, message);
    }
    
    /**
     * Log an error
     * Example: "Failed to send message to student1: Connection reset"
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * Log an error with exception
     */
    public static void error(String message, Exception e) {
        String fullMessage = String.format("%s - Exception: %s", message, e.getMessage());
        log(LogLevel.ERROR, fullMessage);
    }
    
    /**
     * Log an admin action
     * Example: "Admin kicked user student1"
     */
    public static void admin(String message) {
        log(LogLevel.ADMIN, message);
    }
    
    /**
     * Log debug information (only in development)
     */
    public static void debug(String message) {
        // Only log if debug mode is enabled
        // For now, always log (can add a flag later)
        log(LogLevel.DEBUG, message);
    }
    
    
    // ==================== CORE LOGGING METHOD ====================
    
    /**
     * Main logging method - Thread-safe file writing
     */
    private static void log(LogLevel level, String message) {
        lock.lock();  // Acquire lock (wait if another thread is logging)
        try {
            // Get current timestamp
            String timestamp = LocalDateTime.now().format(formatter);
            
            // Format: [2025-11-11 14:30:25] [INFO] User connected
            String logEntry = String.format("[%s] [%s] %s%n", timestamp, level, message);
            
            // Write to file
            try (FileWriter fw = new FileWriter(Constants.CHAT_LOGS_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(logEntry);
            } catch (IOException e) {
                // If logging fails, print to console as fallback
                System.err.println("LOGGER ERROR: " + e.getMessage());
                System.err.println("Failed to log: " + logEntry);
            }
            
            // Also print to console for real-time monitoring
            System.out.print(logEntry);
            
        } finally {
            lock.unlock();  // Always release the lock
        }
    }
    
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Initialize log file (create if doesn't exist, add header)
     */
    public static void initialize() {
        lock.lock();
        try {
            File logFile = new File(Constants.CHAT_LOGS_FILE);
            
            // Create parent directories if needed
            logFile.getParentFile().mkdirs();
            
            // If file doesn't exist, create it with header
            if (!logFile.exists()) {
                try (FileWriter fw = new FileWriter(logFile);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("========================================\n");
                    bw.write("       EduNet Activity Log\n");
                    bw.write("       Started: " + LocalDateTime.now().format(formatter) + "\n");
                    bw.write("========================================\n\n");
                } catch (IOException e) {
                    System.err.println("Failed to initialize log file: " + e.getMessage());
                }
            }
            
            info("Logger initialized successfully");
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Clear the log file (use with caution!)
     */
    public static void clearLogs() {
        lock.lock();
        try {
            File logFile = new File(Constants.CHAT_LOGS_FILE);
            if (logFile.exists()) {
                logFile.delete();
            }
            initialize();  // Recreate with header
            info("Logs cleared");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Read all logs from file (for admin dashboard)
     */
    public static String readLogs() {
        StringBuilder logs = new StringBuilder();
        lock.lock();
        try {
            File logFile = new File(Constants.CHAT_LOGS_FILE);
            if (!logFile.exists()) {
                return "No logs available.";
            }
            
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logs.append(line).append("\n");
                }
            } catch (IOException e) {
                return "Error reading logs: " + e.getMessage();
            }
            
        } finally {
            lock.unlock();
        }
        return logs.toString();
    }
    
    /**
     * Read last N lines of log (for quick view)
     */
    public static String readLastLogs(int numLines) {
        lock.lock();
        try {
            File logFile = new File(Constants.CHAT_LOGS_FILE);
            if (!logFile.exists()) {
                return "No logs available.";
            }
            
            // Read all lines first (simple approach for small files)
            java.util.List<String> allLines = new java.util.ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    allLines.add(line);
                }
            } catch (IOException e) {
                return "Error reading logs: " + e.getMessage();
            }
            
            // Get last N lines
            int startIndex = Math.max(0, allLines.size() - numLines);
            StringBuilder result = new StringBuilder();
            for (int i = startIndex; i < allLines.size(); i++) {
                result.append(allLines.get(i)).append("\n");
            }
            
            return result.toString();
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Get log file size in MB
     */
    public static String getLogFileSize() {
        File logFile = new File(Constants.CHAT_LOGS_FILE);
        if (!logFile.exists()) {
            return "0 MB";
        }
        
        long sizeInBytes = logFile.length();
        double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
        
        if (sizeInMB < 0.01) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeInMB);
        }
    }
}
