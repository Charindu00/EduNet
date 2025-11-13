package client;

import utils.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatClient.java
 * 
 * Network communication layer for EduNet client.
 * Handles connection, authentication, and message sending/receiving.
 * 
 * RESPONSIBILITIES:
 * - Connect to server via TCP socket
 * - Authenticate user
 * - Send messages to server
 * - Receive messages from server (in separate thread)
 * - Notify listeners when messages arrive
 * 
 * DESIGN PATTERN: Observer Pattern
 * - UI components register as listeners
 * - When messages arrive, all listeners are notified
 */
public class ChatClient {
    
    // ==================== FIELDS ====================
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    private User currentUser;
    private boolean connected;
    private boolean authenticated;
    
    // Message listeners (UI components that want to receive messages)
    private List<MessageListener> messageListeners;
    
    // Reader thread (listens for incoming messages)
    private Thread readerThread;
    
    
    // ==================== LISTENER INTERFACE ====================
    
    /**
     * Interface for components that want to receive messages
     * UI windows will implement this!
     */
    public interface MessageListener {
        void onMessageReceived(Message message);
        void onConnectionLost(String reason);
    }
    
    
    // ==================== CONSTRUCTOR ====================
    
    public ChatClient() {
        this.connected = false;
        this.authenticated = false;
        this.messageListeners = new ArrayList<>();
    }
    
    
    // ==================== CONNECTION ====================
    
    /**
     * Connect to the server
     * 
     * @return true if connection successful
     */
    public boolean connect() {
        return connect(Constants.SERVER_IP, Constants.TCP_SERVER_PORT);
    }
    
    /**
     * Connect to server at specific IP and port
     */
    public boolean connect(String serverIP, int port) {
        try {
            System.out.println("Connecting to " + serverIP + ":" + port);
            
            // Create TCP socket
            socket = new Socket(serverIP, port);
            
            // Set up streams (OUTPUT FIRST!)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            
            input = new ObjectInputStream(socket.getInputStream());
            
            connected = true;
            System.out.println("✅ Connected to server!");
            
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            notifyConnectionLost("Failed to connect: " + e.getMessage());
            return false;
        }
    }
    
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * Login to the server
     * 
     * @param username Username
     * @param password Password
     * @param role     User role
     * @return true if login successful
     */
    public boolean login(String username, String password, Constants.UserRole role) {
        if (!connected) {
            System.err.println("Not connected to server!");
            return false;
        }
        
        try {
            System.out.println("Logging in as: " + username);
            
            // Create login message
            String credentials = username + ":" + password + ":" + role;
            Message loginMsg = new Message(
                Constants.MessageType.LOGIN,
                username,
                "SERVER",
                credentials
            );
            
            // Send to server
            output.writeObject(loginMsg);
            output.flush();
            
            // Wait for response
            Message response = (Message) input.readObject();
            
            if (response.getType() == Constants.MessageType.LOGIN_SUCCESS) {
                // Success! Create user object
                this.currentUser = new User(username, password, role);
                this.authenticated = true;
                
                System.out.println("✅ Login successful!");
                
                // Start listening for messages
                startMessageReader();
                
                return true;
                
            } else {
                // Failed
                System.err.println("❌ Login failed: " + response.getContent());
                return false;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Login error: " + e.getMessage());
            notifyConnectionLost("Login error: " + e.getMessage());
            return false;
        }
    }
    
    
    // ==================== MESSAGE SENDING ====================
    
    /**
     * Send a broadcast message (teacher only)
     */
    public void sendBroadcast(String content) {
        Message msg = Message.createBroadcastMessage(currentUser.getUsername(), content);
        sendMessage(msg);
    }
    
    /**
     * Send a private message to specific user
     */
    public void sendPrivateMessage(String recipient, String content) {
        Message msg = Message.createPrivateMessage(
            currentUser.getUsername(), 
            recipient, 
            content
        );
        sendMessage(msg);
    }
    
    /**
     * Send message to teacher (student only)
     */
    public void sendMessageToTeacher(String content) {
        Message msg = new Message(
            Constants.MessageType.CHAT_TO_TEACHER,
            currentUser.getUsername(),
            "TEACHER",
            content
        );
        sendMessage(msg);
    }
    
    /**
     * Request list of online users (admin only)
     */
    public void requestUserList() {
        Message msg = new Message(
            Constants.MessageType.USER_LIST_REQUEST,
            currentUser.getUsername(),
            "SERVER",
            ""
        );
        sendMessage(msg);
    }
    
    /**
     * Send any message
     */
    public synchronized void sendMessage(Message message) {
        if (!authenticated) {
            System.err.println("Not authenticated!");
            return;
        }
        
        try {
            synchronized (output) {
                output.writeObject(message);
                output.flush();
            }
            System.out.println("📤 Sent: " + message.getType());
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
            notifyConnectionLost("Failed to send message: " + e.getMessage());
        }
    }
    
    
    // ==================== MESSAGE RECEIVING ====================
    
    /**
     * Start thread that listens for incoming messages
     * 
     * This runs in background and notifies listeners when messages arrive!
     */
    private void startMessageReader() {
        readerThread = new Thread(() -> {
            System.out.println("👂 Started listening for messages...");
            
            while (connected && authenticated) {
                try {
                    // BLOCKING: Wait for next message
                    Message message = (Message) input.readObject();
                    
                    System.out.println("📥 Received: " + message.getType() + 
                                     " from " + message.getSender());
                    
                    // Notify all listeners (UI windows)
                    notifyMessageReceived(message);
                    
                } catch (EOFException | java.net.SocketException e) {
                    // Connection closed
                    if (connected) {
                        System.err.println("Connection lost!");
                        notifyConnectionLost("Connection to server lost");
                    }
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("Error reading message: " + e.getMessage());
                        notifyConnectionLost("Error reading message: " + e.getMessage());
                    }
                    break;
                }
            }
            
            System.out.println("Reader thread stopped.");
        });
        
        readerThread.setDaemon(true);
        readerThread.start();
    }
    
    
    // ==================== LISTENER MANAGEMENT ====================
    
    /**
     * Register a message listener (UI window)
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
        System.out.println("Added listener: " + listener.getClass().getSimpleName());
    }
    
    /**
     * Remove a message listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    /**
     * Notify all listeners that a message was received
     */
    private void notifyMessageReceived(Message message) {
        for (MessageListener listener : messageListeners) {
            // Run on Swing EDT (Event Dispatch Thread) for thread safety
            javax.swing.SwingUtilities.invokeLater(() -> {
                listener.onMessageReceived(message);
            });
        }
    }
    
    /**
     * Notify all listeners that connection was lost
     */
    private void notifyConnectionLost(String reason) {
        connected = false;
        authenticated = false;
        
        for (MessageListener listener : messageListeners) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                listener.onConnectionLost(reason);
            });
        }
    }
    
    
    // ==================== DISCONNECT ====================
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        try {
            // Send disconnect message
            if (authenticated && currentUser != null) {
                Message disconnectMsg = Message.createDisconnectMessage(
                    currentUser.getUsername()
                );
                sendMessage(disconnectMsg);
            }
            
            // Close resources
            connected = false;
            authenticated = false;
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            
            System.out.println("✅ Disconnected from server");
            
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    
    // ==================== GETTERS ====================
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    public Constants.UserRole getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}
