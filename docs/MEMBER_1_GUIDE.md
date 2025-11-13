# 👤 MEMBER 1: TCP Socket Fundamentals & Server Architecture

## 📋 Complete Viva Preparation Guide

---

## 🎯 Your Responsibility Summary

**Role:** Foundation & Server Setup Specialist  
**Focus:** TCP Socket Programming & Server Lifecycle  
**Files:** `ChatServer.java`, `Constants.java`, `Logger.java`  
**Lines of Code:** ~600 lines  
**Complexity:** ⭐⭐⭐ (Medium)

---

## 📚 PART 1: Network Concepts (Simple Explanation)

### 1.1 What is a Socket?

**Simple Analogy:**
```
Think of a socket like a telephone:
- Server creates a socket = Installing a phone line at office
- Client connects = Someone calling your office number
- Communication happens = Conversation over the phone
```

**Technical Definition:**
A socket is an endpoint for sending/receiving data across a network. It combines:
- **IP Address** - Like your house address (where to send data)
- **Port Number** - Like apartment number (which application)

**Example:**
```
192.168.1.100:5000
    ↑          ↑
IP Address   Port
(Computer)   (Application)
```

---

### 1.2 What is TCP?

**TCP = Transmission Control Protocol**

**Simple Analogy:**
```
TCP is like registered mail:
✅ Guaranteed delivery
✅ Items arrive in order
✅ You get confirmation receipt
✅ If lost, automatically resent
```

**TCP Characteristics:**
```
┌─────────────────────────────────────────┐
│ TCP Features                            │
├─────────────────────────────────────────┤
│ ✅ Connection-oriented                  │
│    (Must connect before sending)        │
│                                         │
│ ✅ Reliable                             │
│    (Guarantees delivery)                │
│                                         │
│ ✅ Ordered                              │
│    (Messages arrive in sequence)        │
│                                         │
│ ✅ Error-checked                        │
│    (Corrupted data detected & resent)   │
│                                         │
│ ❌ Slower than UDP                      │
│    (Due to reliability overhead)        │
└─────────────────────────────────────────┘
```

---

### 1.3 TCP 3-Way Handshake (Important for Viva!)

**What happens when client connects to server?**

```
CLIENT                          SERVER
  │                               │
  │───── 1. SYN ─────────────────>│  "Hello, can I connect?"
  │                               │
  │<──── 2. SYN-ACK ──────────────│  "Yes! Here's my info"
  │                               │
  │───── 3. ACK ─────────────────>│  "Great, let's talk!"
  │                               │
  │═══════ Connected ═════════════│  ✅ Connection established
  │                               │
```

**Step-by-Step Explanation:**

1. **SYN (Synchronize)**
   - Client says: "I want to connect"
   - Sends initial sequence number

2. **SYN-ACK (Synchronize-Acknowledge)**
   - Server says: "OK, I acknowledge your request"
   - Sends its own sequence number
   - Acknowledges client's number

3. **ACK (Acknowledge)**
   - Client says: "I got your acknowledgment"
   - Connection is now established!

**Why 3 steps?**
- Ensures both sides are ready to communicate
- Establishes sequence numbers for ordering
- Confirms bidirectional communication

---

### 1.4 ServerSocket vs Socket

```
┌─────────────────────────────────────────────────┐
│             SOCKET TYPES                        │
├─────────────────────────────────────────────────┤
│                                                 │
│  ServerSocket (Server-side)                     │
│  ════════════════════════                       │
│  • Passive socket (listens only)                │
│  • Waits for incoming connections               │
│  • Bound to specific port                       │
│  • Creates Socket for each client               │
│  • Like a receptionist                          │
│                                                 │
│  Socket (Client-side & Server per-client)       │
│  ════════════════════════════════              │
│  • Active socket (sends/receives)               │
│  • Represents one connection                    │
│  • Has input/output streams                     │
│  • Like a phone call                            │
│                                                 │
└─────────────────────────────────────────────────┘
```

**Visual Example:**
```
SERVER SIDE:
┌─────────────────────┐
│  ServerSocket       │  ← Listens on port 5000
│  (Port 5000)        │
└──────────┬──────────┘
           │
           │ accept() creates new Socket for each client
           │
           ├──> Socket 1 (communicates with Client 1)
           ├──> Socket 2 (communicates with Client 2)
           └──> Socket 3 (communicates with Client 3)
```

---

### 1.5 Port Numbers

**What is a Port?**
- A 16-bit number (0-65535)
- Identifies a specific application/service
- Like apartment numbers in a building

**Port Categories:**
```
0-1023     : Well-known ports (HTTP=80, HTTPS=443)
1024-49151 : Registered ports (applications)
49152-65535: Dynamic ports (temporary)
```

**Our Application Ports:**
```
Port 5000: Main chat server (TCP)
Port 5001: File transfer server (TCP)
Port 6000: UDP announcements (UDP)
```

**Why Multiple Ports?**
```
Imagine a restaurant:
Port 5000 = Main entrance (customers come in)
Port 5001 = Delivery entrance (food deliveries)
Port 6000 = Announcement speaker (everyone hears)

Each entrance serves different purpose!
```

---

## 🔧 PART 2: Your Implementation Details

### 2.1 ChatServer.java - Complete Walkthrough

#### Architecture Overview

```
┌─────────────────────────────────────────────────┐
│           ChatServer Architecture               │
└─────────────────────────────────────────────────┘

Main Thread (ChatServer.start())
    │
    ├─> Initialize ServerSocket (port 5000)
    │
    ├─> Start FileTransferHandler (port 5001)
    │
    ├─> Start UDPAnnouncementServer (port 6000)
    │
    └─> Accept Loop (infinite)
            │
            └─> For each client:
                   • accept() returns Socket
                   • Create ClientHandler
                   • Start thread
                   • Go back to accept()
```

#### Key Code Sections Explained

**Section 1: Server Initialization**

```java
public class ChatServer {
    // ServerSocket - The main listener
    private ServerSocket serverSocket;
    
    // Thread-safe list of all connected clients
    private CopyOnWriteArrayList<ClientHandler> clientHandlers;
    
    // Port number (from Constants)
    private final int port = Constants.TCP_SERVER_PORT; // 5000
    
    // Server state
    private boolean isRunning;
}
```

**Why CopyOnWriteArrayList?**
```
Problem: Multiple threads access client list
- Main thread: Adding new clients
- ClientHandler threads: Reading to broadcast messages
- Remove threads: Removing disconnected clients

Solution: CopyOnWriteArrayList
- Thread-safe without explicit locks
- Safe to iterate while modifying
- Reads are very fast (no locking)
- Writes create new copy (acceptable for rare operations)
```

---

**Section 2: Starting the Server**

```java
public void start() {
    try {
        // STEP 1: Initialize file system
        FileUtils.initializeDirectories();
        Logger.initialize();
        
        // STEP 2: Create ServerSocket
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        Logger.info("Server started on port " + port);
        
        // STEP 3: Start auxiliary services
        fileTransferHandler = new FileTransferHandler(this);
        fileTransferHandler.start(); // Separate thread
        
        udpServer = new UDPAnnouncementServer();
        udpServer.start(); // Separate thread
        
        // STEP 4: Enter accept loop
        acceptClients();
        
    } catch (IOException e) {
        Logger.error("Failed to start server", e);
    }
}
```

**Detailed Explanation:**

**Step 1: Initialize File System**
```java
FileUtils.initializeDirectories();
```
- Creates `data/` folder
- Creates `data/files/lectures/` folder
- Creates `data/files/assignments/` folder
- Creates `users.txt` if not exists
- Creates `chat_logs.txt` if not exists

**Why do this first?**
- Ensures file operations won't fail later
- Prevents runtime errors
- Sets up logging infrastructure

---

**Step 2: Create ServerSocket**
```java
serverSocket = new ServerSocket(port);
```

**What happens behind the scenes?**
1. Operating system reserves port 5000
2. Socket enters LISTENING state
3. OS queue created for pending connections
4. Other applications can't use this port now

**Can fail if:**
- Port already in use (another app using 5000)
- Insufficient permissions (ports < 1024 need admin)
- Network interface unavailable

---

**Step 3: Start Auxiliary Services**
```java
fileTransferHandler = new FileTransferHandler(this);
fileTransferHandler.start();
```

**Why separate thread?**
```
If file transfer ran in same thread:
accept() → Handle file → accept() → Handle file...
❌ Main server blocked during file transfers!

With separate thread:
Thread 1: accept() → accept() → accept() (always ready)
Thread 2: Handle file transfers (independent)
✅ Chat remains responsive during file transfers!
```

---

**Section 3: Accept Loop (Most Important!)**

```java
private void acceptClients() {
    while (isRunning) {
        try {
            // BLOCKING CALL: Waits here until client connects
            Socket clientSocket = serverSocket.accept();
            
            // When a client connects, this line executes
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            Logger.info("Client connected: " + clientIP);
            
            // Create handler for this client
            ClientHandler handler = new ClientHandler(clientSocket, this);
            
            // Add to list (thread-safe)
            clientHandlers.add(handler);
            
            // Start thread (runs in background)
            handler.start();
            
            // Loop back to accept() for next client
            
        } catch (IOException e) {
            if (isRunning) {
                Logger.error("Error accepting client", e);
            }
        }
    }
}
```

**Visual Flow:**
```
TIME: 0s
Server: accept() ⏳ (waiting...)

TIME: 5s
Client 1 connects!
Server: accept() returns Socket 1
Server: Create ClientHandler 1
Server: Start Thread 1
Server: accept() ⏳ (waiting again...)

TIME: 10s
Client 2 connects!
Server: accept() returns Socket 2
Server: Create ClientHandler 2
Server: Start Thread 2
Server: accept() ⏳ (waiting again...)

[Thread 1 and Thread 2 run independently in background]
```

**Key Point for Viva:**
```
Q: Why does the server handle multiple clients?
A: Because accept() creates a NEW socket for each client,
   and each socket is handled by a SEPARATE thread.
   The main thread immediately goes back to accept()
   and waits for the next client.
```

---

**Section 4: Message Broadcasting**

```java
public void broadcastMessage(Message message) {
    Logger.info("Broadcasting: " + message.getContent());
    
    // Iterate through all connected clients
    for (ClientHandler handler : clientHandlers) {
        try {
            handler.sendMessage(message);
        } catch (IOException e) {
            Logger.error("Failed to send to " + handler.getUsername());
        }
    }
}
```

**How it works:**
```
Teacher sends: "Class at 2 PM"
    ↓
Server receives message
    ↓
broadcastMessage() called
    ↓
┌───────────────────────┐
│ For each client:      │
│  - handler 1 (student1)│ ← Send
│  - handler 2 (student2)│ ← Send
│  - handler 3 (student3)│ ← Send
└───────────────────────┘
    ↓
All students receive the message!
```

---

**Section 5: Shutdown**

```java
public void shutdown() {
    try {
        isRunning = false;
        
        // 1. Notify all clients
        Message shutdownMsg = new Message(
            MessageType.SERVER_SHUTDOWN,
            "SERVER",
            "ALL",
            "Server is shutting down"
        );
        broadcastMessage(shutdownMsg);
        
        // 2. Close all client connections
        for (ClientHandler handler : clientHandlers) {
            handler.disconnect();
        }
        clientHandlers.clear();
        
        // 3. Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        
        // 4. Shutdown auxiliary services
        if (fileTransferHandler != null) {
            fileTransferHandler.stop();
        }
        if (udpServer != null) {
            udpServer.stop();
        }
        
        Logger.info("Server shutdown complete");
        
    } catch (IOException e) {
        Logger.error("Error during shutdown", e);
    }
}
```

**Graceful Shutdown Steps:**
1. **Notify clients** - Tell them server is closing
2. **Close connections** - Each client disconnected properly
3. **Close ServerSocket** - Stop accepting new connections
4. **Stop services** - File transfer, UDP servers
5. **Log** - Record shutdown event

**Why graceful shutdown matters:**
- Clients know why connection closed
- Data in transit is saved
- No "connection reset" errors
- Resources properly released

---

### 2.2 Constants.java - Configuration Management

```java
public class Constants {
    
    // ============ NETWORK PORTS ============
    
    public static final int TCP_SERVER_PORT = 5000;
    // Main chat server port
    // All clients connect here first for authentication
    // Always open, accepts unlimited connections
    
    public static final int TCP_FILE_PORT = 5001;
    // Dedicated file transfer port
    // Separate from chat to avoid blocking
    // Only opens when file transfer needed
    
    public static final int UDP_PORT = 6000;
    // UDP announcement broadcast port
    // Connectionless, fast, fire-and-forget
    // Used for teacher announcements
    
    public static final String SERVER_IP = "127.0.0.1";
    // Localhost for testing
    // Change to actual IP for network deployment
    // Example: "192.168.1.100" for LAN
    
    // ============ BUFFER SIZES ============
    
    public static final int FILE_BUFFER_SIZE = 65536;
    // 64KB chunks for file transfer
    // Balance between memory and efficiency
    // Larger = faster but more memory
    // Smaller = slower but less memory
    
    // ============ TIMEOUTS ============
    
    public static final int SOCKET_TIMEOUT = 300000;
    // 5 minutes (300,000 ms)
    // Client disconnected if no activity
    // Prevents dead connections occupying resources
    
    // ============ MESSAGE TYPES ============
    
    public enum MessageType {
        // Authentication
        LOGIN,              // Client → Server: Credentials
        LOGIN_SUCCESS,      // Server → Client: Welcome!
        LOGIN_FAILED,       // Server → Client: Invalid credentials
        
        // Chat
        CHAT_BROADCAST,     // Teacher → All students
        CHAT_PRIVATE,       // User → Specific user
        CHAT_TO_TEACHER,    // Student → All teachers
        
        // File operations
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        FILE_LIST,
        FILE_NOTIFICATION,
        
        // Admin operations
        ADMIN_USER_LIST_REQUEST,
        ADMIN_USER_LIST_RESPONSE,
        ADMIN_KICK_USER,
        
        // System
        SERVER_SHUTDOWN,
        KICKED
    }
    
    // ============ USER ROLES ============
    
    public enum UserRole {
        TEACHER,   // Can broadcast, upload lectures
        STUDENT,   // Can receive, upload assignments
        ADMIN      // Can monitor, kick users
    }
}
```

**Why use Constants class?**

```
WITHOUT Constants:
────────────────────
ChatServer.java:     ServerSocket ss = new ServerSocket(5000);
FileTransfer.java:   Socket s = new Socket("localhost", 5001);
UDPServer.java:      DatagramSocket ds = new DatagramSocket(6000);

Problem: Port hardcoded in 3 places!
If we want to change port, must edit 3 files!
❌ Error-prone, hard to maintain

WITH Constants:
───────────────
ChatServer.java:     ServerSocket ss = new ServerSocket(Constants.TCP_SERVER_PORT);
FileTransfer.java:   Socket s = new Socket(Constants.SERVER_IP, Constants.TCP_FILE_PORT);
UDPServer.java:      DatagramSocket ds = new DatagramSocket(Constants.UDP_PORT);

Change port in ONE place, affects everywhere!
✅ Easy maintenance, no errors
```

---

### 2.3 Logger.java - Thread-Safe Logging

```java
public class Logger {
    
    private static final String LOG_FILE = "data/server.log";
    private static PrintWriter logWriter;
    private static final ReentrantLock lock = new ReentrantLock();
    
    // Log levels
    public enum Level {
        DEBUG,    // Detailed info for debugging
        INFO,     // General information
        WARNING,  // Warning but not error
        ERROR     // Error occurred
    }
    
    // Initialize logger
    public static void initialize() {
        try {
            logWriter = new PrintWriter(
                new FileWriter(LOG_FILE, true),  // append mode
                true  // auto-flush
            );
        } catch (IOException e) {
            System.err.println("Failed to initialize logger");
        }
    }
    
    // Log with level
    private static void log(Level level, String message) {
        lock.lock();  // Acquire lock (thread-safe)
        try {
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String logEntry = String.format("[%s] [%s] %s",
                timestamp, level, message);
            
            // Write to file
            if (logWriter != null) {
                logWriter.println(logEntry);
            }
            
            // Write to console
            System.out.println(logEntry);
            
        } finally {
            lock.unlock();  // Always release lock
        }
    }
    
    // Convenience methods
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void error(String message) {
        log(Level.ERROR, message);
    }
    
    public static void debug(String message) {
        log(Level.DEBUG, message);
    }
}
```

**Why Thread-Safe Logging?**

```
SCENARIO: Multiple threads log simultaneously

Thread 1: Logger.info("Client A connected")
Thread 2: Logger.info("Client B connected")
Thread 3: Logger.error("File transfer failed")

WITHOUT Lock:
─────────────
[2025-11-[2025-11-11 1[2025-11-11 10:30:02] Client A conn10:30:03] ected
❌ Interleaved mess! Corrupted log file!

WITH Lock (ReentrantLock):
──────────────────────────
[2025-11-11 10:30:02] [INFO] Client A connected
[2025-11-11 10:30:03] [INFO] Client B connected
[2025-11-11 10:30:04] [ERROR] File transfer failed
✅ Clean, ordered, readable logs!
```

**How ReentrantLock Works:**

```java
lock.lock();      // Thread acquires lock
try {
    // Critical section - only ONE thread here at a time
    // Write to file safely
} finally {
    lock.unlock();  // Always release, even if error
}
```

**Why use try-finally?**
```
If error occurs in critical section:
- Without finally: Lock never released → DEADLOCK!
- With finally: Lock always released → Safe!
```

---

## 🔗 PART 3: Connections to Other Members

### 3.1 Connection to Member 2 (ClientHandler)

**Your Output → Member 2's Input**

```java
// YOUR CODE (ChatServer.java):
Socket clientSocket = serverSocket.accept();
ClientHandler handler = new ClientHandler(clientSocket, this);
handler.start();

// MEMBER 2'S CODE (ClientHandler.java):
public ClientHandler(Socket socket, ChatServer server) {
    this.socket = socket;  // ← Your socket passed here
    this.server = server;  // ← Your server reference passed here
}
```

**What you provide to Member 2:**
1. **Socket object** - The connected client
2. **Server reference** - To call broadcast methods
3. **Client list** - To add/remove clients

**Interface you must provide:**
```java
// Member 2 calls these methods:
public void addClient(ClientHandler handler);
public void removeClient(ClientHandler handler);
public void broadcastMessage(Message message);
public void sendToTeachers(Message message);
```

---

### 3.2 Connection to Member 4 (File Transfer)

**Integration Point:**

```java
// YOUR CODE (ChatServer.java):
fileTransferHandler = new FileTransferHandler(this);
fileTransferHandler.start();

// MEMBER 4'S CODE (FileTransferHandler.java):
public FileTransferHandler(ChatServer chatServer) {
    this.chatServer = chatServer;  // ← Your server reference
}

// When file uploaded, Member 4 calls:
chatServer.broadcastMessage(fileNotification);
```

**What you provide:**
- **Server reference** - To broadcast file notifications
- **Lifecycle management** - Start/stop file handler
- **Integration** - File transfer runs alongside chat

---

### 3.3 Connection to Member 5 (UDP Server)

**Integration Point:**

```java
// YOUR CODE (ChatServer.java):
udpServer = new UDPAnnouncementServer();
udpServer.start();

// On shutdown:
udpServer.stop();
```

**What you provide:**
- **Lifecycle management** - Start/stop UDP server
- **Coordination** - All servers start together
- **Shutdown** - All servers stop together

---

## 🎓 PART 4: Viva Questions & Answers

### Basic Questions

**Q1: What is a ServerSocket?**
```
A: ServerSocket is a special socket that LISTENS for incoming 
   client connections on a specific port. It's passive - it doesn't 
   send or receive data itself. Instead, when a client connects, 
   it creates a new Socket object for that client.
   
   Think of it as a receptionist who greets visitors and 
   directs them to the right person.
```

**Q2: What does accept() do?**
```
A: accept() is a blocking method that waits for a client to connect.
   When called:
   1. Thread pauses (blocks) until connection arrives
   2. When client connects, returns a NEW Socket
   3. This new Socket represents that specific client
   4. accept() can be called again for next client
   
   It's blocking because the server must wait for clients -
   it can't predict when they'll connect.
```

**Q3: Why do we use port 5000?**
```
A: Port 5000 is in the "registered ports" range (1024-49151),
   which is safe to use for applications without special permissions.
   
   We avoid:
   - 0-1023: Reserved for system services (need admin)
   - Common ports like 8080 (often in use)
   
   Port 5000 is commonly used for development servers.
```

**Q4: What is the difference between Socket and ServerSocket?**
```
A: 
ServerSocket (Server-side):
- Passive: Only listens
- Bound to one port
- Creates Socket objects
- Never sends/receives data itself
- Like a reception desk

Socket (Both sides):
- Active: Sends and receives data
- One per connection
- Has input/output streams
- Actually transfers data
- Like a phone conversation
```

---

### Intermediate Questions

**Q5: Explain the TCP 3-way handshake.**
```
A: It's how TCP establishes a reliable connection:

1. SYN: Client sends "I want to connect" with sequence number
2. SYN-ACK: Server responds "OK, I acknowledge" with its sequence number
3. ACK: Client confirms "I got your acknowledgment"

After these 3 steps, both sides have:
- Confirmed the other is ready
- Exchanged sequence numbers for ordering packets
- Established a reliable bidirectional channel

It's like a phone call:
Client: "Hello?" (SYN)
Server: "Yes, I'm here!" (SYN-ACK)
Client: "Great, let's talk." (ACK)
```

**Q6: Why use multiple ports (5000, 5001, 6000)?**
```
A: Separation of concerns and performance:

Port 5000 (Chat):
- Always open
- Handles frequent small messages
- Low latency required
- Persistent connections

Port 5001 (Files):
- Opens on-demand
- Handles large data transfers
- Can take time without blocking chat
- Temporary connections

Port 6000 (UDP):
- Connectionless broadcasts
- Different protocol (UDP vs TCP)
- One-to-many communication
- Fire-and-forget delivery

Like a restaurant with separate entrances for 
customers, deliveries, and announcements.
```

**Q7: Why is CopyOnWriteArrayList used for client list?**
```
A: Thread safety without explicit locking!

Problem:
- Main thread adds clients: clients.add()
- Handler threads iterate: for (client : clients)
- If add() happens during iteration → ConcurrentModificationException

Solution:
CopyOnWriteArrayList creates a NEW array copy on every write:
- Reads use current array (no locking, fast)
- Writes create new array (slow, but rare)
- Iterators never fail (they read old snapshot)

Perfect for our use case:
- Many reads (broadcasting to all clients)
- Few writes (connect/disconnect is rare)
```

---

### Advanced Questions

**Q8: What happens if the server crashes?**
```
A: Depends on the crash:

1. Graceful shutdown (our code):
   - broadcastMessage(SERVER_SHUTDOWN)
   - Clients notified
   - Connections closed properly
   - Resources released
   - Logs written

2. Sudden crash (power loss, kill -9):
   - No notification sent
   - Client connections timeout
   - OS releases ports eventually
   - Clients detect via IOException
   - Must reconnect manually

3. Network failure:
   - TCP detects via keepalive
   - Both sides timeout
   - Automatic retry mechanisms fail
   - Application-level reconnection needed
```

**Q9: How does the server handle 100 simultaneous clients?**
```
A: Through multithreading:

1. accept() runs in main thread (fast, just accepts)
2. Each client gets own ClientHandler thread
3. 100 clients = 1 main thread + 100 handler threads
4. Each thread independent:
   - Own stack (local variables)
   - Own instruction pointer
   - Scheduled by OS

Resources:
- Each thread: ~1 MB stack memory
- Each socket: ~64 KB OS buffers
- 100 clients ≈ 100 MB + 6.4 MB = ~106 MB

Modern computers can handle thousands of threads,
but thread-per-client model limits scalability.
For 10,000+ clients, would need NIO (non-blocking I/O).
```

**Q10: Explain blocking I/O in accept().**
```
A: Blocking means the thread PAUSES until data/event arrives.

accept() blocks because:
1. Cannot predict when clients will connect
2. Thread enters WAITING state (OS scheduler)
3. OS wakes thread when connection arrives
4. Thread resumes, accept() returns Socket

Alternatives:
- Non-blocking I/O (Java NIO): accept() returns null immediately
- Async I/O: Callback when client connects

We use blocking because:
✅ Simple to understand
✅ Main thread does nothing else
✅ Good for < 10,000 connections
✅ Clear code flow
```

---

## 📊 PART 5: Testing Your Components

### Test Case 1: Server Startup

```java
public class TestServerStartup {
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
        
        // Expected output:
        // [INFO] Server started on port 5000
        // [INFO] File Transfer Server started on port 5001
        // [INFO] UDP Announcement Server started on port 6000
        // [INFO] Waiting for client connections...
    }
}
```

**Verify:**
- ✅ No errors thrown
- ✅ All 3 services started
- ✅ Ports not already in use
- ✅ Logs created in `data/server.log`

---

### Test Case 2: Multiple Client Connections

```java
// Simulate 3 clients connecting
for (int i = 1; i <= 3; i++) {
    Socket client = new Socket("localhost", 5000);
    System.out.println("Client " + i + " connected");
    Thread.sleep(1000);
}

// Expected server logs:
// [INFO] Client connected: 127.0.0.1
// [INFO] Client connected: 127.0.0.1
// [INFO] Client connected: 127.0.0.1
```

---

### Test Case 3: Graceful Shutdown

```java
// Connect clients
// ...

// Shutdown server
server.shutdown();

// Expected:
// [INFO] Broadcasting shutdown message
// [INFO] Closing 3 client connections
// [INFO] Server socket closed
// [INFO] File Transfer Server stopped
// [INFO] UDP Server stopped
// [INFO] Server shutdown complete
```

---

## 📈 PART 6: Performance Considerations

### Resource Usage

```
PER CLIENT:
───────────
Socket:          ~64 KB OS buffers
Thread:          ~1 MB stack
Handler object:  ~1 KB heap

100 Clients:
Socket buffers:  6.4 MB
Threads:         100 MB
Objects:         0.1 MB
Total:           ~106 MB

MAX CLIENTS:
────────────
Theoretical: 65,535 (port limit is unrelated)
Practical:   ~10,000 (thread limit)
Our setting:  50 (safe, controlled)
```

### Bottlenecks

```
1. accept() Loop
   - Single-threaded
   - Can accept ~10,000 connections/second
   - Not a bottleneck for our case

2. Thread Creation
   - Creating thread takes ~1 ms
   - With 50 clients, negligible
   - For 10,000+ clients, use thread pools

3. Network Bandwidth
   - 100 Mbps LAN = 12.5 MB/s
   - Chat messages < 1 KB
   - Can handle thousands of messages/second
   - File transfer limited by bandwidth
```

---

## 🎯 PART 7: Key Takeaways for Viva

### Must Know

1. **ServerSocket creates listening socket on specific port**
2. **accept() blocks until client connects, returns new Socket**
3. **Each client handled by separate thread**
4. **TCP guarantees reliable, ordered delivery**
5. **3-way handshake establishes TCP connection**
6. **Multiple ports for different services (5000, 5001, 6000)**
7. **CopyOnWriteArrayList for thread-safe client list**
8. **Graceful shutdown notifies clients before closing**

### Common Mistakes to Avoid

```
❌ "ServerSocket sends and receives data"
   → NO! ServerSocket only LISTENS and ACCEPTS.
   → The returned Socket handles data transfer.

❌ "accept() is non-blocking"
   → NO! It BLOCKS until a client connects.
   → This is WHY it must run in dedicated thread.

❌ "We can only have one client per port"
   → NO! Port identifies SERVICE, not connection.
   → One ServerSocket on port 5000 can handle 
     thousands of clients, each with unique Socket.

❌ "Thread-per-client scales to millions"
   → NO! Thread-per-client good for < 10,000.
   → Beyond that, use NIO (non-blocking I/O).
```

---

## 📚 PART 8: Additional Study Resources

### Key Java Classes to Study

1. **ServerSocket**
   - `ServerSocket(int port)`
   - `Socket accept()`
   - `void close()`
   - `boolean isClosed()`

2. **InetAddress**
   - `String getHostAddress()`
   - `String getHostName()`
   - `InetAddress getLocalHost()`

3. **Thread**
   - `void start()`
   - `void run()`
   - Thread states: NEW, RUNNABLE, WAITING, TERMINATED

4. **Collections**
   - `CopyOnWriteArrayList`
   - Thread-safe collections
   - Concurrent modifications

---

## ✅ Pre-Viva Checklist

- [ ] Can explain TCP 3-way handshake with diagram
- [ ] Can explain difference between ServerSocket and Socket
- [ ] Understand blocking vs non-blocking I/O
- [ ] Know why we use multiple ports
- [ ] Can explain thread-per-client model
- [ ] Understand CopyOnWriteArrayList
- [ ] Can trace accept() loop flow
- [ ] Know graceful shutdown process
- [ ] Can explain integration with other members
- [ ] Have tested all components

---

**Good luck with your viva! You've got this! 🚀**

Remember: 
- Speak clearly and confidently
- Use diagrams when explaining
- Relate to real-world analogies
- Admit if you don't know something
- Connect concepts to your actual code
