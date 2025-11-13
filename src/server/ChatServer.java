package server;

import utils.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChatServer.java
 * 
 * Main TCP server for EduNet.
 * 
 * RESPONSIBILITIES:
 * - Listen on port 5000 for incoming client connections
 * - Create a ClientHandler thread for each connected client
 * - Maintain list of all active ClientHandlers
 * - Provide methods for broadcasting messages
 * - Handle graceful shutdown
 * 
 * NETWORKING CONCEPTS DEMONSTRATED:
 * ✅ TCP ServerSocket - Listens for connections
 * ✅ Multithreading - One thread per client
 * ✅ Concurrent data structures - CopyOnWriteArrayList for thread safety
 */
public class ChatServer {
    
    // ==================== FIELDS ====================
    
    /**
     * The server socket that listens for connections
     * This is bound to port 5000
     */
    private ServerSocket serverSocket;
    
    /**
     * List of all connected clients (their handler threads)
     * 
     * WHY CopyOnWriteArrayList?
     * - Thread-safe without explicit locking
     * - Multiple threads can iterate while others modify
     * - Perfect for scenarios where reads >> writes
     * - When a client connects/disconnects, list is modified
     * - When broadcasting, we iterate through all clients
     */
    private CopyOnWriteArrayList<ClientHandler> clientHandlers;
    
    /**
     * Server running flag
     */
    private boolean isRunning;
    
    /**
     * Port number (from Constants)
     */
    private final int port;
    
    /**
     * File transfer handler (runs on separate port)
     */
    private FileTransferHandler fileTransferHandler;
    
    /**
     * UDP announcement server (runs on port 6000)
     */
    private UDPAnnouncementServer udpServer;
    
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Initialize the server
     */
    public ChatServer() {
        this.port = Constants.TCP_SERVER_PORT;
        this.clientHandlers = new CopyOnWriteArrayList<>();
        this.isRunning = false;
        this.fileTransferHandler = null;
        this.udpServer = null;
    }
    
    
    // ==================== SERVER STARTUP ====================
    
    /**
     * Start the server
     * 
     * STEPS:
     * 1. Initialize directories and logger
     * 2. Create ServerSocket (binds to port)
     * 3. Loop: Accept clients and create threads
     */
    public void start() {
        try {
            // Initialize file system and logging
            FileUtils.initializeDirectories();
            Logger.initialize();
            
            Logger.info("==============================================");
            Logger.info("       EduNet Server Starting...           ");
            Logger.info("==============================================");
            
            // Create ServerSocket - this BINDS to the port
            // After this line, clients can connect to port 5000
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            Logger.info("Server started successfully on port " + port);
            Logger.info("Waiting for client connections...");
            System.out.println("\n✅ Server is running on port " + port);
            System.out.println("📡 Ready to accept client connections...\n");
            
            // Start file transfer handler
            fileTransferHandler = new FileTransferHandler(this);
            fileTransferHandler.start();
            
            // Start UDP announcement server
            udpServer = new UDPAnnouncementServer();
            udpServer.start();
            
            // Main server loop - accept clients forever
            acceptClients();
            
        } catch (IOException e) {
            Logger.error("Failed to start server on port " + port, e);
            System.err.println("❌ ERROR: Could not start server!");
            System.err.println("   Reason: " + e.getMessage());
            System.err.println("   Tip: Make sure port " + port + " is not already in use.");
        }
    }
    
    
    // ==================== ACCEPT CLIENTS ====================
    
    /**
     * Main server loop - accepts incoming client connections
     * 
     * BLOCKING CALL:
     * serverSocket.accept() blocks (waits) until a client connects.
     * When a client connects, it returns a Socket object.
     * 
     * This runs forever until server is stopped.
     */
    private void acceptClients() {
        while (isRunning) {
            try {
                // BLOCKING CALL: Wait for a client to connect
                System.out.println("⏳ Waiting for next client...");
                Socket clientSocket = serverSocket.accept();
                
                // A client connected! Get their info
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();
                
                Logger.info("New connection from " + clientIP + ":" + clientPort);
                System.out.println("🔌 New connection from " + clientIP + ":" + clientPort);
                
                // Create a handler thread for this client
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientHandlers.add(handler);
                
                // Start the thread (runs handleClient() method)
                handler.start();
                
                System.out.println("👥 Total connected clients: " + clientHandlers.size() + "\n");
                
            } catch (IOException e) {
                // If server is stopping, this is expected
                if (isRunning) {
                    Logger.error("Error accepting client connection", e);
                    System.err.println("❌ Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    
    // ==================== MESSAGE BROADCASTING ====================
    
    /**
     * Broadcast a message to ALL connected clients
     * 
     * USE CASE: Teacher sends message to entire class
     * 
     * @param message The message to broadcast
     * @param sender  The ClientHandler who sent it (exclude from broadcast)
     */
    public void broadcastMessage(Message message, ClientHandler sender) {
        Logger.chat(message);
        
        System.out.println("📢 Broadcasting message from " + message.getSender() + 
                          " to " + clientHandlers.size() + " clients");
        
        int successCount = 0;
        for (ClientHandler client : clientHandlers) {
            // Don't send message back to sender
            if (client != sender && client.isAuthenticated()) {
                boolean sent = client.sendMessage(message);
                if (sent) successCount++;
            }
        }
        
        System.out.println("   ✅ Delivered to " + successCount + " clients\n");
    }
    
    /**
     * Send message to a specific user by username
     * 
     * USE CASE: Private message between teacher and student
     * 
     * @param message   The message to send
     * @param recipient Username of recipient
     * @return true if message was delivered
     */
    public boolean sendPrivateMessage(Message message, String recipient) {
        Logger.chat(message);
        
        System.out.println("📨 Sending private message from " + message.getSender() + 
                          " to " + recipient);
        
        for (ClientHandler client : clientHandlers) {
            if (client.getUsername() != null && 
                client.getUsername().equals(recipient) && 
                client.isAuthenticated()) {
                
                boolean sent = client.sendMessage(message);
                if (sent) {
                    System.out.println("   ✅ Message delivered to " + recipient + "\n");
                    return true;
                }
            }
        }
        
        System.out.println("   ❌ User " + recipient + " not found or offline\n");
        return false;
    }
    
    /**
     * Send message to all users with a specific role
     * 
     * USE CASE: Send message to all students or all teachers
     */
    public void sendToRole(Message message, Constants.UserRole role) {
        Logger.chat(message);
        
        System.out.println("📢 Sending message to all " + role + "s");
        
        int successCount = 0;
        for (ClientHandler client : clientHandlers) {
            if (client.isAuthenticated() && client.getUser().getRole() == role) {
                boolean sent = client.sendMessage(message);
                if (sent) successCount++;
            }
        }
        
        System.out.println("   ✅ Delivered to " + successCount + " " + role + "s\n");
    }
    
    
    // ==================== CLIENT MANAGEMENT ====================
    
    /**
     * Remove a client handler from the list (when they disconnect)
     */
    public void removeClient(ClientHandler handler) {
        clientHandlers.remove(handler);
        
        String username = handler.getUsername();
        if (username != null) {
            Logger.info("User " + username + " disconnected. Remaining: " + clientHandlers.size());
            System.out.println("👋 User " + username + " disconnected");
            System.out.println("👥 Total connected clients: " + clientHandlers.size() + "\n");
            
            // Notify other clients about disconnection
            Message notification = new Message(
                Constants.MessageType.DISCONNECT,
                "SERVER",
                "ALL",
                username + " has left the chat"
            );
            broadcastMessage(notification, null);
        }
    }
    
    /**
     * Get list of all connected users (for admin dashboard)
     */
    public List<User> getConnectedUsers() {
        List<User> users = new ArrayList<>();
        for (ClientHandler handler : clientHandlers) {
            if (handler.isAuthenticated()) {
                users.add(new User(handler.getUser())); // Copy constructor
            }
        }
        return users;
    }
    
    /**
     * Get number of connected clients
     */
    public int getClientCount() {
        return clientHandlers.size();
    }
    
    /**
     * Check if a username is already connected
     */
    public boolean isUsernameConnected(String username) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.isAuthenticated() && 
                handler.getUsername() != null && 
                handler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    
    // ==================== SERVER SHUTDOWN ====================
    
    /**
     * Gracefully stop the server
     */
    public void stop() {
        Logger.info("Server shutting down...");
        System.out.println("\n🛑 Shutting down server...");
        
        isRunning = false;
        
        // Notify all clients
        Message shutdownMsg = new Message(
            Constants.MessageType.SERVER_SHUTDOWN,
            "SERVER",
            "ALL",
            "Server is shutting down. Please reconnect later."
        );
        
        // Disconnect all clients
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(shutdownMsg);
            handler.shutdown();
        }
        
        clientHandlers.clear();
        
        // Stop file transfer handler
        if (fileTransferHandler != null) {
            fileTransferHandler.shutdown();
        }
        
        // Stop UDP announcement server
        if (udpServer != null) {
            udpServer.stop();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.error("Error closing server socket", e);
        }
        
        Logger.info("Server stopped successfully");
        System.out.println("✅ Server stopped.\n");
    }
    
    
    // ==================== MAIN METHOD ====================
    
    /**
     * Entry point - Start the server
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║         EduNet Server - v1.0             ║");
        System.out.println("║   Educational Communication Platform      ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // Create and start server
        ChatServer server = new ChatServer();
        
        // Add shutdown hook (handles Ctrl+C gracefully)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        // Start accepting connections
        server.start();
    }
}
