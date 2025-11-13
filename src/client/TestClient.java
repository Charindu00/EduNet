package client;

import utils.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * TestClient.java
 * 
 * Simple command-line client to test server functionality.
 * 
 * PURPOSE:
 * - Verify server is working correctly
 * - Demonstrate TCP client-server communication
 * - Test authentication and messaging
 * 
 * This is a SIMPLE version - Phase 3 will have full Swing UI!
 */
public class TestClient {
    
    // ==================== FIELDS ====================
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String username;
    private boolean connected;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public TestClient() {
        this.connected = false;
    }
    
    
    // ==================== CONNECTION ====================
    
    /**
     * Connect to the server
     * 
     * STEPS:
     * 1. Create Socket to server IP and port
     * 2. Set up streams (OUTPUT first!)
     * 3. Connection established!
     */
    public boolean connect(String serverIP, int port) {
        try {
            System.out.println("\n🔌 Connecting to server at " + serverIP + ":" + port + "...");
            
            // Create TCP socket connection
            socket = new Socket(serverIP, port);
            
            System.out.println("✅ Connected to server!");
            System.out.println("   Local address: " + socket.getLocalAddress().getHostAddress() + 
                             ":" + socket.getLocalPort());
            
            // Set up streams (OUTPUT FIRST!)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            
            input = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("✅ Streams initialized!");
            
            connected = true;
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            System.err.println("   Make sure the server is running!");
            return false;
        }
    }
    
    
    // ==================== LOGIN ====================
    
    /**
     * Login to the server
     * 
     * PROTOCOL:
     * 1. Send LOGIN message with credentials
     * 2. Wait for response (LOGIN_SUCCESS or LOGIN_FAILED)
     */
    public boolean login(String username, String password, Constants.UserRole role) {
        try {
            this.username = username;
            
            System.out.println("\n🔐 Logging in as: " + username + " (Role: " + role + ")");
            
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
            
            System.out.println("📤 Login request sent, waiting for response...");
            
            // Wait for response
            Message response = (Message) input.readObject();
            
            System.out.println("📥 Received: " + response.getType() + " - " + response.getContent());
            
            if (response.getType() == Constants.MessageType.LOGIN_SUCCESS) {
                System.out.println("✅ Login successful!");
                return true;
            } else {
                System.out.println("❌ Login failed: " + response.getContent());
                return false;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Login error: " + e.getMessage());
            return false;
        }
    }
    
    
    // ==================== SEND MESSAGE ====================
    
    /**
     * Send a chat message
     */
    public void sendMessage(Constants.MessageType type, String recipient, String content) {
        try {
            Message msg = new Message(type, username, recipient, content);
            
            output.writeObject(msg);
            output.flush();
            
            System.out.println("📤 Sent: [" + type + "] " + content);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to send message: " + e.getMessage());
        }
    }
    
    
    // ==================== RECEIVE MESSAGES ====================
    
    /**
     * Start listening for incoming messages (in separate thread)
     */
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            System.out.println("\n👂 Started listening for messages...\n");
            
            while (connected) {
                try {
                    Message msg = (Message) input.readObject();
                    
                    // Display received message
                    System.out.println("\n📥 RECEIVED MESSAGE:");
                    System.out.println("   Type: " + msg.getType());
                    System.out.println("   From: " + msg.getSender());
                    System.out.println("   To: " + (msg.getRecipient() != null ? msg.getRecipient() : "ALL"));
                    System.out.println("   Content: " + msg.getContent());
                    System.out.println("   Time: " + msg.getTimestamp());
                    System.out.println();
                    
                } catch (EOFException e) {
                    System.out.println("\n❌ Server disconnected.");
                    connected = false;
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        System.err.println("\n❌ Error receiving message: " + e.getMessage());
                    }
                    break;
                }
            }
            
            System.out.println("👋 Stopped listening.");
        });
        
        listenerThread.setDaemon(true);  // Dies when main thread exits
        listenerThread.start();
    }
    
    
    // ==================== DISCONNECT ====================
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (connected) {
                // Send disconnect message
                Message disconnectMsg = Message.createDisconnectMessage(username);
                output.writeObject(disconnectMsg);
                output.flush();
                
                connected = false;
                
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
                
                System.out.println("\n✅ Disconnected from server.");
            }
        } catch (IOException e) {
            System.err.println("❌ Error during disconnect: " + e.getMessage());
        }
    }
    
    
    // ==================== MAIN - INTERACTIVE TEST ====================
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║      EduNet Test Client - v1.0           ║");
        System.out.println("║   Testing Server Communication           ║");
        System.out.println("╚════════════════════════════════════════════╝");
        
        Scanner scanner = new Scanner(System.in);
        TestClient client = new TestClient();
        
        // Connect to server
        if (!client.connect(Constants.SERVER_IP, Constants.TCP_SERVER_PORT)) {
            System.out.println("\n⚠️  Make sure the server is running first!");
            System.out.println("   Run: java -cp bin server.ChatServer");
            scanner.close();
            return;
        }
        
        // Get login credentials
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Available test accounts:");
        System.out.println("  Teacher: teacher1 / teacher123");
        System.out.println("  Student: student1 / student123");
        System.out.println("  Admin:   admin1 / admin123");
        System.out.println("=".repeat(50));
        
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("Enter role (TEACHER/STUDENT/ADMIN): ");
        String roleStr = scanner.nextLine().trim().toUpperCase();
        Constants.UserRole role;
        
        try {
            role = Constants.UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Invalid role. Using STUDENT.");
            role = Constants.UserRole.STUDENT;
        }
        
        // Login
        if (!client.login(username, password, role)) {
            System.out.println("\n❌ Login failed. Exiting.");
            client.disconnect();
            scanner.close();
            return;
        }
        
        // Start listening for messages
        client.startListening();
        
        // Interactive menu
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Commands:");
        System.out.println("  1 - Send broadcast message (teacher only)");
        System.out.println("  2 - Send private message");
        System.out.println("  3 - Send message to teacher (student)");
        System.out.println("  4 - Request user list (admin)");
        System.out.println("  5 - Disconnect");
        System.out.println("=".repeat(50));
        
        boolean running = true;
        while (running) {
            System.out.print("\nEnter command (1-5): ");
            String cmd = scanner.nextLine().trim();
            
            switch (cmd) {
                case "1":
                    // Broadcast
                    if (role == Constants.UserRole.TEACHER) {
                        System.out.print("Enter message to broadcast: ");
                        String msg = scanner.nextLine();
                        client.sendMessage(Constants.MessageType.CHAT_BROADCAST, "ALL", msg);
                    } else {
                        System.out.println("❌ Only teachers can broadcast!");
                    }
                    break;
                    
                case "2":
                    // Private message
                    System.out.print("Enter recipient username: ");
                    String recipient = scanner.nextLine().trim();
                    System.out.print("Enter message: ");
                    String privateMsg = scanner.nextLine();
                    client.sendMessage(Constants.MessageType.CHAT_PRIVATE, recipient, privateMsg);
                    break;
                    
                case "3":
                    // Message to teacher
                    System.out.print("Enter message for teacher: ");
                    String teacherMsg = scanner.nextLine();
                    client.sendMessage(Constants.MessageType.CHAT_TO_TEACHER, "TEACHER", teacherMsg);
                    break;
                    
                case "4":
                    // User list
                    if (role == Constants.UserRole.ADMIN) {
                        client.sendMessage(Constants.MessageType.USER_LIST_REQUEST, "SERVER", "");
                    } else {
                        System.out.println("❌ Only admins can request user list!");
                    }
                    break;
                    
                case "5":
                    // Disconnect
                    running = false;
                    break;
                    
                default:
                    System.out.println("❌ Invalid command!");
            }
        }
        
        client.disconnect();
        scanner.close();
        System.out.println("\n👋 Goodbye!");
    }
}
