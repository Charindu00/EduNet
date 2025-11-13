# 👤 MEMBER 3: Message Routing & TCP Communication

## 📋 Complete Viva Preparation Guide

**Role:** Message Protocol & Routing Specialist  
**Focus:** Message Design, Client Communication, Observer Pattern  
**Files:** `Message.java`, `ChatClient.java`, Message Routing Logic  
**Lines of Code:** ~900 lines  
**Complexity:** ⭐⭐⭐⭐ (High)

---

## 📚 PART 1: Network Concepts

### 1.1 What is a Protocol?

**Simple Analogy:**
```
Protocol = Set of rules for communication

Like talking on phone:
1. Caller says "Hello"
2. Receiver says "Hi, who's this?"
3. Caller identifies themselves
4. Conversation begins

If both don't follow this pattern → confusion!
```

**In Networking:**
```
Protocol defines:
✅ Message format (structure)
✅ Message types (what each message means)
✅ Message sequence (order of exchange)
✅ Error handling (what if something fails)
```

---

### 1.2 Our Message Protocol

```
┌─────────────────────────────────────────┐
│          MESSAGE STRUCTURE              │
├─────────────────────────────────────────┤
│ Type:      MessageType                  │
│ Sender:    String (username)            │
│ Receiver:  String (username or "ALL")   │
│ Content:   String (actual data)         │
│ Timestamp: LocalDateTime                │
└─────────────────────────────────────────┘

Example LOGIN message:
Type:      LOGIN
Sender:    "student1"
Receiver:  "SERVER"
Content:   "student1:pass123:STUDENT"
Timestamp: 2025-11-11T10:30:00
```

---

### 1.3 Message Types (Complete List)

```java
// AUTHENTICATION
LOGIN              // Client → Server: Credentials
LOGIN_SUCCESS      // Server → Client: Welcome!
LOGIN_FAILED       // Server → Client: Access denied

// CHAT COMMUNICATION
CHAT_BROADCAST     // Teacher → All: Announcement
CHAT_TO_TEACHER    // Student → Teachers: Question
CHAT_PRIVATE       // User → User: Private message

// FILE OPERATIONS
FILE_UPLOAD        // Start upload
FILE_DOWNLOAD      // Start download
FILE_LIST          // Request available files
FILE_NOTIFICATION  // New file available

// ADMIN OPERATIONS
ADMIN_USER_LIST_REQUEST    // Get connected users
ADMIN_USER_LIST_RESPONSE   // Here's the list
ADMIN_KICK_USER            // Disconnect user

// SYSTEM
SERVER_SHUTDOWN    // Server closing
KICKED             // You've been kicked
```

---

### 1.4 Observer Pattern

**Problem:**
```
How does UI know when message arrives?

❌ BAD: UI constantly checks
while (true) {
    if (newMessage) {
        display(message);
    }
}
// Wastes CPU!

✅ GOOD: Network layer notifies UI
networkLayer.onMessageReceived = () -> {
    display(message);
}
// Efficient, event-driven!
```

**Observer Pattern:**
```
Subject (ChatClient)          Observer (UI Window)
    │                               │
    │──── addListener(observer) ────│
    │                               │
    │ Message arrives               │
    │                               │
    │──── notifyListeners() ───────>│
    │                               │
    │                               └─> Display message
```

---

### 1.5 Client-Server Communication Flow

```
FULL CYCLE:

1. Client creates message
   ↓
2. Client sends via socket
   ↓
3. Network transmits bytes
   ↓
4. Server receives message
   ↓
5. Server routes based on type
   ↓
6. Server determines recipients
   ↓
7. Server sends to recipients
   ↓
8. Recipients' clients receive
   ↓
9. Clients notify UI listeners
   ↓
10. UI displays message
```

---

## 🔧 PART 2: Implementation Details

### 2.1 Message.java - Complete Code

```java
package utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message - Universal communication object
 * 
 * All data exchanged between client and server uses this format.
 * Serializable allows it to be sent over ObjectOutputStream.
 */
public class Message implements Serializable {
    
    // Serialization version (important for compatibility)
    private static final long serialVersionUID = 1L;
    
    // ========== FIELDS ==========
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Full constructor
     */
    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Quick constructor for broadcast
     */
    public static Message broadcast(String sender, String content) {
        return new Message(MessageType.CHAT_BROADCAST, sender, "ALL", content);
    }
    
    /**
     * Quick constructor for private message
     */
    public static Message privateTo(String sender, String receiver, String content) {
        return new Message(MessageType.CHAT_PRIVATE, sender, receiver, content);
    }
    
    // ========== GETTERS ==========
    
    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // ========== FORMATTING ==========
    
    /**
     * Format for display in UI
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    /**
     * Format complete message for chat display
     */
    public String formatForDisplay() {
        return String.format("[%s] %s: %s",
            getFormattedTimestamp(),
            sender,
            content
        );
    }
    
    @Override
    public String toString() {
        return String.format("Message[type=%s, from=%s, to=%s, content=%s]",
            type, sender, receiver, content);
    }
}
```

**Why Serializable?**
```
Without Serializable:
output.writeObject(message);
→ NotSerializableException!

With Serializable:
✅ Java automatically converts object to bytes
✅ Sends over network
✅ Receiving side reconstructs object
✅ All fields preserved (type, sender, content, etc.)
```

---

### 2.2 ChatClient.java - Complete Implementation

```java
package client;

import utils.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatClient - Network communication layer
 * 
 * Handles all network operations for the client.
 * UI components register as listeners to receive messages.
 */
public class ChatClient {
    
    // ========== CONNECTION ==========
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    // ========== STATE ==========
    private User currentUser;
    private boolean connected;
    private boolean authenticated;
    
    // ========== OBSERVERS ==========
    private List<MessageListener> messageListeners;
    private Thread readerThread;
    
    // ========== LISTENER INTERFACE ==========
    
    /**
     * Observer interface for UI components
     */
    public interface MessageListener {
        void onMessageReceived(Message message);
        void onConnectionLost(String reason);
    }
    
    // ========== CONSTRUCTOR ==========
    
    public ChatClient() {
        this.connected = false;
        this.authenticated = false;
        this.messageListeners = new ArrayList<>();
    }
    
    // ========== CONNECTION ==========
    
    /**
     * Connect to server
     */
    public boolean connect() {
        return connect(Constants.SERVER_IP, Constants.TCP_SERVER_PORT);
    }
    
    public boolean connect(String serverIP, int port) {
        try {
            System.out.println("Connecting to " + serverIP + ":" + port);
            
            // Create TCP socket
            socket = new Socket(serverIP, port);
            
            // Setup streams (OUTPUT FIRST!)
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
    
    // ========== AUTHENTICATION ==========
    
    /**
     * Login to server
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
                // Success!
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
            System.err.println("❌ Login error: " + e.getMessage());
            return false;
        }
    }
    
    // ========== MESSAGE READER THREAD ==========
    
    /**
     * Start background thread to listen for messages
     * 
     * WHY SEPARATE THREAD?
     * input.readObject() BLOCKS until message arrives.
     * If we read in main thread, entire UI would freeze!
     */
    private void startMessageReader() {
        readerThread = new Thread(() -> {
            try {
                while (connected && !socket.isClosed()) {
                    // BLOCKING: Wait for message
                    Message message = (Message) input.readObject();
                    
                    // Message arrived! Notify all listeners
                    notifyListeners(message);
                }
            } catch (EOFException e) {
                notifyConnectionLost("Server closed connection");
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    notifyConnectionLost("Connection error: " + e.getMessage());
                }
            }
        }, "MessageReaderThread");
        
        readerThread.setDaemon(true);  // Don't prevent JVM shutdown
        readerThread.start();
        
        System.out.println("📨 Message reader thread started");
    }
    
    // ========== SENDING MESSAGES ==========
    
    /**
     * Send message to server
     */
    public void sendMessage(Message message) throws IOException {
        if (!connected || !authenticated) {
            throw new IOException("Not connected or authenticated");
        }
        
        synchronized (output) {
            output.writeObject(message);
            output.flush();
        }
    }
    
    /**
     * Send chat message (broadcast or to teacher)
     */
    public void sendChat(String content) throws IOException {
        MessageType type = currentUser.getRole() == Constants.UserRole.TEACHER ?
            Constants.MessageType.CHAT_BROADCAST :
            Constants.MessageType.CHAT_TO_TEACHER;
        
        Message msg = new Message(type, currentUser.getUsername(), "ALL", content);
        sendMessage(msg);
    }
    
    // ========== OBSERVER PATTERN ==========
    
    /**
     * Add listener (Observer pattern)
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    /**
     * Remove listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    /**
     * Notify all listeners of new message
     */
    private void notifyListeners(Message message) {
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of connection loss
     */
    private void notifyConnectionLost(String reason) {
        connected = false;
        for (MessageListener listener : messageListeners) {
            try {
                listener.onConnectionLost(reason);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    // ========== DISCONNECTION ==========
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            connected = false;
            
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
            
            System.out.println("Disconnected from server");
            
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    // ========== GETTERS ==========
    
    public User getCurrentUser() { return currentUser; }
    public String getUsername() { return currentUser != null ? currentUser.getUsername() : ""; }
    public boolean isConnected() { return connected; }
    public boolean isAuthenticated() { return authenticated; }
}
```

---

### 2.3 Message Routing in ChatServer

```java
/**
 * Broadcast message to ALL connected clients
 */
public void broadcastMessage(Message message) {
    Logger.info("Broadcasting from " + message.getSender());
    
    for (ClientHandler handler : clientHandlers) {
        try {
            handler.sendMessage(message);
        } catch (IOException e) {
            Logger.error("Failed to send to " + handler.getUsername());
        }
    }
}

/**
 * Send to all TEACHERS only
 */
public void sendToTeachers(Message message) {
    Logger.info("Sending to teachers from " + message.getSender());
    
    for (ClientHandler handler : clientHandlers) {
        if (handler.getRole() == UserRole.TEACHER) {
            try {
                handler.sendMessage(message);
            } catch (IOException e) {
                Logger.error("Failed to send to teacher");
            }
        }
    }
}

/**
 * Send PRIVATE message to specific user
 */
public void sendPrivateMessage(String recipientUsername, Message message) {
    Logger.info("Private: " + message.getSender() + " → " + recipientUsername);
    
    for (ClientHandler handler : clientHandlers) {
        if (handler.getUsername().equals(recipientUsername)) {
            try {
                handler.sendMessage(message);
                return;  // Found and sent
            } catch (IOException e) {
                Logger.error("Failed to send private message");
            }
        }
    }
    
    // Recipient not found
    Logger.warning("User not found: " + recipientUsername);
}
```

---

## 🔗 PART 3: Connections to Other Members

### To Member 1 (ChatServer)
```
You provide routing methods:
- broadcastMessage()
- sendToTeachers()
- sendPrivateMessage()

Member 1 calls these from server.
```

### To Member 2 (ClientHandler)
```
Member 2 uses your Message class:
- Receives Message objects
- Processes based on type
- Sends Message responses

Your protocol defines what Member 2 handles!
```

### To Member 4 & 5
```
File notifications use your Message protocol
UDP announcements might trigger Messages
All communication uses your Message structure
```

---

## 🎓 PART 4: Viva Questions

**Q1: Why use Message objects instead of plain strings?**
```
A: Structure and type safety!

Plain strings:
"LOGIN:user1:pass123"  → Must parse manually
"CHAT:user1:ALL:Hello" → Error-prone
❌ No compiler checks

Message objects:
Message msg = new Message(LOGIN, "user1", "SERVER", "user1:pass123");
✅ Type-safe (MessageType enum)
✅ No parsing errors
✅ Easy to extend (add new fields)
✅ Automatic serialization
```

**Q2: Explain the Observer pattern in our app.**
```
A: ChatClient = Subject (has message data)
   UI Windows = Observers (want to be notified)

Flow:
1. Window calls: client.addMessageListener(this)
2. ChatClient adds window to list
3. Message arrives from network
4. ChatClient calls: listener.onMessageReceived(msg)
5. All registered windows notified
6. Windows update UI

Benefits:
✅ Decoupling (network layer doesn't know about UI)
✅ Multiple listeners (multiple windows can listen)
✅ Event-driven (reactive, not polling)
```

**Q3: Why separate thread for reading messages?**
```
A: Because readObject() BLOCKS!

If reading in UI thread:
UI Thread: readObject() → ⏸️ BLOCKED
User clicks button → 💀 No response! UI frozen!

With separate thread:
Reader Thread: readObject() → ⏸️ BLOCKED (only this thread)
UI Thread: Responsive, handles clicks ✅
When message arrives: Reader thread notifies UI ✅
```

---

## ✅ Key Takeaways

1. **Protocol = structured communication rules**
2. **Message class = universal communication object**
3. **Observer pattern = event-driven notification**
4. **Separate reader thread prevents UI freezing**
5. **Routing logic determines message destinations**
6. **Type safety through MessageType enum**
7. **Serialization enables object transmission**

**Master these concepts and you understand the heart of the application! 🎯**
