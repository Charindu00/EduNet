# 🎓 EduNet Group Project - Team Division (5 Members)

## 📋 **Work Distribution by Network Programming Concepts**

This document divides the EduNet project into 5 equal parts, each focusing on different network programming concepts. Each member gets both implementation and documentation responsibilities.

---

## 👤 **MEMBER 1: TCP Socket Fundamentals & Server Architecture**

### **Core Responsibility:** Foundation & Server Setup

### **Files to Implement:**
```
1. src/server/ChatServer.java
   - ServerSocket creation and binding
   - Accept client connections loop
   - Server lifecycle management
   - Graceful shutdown handling

2. src/utils/Constants.java
   - Port definitions (5000, 5001, 6000)
   - Buffer sizes
   - Timeout configurations
   - Message type enums

3. src/utils/Logger.java
   - Thread-safe logging with ReentrantLock
   - File I/O for log persistence
   - Multiple log levels
```

### **Network Concepts Covered:**
- ✅ **ServerSocket** - Listening for connections
- ✅ **Port Binding** - Binding to specific ports
- ✅ **Connection Acceptance** - accept() blocking call
- ✅ **Socket Lifecycle** - Opening, using, closing sockets
- ✅ **Multi-port Architecture** - Running multiple services

### **Key Code Sections:**
```java
// ServerSocket creation
ServerSocket serverSocket = new ServerSocket(5000);

// Accept loop
while (isRunning) {
    Socket clientSocket = serverSocket.accept();
    // Handle client...
}

// Resource cleanup
serverSocket.close();
```

### **Documentation Tasks:**
- Explain TCP 3-way handshake
- Document ServerSocket API
- Describe port binding process
- Explain blocking I/O in accept()

### **Lines of Code:** ~600 lines
### **Complexity:** ⭐⭐⭐ (Medium)

---

## 👤 **MEMBER 2: Multithreading & Concurrent Client Handling**

### **Core Responsibility:** Per-Client Thread Management

### **Files to Implement:**
```
1. src/server/ClientHandler.java
   - Per-client thread creation
   - Socket I/O streams (ObjectInputStream/ObjectOutputStream)
   - Message reading loop
   - Client disconnection handling
   - User authentication

2. src/utils/User.java
   - User model
   - File-based storage format
   - Credential management

3. src/utils/FileUtils.java
   - loadUsers() from file
   - authenticateUser()
   - Directory initialization
```

### **Network Concepts Covered:**
- ✅ **Multithreading** - One thread per client
- ✅ **Thread Safety** - CopyOnWriteArrayList usage
- ✅ **Socket Streams** - InputStream/OutputStream
- ✅ **Object Serialization** - Send/receive Message objects
- ✅ **Blocking I/O** - readObject() blocks until data arrives

### **Key Code Sections:**
```java
// Create thread for each client
ClientHandler handler = new ClientHandler(clientSocket, server);
Thread thread = new Thread(handler);
thread.start();

// Read messages in loop
while (running) {
    Message msg = (Message) ois.readObject();
    processMessage(msg);
}

// Thread-safe client list
CopyOnWriteArrayList<ClientHandler> clients;
```

### **Documentation Tasks:**
- Explain thread-per-client model
- Document thread safety issues
- Describe blocking I/O behavior
- Explain serialization process

### **Lines of Code:** ~800 lines
### **Complexity:** ⭐⭐⭐⭐ (High)

---

## 👤 **MEMBER 3: Message Routing & TCP Communication**

### **Core Responsibility:** Message Protocol & Routing Logic

### **Files to Implement:**
```
1. src/utils/Message.java
   - Message structure (type, sender, recipient, content)
   - Serializable implementation
   - Factory methods
   - Message formatting

2. src/client/ChatClient.java
   - Socket connection to server
   - Send/receive messages
   - MessageListener pattern (Observer)
   - Background message reader thread
   - Connection management

3. Message routing in ChatServer:
   - broadcastMessage()
   - sendPrivateMessage()
   - routeMessage() logic
```

### **Network Concepts Covered:**
- ✅ **Message Protocol Design** - Structure and format
- ✅ **Client Socket** - Connecting to server
- ✅ **Bidirectional Communication** - Send and receive
- ✅ **Observer Pattern** - MessageListener callbacks
- ✅ **Message Routing** - Broadcast vs unicast

### **Key Code Sections:**
```java
// Message structure
public class Message implements Serializable {
    private MessageType type;
    private String sender;
    private String recipient;
    private String content;
}

// Send message
oos.writeObject(message);
oos.flush();

// Receive message
Message msg = (Message) ois.readObject();

// Broadcast to all
for (ClientHandler client : clients) {
    client.sendMessage(message);
}
```

### **Documentation Tasks:**
- Design message protocol specification
- Document message types and formats
- Explain broadcast vs unicast routing
- Describe observer pattern usage

### **Lines of Code:** ~900 lines
### **Complexity:** ⭐⭐⭐⭐ (High)

---

## 👤 **MEMBER 4: Binary File Transfer & Streaming**

### **Core Responsibility:** TCP File Transfer System

### **Files to Implement:**
```
1. src/server/FileTransferHandler.java
   - Separate ServerSocket on port 5001
   - Handle UPLOAD requests
   - Handle DOWNLOAD requests
   - Binary data streaming
   - File organization (lectures/assignments)

2. src/client/FileTransferClient.java
   - Upload files with progress tracking
   - Download files with progress tracking
   - ProgressListener callback interface
   - Chunked transfer (64KB buffers)

3. UI Integration:
   - TeacherWindow.handleFileUpload()
   - StudentWindow.handleFileDownload()
```

### **Network Concepts Covered:**
- ✅ **Binary Data Transfer** - Streaming bytes vs objects
- ✅ **Separate Port Architecture** - Dedicated file port
- ✅ **Buffered I/O** - FileInputStream/FileOutputStream
- ✅ **Progress Tracking** - Callback mechanism
- ✅ **Large Data Handling** - Chunked transfer

### **Key Code Sections:**
```java
// Send file metadata
dos.writeUTF("UPLOAD");
dos.writeUTF(username);
dos.writeLong(fileSize);

// Stream file in chunks
byte[] buffer = new byte[65536];  // 64KB
while ((bytesRead = fis.read(buffer)) != -1) {
    dos.write(buffer, 0, bytesRead);
    // Update progress...
}

// Receive file
long totalReceived = 0;
while (totalReceived < fileSize) {
    int bytesRead = dis.read(buffer);
    fos.write(buffer, 0, bytesRead);
    totalReceived += bytesRead;
}
```

### **Documentation Tasks:**
- Explain binary vs text transfer
- Document file transfer protocol
- Describe buffering strategies
- Explain progress tracking mechanism

### **Lines of Code:** ~950 lines
### **Complexity:** ⭐⭐⭐⭐⭐ (Very High)

---

## 👤 **MEMBER 5: UDP Communication & Real-Time Broadcasting**

### **Core Responsibility:** Connectionless UDP Protocol

### **Files to Implement:**
```
1. src/server/UDPAnnouncementServer.java
   - DatagramSocket on port 6000
   - Receive UDP packets
   - Maintain registered clients list
   - Broadcast to all registered students
   - Handle REGISTER/UNREGISTER/ANNOUNCEMENT

2. src/client/UDPAnnouncementListener.java
   - Client-side DatagramSocket
   - Register with server
   - Receive announcements
   - AnnouncementCallback interface
   - Background listener thread

3. UI Integration:
   - TeacherWindow.handleAnnouncement()
   - StudentWindow.startUDPListener()
```

### **Network Concepts Covered:**
- ✅ **UDP Protocol** - Connectionless communication
- ✅ **DatagramSocket** - Send/receive datagrams
- ✅ **DatagramPacket** - UDP packet structure
- ✅ **Fire-and-Forget** - No ACK, no retransmit
- ✅ **Broadcast Pattern** - One-to-many communication

### **Key Code Sections:**
```java
// Create UDP socket
DatagramSocket socket = new DatagramSocket(6000);

// Receive packet
byte[] buffer = new byte[65536];
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
socket.receive(packet);  // Blocks until packet arrives

// Extract data
String message = new String(packet.getData(), 0, packet.getLength());
InetAddress sender = packet.getAddress();

// Send packet
byte[] data = message.getBytes();
DatagramPacket packet = new DatagramPacket(
    data, data.length,
    address, port
);
socket.send(packet);

// Broadcast to all
for (InetSocketAddress client : registeredClients) {
    socket.send(createPacket(message, client));
}
```

### **Documentation Tasks:**
- Explain UDP vs TCP differences
- Document DatagramSocket API
- Describe connectionless communication
- Explain when to use UDP vs TCP

### **Lines of Code:** ~700 lines
### **Complexity:** ⭐⭐⭐⭐ (High)

---

## 📊 **Summary Table**

| Member | Primary Focus | Key Concepts | Files | LOC | Difficulty |
|--------|---------------|--------------|-------|-----|------------|
| **1** | TCP Server Foundation | ServerSocket, Port Binding, Accept Loop | 3 files | ~600 | ⭐⭐⭐ |
| **2** | Multithreading & Clients | Threads, Concurrency, Blocking I/O | 3 files | ~800 | ⭐⭐⭐⭐ |
| **3** | Message Protocol & Routing | Serialization, Observer Pattern, Routing | 3 files | ~900 | ⭐⭐⭐⭐ |
| **4** | Binary File Transfer | Binary Streams, Chunking, Progress | 3 files | ~950 | ⭐⭐⭐⭐⭐ |
| **5** | UDP Broadcasting | DatagramSocket, Connectionless, UDP | 3 files | ~700 | ⭐⭐⭐⭐ |

---

## 🎯 **Shared Responsibilities (All Members)**

### **Everyone Must:**
1. **Test their components thoroughly**
2. **Write JavaDoc comments** for all public methods
3. **Document their network concepts** in detail
4. **Create demo/test cases** for their features
5. **Coordinate with team** on interfaces and integration

### **Common Files (Collaborative):**
- `LoginWindow.java` - Entry point (Member 3 leads)
- `TeacherWindow.java` - UI (Members 4 & 5 integrate features)
- `StudentWindow.java` - UI (Members 4 & 5 integrate features)
- `AdminDashboard.java` - Admin UI (Member 1 or 2)

---

## 📝 **Documentation Requirements (Each Member)**

### **Must Include:**
1. **Concept Explanation** (2-3 pages)
   - What is the concept?
   - Why is it important?
   - How does it work?

2. **Code Walkthrough** (1-2 pages)
   - Key code sections explained
   - Step-by-step execution flow
   - Important design decisions

3. **Diagrams** (2-3 diagrams)
   - Architecture diagram
   - Sequence diagram
   - Data flow diagram

4. **Testing & Results** (1 page)
   - Test cases
   - Screenshots
   - Performance observations

5. **Challenges & Solutions** (1 page)
   - Problems encountered
   - How they were solved
   - Lessons learned

**Total per member: 7-10 pages of documentation**

---

## 🔄 **Integration Points**

### **Member 1 → Member 2:**
- `ChatServer` creates `ClientHandler` threads
- Server socket passed to handlers

### **Member 2 → Member 3:**
- `ClientHandler` uses `Message` for communication
- Message routing back to server

### **Member 3 → Member 4:**
- `ChatClient` provides connection infrastructure
- File notifications sent as `Message` objects

### **Member 4 → Member 5:**
- Both use separate ports (5001 vs 6000)
- File upload triggers announcement option

### **Member 5 → Member 1:**
- UDP server integrated into main `ChatServer`
- Shutdown coordination

---

## ⏱️ **Suggested Timeline**

### **Week 1: Individual Implementation**
- Each member implements their core components
- Write unit tests
- Document code with JavaDoc

### **Week 2: Integration**
- Integrate all components
- Test inter-component communication
- Fix bugs and issues

### **Week 3: Testing & Documentation**
- Comprehensive system testing
- Write detailed documentation
- Create presentation materials
- Prepare demo

---

## 🎓 **Learning Outcomes (Per Member)**

### **Member 1 - Server Architecture:**
- Master ServerSocket and port management
- Understand server lifecycle
- Learn resource management

### **Member 2 - Concurrency:**
- Master multithreading concepts
- Understand thread safety issues
- Learn concurrent data structures

### **Member 3 - Protocol Design:**
- Master message protocol design
- Understand serialization
- Learn routing algorithms

### **Member 4 - Binary I/O:**
- Master file streaming
- Understand binary vs text data
- Learn progress tracking

### **Member 5 - UDP Protocol:**
- Master connectionless communication
- Understand UDP vs TCP tradeoffs
- Learn broadcast patterns

---

## 🏆 **Grading Criteria Alignment**

Each member demonstrates:
- ✅ **TCP Socket Programming** (Members 1-4)
- ✅ **UDP Programming** (Member 5)
- ✅ **Multithreading** (Member 2, integrated by all)
- ✅ **File I/O** (Member 4)
- ✅ **Protocol Design** (Member 3)
- ✅ **Client-Server Architecture** (All members)

---

## 📞 **Contact & Coordination**

### **Team Lead Responsibilities:**
1. **Member 1** - Overall coordination, server setup
2. **Member 3** - Protocol standards, message formats
3. **All** - Weekly sync meetings

### **Communication:**
- Daily updates on progress
- Immediate notification of blocking issues
- Code reviews before merging

---

## 🚀 **Success Tips**

1. **Start with interfaces** - Define contracts between components
2. **Test in isolation** - Each member tests their part independently
3. **Mock dependencies** - Use mock data until integration
4. **Document as you go** - Don't wait until the end
5. **Communicate often** - Daily standups recommended

---

**This division ensures:**
- ✅ Equal workload (~700-950 LOC per member)
- ✅ Distinct network concepts per member
- ✅ Clear ownership and responsibility
- ✅ Balanced difficulty levels
- ✅ Natural integration points
- ✅ Complete coverage of network programming topics

**Good luck with your project! 🎓✨**
