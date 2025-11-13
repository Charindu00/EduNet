package utils;

/**
 * Constants.java
 * 
 * Central configuration file for EduNet application.
 * Contains all ports, paths, and message type constants used across the system.
 * 
 * Purpose: Avoid hardcoding values - makes maintenance easier!
 */
public class Constants {
    
    // ==================== NETWORK CONFIGURATION ====================
    
    /**
     * TCP Server Port - Used for main chat server and login
     * All clients connect to this port for authentication and messaging
     */
    public static final int TCP_SERVER_PORT = 5000;
    
    /**
     * TCP File Transfer Port - Dedicated port for file uploads/downloads
     * Separating file transfers prevents blocking chat messages
     */
    public static final int TCP_FILE_PORT = 5001;
    
    /**
     * UDP Broadcast Port - Used for teacher announcements
     * All students listen on this port for broadcast messages
     */
    public static final int UDP_PORT = 6000;
    
    /**
     * Server IP Address - Use localhost for local testing
     * For classroom network, change to actual server machine IP
     */
    public static final String SERVER_IP = "127.0.0.1";
    
    /**
     * Maximum clients that can connect simultaneously
     */
    public static final int MAX_CLIENTS = 50;
    
    
    // ==================== FILE PATHS ====================
    
    /**
     * Base data directory - stores all persistent data
     */
    public static final String DATA_DIR = "data/";
    
    /**
     * User credentials file - Format: username:password:role
     */
    public static final String USERS_FILE = DATA_DIR + "users.txt";
    
    /**
     * Chat logs file - Stores all messages with timestamps
     */
    public static final String CHAT_LOGS_FILE = DATA_DIR + "chat_logs.txt";
    
    /**
     * Lecture files directory - Teacher uploads stored here
     */
    public static final String LECTURES_DIR = DATA_DIR + "files/lectures/";
    
    /**
     * Assignment files directory - Student submissions stored here
     */
    public static final String ASSIGNMENTS_DIR = DATA_DIR + "files/assignments/";
    
    
    // ==================== MESSAGE TYPES ====================
    
    /**
     * Message Type Enum - Defines all possible message categories
     * 
     * This is crucial for the server to route messages correctly!
     * Each message sent between client and server has a type.
     */
    public enum MessageType {
        // Authentication
        LOGIN,              // Client sends credentials to server
        LOGIN_SUCCESS,      // Server confirms successful login
        LOGIN_FAILED,       // Server rejects login (wrong credentials)
        
        // Chat messages
        CHAT_BROADCAST,     // Teacher sends message to all students
        CHAT_PRIVATE,       // One-to-one message
        CHAT_TO_TEACHER,    // Student sends message to teacher
        
        // File transfer
        FILE_UPLOAD,        // Client wants to upload a file
        FILE_DOWNLOAD,      // Client wants to download a file
        FILE_LIST,          // Request list of available files
        FILE_NOTIFICATION,  // Server notifies clients about new file
        
        // Announcements (UDP)
        ANNOUNCEMENT,       // Teacher broadcasts announcement
        
        // Admin operations
        USER_LIST_REQUEST,  // Admin requests list of online users
        USER_LIST_RESPONSE, // Server sends list to admin
        KICK_USER,          // Admin disconnects a user
        
        // System
        DISCONNECT,         // Client is disconnecting gracefully
        SERVER_SHUTDOWN,    // Server is shutting down
        HEARTBEAT           // Keep-alive ping
    }
    
    
    // ==================== USER ROLES ====================
    
    /**
     * User Role Enum - Defines access levels
     */
    public enum UserRole {
        TEACHER,    // Can broadcast, upload lectures, send announcements
        STUDENT,    // Can chat with teacher, upload assignments, receive announcements
        ADMIN       // Can monitor all activity, view logs, manage connections
    }
    
    
    // ==================== PROTOCOL SETTINGS ====================
    
    /**
     * Maximum message length in characters
     */
    public static final int MAX_MESSAGE_LENGTH = 5000;
    
    /**
     * File transfer buffer size (64KB chunks)
     * Larger = faster transfer, but uses more memory
     */
    public static final int FILE_BUFFER_SIZE = 65536;
    
    /**
     * Socket timeout in milliseconds (30 seconds)
     * If no data received in this time, connection may be dead
     */
    public static final int SOCKET_TIMEOUT = 30000;
    
    
    // ==================== UI SETTINGS ====================
    
    /**
     * Application title
     */
    public static final String APP_NAME = "EduNet - Educational Communication Platform";
    
    /**
     * Window dimensions
     */
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    
    
    // ==================== PRIVATE CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation
     * This class should only be used for its static constants
     */
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
