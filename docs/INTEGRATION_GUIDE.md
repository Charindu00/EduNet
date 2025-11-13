# 🔗 Integration Guide: How All Members Connect

## 📋 Complete Team Integration Overview

This document explains how each member's work connects with others, forming a complete system.

---

## 🎯 The Big Picture

```
                    ┌──────────────────────────────────┐
                    │     EDUNET ARCHITECTURE          │
                    └──────────────────────────────────┘

┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  MEMBER 1   │────────>│  MEMBER 2   │────────>│  MEMBER 3   │
│  Server     │         │  Client     │         │  Message    │
│  Foundation │         │  Handler    │         │  Protocol   │
└─────────────┘         └─────────────┘         └─────────────┘
      │                       │                        │
      │                       │                        │
      ▼                       ▼                        ▼
┌─────────────┐         ┌─────────────┐         
│  MEMBER 4   │         │  MEMBER 5   │         
│  File       │         │  UDP        │         
│  Transfer   │         │  Broadcast  │         
└─────────────┘         └─────────────┘         
```

---

## 🔄 Connection Flow Matrix

### MEMBER 1 → MEMBER 2

**What Member 1 Provides:**
```java
// Member 1 (ChatServer.java)
Socket clientSocket = serverSocket.accept();  // Accepts connection
ClientHandler handler = new ClientHandler(clientSocket, this);
handler.start();  // Starts Member 2's thread
```

**What Member 2 Receives:**
```java
// Member 2 (ClientHandler.java)
public ClientHandler(Socket socket, ChatServer server) {
    this.socket = socket;      // ← Socket from Member 1
    this.server = server;      // ← Server reference from Member 1
}
```

**Interface Contract:**
```java
// Member 1 must provide these methods for Member 2:
public void addClient(ClientHandler handler);
public void removeClient(ClientHandler handler);
public void broadcastMessage(Message message);
public void sendToTeachers(Message message);
public void sendPrivateMessage(String recipient, Message message);
public String getConnectedUsersList();
public void kickUser(String username, String adminName);
```

---

### MEMBER 2 → MEMBER 3

**What Member 3 Provides:**
```java
// Member 3 (Message.java)
public class Message implements Serializable {
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
}
```

**How Member 2 Uses It:**
```java
// Member 2 (ClientHandler.java)
Message loginMsg = (Message) input.readObject();  // Receive
processMessage(loginMsg);                          // Process
sendMessage(responseMsg);                          // Send
```

**Message Types Member 2 Handles:**
```
LOGIN              → Authenticate user
CHAT_BROADCAST     → Send to all if teacher
CHAT_TO_TEACHER    → Send to teachers if student
CHAT_PRIVATE       → Send to specific user
FILE_NOTIFICATION  → Broadcast file availability
ADMIN_*            → Admin commands
```

---

### MEMBER 3 → MEMBER 2

**What Member 2 Provides:**
```java
// Member 2 (ClientHandler.java)
public void sendMessage(Message message) throws IOException {
    synchronized (output) {
        output.writeObject(message);  // Uses Member 3's Message
        output.flush();
    }
}
```

**How Member 3 Uses It:**
```java
// Member 3 (ChatClient.java)
Message msg = new Message(LOGIN, username, "SERVER", credentials);
client.sendMessage(msg);  // → Goes to Member 2's handler
```

**Complete Flow:**
```
Client (Member 3) → Creates Message
    ↓
Client sends via socket
    ↓
Server (Member 1) → Accepts connection
    ↓
ClientHandler (Member 2) → Receives Message
    ↓
Process and route
    ↓
Send response Message back
    ↓
Client (Member 3) → Receives and displays
```

---

### MEMBER 1 → MEMBER 4

**Integration Point:**
```java
// Member 1 (ChatServer.java)
public void start() {
    // Start main server
    serverSocket = new ServerSocket(5000);
    
    // Start file transfer handler
    fileTransferHandler = new FileTransferHandler(this);  // Pass 'this'
    fileTransferHandler.start();
    
    // Both run concurrently!
}
```

**What Member 4 Receives:**
```java
// Member 4 (FileTransferHandler.java)
public FileTransferHandler(ChatServer chatServer) {
    this.chatServer = chatServer;  // ← Server reference from Member 1
}

// When file uploaded, notify via Member 1:
chatServer.broadcastMessage(fileNotification);
```

**Communication:**
```
Member 4 needs Member 1 for:
1. Server reference (to broadcast notifications)
2. Lifecycle management (start/stop with main server)
3. Client list access (who to notify)
```

---

### MEMBER 4 → MEMBER 3

**File Notification Flow:**
```java
// Member 4 (FileTransferHandler.java)
private void notifyNewFile(String filename, String uploader, UserRole role) {
    // Creates Member 3's Message object
    Message msg = new Message(
        MessageType.FILE_NOTIFICATION,
        "SERVER",
        "ALL",
        "New lecture: " + filename
    );
    
    // Sends via Member 1's broadcast
    chatServer.broadcastMessage(msg);
}
```

**How Member 3 Receives:**
```java
// Member 3 (ChatClient.java)
private void notifyListeners(Message message) {
    if (message.getType() == FILE_NOTIFICATION) {
        for (MessageListener listener : messageListeners) {
            listener.onMessageReceived(message);  // UI displays notification
        }
    }
}
```

---

### MEMBER 1 → MEMBER 5

**Integration:**
```java
// Member 1 (ChatServer.java)
public void start() {
    // Start main chat server
    serverSocket = new ServerSocket(5000);
    
    // Start file transfer
    fileTransferHandler = new FileTransferHandler(this);
    fileTransferHandler.start();
    
    // Start UDP server
    udpServer = new UDPAnnouncementServer();  // Member 5's code
    udpServer.start();
    
    // All three services running independently!
}

public void shutdown() {
    // Stop all services
    serverSocket.close();
    fileTransferHandler.stopServer();
    udpServer.stop();  // Member 5's stop method
}
```

**Why Independent:**
```
UDP (Member 5) runs completely separate from TCP (Members 1-4)
- Different port (6000 vs 5000/5001)
- Different protocol (UDP vs TCP)
- Different thread
- Different socket type (DatagramSocket vs ServerSocket)

Only integration: Lifecycle management (start/stop together)
```

---

### MEMBER 5 → UI (Students)

**Student Window Integration:**
```java
// StudentWindow.java
public StudentWindow(ChatClient client) {
    // Member 3's client
    this.client = client;
    
    // Member 5's listener
    this.udpListener = new UDPAnnouncementListener(new AnnouncementCallback() {
        @Override
        public void onAnnouncementReceived(String announcement) {
            // Display popup to student
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    StudentWindow.this,
                    announcement,
                    "📢 Teacher Announcement",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
        }
    });
    
    udpListener.start();
}
```

---

## 📊 Data Flow Examples

### Example 1: Student Sends Message to Teacher

```
┌──────────┐                                              ┌──────────┐
│ STUDENT  │                                              │ TEACHER  │
│  (UI)    │                                              │   (UI)   │
└────┬─────┘                                              └────┬─────┘
     │                                                          │
     │ 1. Type "Hello teacher"                                 │
     │ 2. Click Send                                           │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 3      │                                           │
│  ChatClient     │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 3. Create Message(CHAT_TO_TEACHER, "student1", "TEACHER", "Hello")
     │ 4. sendMessage(msg)                                     │
     │                                                          │
     ▼                                                          │
   (TCP Socket - Port 5000)                                   │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 1      │                                           │
│  ChatServer     │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 5. Route to ClientHandler                              │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 2      │                                           │
│ ClientHandler   │                                           │
│  (student1)     │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 6. Process message                                      │
     │ 7. server.sendToTeachers(msg)                          │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 1      │                                           │
│  ChatServer     │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 8. Loop teachers: handler.sendMessage(msg)             │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 2      │                                           │
│ ClientHandler   │                                           │
│  (teacher1)     │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 9. output.writeObject(msg)                             │
     │                                                          │
     ▼                                                          │
   (TCP Socket - Port 5000)                                   │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│   MEMBER 3      │                                           │
│  ChatClient     │                                           │
│  (teacher)      │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 10. Message reader thread receives                     │
     │ 11. notifyListeners(msg)                               │
     │                                                          │
     ▼                                                          ▼
┌──────────┐                                              ┌──────────┐
│ TEACHER  │                                              │ TEACHER  │
│   (UI)   │                                              │   (UI)   │
└──────────┘                                              └──────────┘
                12. Display: "[student1]: Hello teacher"
```

---

### Example 2: Teacher Uploads Lecture

```
┌──────────┐
│ TEACHER  │
│   (UI)   │
└────┬─────┘
     │
     │ 1. Select file: "Chapter5.pdf"
     │ 2. Click Upload
     │
     ▼
┌─────────────────────┐
│     MEMBER 3        │
│  (UI Integration)   │
└────┬────────────────┘
     │
     │ 3. FileTransferClient ftc = new FileTransferClient(...)
     │ 4. ftc.uploadFile(file, progressCallback)
     │
     ▼
┌─────────────────────┐
│     MEMBER 4        │
│ FileTransferClient  │
└────┬────────────────┘
     │
     │ 5. Connect to port 5001 (NEW connection)
     │ 6. Send: "UPLOAD", username, role, filename, filesize
     │ 7. Send file bytes in 64KB chunks
     │
     ▼
   (TCP Socket - Port 5001)
     │
     ▼
┌─────────────────────┐
│     MEMBER 4        │
│ FileTransferHandler │
└────┬────────────────┘
     │
     │ 8. Receive metadata
     │ 9. Receive file data
     │ 10. Save to data/files/lectures/Chapter5.pdf
     │ 11. Send "SUCCESS"
     │ 12. Create file notification
     │
     ▼
┌─────────────────────┐
│     MEMBER 3        │
│     Message         │
└────┬────────────────┘
     │
     │ 13. Message(FILE_NOTIFICATION, "SERVER", "ALL", "New lecture...")
     │
     ▼
┌─────────────────────┐
│     MEMBER 1        │
│   ChatServer        │
└────┬────────────────┘
     │
     │ 14. broadcastMessage(notification)
     │
     ▼
┌─────────────────────┐
│     MEMBER 2        │
│  ClientHandler      │
│  (all clients)      │
└────┬────────────────┘
     │
     │ 15. Send to each client
     │
     ▼
   (All students receive notification via Member 3's ChatClient)
```

---

### Example 3: Teacher Sends Announcement (UDP)

```
┌──────────┐                                              ┌──────────┐
│ TEACHER  │                                              │ STUDENTS │
│   (UI)   │                                              │   (UI)   │
└────┬─────┘                                              └────┬─────┘
     │                                                          │
     │ 1. Type "Class at 2 PM"                                 │
     │ 2. Click Announcement                                   │
     │                                                          │
     ▼                                                          │
┌─────────────────┐                                           │
│  TEACHER WINDOW │                                           │
└────┬────────────┘                                           │
     │                                                          │
     │ 3. Create UDP packet                                    │
     │ 4. Send to server:6000                                  │
     │                                                          │
     ▼                                                          │
   (UDP Packet)                                                │
     │                                                          │
     ▼                                                          │
┌─────────────────────┐                                       │
│     MEMBER 5        │                                       │
│UDPAnnouncementServer│                                       │
└────┬────────────────┘                                       │
     │                                                          │
     │ 5. Receive packet                                       │
     │ 6. Parse: "ANNOUNCE|Class at 2 PM"                     │
     │ 7. Loop through registeredClients                      │
     │                                                          │
     ├──> Send to Student1:54321 ─┐                           │
     ├──> Send to Student2:54322 ─┼──> (UDP Packets)          │
     └──> Send to Student3:54323 ─┘                           │
                                    │                          │
                                    ▼                          │
                            ┌─────────────────────┐           │
                            │     MEMBER 5        │           │
                            │UDPAnnouncementListener│         │
                            │   (each student)    │           │
                            └────┬────────────────┘           │
                                 │                             │
                                 │ 8. Receive announcement     │
                                 │ 9. Trigger callback         │
                                 │                             │
                                 ▼                             ▼
                            ┌──────────┐                  ┌──────────┐
                            │ STUDENT  │                  │ STUDENTS │
                            │   (UI)   │                  │   (UI)   │
                            └──────────┘                  └──────────┘
                             10. Show popup: "📢 Class at 2 PM"
```

---

## 🎯 Key Integration Points Summary

### Member 1 ← → Member 2
```
Socket connection
ClientHandler creation
Broadcast methods
Client list management
```

### Member 2 ← → Member 3
```
Message objects
Serialization/deserialization
Message routing
Type-based processing
```

### Member 1 ← → Member 4
```
Lifecycle management
Server reference
Broadcast notifications
```

### Member 4 ← → Member 3
```
File notifications
Message protocol
UI integration
```

### Member 1 ← → Member 5
```
Lifecycle management
Independent operation
Separate port/protocol
```

### Member 5 ← → UI
```
Announcement callbacks
Registration
Display notifications
```

---

## ✅ Integration Checklist

For successful integration, ensure:

- [ ] **Member 1** provides all required methods for Member 2
- [ ] **Member 2** properly uses Member 3's Message class
- [ ] **Member 3** defines complete MessageType enum for all operations
- [ ] **Member 4** sends notifications via Member 1's broadcast
- [ ] **Member 5** starts/stops with Member 1's lifecycle
- [ ] All **port numbers** match in Constants.java
- [ ] All **Message types** are handled consistently
- [ ] **Error handling** consistent across all members
- [ ] **Logging** consistent across all members

---

## 🚀 Testing Integration

### Test 1: End-to-End Chat
```
1. Member 1: Start server
2. Member 3: Connect 2 clients (teacher, student)
3. Member 2: Authenticate both
4. Student sends message
5. Member 3: Teacher receives
✅ Tests Members 1, 2, 3 integration
```

### Test 2: File Upload
```
1. Start server (Members 1, 4)
2. Teacher uploads file (Member 4 client)
3. File saved (Member 4 server)
4. Notification broadcast (Member 1)
5. Students receive notification (Member 3)
✅ Tests Members 1, 3, 4 integration
```

### Test 3: UDP Announcement
```
1. Start server (Members 1, 5)
2. Students connect and register (Member 5 client)
3. Teacher sends announcement
4. Server broadcasts (Member 5 server)
5. Students receive popup
✅ Tests Member 5 integration
```

---

**This document serves as your integration roadmap! 🗺️**
