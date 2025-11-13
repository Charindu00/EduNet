# EduNet - Network Concepts & Application Flow Guide

## 📚 Table of Contents
1. [Network Architecture Overview](#network-architecture-overview)
2. [Key Network Concepts](#key-network-concepts)
3. [Complete Application Flow](#complete-application-flow)
4. [Role-Based Workflows](#role-based-workflows)
5. [Backend Implementation Details](#backend-implementation-details)

---

## 🏗️ Network Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                    EDUNET ARCHITECTURE                       │
└─────────────────────────────────────────────────────────────┘

SERVER SIDE (Multithreaded)                CLIENT SIDE
═══════════════════════════                ═══════════════

┌─────────────────────┐                    ┌──────────────┐
│   ChatServer        │                    │ LoginWindow  │
│   Port: 5000        │◄───TCP───────────►│              │
│   (Main Server)     │   Persistent      │ ChatClient   │
└──────────┬──────────┘   Connection      └──────┬───────┘
           │                                      │
           ├──► ClientHandler Thread 1           │
           ├──► ClientHandler Thread 2           │
           ├──► ClientHandler Thread 3           │
           └──► ClientHandler Thread N           │
                                                  │
┌─────────────────────┐                          │
│ FileTransferHandler │                    ┌─────▼────────┐
│   Port: 5001        │◄───TCP────────────►│ FileTransfer │
│ (Separate Thread)   │   On-Demand       │   Client     │
└─────────────────────┘   Connection      └──────────────┘

┌─────────────────────┐                    ┌──────────────┐
│ UDPAnnouncementSvr  │                    │     UDP      │
│   Port: 6000        │◄───UDP────────────►│ Announcement │
│ (Broadcast Thread)  │   Broadcast       │   Listener   │
└─────────────────────┘   Messages        └──────────────┘
```

### Three Network Communication Channels

| Channel | Protocol | Port | Purpose | Connection Type |
|---------|----------|------|---------|-----------------|
| **Main Chat** | TCP | 5000 | Authentication, messaging, user management | Persistent (always connected) |
| **File Transfer** | TCP | 5001 | Upload/download lectures and assignments | On-demand (connects when needed) |
| **Announcements** | UDP | 6000 | Teacher broadcasts to all students | Connectionless (fire and forget) |

---

## 🌐 Key Network Concepts

### 1. TCP (Transmission Control Protocol)

**Used for: Main chat server (5000) and File transfer (5001)**

```java
// Server Side - Listening for connections
ServerSocket serverSocket = new ServerSocket(5000);  // Bind to port
Socket clientSocket = serverSocket.accept();         // Wait for client (BLOCKING)

// Client Side - Connecting to server
Socket socket = new Socket("127.0.0.1", 5000);       // Connect to server
```

**Key Characteristics:**
- ✅ **Reliable**: Guarantees message delivery
- ✅ **Ordered**: Messages arrive in the order sent
- ✅ **Connection-oriented**: Must establish connection first (3-way handshake)
- ✅ **Error checking**: Automatic retransmission if packets lost
- ❌ **Overhead**: Higher than UDP due to reliability mechanisms

**When to use:**
- Authentication (can't afford to lose login credentials)
- Chat messages (must arrive in order)
- File transfers (every byte must arrive correctly)

---

### 2. UDP (User Datagram Protocol)

**Used for: Teacher announcements (6000)**

```java
// Server Side - Send broadcast
DatagramSocket socket = new DatagramSocket(6000);
byte[] data = message.getBytes();
DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
socket.send(packet);  // Fire and forget!

// Client Side - Receive broadcast
DatagramSocket socket = new DatagramSocket();  // Random port
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet);  // Wait for announcement
```

**Key Characteristics:**
- ⚡ **Fast**: No connection overhead
- ⚡ **Low latency**: Minimal protocol processing
- ❌ **Unreliable**: No delivery guarantee
- ❌ **Unordered**: Messages may arrive out of order
- ✅ **Lightweight**: Perfect for non-critical broadcasts

**When to use:**
- Announcements (if one student misses it, not critical)
- Real-time updates where latest info matters more than history
- Notifications that can be repeated

---

### 3. Object Serialization (Java Streams)

**Used for: Sending structured messages between client and server**

```java
// Define Message class (implements Serializable)
public class Message implements Serializable {
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
}

// Server Side - Send/Receive objects
ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

// Send a message object
Message msg = new Message(MessageType.CHAT_BROADCAST, "teacher1", "ALL", "Hello class!");
output.writeObject(msg);

// Receive a message object
Message received = (Message) input.readObject();
```

**Why use Object Streams?**
- ✅ **Structured data**: Send complex objects, not just strings
- ✅ **Type safety**: Java enforces data types
- ✅ **Automatic serialization**: Java handles conversion to bytes
- ✅ **Easy to extend**: Add new fields without changing protocol

---

### 4. Multithreading Architecture

**Why threads are essential:**

```
WITHOUT THREADS (Sequential):
Client 1 connects → Server handles → Client 1 disconnects
Client 2 connects → Server handles → Client 2 disconnects
❌ Only one client can be served at a time!

WITH THREADS (Concurrent):
Client 1 connects → Thread 1 handles Client 1 ┐
Client 2 connects → Thread 2 handles Client 2 ├─ All run simultaneously
Client 3 connects → Thread 3 handles Client 3 ┘
✅ Multiple clients served concurrently!
```

**Thread-safe data structures:**
```java
// CopyOnWriteArrayList - Thread-safe without explicit locking
private CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

// Safe operations even when multiple threads access
clients.add(newClient);           // Thread 1
for (ClientHandler c : clients) { // Thread 2 can iterate safely
    c.sendMessage(msg);
}
```

---

### 5. Port Separation Strategy

**Why use multiple ports?**

```
SINGLE PORT (Port 5000 for everything):
┌─────────────────────────────────────────┐
│ Chat msg → File upload → Chat msg → ... │
│ ❌ Large file blocks all chat messages  │
└─────────────────────────────────────────┘

MULTIPLE PORTS:
Port 5000: Chat msg → Chat msg → Chat msg → ...     ✅ Always responsive
Port 5001: File upload (10 MB) ...                   ✅ Doesn't block chat
Port 6000: Announcements (UDP) ...                   ✅ Instant delivery
```

---

## 🔄 Complete Application Flow

### Phase 1: Application Startup

```
┌──────────────┐
│ USER STARTS  │
│ APPLICATION  │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────────────────────────┐
│ 1. LoginWindow Opens                                 │
│    - Displays username/password fields              │
│    - Shows role dropdown (Teacher/Student/Admin)    │
│    - Status: "Ready to connect"                     │
└──────┬──────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────┐
│ 2. ChatClient Created                               │
│    - Client object initialized                      │
│    - Socket = null (not connected yet)              │
│    - connected = false                              │
│    - authenticated = false                          │
└──────┬──────────────────────────────────────────────┘
       │
       │ User enters credentials and clicks "Login"
       ▼
```

---

### Phase 2: Connection & Authentication

#### Step 2.1: TCP Connection Establishment

```java
// CLIENT SIDE (ChatClient.java)
┌─────────────────────────────────────────────────────────┐
│ client.connect()                                        │
│                                                         │
│ 1. Create TCP socket                                   │
│    socket = new Socket("127.0.0.1", 5000)             │
│                                                         │
│    Behind the scenes:                                  │
│    - OS performs 3-way TCP handshake:                 │
│      Client → SYN → Server                            │
│      Client ← SYN-ACK ← Server                        │
│      Client → ACK → Server                            │
│    - Connection established!                          │
│                                                         │
│ 2. Create output stream (MUST BE FIRST!)              │
│    output = new ObjectOutputStream(socket.getOutputStream())│
│    output.flush()  // Send header                     │
│                                                         │
│ 3. Create input stream                                │
│    input = new ObjectInputStream(socket.getInputStream())│
│                                                         │
│ ✅ connected = true                                    │
└─────────────────────────────────────────────────────────┘
       │
       │ TCP connection established
       ▼

// SERVER SIDE (ChatServer.java)
┌─────────────────────────────────────────────────────────┐
│ serverSocket.accept()  // Was blocking, waiting...     │
│                                                         │
│ 1. Accept() returns a Socket for this client          │
│    Socket clientSocket = serverSocket.accept()         │
│                                                         │
│ 2. Create ClientHandler thread                        │
│    ClientHandler handler = new ClientHandler(clientSocket, this)│
│    handler.start()  // Starts new thread              │
│                                                         │
│ 3. Server goes back to accept() for next client       │
│    (Main server thread never blocks!)                 │
└─────────────────────────────────────────────────────────┘
       │
       │ ClientHandler thread running
       ▼

// CLIENT HANDLER (ClientHandler.java - Thread)
┌─────────────────────────────────────────────────────────┐
│ ClientHandler.run()                                     │
│                                                         │
│ 1. Setup streams                                       │
│    output = new ObjectOutputStream(socket.getOutputStream())│
│    input = new ObjectInputStream(socket.getInputStream())│
│                                                         │
│ 2. Call authenticate()                                 │
│    - Ready to receive login credentials                │
└─────────────────────────────────────────────────────────┘
```

#### Step 2.2: Authentication Handshake

```java
// CLIENT SIDE
┌─────────────────────────────────────────────────────────┐
│ client.login("teacher1", "pass123", TEACHER)           │
│                                                         │
│ 1. Create login message                                │
│    credentials = "teacher1:pass123:TEACHER"            │
│    Message loginMsg = new Message(                     │
│        MessageType.LOGIN,                              │
│        "teacher1",                                     │
│        "SERVER",                                       │
│        credentials                                     │
│    )                                                   │
│                                                         │
│ 2. Send to server                                      │
│    output.writeObject(loginMsg)                        │
│    output.flush()                                      │
│                                                         │
│ 3. Wait for response (BLOCKING)                        │
│    Message response = (Message) input.readObject()     │
└─────────────────────────────────────────────────────────┘
       │
       │ Message travels over TCP
       ▼

// SERVER SIDE (ClientHandler thread)
┌─────────────────────────────────────────────────────────┐
│ authenticate()                                          │
│                                                         │
│ 1. Read login message                                  │
│    Message loginMsg = (Message) input.readObject()     │
│                                                         │
│ 2. Parse credentials                                   │
│    String[] parts = loginMsg.getContent().split(":")   │
│    username = parts[0]  // "teacher1"                  │
│    password = parts[1]  // "pass123"                   │
│    role = parts[2]      // "TEACHER"                   │
│                                                         │
│ 3. Verify against users.txt                            │
│    boolean valid = FileUtils.validateUser(username, password, role)│
│                                                         │
│ 4a. If VALID:                                          │
│     this.user = new User(username, password, role)     │
│     this.authenticated = true                          │
│     server.addClient(this)  // Add to active clients   │
│                                                         │
│     Message success = new Message(LOGIN_SUCCESS, "SERVER", username, "Welcome!")│
│     output.writeObject(success)                        │
│                                                         │
│ 4b. If INVALID:                                        │
│     Message failure = new Message(LOGIN_FAILED, "SERVER", username, "Invalid credentials")│
│     output.writeObject(failure)                        │
│     socket.close()  // Disconnect                      │
└─────────────────────────────────────────────────────────┘
       │
       │ Response travels back
       ▼

// CLIENT SIDE
┌─────────────────────────────────────────────────────────┐
│ Receive response                                        │
│                                                         │
│ if (response.getType() == LOGIN_SUCCESS)               │
│     this.currentUser = new User(username, password, role)│
│     this.authenticated = true                          │
│     startMessageReader()  // Start listening thread    │
│     return true                                        │
│                                                         │
│ else                                                   │
│     Show error dialog                                  │
│     return false                                       │
└─────────────────────────────────────────────────────────┘
```

---

### Phase 3: Post-Login - Starting Message Listener

```java
// CLIENT SIDE (ChatClient.java)
┌─────────────────────────────────────────────────────────┐
│ startMessageReader()                                    │
│                                                         │
│ 1. Create background thread                            │
│    readerThread = new Thread(() -> {                   │
│        while (connected) {                             │
│            Message msg = (Message) input.readObject()  │
│            notifyListeners(msg)  // Tell UI!           │
│        }                                               │
│    })                                                  │
│                                                         │
│ 2. Start thread                                        │
│    readerThread.start()                                │
│                                                         │
│ WHY SEPARATE THREAD?                                   │
│ - input.readObject() BLOCKS until message arrives      │
│ - UI thread must stay responsive                       │
│ - This thread ONLY reads messages                      │
│ - UI thread can send messages anytime                  │
└─────────────────────────────────────────────────────────┘
```

---

### Phase 4: Opening Role-Based Window

```java
// CLIENT SIDE (LoginWindow.java)
┌─────────────────────────────────────────────────────────┐
│ After successful login, open appropriate window:       │
│                                                         │
│ if (role == TEACHER)                                   │
│     new TeacherWindow(client)                          │
│                                                         │
│ else if (role == STUDENT)                              │
│     new StudentWindow(client)                          │
│                                                         │
│ else if (role == ADMIN)                                │
│     new AdminDashboard(client)                         │
│                                                         │
│ dispose()  // Close login window                       │
└─────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│ Role Window Constructor                                 │
│                                                         │
│ 1. Save reference to ChatClient                        │
│    this.client = client                                │
│                                                         │
│ 2. Register as message listener (Observer Pattern)     │
│    client.addMessageListener(this)                     │
│                                                         │
│ 3. Initialize UI components                            │
│    - Chat area (JTextArea)                             │
│    - Message input field                               │
│    - Role-specific buttons                             │
│                                                         │
│ 4. For STUDENT: Start UDP listener                     │
│    udpListener = new UDPAnnouncementListener(callback) │
│    udpListener.start()                                 │
└─────────────────────────────────────────────────────────┘
```

---

## 👥 Role-Based Workflows

### 🎓 STUDENT Workflow

#### Action 1: Sending Message to Teacher

```
┌────────────────┐
│ STUDENT TYPES  │
│ "Hello teacher"│
│ Clicks Send    │
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ StudentWindow - Send Button Action                      │
│                                                         │
│ 1. Get message text                                    │
│    String text = messageField.getText()                │
│                                                         │
│ 2. Create message object                               │
│    Message msg = new Message(                          │
│        MessageType.CHAT_TO_TEACHER,                    │
│        client.getUsername(),  // "student1"            │
│        "TEACHER",              // To all teachers      │
│        text                    // "Hello teacher"      │
│    )                                                   │
│                                                         │
│ 3. Send through ChatClient                             │
│    client.sendMessage(msg)                             │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ ChatClient.sendMessage()                                │
│                                                         │
│ output.writeObject(msg)  // Send over TCP socket       │
│ output.flush()                                         │
└─────────────────────────────────────────────────────────┘
         │
         │ Message travels over network
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - ClientHandler for this student                │
│                                                         │
│ 1. Receive message                                     │
│    Message msg = (Message) input.readObject()          │
│                                                         │
│ 2. Process based on type                               │
│    if (msg.getType() == CHAT_TO_TEACHER)              │
│        server.sendToAllTeachers(msg)                   │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ ChatServer.sendToAllTeachers()                          │
│                                                         │
│ for (ClientHandler handler : clientHandlers) {         │
│     if (handler.getRole() == TEACHER) {                │
│         handler.sendMessage(msg)                       │
│     }                                                  │
│ }                                                      │
└─────────────────────────────────────────────────────────┘
         │
         │ Message sent to each teacher
         ▼
┌─────────────────────────────────────────────────────────┐
│ TEACHER's ClientHandler                                 │
│                                                         │
│ output.writeObject(msg)  // Send to teacher's client   │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ TEACHER's ChatClient - Message Reader Thread            │
│                                                         │
│ Message msg = (Message) input.readObject()             │
│ notifyListeners(msg)  // Notify all registered UI      │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ TeacherWindow.onMessageReceived(msg)                    │
│                                                         │
│ SwingUtilities.invokeLater(() -> {                     │
│     chatArea.append("[student1]: Hello teacher\n")     │
│ })                                                     │
│                                                         │
│ ✅ Message displayed in teacher's window!              │
└─────────────────────────────────────────────────────────┘
```

#### Action 2: Receiving Teacher Announcement (UDP)

```
┌────────────────┐
│ TEACHER sends  │
│ UDP Announcement│
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ TeacherWindow - Announcement Button                     │
│                                                         │
│ 1. Get announcement text                               │
│    String announcement = JOptionPane.showInputDialog(...)│
│                                                         │
│ 2. Send UDP packet to server                           │
│    DatagramSocket socket = new DatagramSocket()        │
│    byte[] data = ("ANNOUNCE|" + announcement).getBytes()│
│    DatagramPacket packet = new DatagramPacket(         │
│        data, data.length,                              │
│        InetAddress.getByName("localhost"),             │
│        Constants.UDP_PORT  // 6000                     │
│    )                                                   │
│    socket.send(packet)  // Fire and forget!            │
└─────────────────────────────────────────────────────────┘
         │
         │ UDP packet travels to server
         ▼
┌─────────────────────────────────────────────────────────┐
│ UDPAnnouncementServer (Server side)                     │
│                                                         │
│ 1. Receive UDP packet                                  │
│    DatagramPacket packet = new DatagramPacket(buffer, len)│
│    socket.receive(packet)                              │
│                                                         │
│ 2. Parse message                                       │
│    String message = new String(packet.getData())       │
│    // "ANNOUNCE|Class starts at 2 PM"                 │
│                                                         │
│ 3. Broadcast to all registered students                │
│    for (InetSocketAddress studentAddr : registeredClients) {│
│        DatagramPacket outPacket = new DatagramPacket(  │
│            data, data.length,                          │
│            studentAddr.getAddress(),                   │
│            studentAddr.getPort()                       │
│        )                                               │
│        socket.send(outPacket)                          │
│    }                                                   │
└─────────────────────────────────────────────────────────┘
         │
         │ UDP packets sent to all students
         ▼
┌─────────────────────────────────────────────────────────┐
│ STUDENT's UDPAnnouncementListener (Client side)         │
│                                                         │
│ 1. Receive UDP packet (was waiting...)                │
│    DatagramPacket packet = new DatagramPacket(buffer, len)│
│    socket.receive(packet)                              │
│                                                         │
│ 2. Extract announcement                                │
│    String announcement = new String(packet.getData())  │
│                                                         │
│ 3. Notify callback                                     │
│    callback.onAnnouncementReceived(announcement)       │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ StudentWindow - UDP Callback                            │
│                                                         │
│ SwingUtilities.invokeLater(() -> {                     │
│     JOptionPane.showMessageDialog(                     │
│         this,                                          │
│         "📢 ANNOUNCEMENT: Class starts at 2 PM",       │
│         "Teacher Announcement",                        │
│         JOptionPane.INFORMATION_MESSAGE                │
│     )                                                  │
│ })                                                     │
│                                                         │
│ ✅ Popup shown to student immediately!                 │
└─────────────────────────────────────────────────────────┘
```

#### Action 3: Downloading Lecture File

```
┌────────────────┐
│ STUDENT clicks │
│ "Download File"│
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ StudentWindow - Download Button                         │
│                                                         │
│ 1. Request file list from server                       │
│    Message listMsg = new Message(FILE_LIST, ...)       │
│    client.sendMessage(listMsg)                         │
│                                                         │
│ 2. Receive file list (via TCP)                         │
│    // Server sends list of available lectures          │
│                                                         │
│ 3. Show file selection dialog                          │
│    String[] files = {"Lecture1.pdf", "Lecture2.pptx"}  │
│    String selected = JOptionPane.showInputDialog(...)  │
│                                                         │
│ 4. Initiate download                                   │
│    FileTransferClient ftClient = new FileTransferClient(...)│
│    ftClient.downloadFile(selected, saveLocation, progressCallback)│
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ FileTransferClient.downloadFile()                       │
│                                                         │
│ 1. Connect to file transfer port (NEW connection!)     │
│    socket = new Socket("127.0.0.1", 5001)             │
│                                                         │
│ 2. Send download request                               │
│    dos.writeUTF("DOWNLOAD")                            │
│    dos.writeUTF(username)                              │
│    dos.writeUTF(role.toString())                       │
│    dos.writeUTF(filename)  // "Lecture1.pdf"           │
│    dos.flush()                                         │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - FileTransferHandler                            │
│                                                         │
│ 1. Accept connection (separate thread)                 │
│    Socket clientSocket = serverSocket.accept()         │
│                                                         │
│ 2. Read request                                        │
│    String action = dis.readUTF()     // "DOWNLOAD"     │
│    String username = dis.readUTF()   // "student1"     │
│    String role = dis.readUTF()       // "STUDENT"      │
│    String filename = dis.readUTF()   // "Lecture1.pdf" │
│                                                         │
│ 3. Locate file                                         │
│    File file = new File(Constants.LECTURES_DIR + filename)│
│                                                         │
│ 4. Send file metadata                                  │
│    dos.writeUTF("SUCCESS")                             │
│    dos.writeLong(file.length())  // File size          │
│                                                         │
│ 5. Send file data in chunks                            │
│    FileInputStream fis = new FileInputStream(file)     │
│    byte[] buffer = new byte[65536]  // 64KB chunks     │
│    while ((bytesRead = fis.read(buffer)) > 0) {       │
│        dos.write(buffer, 0, bytesRead)                 │
│    }                                                   │
│    dos.flush()                                         │
│                                                         │
│ 6. Close connection                                    │
│    socket.close()                                      │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ FileTransferClient (continued)                          │
│                                                         │
│ 1. Receive file metadata                               │
│    String response = dis.readUTF()  // "SUCCESS"       │
│    long fileSize = dis.readLong()                      │
│                                                         │
│ 2. Receive file data                                   │
│    FileOutputStream fos = new FileOutputStream(saveLocation)│
│    byte[] buffer = new byte[65536]                     │
│    long totalReceived = 0                              │
│                                                         │
│    while (totalReceived < fileSize) {                  │
│        int bytesRead = dis.read(buffer)                │
│        fos.write(buffer, 0, bytesRead)                 │
│        totalReceived += bytesRead                      │
│                                                         │
│        // Update progress                              │
│        int progress = (int)((totalReceived * 100) / fileSize)│
│        progressCallback.onProgress(progress)           │
│    }                                                   │
│                                                         │
│ 3. Close streams                                       │
│    fos.close()                                         │
│    socket.close()                                      │
│                                                         │
│ ✅ File downloaded successfully!                       │
└─────────────────────────────────────────────────────────┘
```

---

### 👨‍🏫 TEACHER Workflow

#### Action 1: Broadcasting Message to All Students

```
┌────────────────┐
│ TEACHER types  │
│ "Good morning" │
│ Clicks Broadcast│
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ TeacherWindow - Broadcast Button                        │
│                                                         │
│ Message msg = new Message(                             │
│     MessageType.CHAT_BROADCAST,                        │
│     "teacher1",                                        │
│     "ALL",                                             │
│     "Good morning"                                     │
│ )                                                      │
│ client.sendMessage(msg)                                │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - Receives broadcast message                     │
│                                                         │
│ server.broadcastMessage(msg)                            │
│                                                         │
│ for (ClientHandler handler : clientHandlers) {         │
│     handler.sendMessage(msg)                           │
│ }                                                      │
│                                                         │
│ ✅ Message sent to ALL connected clients               │
│    (students, teachers, admins all receive it)         │
└─────────────────────────────────────────────────────────┘
```

#### Action 2: Uploading Lecture File

```
┌────────────────┐
│ TEACHER clicks │
│ "Upload Lecture"│
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ TeacherWindow - Upload Button                           │
│                                                         │
│ 1. Open file chooser                                   │
│    JFileChooser chooser = new JFileChooser()           │
│    File file = chooser.getSelectedFile()               │
│                                                         │
│ 2. Create file transfer client                         │
│    FileTransferClient ftClient = new FileTransferClient(...)│
│                                                         │
│ 3. Upload with progress tracking                       │
│    boolean success = ftClient.uploadFile(              │
│        file,                                           │
│        (message, progress) -> {                        │
│            progressBar.setValue(progress)              │
│            statusLabel.setText(message)                │
│        }                                               │
│    )                                                   │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ FileTransferClient.uploadFile()                         │
│                                                         │
│ 1. Connect to port 5001                                │
│    socket = new Socket("127.0.0.1", 5001)             │
│                                                         │
│ 2. Send metadata                                       │
│    dos.writeUTF("UPLOAD")                              │
│    dos.writeUTF("teacher1")                            │
│    dos.writeUTF("TEACHER")                             │
│    dos.writeUTF("Lecture5.pdf")                        │
│    dos.writeLong(file.length())  // 2MB                │
│                                                         │
│ 3. Send file in 64KB chunks                            │
│    while ((bytesRead = fis.read(buffer)) > 0) {       │
│        dos.write(buffer, 0, bytesRead)                 │
│        sentBytes += bytesRead                          │
│        int progress = (sentBytes * 100) / totalBytes   │
│        progressCallback.onProgress(progress)           │
│    }                                                   │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - FileTransferHandler                            │
│                                                         │
│ 1. Receive metadata                                    │
│    String action = dis.readUTF()     // "UPLOAD"       │
│    String username = dis.readUTF()   // "teacher1"     │
│    String role = dis.readUTF()       // "TEACHER"      │
│    String filename = dis.readUTF()   // "Lecture5.pdf" │
│    long fileSize = dis.readLong()    // 2097152 bytes  │
│                                                         │
│ 2. Determine save path                                 │
│    String savePath = Constants.LECTURES_DIR + filename │
│    // "data/files/lectures/Lecture5.pdf"              │
│                                                         │
│ 3. Receive and save file                               │
│    FileOutputStream fos = new FileOutputStream(savePath)│
│    long received = 0                                   │
│    while (received < fileSize) {                       │
│        int bytesRead = dis.read(buffer)                │
│        fos.write(buffer, 0, bytesRead)                 │
│        received += bytesRead                           │
│    }                                                   │
│                                                         │
│ 4. Send confirmation                                   │
│    dos.writeUTF("SUCCESS")                             │
│                                                         │
│ 5. Notify all clients about new file                   │
│    Message notification = new Message(                 │
│        FILE_NOTIFICATION,                              │
│        "SERVER",                                       │
│        "ALL",                                          │
│        "New lecture available: Lecture5.pdf"           │
│    )                                                   │
│    chatServer.broadcastMessage(notification)           │
│                                                         │
│ ✅ File saved and all students notified!               │
└─────────────────────────────────────────────────────────┘
```

---

### 🛡️ ADMIN Workflow

#### Action 1: Viewing Connected Users

```
┌────────────────┐
│ ADMIN opens    │
│ Dashboard      │
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ AdminDashboard Constructor                              │
│                                                         │
│ 1. Start auto-refresh timer                            │
│    refreshTimer = new Timer(5000, e -> {               │
│        requestUserList()                               │
│    })                                                  │
│    refreshTimer.start()                                │
│                                                         │
│ 2. Initial user list request                           │
│    requestUserList()                                   │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ requestUserList()                                       │
│                                                         │
│ Message request = new Message(                         │
│     MessageType.ADMIN_USER_LIST_REQUEST,               │
│     "admin1",                                          │
│     "SERVER",                                          │
│     ""                                                 │
│ )                                                      │
│ client.sendMessage(request)                            │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - Process admin request                          │
│                                                         │
│ if (msg.getType() == ADMIN_USER_LIST_REQUEST) {        │
│     if (sender.getRole() == ADMIN) {                   │
│         String userList = server.getConnectedUsersList()│
│         // Format: "username1:TEACHER|username2:STUDENT|..."│
│                                                         │
│         Message response = new Message(                │
│             ADMIN_USER_LIST_RESPONSE,                  │
│             "SERVER",                                  │
│             sender.getUsername(),                      │
│             userList                                   │
│         )                                              │
│         sender.sendMessage(response)                   │
│     } else {                                           │
│         // Unauthorized! Log security event            │
│     }                                                  │
│ }                                                      │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ AdminDashboard.onMessageReceived()                      │
│                                                         │
│ if (msg.getType() == ADMIN_USER_LIST_RESPONSE) {       │
│     String userList = msg.getContent()                 │
│     updateUserTable(userList)                          │
│                                                         │
│     // Parse and populate JTable                       │
│     String[] users = userList.split("\\|")             │
│     for (String user : users) {                        │
│         String[] parts = user.split(":")               │
│         tableModel.addRow(new Object[]{                │
│             parts[0],  // Username                     │
│             parts[1],  // Role                         │
│             parts[2],  // IP address                   │
│             parts[3]   // Connection time              │
│         })                                             │
│     }                                                  │
│                                                         │
│     ✅ User table updated with live data!              │
│ }                                                      │
└─────────────────────────────────────────────────────────┘
```

#### Action 2: Kicking a User

```
┌────────────────┐
│ ADMIN selects  │
│ user in table  │
│ Clicks "Kick"  │
└────────┬───────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ AdminDashboard - Kick Button                            │
│                                                         │
│ 1. Get selected user                                   │
│    int row = userTable.getSelectedRow()                │
│    String username = (String) tableModel.getValueAt(row, 0)│
│                                                         │
│ 2. Confirm action                                      │
│    int confirm = JOptionPane.showConfirmDialog(...)    │
│                                                         │
│ 3. Send kick command                                   │
│    Message kickMsg = new Message(                      │
│        MessageType.ADMIN_KICK_USER,                    │
│        "admin1",                                       │
│        "SERVER",                                       │
│        username  // User to kick                       │
│    )                                                   │
│    client.sendMessage(kickMsg)                         │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ SERVER - Process kick command                           │
│                                                         │
│ if (msg.getType() == ADMIN_KICK_USER) {                │
│     if (sender.getRole() == ADMIN) {                   │
│         String targetUser = msg.getContent()           │
│         server.kickUser(targetUser, sender.getUsername())│
│     }                                                  │
│ }                                                      │
└─────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│ ChatServer.kickUser()                                   │
│                                                         │
│ for (ClientHandler handler : clientHandlers) {         │
│     if (handler.getUsername().equals(targetUser)) {    │
│         // Notify user they're being kicked            │
│         Message kickMsg = new Message(                 │
│             KICKED,                                    │
│             "SERVER",                                  │
│             targetUser,                                │
│             "You have been kicked by " + adminName     │
│         )                                              │
│         handler.sendMessage(kickMsg)                   │
│                                                         │
│         // Close connection                            │
│         handler.disconnect()                           │
│                                                         │
│         // Remove from list                            │
│         clientHandlers.remove(handler)                 │
│                                                         │
│         Logger.info(targetUser + " kicked by " + adminName)│
│         break                                          │
│     }                                                  │
│ }                                                      │
│                                                         │
│ ✅ User disconnected!                                  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Backend Implementation Details

### 1. Message Protocol Structure

```java
// Every message sent has this structure
public class Message implements Serializable {
    private MessageType type;        // What kind of message?
    private String sender;            // Who sent it?
    private String receiver;          // Who should receive it?
    private String content;           // The actual data
    private LocalDateTime timestamp;  // When was it sent?
}

// Example messages:
┌──────────────────────────────────────────────────────┐
│ LOGIN MESSAGE                                        │
│ type: LOGIN                                          │
│ sender: "student1"                                   │
│ receiver: "SERVER"                                   │
│ content: "student1:password123:STUDENT"              │
│ timestamp: 2025-11-11T10:30:00                       │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ CHAT MESSAGE                                         │
│ type: CHAT_BROADCAST                                 │
│ sender: "teacher1"                                   │
│ receiver: "ALL"                                      │
│ content: "Today's class is at 2 PM"                  │
│ timestamp: 2025-11-11T10:35:00                       │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│ FILE NOTIFICATION                                    │
│ type: FILE_NOTIFICATION                              │
│ sender: "SERVER"                                     │
│ receiver: "ALL"                                      │
│ content: "New lecture uploaded: Chapter5.pdf"        │
│ timestamp: 2025-11-11T10:40:00                       │
└──────────────────────────────────────────────────────┘
```

### 2. Thread Management Architecture

```
SERVER MAIN THREAD:
┌────────────────────────────────────────────┐
│ while (true) {                             │
│     Socket client = serverSocket.accept() │  ← BLOCKING
│     new ClientHandler(client).start()      │  ← Create thread
│ }                                          │
└────────────────────────────────────────────┘

CLIENT HANDLER THREADS (One per client):
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ ClientHandler 1 │ │ ClientHandler 2 │ │ ClientHandler 3 │
│   (student1)    │ │   (teacher1)    │ │   (student2)    │
│                 │ │                 │ │                 │
│ while (true) {  │ │ while (true) {  │ │ while (true) {  │
│   read msg      │ │   read msg      │ │   read msg      │
│   process       │ │   process       │ │   process       │
│   send response │ │   send response │ │   send response │
│ }               │ │ }               │ │ }               │
└─────────────────┘ └─────────────────┘ └─────────────────┘
   ↕                   ↕                   ↕
[student1 socket]   [teacher1 socket]   [student2 socket]

FILE TRANSFER THREAD:
┌────────────────────────────────────────────┐
│ while (true) {                             │
│     Socket client = ftSocket.accept()      │
│     new Thread(() -> {                     │
│         handleFileTransfer(client)         │
│     }).start()                             │
│ }                                          │
└────────────────────────────────────────────┘

UDP ANNOUNCEMENT THREAD:
┌────────────────────────────────────────────┐
│ while (true) {                             │
│     receive UDP packet                     │
│     if (REGISTER) {                        │
│         add to client list                 │
│     } else if (ANNOUNCE) {                 │
│         broadcast to all                   │
│     }                                      │
│ }                                          │
└────────────────────────────────────────────┘
```

### 3. File Transfer Protocol

```
UPLOAD PROTOCOL:
═════════════
Client → Server: "UPLOAD"
Client → Server: username (String)
Client → Server: role (String)
Client → Server: filename (String)
Client → Server: filesize (long)
Client → Server: [file data bytes...]
Client ← Server: "SUCCESS" or "ERROR: reason"

DOWNLOAD PROTOCOL:
═════════════════
Client → Server: "DOWNLOAD"
Client → Server: username (String)
Client → Server: role (String)
Client → Server: filename (String)
Client ← Server: "SUCCESS" or "ERROR: reason"
Client ← Server: filesize (long)
Client ← Server: [file data bytes...]

Why use separate streams?
- DataInputStream/DataOutputStream for metadata (strings, longs)
- Raw byte arrays for file content (efficient, no encoding overhead)
```

### 4. UDP Registration Process

```
STUDENT STARTUP:
1. UDPAnnouncementListener.start()
2. Create DatagramSocket (OS assigns random port, e.g., 54321)
3. Send registration to server:
   ┌──────────────────────────────────────┐
   │ To: localhost:6000                   │
   │ Data: "REGISTER"                     │
   │ From: [client IP]:[random port]      │
   └──────────────────────────────────────┘

SERVER RECEIVES:
4. UDPAnnouncementServer receives packet
5. Extract sender address and port from packet
6. Add to registeredClients list:
   registeredClients.add(new InetSocketAddress(senderIP, senderPort))
7. Send confirmation back to student:
   ┌──────────────────────────────────────┐
   │ To: [client IP]:[random port]        │
   │ Data: "REGISTERED"                   │
   └──────────────────────────────────────┘

TEACHER BROADCASTS:
8. Teacher sends announcement
9. Server receives on port 6000
10. Server loops through all registered clients:
    for (InetSocketAddress student : registeredClients) {
        send announcement to student.address:student.port
    }

11. All students receive immediately (UDP is fast!)
```

### 5. Concurrency & Thread Safety

```java
// PROBLEM: Multiple threads accessing same list
List<ClientHandler> clients = new ArrayList<>();

Thread 1: clients.add(newClient)      // Adding
Thread 2: for (ClientHandler c : clients) { ... }  // Iterating
❌ ConcurrentModificationException!

// SOLUTION: Use thread-safe collection
CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

Thread 1: clients.add(newClient)      // Safe - creates new copy
Thread 2: for (ClientHandler c : clients) { ... }  // Safe - reads old copy
✅ No exception! Multiple threads can safely access

How it works:
- Reads operate on current array (fast, no locking)
- Writes create a new copy of array (slower, but writes are rare)
- Perfect for our use case: many reads (broadcasting), few writes (connect/disconnect)
```

### 6. Observer Pattern Implementation

```java
// CLIENT SIDE:
public interface MessageListener {
    void onMessageReceived(Message message);
    void onConnectionLost(String reason);
}

// ChatClient maintains list of listeners
private List<MessageListener> messageListeners = new ArrayList<>();

// UI windows register themselves
public void addMessageListener(MessageListener listener) {
    messageListeners.add(listener);
}

// When message arrives, notify all listeners
private void notifyListeners(Message msg) {
    for (MessageListener listener : messageListeners) {
        listener.onMessageReceived(msg);
    }
}

// WHY THIS PATTERN?
- Decouples network layer (ChatClient) from UI layer (Windows)
- Multiple UI components can listen to same client
- Easy to add new features without modifying existing code
- Follows "Open/Closed Principle" - open for extension, closed for modification
```

---

## 🎯 Summary of Network Concepts

| Concept | Technology | Purpose | Port |
|---------|-----------|---------|------|
| **Reliable Messaging** | TCP Sockets | Login, chat messages | 5000 |
| **File Transfer** | TCP Sockets + Data Streams | Upload/download files | 5001 |
| **Fast Announcements** | UDP Datagrams | Teacher broadcasts | 6000 |
| **Concurrency** | Java Threads | Handle multiple clients | N/A |
| **Serialization** | ObjectOutputStream | Send Java objects over network | N/A |
| **Thread Safety** | CopyOnWriteArrayList | Prevent race conditions | N/A |
| **Design Pattern** | Observer | Decouple UI from network layer | N/A |

---

## 📖 Learning Outcomes

After studying this application, you should understand:

### 1. **Socket Programming**
   - How to create client and server sockets
   - TCP 3-way handshake process
   - Why output stream must be created before input stream

### 2. **Protocol Design**
   - Creating structured message formats
   - Separating metadata from data
   - Request-response patterns

### 3. **Multithreading**
   - Why threads are necessary for concurrent connections
   - One thread per client model
   - Background threads for listeners

### 4. **UDP vs TCP**
   - When to use reliable TCP (critical data)
   - When to use fast UDP (notifications)
   - Trade-offs between reliability and speed

### 5. **Software Architecture**
   - Separation of concerns (network, UI, business logic)
   - Observer pattern for event-driven programming
   - Thread-safe data structures

### 6. **File I/O over Network**
   - Chunked data transfer
   - Progress tracking
   - Handling large files without blocking

---

## 🚀 Next Steps

1. **Run the application** - See the concepts in action
2. **Add breakpoints** - Debug and watch the flow
3. **Monitor with Wireshark** - See actual network packets
4. **Experiment** - Modify protocols and see what breaks
5. **Extend** - Add new features using the same patterns

---

**This document provides a complete understanding of how EduNet uses network programming concepts to create a functional educational platform. Use it as a reference while coding or studying!** 🎓

