# 👤 MEMBER 2: Multithreading & Concurrent Client Handling

## 📋 Complete Viva Preparation Guide

---

## 🎯 Your Responsibility Summary

**Role:** Concurrency & Per-Client Communication Specialist  
**Focus:** Multithreading, Socket Streams, Authentication  
**Files:** `ClientHandler.java`, `User.java`, `FileUtils.java`  
**Lines of Code:** ~800 lines  
**Complexity:** ⭐⭐⭐⭐ (High)

---

## 📚 PART 1: Network Concepts (Simple Explanation)

### 1.1 What is Multithreading?

**Simple Analogy:**
```
SINGLE-THREADED (One waiter in restaurant):
Customer 1 orders → Waiter serves → Customer 1 finishes
Customer 2 orders → Waiter serves → Customer 2 finishes
❌ Everyone waits in line!

MULTI-THREADED (Multiple waiters):
Customer 1 orders → Waiter 1 serves ┐
Customer 2 orders → Waiter 2 serves ├─ All served simultaneously!
Customer 3 orders → Waiter 3 serves ┘
✅ No waiting!
```

**Technical Definition:**
```
Thread = Independent execution path in a program
- Each thread has its own:
  ✅ Stack (local variables)
  ✅ Program counter (current instruction)
  ✅ Registers
  
- Threads share:
  ⚠️ Heap (objects, fields)
  ⚠️ Static variables
  ⚠️ Open files/sockets
```

**Why threads are ESSENTIAL for servers:**
```
WITHOUT THREADS:
────────────────
Server: Accept client 1
Server: Handle all client 1 requests (blocking!)
Server: Client 1 disconnects
Server: Accept client 2
...
❌ Only 1 client at a time!

WITH THREADS:
─────────────
Main Thread:    Accept client 1 → Create Thread 1 → Accept client 2 → Create Thread 2 → ...
Thread 1:       Handle client 1 independently
Thread 2:       Handle client 2 independently
Thread 3:       Handle client 3 independently
✅ Multiple clients simultaneously!
```

---

### 1.2 Socket Streams (Input/Output)

**What are streams?**
```
Stream = Flow of data (like water in pipe)

OutputStream (Writing):
  Your Program → OutputStream → Network → Other Program
  
InputStream (Reading):
  Other Program → Network → InputStream → Your Program
```

**Types of Streams in Our App:**

```
┌─────────────────────────────────────────────────┐
│           STREAM TYPES                          │
├─────────────────────────────────────────────────┤
│                                                 │
│  ObjectOutputStream/ObjectInputStream           │
│  ═══════════════════════════════════           │
│  • Send/receive Java objects                    │
│  • Automatic serialization                      │
│  • Used for Message objects                     │
│  • High-level, easy to use                      │
│                                                 │
│  Example:                                       │
│  oos.writeObject(message);                      │
│  Message msg = (Message) ois.readObject();      │
│                                                 │
│────────────────────────────────────────────────│
│                                                 │
│  DataOutputStream/DataInputStream               │
│  ════════════════════════════════              │
│  • Send/receive primitive types                 │
│  • writeInt(), writeUTF(), writeLong()          │
│  • Used for file transfer metadata              │
│  • More control, slightly faster                │
│                                                 │
│  Example:                                       │
│  dos.writeUTF("filename.pdf");                  │
│  String name = dis.readUTF();                   │
│                                                 │
└─────────────────────────────────────────────────┘
```

**CRITICAL: Stream Order!**
```java
// ❌ WRONG ORDER - Will deadlock!
input = new ObjectInputStream(socket.getInputStream());
output = new ObjectOutputStream(socket.getOutputStream());

// WHY? ObjectInputStream waits for header that 
// ObjectOutputStream hasn't sent yet!

// ✅ CORRECT ORDER
output = new ObjectOutputStream(socket.getOutputStream());
output.flush();  // Send header immediately!
input = new ObjectInputStream(socket.getInputStream());

// Now ObjectInputStream receives the header and proceeds
```

---

### 1.3 Blocking I/O

**What does "blocking" mean?**

```
Blocking = Thread PAUSES until operation completes

Example: Reading from socket
────────────────────────────
Message msg = input.readObject();  // ⏸️ Thread stops here
System.out.println("Received!");    // Executes ONLY after readObject() returns

If no data arrives, thread WAITS FOREVER!
(or until timeout/connection closes)
```

**Visual Timeline:**
```
TIME: 0s    Thread calls readObject()
            ⏸️ Thread enters WAITING state
            
TIME: 1s    (still waiting... no data)
TIME: 2s    (still waiting... no data)
TIME: 3s    (still waiting... no data)
TIME: 4s    📨 Message arrives!
            ▶️ Thread WAKES UP
            readObject() returns the message
            Thread continues to next line
```

**Why this matters for servers:**
```
If we handle clients in main thread:

Main Thread:
    accept() → ⏸️ wait for client
    client connects → ▶️
    readObject() → ⏸️ wait for message
    (STUCK HERE if client is slow!)
    
Meanwhile: 10 other clients trying to connect... can't!

Solution: Each client in separate thread!

Main Thread:        accept() → create thread → accept() → create thread → ...
Client 1 Thread:    readObject() ⏸️ (only THIS thread blocked)
Client 2 Thread:    readObject() ⏸️ (only THIS thread blocked)
✅ Other clients can still connect!
```

---

### 1.4 Thread Safety & Race Conditions

**What is a race condition?**

```
Scenario: Two threads modify same variable

int count = 0;  // Shared variable

Thread 1:               Thread 2:
count = count + 1;      count = count + 1;

Expected result: count = 2
Actual result: count = 1 (sometimes!)

WHY?
────
Thread 1: Read count (0)     │ Thread 2: Read count (0)
Thread 1: Calculate 0+1=1    │ Thread 2: Calculate 0+1=1
Thread 1: Write count=1      │
                             │ Thread 2: Write count=1
Final: count = 1 ❌
```

**Our Solution: Thread-Safe Collections**

```java
// ❌ NOT thread-safe
List<ClientHandler> clients = new ArrayList<>();

// ✅ Thread-safe
List<ClientHandler> clients = new CopyOnWriteArrayList<>();
```

**How CopyOnWriteArrayList works:**
```
Reads (frequent):
  • Direct access to current array
  • No locking needed
  • Very fast ⚡

Writes (rare):
  • Create NEW array with modifications
  • Atomically replace old array
  • Slow but safe 🔒

Perfect for our case:
  - Broadcasting: Many reads
  - Connect/Disconnect: Few writes
```

---

### 1.5 Object Serialization

**What is serialization?**

```
Serialization = Converting object to bytes
Deserialization = Converting bytes back to object

Why needed?
──────────
Network transmits BYTES, not objects!

Java Object → Serialize → Byte Stream → Network → Deserialize → Java Object
```

**Example:**
```java
// Define serializable class
public class Message implements Serializable {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
}

// Send object
Message msg = new Message("user1", "Hello", LocalDateTime.now());
output.writeObject(msg);  // Automatically serialized!

// Receive object
Message received = (Message) input.readObject();  // Automatically deserialized!
System.out.println(received.getContent());  // "Hello"
```

**What gets serialized?**
```
✅ Primitive fields (int, long, double, etc.)
✅ String fields
✅ Serializable object fields
✅ Collections of serializable objects
❌ transient fields (explicitly marked to skip)
❌ static fields (belong to class, not instance)
```

---

## 🔧 PART 2: Your Implementation Details

### 2.1 ClientHandler.java - Complete Walkthrough

#### Architecture Overview

```
┌─────────────────────────────────────────────────┐
│        ClientHandler Lifecycle                  │
└─────────────────────────────────────────────────┘

Server accepts connection
    ↓
new ClientHandler(socket, server)
    ↓
handler.start()  ← Starts new thread
    ↓
    ┌─────────────────────────────────┐
    │ Thread Execution (run method)   │
    ├─────────────────────────────────┤
    │ 1. Setup streams                │
    │ 2. Authenticate user             │
    │ 3. If success:                   │
    │    - Add to server's client list│
    │    - Enter message loop          │
    │    - Process messages            │
    │ 4. If disconnect:                │
    │    - Remove from list            │
    │    - Close resources             │
    │    - Log disconnection           │
    └─────────────────────────────────┘
```

---

#### Key Code Sections Explained

**Section 1: Class Fields**

```java
public class ClientHandler extends Thread {
    
    // ========== CONNECTION ==========
    private Socket socket;              // This client's connection
    private ChatServer server;          // Reference to main server
    
    // ========== STREAMS ==========
    private ObjectOutputStream output;  // Send messages TO client
    private ObjectInputStream input;    // Receive messages FROM client
    
    // ========== STATE ==========
    private User user;                  // User info (after login)
    private boolean authenticated;      // Has user logged in?
    private boolean running;            // Is handler still active?
    
    // Constructor
    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.authenticated = false;
        this.running = true;
        this.user = null;
    }
}
```

**Why extend Thread?**
```
Option 1: Extend Thread
public class ClientHandler extends Thread {
    public void run() { ... }
}
Usage: handler.start()

Option 2: Implement Runnable
public class ClientHandler implements Runnable {
    public void run() { ... }
}
Usage: new Thread(handler).start()

We chose Option 1 because:
✅ Simpler syntax
✅ Direct thread control
✅ Can override Thread methods
```

---

**Section 2: Thread Execution (run method)**

```java
@Override
public void run() {
    try {
        // STEP 1: Setup communication streams
        setupStreams();
        
        // STEP 2: Authenticate user
        if (!authenticate()) {
            Logger.error("Authentication failed for " + socket.getInetAddress());
            return;  // Exit thread
        }
        
        // STEP 3: Handle client messages
        handleClient();
        
    } catch (IOException e) {
        Logger.error("ClientHandler error: " + e.getMessage());
    } finally {
        // STEP 4: Always cleanup
        cleanup();
    }
}
```

**Execution Flow:**
```
start() called by server
    ↓
JVM creates new thread
    ↓
JVM calls run() in new thread
    ↓
setupStreams() → authenticate() → handleClient()
    ↓
If error or disconnect → cleanup()
    ↓
Thread terminates
```

---

**Section 3: Stream Setup**

```java
private void setupStreams() throws IOException {
    // CRITICAL: Output FIRST!
    output = new ObjectOutputStream(socket.getOutputStream());
    output.flush();  // Force header to be sent
    
    // Then input
    input = new ObjectInputStream(socket.getInputStream());
    
    Logger.debug("Streams initialized for " + socket.getInetAddress());
}
```

**Why this order?**
```
ObjectOutputStream constructor writes a HEADER:
┌──────────────────────────┐
│ Magic Number: 0xACED     │
│ Version: 5               │
│ Stream data...           │
└──────────────────────────┘

ObjectInputStream constructor EXPECTS this header:
┌──────────────────────────┐
│ Read magic number        │
│ Read version             │
│ Initialize...            │
└──────────────────────────┘

If we create InputStream first:
Client creates OutputStream → Sends header
Server creates InputStream → Waits for header
But server hasn't created OutputStream yet!
Client's InputStream blocks waiting for server's header!
Server's InputStream blocks waiting for client's header!
❌ DEADLOCK!

Correct order:
Server creates OutputStream → Sends header
Client creates OutputStream → Sends header
Server creates InputStream → Receives header ✅
Client creates InputStream → Receives header ✅
Both sides proceed successfully!
```

---

**Section 4: Authentication**

```java
private boolean authenticate() throws IOException {
    try {
        // STEP 1: Wait for login message
        Message loginMsg = (Message) input.readObject();
        
        // STEP 2: Verify it's a login message
        if (loginMsg.getType() != MessageType.LOGIN) {
            sendMessage(new Message(
                MessageType.LOGIN_FAILED,
                "SERVER",
                "",
                "Expected login message"
            ));
            return false;
        }
        
        // STEP 3: Parse credentials
        String credentials = loginMsg.getContent();
        String[] parts = credentials.split(":");
        
        if (parts.length != 3) {
            sendMessage(new Message(
                MessageType.LOGIN_FAILED,
                "SERVER",
                "",
                "Invalid credentials format"
            ));
            return false;
        }
        
        String username = parts[0];
        String password = parts[1];
        String roleStr = parts[2];
        
        // STEP 4: Validate against database (users.txt)
        UserRole role = UserRole.valueOf(roleStr);
        boolean valid = FileUtils.validateUser(username, password, role);
        
        if (!valid) {
            sendMessage(new Message(
                MessageType.LOGIN_FAILED,
                "SERVER",
                username,
                "Invalid username or password"
            ));
            Logger.error("Failed login attempt: " + username);
            return false;
        }
        
        // STEP 5: Success!
        this.user = new User(username, password, role);
        this.authenticated = true;
        
        // Add to server's active clients
        server.addClient(this);
        
        // Send success message
        sendMessage(new Message(
            MessageType.LOGIN_SUCCESS,
            "SERVER",
            username,
            "Welcome to EduNet!"
        ));
        
        Logger.info(username + " logged in as " + role);
        return true;
        
    } catch (ClassNotFoundException e) {
        Logger.error("Invalid message format during login");
        return false;
    }
}
```

**Authentication Flow:**
```
CLIENT                          SERVER (This code)
  │                               │
  │─── LOGIN Message ────────────>│
  │    (username:password:role)   │
  │                               │ Receive message
  │                               │ Parse credentials
  │                               │ Check users.txt
  │                               │
  │<── LOGIN_SUCCESS ─────────────│ If valid
  │    or                         │
  │<── LOGIN_FAILED ──────────────│ If invalid
  │                               │
```

**Security Note:**
```
Current implementation:
- Password sent in plain text ❌
- No encryption ❌
- Stored in plain text file ❌

Production improvement:
- Hash passwords (SHA-256) ✅
- Use SSL/TLS for encryption ✅
- Database storage ✅
- Salt + hash for storage ✅

For educational project, plain text acceptable.
For real application, NEVER do this!
```

---

**Section 5: Message Loop**

```java
private void handleClient() throws IOException {
    String username = user.getUsername();
    Logger.info("Handling messages for " + username);
    
    while (running && !socket.isClosed()) {
        try {
            // ⏸️ BLOCKING: Wait for message
            Message message = (Message) input.readObject();
            
            // ▶️ Message arrived! Process it
            Logger.debug("Received from " + username + ": " + message.getType());
            
            // Route message based on type
            processMessage(message);
            
        } catch (ClassNotFoundException e) {
            Logger.error("Invalid message format from " + username);
            break;
        } catch (EOFException e) {
            // Client disconnected gracefully
            Logger.info(username + " disconnected");
            break;
        } catch (SocketException e) {
            // Connection reset/closed
            Logger.info(username + " connection lost");
            break;
        }
    }
}
```

**Loop Visualization:**
```
TIME: 0s    while (running) → true
            readObject() → ⏸️ WAITING

TIME: 5s    Client sends message
            readObject() → ▶️ Returns message
            processMessage(msg)
            Loop back
            readObject() → ⏸️ WAITING

TIME: 10s   Client sends another message
            readObject() → ▶️ Returns message
            processMessage(msg)
            Loop back
            readObject() → ⏸️ WAITING

TIME: 15s   Client disconnects
            readObject() → Throws EOFException
            Catch block → break
            Loop exits
            handleClient() returns
            finally block → cleanup()
            Thread terminates
```

---

**Section 6: Message Processing**

```java
private void processMessage(Message message) throws IOException {
    MessageType type = message.getType();
    
    switch (type) {
        case CHAT_BROADCAST:
            // Teacher broadcasting to all students
            if (user.getRole() == UserRole.TEACHER) {
                server.broadcastMessage(message);
                Logger.info("Broadcast from " + user.getUsername());
            } else {
                sendMessage(new Message(
                    MessageType.ERROR,
                    "SERVER",
                    user.getUsername(),
                    "Only teachers can broadcast"
                ));
            }
            break;
            
        case CHAT_TO_TEACHER:
            // Student sending to all teachers
            if (user.getRole() == UserRole.STUDENT) {
                server.sendToTeachers(message);
                Logger.info("Student message from " + user.getUsername());
            }
            break;
            
        case CHAT_PRIVATE:
            // Private message to specific user
            String recipient = message.getReceiver();
            server.sendPrivateMessage(recipient, message);
            Logger.info("Private message: " + user.getUsername() + " → " + recipient);
            break;
            
        case FILE_NOTIFICATION:
            // Broadcast file availability
            server.broadcastMessage(message);
            break;
            
        case ADMIN_USER_LIST_REQUEST:
            // Admin requesting user list
            if (user.getRole() == UserRole.ADMIN) {
                String userList = server.getConnectedUsersList();
                sendMessage(new Message(
                    MessageType.ADMIN_USER_LIST_RESPONSE,
                    "SERVER",
                    user.getUsername(),
                    userList
                ));
            }
            break;
            
        case ADMIN_KICK_USER:
            // Admin kicking a user
            if (user.getRole() == UserRole.ADMIN) {
                String targetUser = message.getContent();
                server.kickUser(targetUser, user.getUsername());
            }
            break;
            
        default:
            Logger.warning("Unknown message type: " + type);
    }
}
```

**Role-Based Access Control:**
```
┌──────────────────────────────────────────────┐
│ Message Type         │ Allowed Roles         │
├──────────────────────────────────────────────┤
│ CHAT_BROADCAST       │ TEACHER only          │
│ CHAT_TO_TEACHER      │ STUDENT only          │
│ CHAT_PRIVATE         │ ALL                   │
│ FILE_NOTIFICATION    │ TEACHER, STUDENT      │
│ ADMIN_USER_LIST_REQ  │ ADMIN only            │
│ ADMIN_KICK_USER      │ ADMIN only            │
└──────────────────────────────────────────────┘

If unauthorized user tries restricted action:
→ Send ERROR message
→ Log security event
→ Don't process request
```

---

**Section 7: Sending Messages**

```java
public void sendMessage(Message message) throws IOException {
    synchronized (output) {  // Thread-safe!
        output.writeObject(message);
        output.flush();
    }
}
```

**Why synchronized?**
```
SCENARIO: Teacher broadcasts to 3 students
Server calls sendMessage() on each ClientHandler
These calls might happen FROM DIFFERENT THREADS!

Thread A: handler1.sendMessage(msg)  ┐
Thread B: handler2.sendMessage(msg)  ├─ Concurrent calls!
Thread C: handler3.sendMessage(msg)  ┘

If NOT synchronized:
Thread A: output.writeObject(msg)  ← Start writing
Thread B: output.writeObject(msg)  ← Interrupts! Data corrupted!
❌ Corrupted stream, both messages lost!

With synchronized:
Thread A: Acquires lock → writeObject → flush → Releases lock
Thread B: Waits... → Acquires lock → writeObject → flush → Releases lock
✅ Each message sent completely before next one starts!
```

**synchronized explained:**
```java
synchronized (output) {
    // Only ONE thread can be here at a time
    // Other threads wait in queue
    output.writeObject(message);
    output.flush();
    // Lock automatically released at end of block
}
```

---

**Section 8: Cleanup**

```java
private void cleanup() {
    try {
        running = false;
        
        // Remove from server's client list
        if (authenticated) {
            server.removeClient(this);
            Logger.info(user.getUsername() + " disconnected");
        }
        
        // Close streams
        if (output != null) {
            output.close();
        }
        if (input != null) {
            input.close();
        }
        
        // Close socket
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
    } catch (IOException e) {
        Logger.error("Error during cleanup", e);
    }
}
```

**Why cleanup is CRITICAL:**
```
Without cleanup:
- Socket remains open → Resource leak
- Streams consume memory → Memory leak
- Client remains in server list → Ghost users
- Thread never terminates → Thread leak

With cleanup:
✅ Socket closed → Port freed
✅ Streams closed → Buffers released
✅ Client removed → Accurate user list
✅ Thread terminates → Resources reclaimed

finally block ensures cleanup ALWAYS runs:
- Even if exception thrown
- Even if return statement executed
- Even if error occurred
```

---

### 2.2 User.java - User Model

```java
public class User implements Serializable {
    
    private String username;
    private String password;
    private UserRole role;
    private LocalDateTime lastLogin;
    
    // Constructor
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.lastLogin = LocalDateTime.now();
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    
    // Convert to file format (for storage)
    public String toFileFormat() {
        return username + ":" + password + ":" + role;
    }
    
    // Create from file format
    public static User fromFileFormat(String line) {
        String[] parts = line.split(":");
        return new User(parts[0], parts[1], UserRole.valueOf(parts[2]));
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
```

**Why Serializable?**
```
User objects might be:
1. Sent over network (future feature)
2. Stored in session
3. Passed between threads

Implementing Serializable allows these operations.
```

---

### 2.3 FileUtils.java - User Authentication

```java
public class FileUtils {
    
    private static final String USERS_FILE = Constants.USERS_FILE;
    
    /**
     * Validate user credentials
     */
    public static boolean validateUser(String username, String password, UserRole role) {
        try {
            List<User> users = loadUsers();
            
            for (User user : users) {
                if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password) &&
                    user.getRole() == role) {
                    return true;  // Valid credentials
                }
            }
            
            return false;  // Not found or invalid
            
        } catch (IOException e) {
            Logger.error("Error validating user", e);
            return false;
        }
    }
    
    /**
     * Load all users from file
     */
    public static List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        
        if (!file.exists()) {
            // Create default users
            createDefaultUsers();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    User user = User.fromFileFormat(line);
                    users.add(user);
                }
            }
        }
        
        return users;
    }
    
    /**
     * Create default users (first run)
     */
    private static void createDefaultUsers() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println("# EduNet Users");
            writer.println("# Format: username:password:role");
            writer.println();
            writer.println("teacher1:pass123:TEACHER");
            writer.println("student1:pass123:STUDENT");
            writer.println("student2:pass123:STUDENT");
            writer.println("admin1:admin123:ADMIN");
        }
    }
}
```

**File Format (users.txt):**
```
# EduNet Users
# Format: username:password:role

teacher1:pass123:TEACHER
student1:pass123:STUDENT
student2:pass123:STUDENT
admin1:admin123:ADMIN
```

**Authentication Process:**
```
1. User enters: "student1", "pass123", "STUDENT"
2. validateUser() called
3. loadUsers() reads file
4. Loop through users:
   - Check username matches: "student1" ✅
   - Check password matches: "pass123" ✅
   - Check role matches: STUDENT ✅
5. Return true (valid)
6. ClientHandler creates User object
7. Client authenticated!
```

---

## 🔗 PART 3: Connections to Other Members

### 3.1 Connection to Member 1 (ChatServer)

**Member 1's Output → Your Input:**

```java
// MEMBER 1's CODE (ChatServer.java):
Socket clientSocket = serverSocket.accept();
ClientHandler handler = new ClientHandler(clientSocket, this);
clientHandlers.add(handler);
handler.start();

// YOUR CODE (ClientHandler.java):
public ClientHandler(Socket socket, ChatServer server) {
    this.socket = socket;      // ← Socket from Member 1
    this.server = server;      // ← Server reference from Member 1
}
```

**What Member 1 provides:**
- ✅ Connected Socket object
- ✅ ChatServer reference for callbacks
- ✅ Client list (CopyOnWriteArrayList)

**What you provide back:**
```java
// Called from your code:
server.addClient(this);           // Add yourself to list
server.broadcastMessage(message); // Broadcast to all
server.removeClient(this);        // Remove on disconnect
```

---

### 3.2 Connection to Member 3 (Message)

**Your code heavily uses Member 3's Message class:**

```java
// MEMBER 3's CODE (Message.java):
public class Message implements Serializable {
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    // ... methods
}

// YOUR CODE (ClientHandler.java):
Message loginMsg = (Message) input.readObject();  // ← Receive
processMessage(loginMsg);                          // ← Process
sendMessage(responseMsg);                          // ← Send
```

**Dependency:**
```
Your ClientHandler DEPENDS ON Member 3's Message class
If Message class changes, your code must adapt
This is why clear interfaces are important!
```

---

### 3.3 Connection to Member 4 & 5

**File Transfer:**
```java
// When file uploaded, Member 4 creates notification:
Message notification = new Message(
    MessageType.FILE_NOTIFICATION,
    "SERVER",
    "ALL",
    "New lecture: Chapter5.pdf"
);

// Your code receives and broadcasts:
case FILE_NOTIFICATION:
    server.broadcastMessage(message);  // Send to all clients
    break;
```

**UDP Announcements:**
```java
// Member 5 handles UDP independently
// But your code might receive UDP-related messages
// for registration/coordination
```

---

## 🎓 PART 4: Viva Questions & Answers

### Basic Questions

**Q1: What is a thread?**
```
A: A thread is an independent execution path within a program.
   Think of it as a worker that can perform tasks independently.
   
   Multiple threads = Multiple workers
   Each can do different tasks simultaneously
   All share the same workspace (memory)
   
   In our server:
   - Main thread accepts connections
   - Each ClientHandler is a thread handling one client
   - All threads run concurrently
```

**Q2: Why does each client need a separate thread?**
```
A: Because readObject() is BLOCKING.
   
   If we handled all clients in one thread:
   readObject() for Client 1 blocks → Can't handle Client 2!
   
   With separate threads:
   Thread 1: readObject() for Client 1 (blocked)
   Thread 2: readObject() for Client 2 (blocked)
   Thread 3: readObject() for Client 3 (blocked)
   
   Each thread blocks independently!
   When ANY client sends data, that thread wakes up.
   Other threads continue waiting.
```

**Q3: What is ObjectInputStream/ObjectOutputStream?**
```
A: High-level streams for sending Java objects over network.
   
   ObjectOutputStream:
   - writeObject(msg) → Automatically converts object to bytes
   - Handles all serialization
   - Sends over network
   
   ObjectInputStream:
   - readObject() → Receives bytes from network
   - Automatically converts back to object
   - Returns original object
   
   Advantage: Don't manually convert objects to/from bytes!
```

**Q4: Why must we create OutputStream before InputStream?**
```
A: ObjectOutputStream constructor writes a HEADER.
   ObjectInputStream constructor EXPECTS to read this header.
   
   If both sides create InputStream first:
   Side A: InputStream waits for header from Side B
   Side B: InputStream waits for header from Side A
   → DEADLOCK! Both waiting forever!
   
   Correct order:
   Both sides create OutputStream first (sends header)
   Then both create InputStream (receives header)
   → Both proceed successfully!
```

---

### Intermediate Questions

**Q5: Explain the authentication flow.**
```
A: Step-by-step:

1. Client connects → Socket created
2. Server creates ClientHandler thread
3. ClientHandler calls authenticate()
4. Waits for LOGIN message from client
5. Receives message: "username:password:role"
6. Parses the credentials
7. Calls FileUtils.validateUser()
8. Checks against users.txt file
9. If valid:
   - Creates User object
   - Sets authenticated = true
   - Adds to server's client list
   - Sends LOGIN_SUCCESS
10. If invalid:
   - Sends LOGIN_FAILED
   - Closes connection
   - Thread terminates
```

**Q6: What is synchronized and why do we need it?**
```
A: synchronized ensures only ONE thread executes code block at a time.

Problem:
Multiple threads call sendMessage() on same ClientHandler
Both try to write to same OutputStream simultaneously
→ Data corruption!

Solution:
synchronized (output) {
    // Only one thread here at a time
    output.writeObject(message);
}

How it works:
Thread 1: Acquires lock → Sends message → Releases lock
Thread 2: Waits for lock → Acquires lock → Sends → Releases
Thread 3: Waits for lock → Acquires lock → Sends → Releases

Result: Messages sent sequentially, no corruption!
```

**Q7: What happens when a client disconnects?**
```
A: Several scenarios:

1. Graceful disconnect (client closes properly):
   - readObject() throws EOFException
   - Caught in handleClient()
   - Loop breaks
   - finally block executes cleanup()
   - Resources released

2. Network failure (cable unplugged):
   - readObject() eventually times out
   - Throws SocketException
   - Caught and handled same way

3. Unexpected crash:
   - TCP detects via keepalive packets
   - Eventually throws IOException
   - Cleanup still executes

In ALL cases:
- cleanup() method runs
- Socket closed
- Client removed from list
- Thread terminates
```

---

### Advanced Questions

**Q8: Explain race conditions with an example.**
```
A: Race condition = Multiple threads access shared data
   without proper synchronization.

Example in our context:

List<ClientHandler> clients = new ArrayList<>();

Thread A (Main):           Thread B (Broadcast):
clients.add(newClient);    for (Client c : clients) {
                               c.sendMessage(msg);
                           }

If add() happens during iteration:
→ ConcurrentModificationException!

Solution: CopyOnWriteArrayList
- Reads use current array snapshot
- Writes create new array copy
- No exception, always safe!

Why this works:
- Broadcasting (reads) is frequent → fast
- Connect/disconnect (writes) is rare → acceptable to be slow
```

**Q9: What is blocking I/O and what are its pros/cons?**
```
A: Blocking I/O = Thread pauses until operation completes

Pros:
✅ Simple to understand and code
✅ Natural flow: read → process → read
✅ No complex state machines
✅ Good for moderate client count (<10,000)

Cons:
❌ One thread per client (memory intensive)
❌ 10,000 clients = 10,000 threads!
❌ Context switching overhead
❌ Thread creation cost

Alternatives:
- Non-blocking I/O (NIO): One thread handles multiple clients
- Async I/O: Callbacks when data arrives
- Event-driven: Select/poll/epoll

We use blocking because:
- Educational project (clarity > efficiency)
- Expected client count < 100
- Simplifies error handling
```

**Q10: How do you prevent memory leaks?**
```
A: Memory leak = Resources not released, accumulate over time

Our prevention strategies:

1. Always close streams:
   try {
       output.writeObject(msg);
   } finally {
       output.close();  // Always executes!
   }

2. Remove disconnected clients:
   cleanup() {
       server.removeClient(this);  // Remove from list
   }

3. Close sockets:
   if (socket != null && !socket.isClosed()) {
       socket.close();
   }

4. Thread termination:
   When run() method returns, thread is garbage collected

5. Use try-with-resources:
   try (BufferedReader reader = new BufferedReader(...)) {
       // Automatically closed
   }

Signs of memory leak in our app:
- Client list grows but never shrinks
- Thread count keeps increasing
- Sockets in CLOSE_WAIT state
- Memory usage continuously rises
```

---

## 📊 PART 5: Testing Your Components

### Test Case 1: Thread Creation

```java
public class TestThreadCreation {
    public static void main(String[] args) throws Exception {
        // Create fake socket (for testing)
        ServerSocket server = new ServerSocket(5000);
        
        // Accept 3 clients
        for (int i = 1; i <= 3; i++) {
            Socket client = server.accept();
            ClientHandler handler = new ClientHandler(client, null);
            handler.start();
            System.out.println("Thread " + i + " created");
        }
        
        // Check: Should see 3 threads running
        Thread.getAllStackTraces().keySet().forEach(thread -> {
            if (thread instanceof ClientHandler) {
                System.out.println("ClientHandler thread: " + thread.getName());
            }
        });
    }
}
```

---

### Test Case 2: Authentication

```java
public class TestAuthentication {
    public static void main(String[] args) {
        // Test valid credentials
        boolean valid = FileUtils.validateUser("teacher1", "pass123", UserRole.TEACHER);
        System.out.println("Valid credentials: " + valid);  // Should be true
        
        // Test invalid username
        boolean invalid1 = FileUtils.validateUser("unknown", "pass123", UserRole.TEACHER);
        System.out.println("Invalid username: " + invalid1);  // Should be false
        
        // Test invalid password
        boolean invalid2 = FileUtils.validateUser("teacher1", "wrong", UserRole.TEACHER);
        System.out.println("Invalid password: " + invalid2);  // Should be false
        
        // Test wrong role
        boolean invalid3 = FileUtils.validateUser("teacher1", "pass123", UserRole.STUDENT);
        System.out.println("Wrong role: " + invalid3);  // Should be false
    }
}
```

---

### Test Case 3: Concurrent Message Sending

```java
public class TestConcurrentSending {
    public static void main(String[] args) throws Exception {
        // Create ClientHandler (with real connection)
        // ...
        
        // Simulate 10 threads sending messages simultaneously
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 1; i <= 10; i++) {
            final int num = i;
            executor.submit(() -> {
                try {
                    Message msg = new Message(
                        MessageType.CHAT_BROADCAST,
                        "test",
                        "ALL",
                        "Message " + num
                    );
                    handler.sendMessage(msg);
                    System.out.println("Sent message " + num);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown();
        
        // Expected: All 10 messages sent successfully, no corruption
    }
}
```

---

## 📈 PART 6: Performance Considerations

### Thread Overhead

```
Per Thread:
───────────
Stack:       ~1 MB (default JVM setting)
Meta data:   ~0.5 KB
OS overhead: ~0.5 KB

50 threads:  ~50 MB memory
100 threads: ~100 MB memory

Thread creation time: ~1 ms
Context switch: ~1-10 µs
```

### Scalability Limits

```
Thread-per-client model:
────────────────────────
Theoretical max: ~10,000 threads (depends on OS/JVM)
Practical max:   ~1,000 threads (performance degrades)
Our setting:     50 clients (safe, responsive)

Beyond 1,000 clients:
→ Use NIO (Non-blocking I/O)
→ Use async frameworks (Netty, Vert.x)
→ Use reactive streams
```

---

## 🎯 PART 7: Key Takeaways for Viva

### Must Know

1. **Thread = independent execution path**
2. **Each client = separate thread (concurrent handling)**
3. **ObjectInputStream/ObjectOutputStream for object serialization**
4. **Output stream MUST be created before input stream**
5. **readObject() blocks until data arrives**
6. **synchronized prevents concurrent access corruption**
7. **Race condition = unsynchronized shared data access**
8. **CopyOnWriteArrayList = thread-safe without locks**
9. **Authentication = validate credentials against users.txt**
10. **Cleanup in finally block ensures resource release**

### Common Mistakes to Avoid

```
❌ "We use threads to make code faster"
   → Partly true, but main reason is CONCURRENCY
   → Handle multiple clients SIMULTANEOUSLY

❌ "synchronized makes code faster"
   → NO! It makes code THREAD-SAFE (slower but correct)

❌ "Each thread has its own memory"
   → NO! Threads share HEAP, have separate STACKS

❌ "readObject() returns null if no data"
   → NO! It BLOCKS (waits) until data arrives
```

---

## ✅ Pre-Viva Checklist

- [ ] Can explain what a thread is and why we need them
- [ ] Understand blocking I/O and its implications
- [ ] Know the correct order for creating streams
- [ ] Can explain authentication flow step-by-step
- [ ] Understand race conditions and solutions
- [ ] Know why synchronized is needed
- [ ] Can explain message processing flow
- [ ] Understand cleanup and resource management
- [ ] Know integration points with other members
- [ ] Have tested all components thoroughly

---

**Good luck! Master concurrency and you've mastered the hardest part! 💪**
