package server;

import utils.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * ClientHandler.java
 * 
 * Thread that handles communication with ONE client.
 * 
 * RESPONSIBILITIES:
 * - Authenticate user (login handshake)
 * - Read incoming messages from client
 * - Send outgoing messages to client
 * - Handle disconnection
 * 
 * NETWORKING CONCEPTS:
 * ✅ ObjectInputStream/ObjectOutputStream - Send/receive Java objects
 * ✅ Threading - Each client runs in separate thread
 * ✅ Socket communication - TCP streams
 * 
 * LIFECYCLE:
 * 1. Constructor receives Socket from server
 * 2. start() is called → run() method executes
 * 3. handleClient() authenticates and processes messages
 * 4. On disconnect or error → cleanup()
 */
public class ClientHandler extends Thread {
    
    // ==================== FIELDS ====================
    
    /**
     * The socket connection to THIS client
     */
    private Socket socket;
    
    /**
     * Reference to the main server (for broadcasting, etc.)
     */
    private ChatServer server;
    
    /**
     * User information (after successful login)
     */
    private User user;
    
    /**
     * Output stream - Send messages TO client
     * ObjectOutputStream can send entire Java objects!
     */
    private ObjectOutputStream output;
    
    /**
     * Input stream - Receive messages FROM client
     */
    private ObjectInputStream input;
    
    /**
     * Is this client authenticated?
     */
    private boolean authenticated;
    
    /**
     * Is handler still running?
     */
    private boolean running;
    
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new client handler
     * 
     * @param socket The client's socket connection
     * @param server Reference to main server
     */
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.authenticated = false;
        this.running = true;
        this.user = null;
    }
    
    
    // ==================== THREAD RUN METHOD ====================
    
    /**
     * Thread execution starts here (called by start())
     * 
     * This runs in a SEPARATE THREAD from the main server!
     * That's why the server can handle multiple clients simultaneously.
     */
    @Override
    public void run() {
        try {
            // Set up streams
            setupStreams();
            
            // Handle this client
            handleClient();
            
        } catch (IOException e) {
            Logger.error("ClientHandler error: " + e.getMessage());
        } finally {
            // Always cleanup when done
            cleanup();
        }
    }
    
    
    // ==================== STREAM SETUP ====================
    
    /**
     * Initialize input and output streams
     * 
     * IMPORTANT: Create OUTPUT stream FIRST!
     * ObjectOutputStream writes a header that ObjectInputStream expects.
     * If you create input first, it will block waiting for that header.
     */
    private void setupStreams() throws IOException {
        // OUTPUT FIRST!
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();  // Force header to be sent
        
        // INPUT SECOND
        input = new ObjectInputStream(socket.getInputStream());
        
        Logger.debug("Streams set up for client: " + socket.getInetAddress());
    }
    
    
    // ==================== CLIENT HANDLING ====================
    
    /**
     * Main client handling logic
     * 
     * FLOW:
     * 1. Authenticate user (login)
     * 2. If successful, enter message loop
     * 3. Process messages until disconnect
     */
    private void handleClient() throws IOException {
        String clientIP = socket.getInetAddress().getHostAddress();
        
        // Step 1: AUTHENTICATION
        if (!authenticate()) {
            Logger.error("Authentication failed for " + clientIP);
            sendMessage(new Message(
                Constants.MessageType.LOGIN_FAILED,
                "SERVER",
                null,
                "Authentication failed. Invalid credentials."
            ));
            return;  // Exit thread
        }
        
        // Step 2: Send success confirmation
        Logger.info("User " + user.getUsername() + " authenticated successfully");
        sendMessage(new Message(
            Constants.MessageType.LOGIN_SUCCESS,
            "SERVER",
            user.getUsername(),
            "Welcome to EduNet, " + user.getUsername() + "!"
        ));
        
        // Notify others about new user
        Message joinNotification = new Message(
            Constants.MessageType.CHAT_BROADCAST,
            "SERVER",
            "ALL",
            user.getUsername() + " (" + user.getRole() + ") has joined the chat"
        );
        server.broadcastMessage(joinNotification, this);
        
        // Step 3: MESSAGE LOOP - Process messages until disconnect
        processMessages();
    }
    
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * Authenticate the user
     * 
     * PROTOCOL:
     * 1. Client sends LOGIN message with format: "username:password:role"
     * 2. Server validates against users.txt
     * 3. If valid, create User object and mark as authenticated
     * 
     * @return true if authentication successful
     */
    private boolean authenticate() {
        try {
            // Wait for login message
            Message loginMsg = (Message) input.readObject();
            
            if (loginMsg.getType() != Constants.MessageType.LOGIN) {
                Logger.error("First message was not LOGIN type");
                return false;
            }
            
            // Parse credentials: "username:password:role"
            String credentials = loginMsg.getContent();
            String[] parts = credentials.split(":");
            
            if (parts.length != 3) {
                Logger.error("Invalid credentials format");
                return false;
            }
            
            String username = parts[0].trim();
            String password = parts[1].trim();
            String roleStr = parts[2].trim();
            
            // Check if username already connected
            if (server.isUsernameConnected(username)) {
                Logger.error("User " + username + " already connected");
                sendMessage(new Message(
                    Constants.MessageType.LOGIN_FAILED,
                    "SERVER",
                    null,
                    "User already connected. Please use a different username."
                ));
                return false;
            }
            
            // Authenticate using FileUtils
            User authenticatedUser = FileUtils.authenticateUser(username, password);
            
            if (authenticatedUser == null) {
                Logger.error("Invalid credentials for user: " + username);
                return false;
            }
            
            // Verify role matches
            if (!authenticatedUser.getRole().toString().equals(roleStr)) {
                Logger.error("Role mismatch for user: " + username);
                return false;
            }
            
            // SUCCESS! Set up user
            this.user = authenticatedUser;
            this.user.setSocket(socket);
            this.user.setOnline(true);
            this.authenticated = true;
            
            Logger.info("✅ User authenticated: " + username + " (Role: " + user.getRole() + ")");
            return true;
            
        } catch (ClassNotFoundException | IOException e) {
            Logger.error("Authentication error", e);
            return false;
        }
    }
    
    
    // ==================== MESSAGE PROCESSING ====================
    
    /**
     * Main message loop - runs until client disconnects
     * 
     * This is where we spend most of our time!
     * Continuously read messages and route them appropriately.
     */
    private void processMessages() {
        while (running && authenticated) {
            try {
                // BLOCKING: Wait for next message from client
                Message message = (Message) input.readObject();
                
                if (message == null) {
                    break;  // Client disconnected
                }
                
                // Route message based on type
                routeMessage(message);
                
            } catch (SocketException e) {
                // Client disconnected (socket closed)
                Logger.info("Client " + user.getUsername() + " disconnected (socket closed)");
                break;
            } catch (EOFException e) {
                // Client disconnected (end of stream)
                Logger.info("Client " + user.getUsername() + " disconnected (EOF)");
                break;
            } catch (ClassNotFoundException | IOException e) {
                Logger.error("Error reading message from " + user.getUsername(), e);
                break;
            }
        }
    }
    
    
    // ==================== MESSAGE ROUTING ====================
    
    /**
     * Route message to appropriate handler based on type
     * 
     * This is like a switchboard operator!
     */
    private void routeMessage(Message message) {
        Logger.debug("Routing message: " + message.getType() + " from " + user.getUsername());
        
        switch (message.getType()) {
            case CHAT_BROADCAST:
                // Teacher broadcasts to all students
                handleBroadcast(message);
                break;
                
            case CHAT_PRIVATE:
                // Private message to specific user
                handlePrivateMessage(message);
                break;
                
            case CHAT_TO_TEACHER:
                // Student messages teacher
                handleMessageToTeacher(message);
                break;
                
            case FILE_UPLOAD:
                // File transfer (will implement in Phase 3)
                handleFileUpload(message);
                break;
                
            case FILE_DOWNLOAD:
                // File download request
                handleFileDownload(message);
                break;
                
            case USER_LIST_REQUEST:
                // Admin wants list of connected users
                handleUserListRequest();
                break;
                
            case DISCONNECT:
                // Client wants to disconnect gracefully
                handleDisconnect(message);
                break;
                
            default:
                Logger.error("Unknown message type: " + message.getType());
        }
    }
    
    
    // ==================== MESSAGE HANDLERS ====================
    
    /**
     * Handle broadcast message (teacher to all)
     */
    private void handleBroadcast(Message message) {
        // Only teachers can broadcast
        if (!user.isTeacher()) {
            sendMessage(new Message(
                Constants.MessageType.CHAT_PRIVATE,
                "SERVER",
                user.getUsername(),
                "Permission denied: Only teachers can broadcast."
            ));
            return;
        }
        
        server.broadcastMessage(message, this);
    }
    
    /**
     * Handle private message
     */
    private void handlePrivateMessage(Message message) {
        String recipient = message.getRecipient();
        boolean delivered = server.sendPrivateMessage(message, recipient);
        
        if (!delivered) {
            // Notify sender that message wasn't delivered
            sendMessage(new Message(
                Constants.MessageType.CHAT_PRIVATE,
                "SERVER",
                user.getUsername(),
                "User " + recipient + " is not online."
            ));
        }
    }
    
    /**
     * Handle message to teacher (students use this)
     */
    private void handleMessageToTeacher(Message message) {
        // Send to all teachers
        server.sendToRole(message, Constants.UserRole.TEACHER);
    }
    
    /**
     * Handle file upload (placeholder for Phase 3)
     */
    private void handleFileUpload(Message message) {
        Logger.info("File upload request from " + user.getUsername());
        sendMessage(new Message(
            Constants.MessageType.CHAT_PRIVATE,
            "SERVER",
            user.getUsername(),
            "File transfer will be implemented in Phase 3!"
        ));
    }
    
    /**
     * Handle file download (placeholder for Phase 3)
     */
    private void handleFileDownload(Message message) {
        Logger.info("File download request from " + user.getUsername());
        sendMessage(new Message(
            Constants.MessageType.CHAT_PRIVATE,
            "SERVER",
            user.getUsername(),
            "File transfer will be implemented in Phase 3!"
        ));
    }
    
    /**
     * Handle user list request (admin)
     */
    private void handleUserListRequest() {
        if (!user.isAdmin()) {
            sendMessage(new Message(
                Constants.MessageType.CHAT_PRIVATE,
                "SERVER",
                user.getUsername(),
                "Permission denied: Admin only."
            ));
            return;
        }
        
        // Get list and send back
        Message response = new Message(
            Constants.MessageType.USER_LIST_RESPONSE,
            "SERVER",
            user.getUsername(),
            "Connected users",
            server.getConnectedUsers()
        );
        sendMessage(response);
    }
    
    /**
     * Handle graceful disconnect
     */
    private void handleDisconnect(Message message) {
        Logger.info("User " + user.getUsername() + " requested disconnect");
        running = false;
    }
    
    
    // ==================== SEND MESSAGE ====================
    
    /**
     * Send a message TO this client
     * 
     * @param message Message to send
     * @return true if sent successfully
     */
    public boolean sendMessage(Message message) {
        try {
            synchronized (output) {  // Prevent concurrent writes
                output.writeObject(message);
                output.flush();
            }
            return true;
        } catch (IOException e) {
            Logger.error("Failed to send message to " + 
                        (user != null ? user.getUsername() : "unknown"), e);
            return false;
        }
    }
    
    
    // ==================== GETTERS ====================
    
    public User getUser() {
        return user;
    }
    
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    
    // ==================== CLEANUP ====================
    
    /**
     * Shutdown this handler
     */
    public void shutdown() {
        running = false;
        cleanup();
    }
    
    /**
     * Clean up resources when client disconnects
     */
    private void cleanup() {
        running = false;
        
        if (user != null) {
            user.setOnline(false);
            Logger.info("Cleaning up handler for " + user.getUsername());
        }
        
        // Close streams
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Logger.error("Error closing streams", e);
        }
        
        // Remove from server's client list
        server.removeClient(this);
    }
}
