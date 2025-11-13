# 🎯 EduNet Demonstration Guide - Team Presentation Plan

**Complete Step-by-Step Guide for Demonstrating Each Member's Part**

---

## 📋 Table of Contents
1. [Pre-Demo Setup](#pre-demo-setup)
2. [Demonstration Flow](#demonstration-flow)
3. [Member 1: Server & TCP Socket Programming](#member-1-server--tcp-socket-programming)
4. [Member 2: Multithreading & Concurrent Clients](#member-2-multithreading--concurrent-clients)
5. [Member 3: Message Protocol & Serialization](#member-3-message-protocol--serialization)
6. [Member 4: File Transfer System](#member-4-file-transfer-system)
7. [Member 5: UDP Broadcasting](#member-5-udp-broadcasting)
8. [Closing & Q&A Tips](#closing--qa-tips)

---

## 🔧 Pre-Demo Setup (5 minutes before)

### **Hardware Setup:**
```
1. Main Screen: Server terminal (visible to audience)
2. Laptop/Side Screen: Multiple client windows
3. Have backup: Pre-recorded video (if network fails)
```

### **Software Checklist:**
- [ ] Server compiled and ready
- [ ] At least 3 client windows prepared
- [ ] Test files in `data/files/lectures/` and `data/files/assignments/`
- [ ] All team members know their credentials:
  - Teacher: `teacher1 / teacher123`
  - Student1: `student1 / student123`
  - Student2: `student2 / student123`
  - Admin: `admin1 / admin123`

### **File Preparation:**
```powershell
# Create demo files if not exist
echo "Java Programming Basics - Lecture 1" > data\files\lectures\demo-lecture.txt
echo "Assignment: Create a Chat Application" > data\files\assignments\demo-assignment.txt
```

### **Terminal Setup:**
```
Terminal 1: Server (visible to audience)
Terminal 2: Ready for commands (if needed)
Browser/Notepad: Open member guides for reference
```

---

## 🎬 Demonstration Flow (Total: 25-30 minutes)

### **Opening (2 minutes) - All Members**

**Speaker:** Any team member or designated leader

**Script:**
```
"Good morning/afternoon everyone. We are presenting EduNet, an educational 
communication platform that demonstrates network programming concepts including 
TCP/UDP protocols, multithreading, and file transfer systems.

Our team has 5 members, each responsible for a key component:
- Member 1: Server Architecture & TCP
- Member 2: Multithreading
- Member 3: Message Protocol
- Member 4: File Transfer
- Member 5: UDP Broadcasting

Let's begin with the demonstration."
```

---

## 👤 Member 1: Server & TCP Socket Programming (5 minutes)

### **Your Role:** Explain and demonstrate the server foundation

### **What to Say:**

```
"I'm responsible for the Server Architecture and TCP Socket Programming.
Let me start the server and explain what happens."
```

### **Live Demonstration:**

**Step 1: Start Server**
```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
java -cp bin server.ChatServer
```

**What to Point Out:**
```
✅ [Show terminal output]
"As you can see, three servers start:
1. Chat Server on port 5000 - TCP protocol for reliable messaging
2. File Transfer Server on port 5001 - Separate TCP connection for files
3. UDP Announcement Server on port 6000 - For broadcasting

This demonstrates the TCP socket lifecycle:
- Socket creation
- Binding to ports
- Listening for connections
- Accepting client connections"
```

**Step 2: Explain the Code (while server is running)**

```
"Let me explain the key components of ChatServer.java:

[Open ChatServer.java or show printed diagram]

1. ServerSocket Creation:
   - We create a ServerSocket on port 5000
   - This is the TCP server endpoint

2. Accept Loop:
   - Server waits in an infinite loop
   - When a client connects, accept() creates a new Socket
   - Each connection gets a unique socket

3. Thread-per-Client:
   - We spawn a ClientHandler thread for each connection
   - This enables multiple simultaneous clients
   - [This leads to Member 2's part]"
```

**Step 3: Connect One Client**

```powershell
# Open new terminal
java -cp bin client.ui.LoginWindow
# Login as teacher1
```

**What to Say:**
```
"Watch the server terminal. When I login..."
[Login as teacher1]

✅ "See the server log: 'New client connected' and 'teacher1 logged in'
✅ This shows the TCP handshake was successful
✅ The server created a ClientHandler thread for this connection"
```

### **Key Points to Emphasize:**
- ✅ TCP ensures reliable, ordered delivery
- ✅ Three-way handshake (mention briefly)
- ✅ Port numbers and why we use different ports
- ✅ ServerSocket vs Socket distinction

### **Common Questions You Might Get:**
**Q: Why use port 5000?**
A: "Port 5000 is in the user/registered range (1024-49151). Ports below 1024 require admin privileges."

**Q: What's the difference between TCP and UDP?**
A: "TCP is connection-oriented and reliable - like a phone call. UDP is connectionless and faster - like sending postcards. I'll demonstrate both."

---

## 👥 Member 2: Multithreading & Concurrent Clients (5 minutes)

### **Your Role:** Demonstrate concurrent client handling

### **What to Say:**

```
"My part is Multithreading - enabling multiple clients to connect simultaneously.
Without threads, the server could only handle one client at a time."
```

### **Live Demonstration:**

**Step 1: Connect Multiple Clients**

```
"Let me connect 3 clients simultaneously to show concurrent handling."
```

```powershell
# Terminal 2: Student 1
java -cp bin client.ui.LoginWindow
# Login as student1

# Terminal 3: Student 2  
java -cp bin client.ui.LoginWindow
# Login as student2

# Terminal 4: Admin
java -cp bin client.ui.LoginWindow
# Login as admin1
```

**What to Point Out:**
```
✅ [Show server terminal]
"Notice the server handles all 4 connections simultaneously:
- teacher1 (TEACHER)
- student1 (STUDENT)
- student2 (STUDENT)
- admin1 (ADMIN)

Each has their own ClientHandler thread running in parallel."
```

**Step 2: Demonstrate Concurrent Messaging**

```
"Now watch - all clients can send messages at the same time."

[In teacher window]: "Hello students!"
[In student1 window]: "Hello teacher!"
[In student2 window]: "Hi everyone!"

✅ "All messages are processed simultaneously without blocking.
✅ This is the power of multithreading."
```

**Step 3: Explain Thread Safety**

```
"Let me explain how we handle thread safety:

[Open or reference ClientHandler.java]

1. CopyOnWriteArrayList:
   - Thread-safe list for storing connected clients
   - Multiple threads can read/write safely

2. Synchronized Blocks:
   - When broadcasting messages, we synchronize access
   - Prevents race conditions

3. Thread Lifecycle:
   - Each ClientHandler runs independently
   - When client disconnects, thread terminates gracefully
   - Resources are cleaned up properly"
```

**Step 4: Demonstrate Thread Independence**

```
[Close student2 window]

✅ "See? Student2 disconnected, but the other clients continue working.
✅ Their threads are independent - one failure doesn't affect others."
```

### **Key Points to Emphasize:**
- ✅ Thread-per-client model vs alternatives
- ✅ Thread safety mechanisms (synchronized, CopyOnWriteArrayList)
- ✅ Scalability (could handle 100+ clients)
- ✅ Resource management and thread cleanup

### **Common Questions:**
**Q: Why not use a thread pool?**
A: "Thread pools are more efficient for high loads. Our design prioritizes simplicity and clarity for educational purposes, but we could scale to thread pools for production."

**Q: What happens if threads conflict?**
A: "We use synchronized blocks and thread-safe collections to prevent conflicts. For example, when broadcasting, only one thread can modify the client list at a time."

---

## 📨 Member 3: Message Protocol & Serialization (5 minutes)

### **Your Role:** Explain how messages are structured and transmitted

### **What to Say:**

```
"I'm responsible for the Message Protocol - how data is structured, 
serialized, and transmitted between client and server."
```

### **Live Demonstration:**

**Step 1: Explain Message Structure**

```
[Show Message.java code or diagram]

"Our Message class has these components:
1. Type: LOGIN, CHAT, PRIVATE, FILE_NOTIFICATION, etc.
2. Sender: Username of who sent it
3. Recipient: For private messages, who receives it
4. Content: The actual message text
5. Timestamp: When it was sent

This is an object-oriented approach to network communication."
```

**Step 2: Demonstrate Different Message Types**

**A) LOGIN Message:**
```
"When I login, the client creates a LOGIN message..."

[Show login process]

✅ Server receives: Type=LOGIN, Content="teacher1:password:TEACHER"
✅ Server validates and sends response
✅ Uses ObjectInputStream/ObjectOutputStream for serialization
```

**B) CHAT Message (Broadcast):**
```
[In teacher window, type]: "This is a broadcast message"
[Click Broadcast to All]

"This creates a CHAT message:
- Type: CHAT
- Sender: teacher1
- Content: 'This is a broadcast message'
- Recipient: null (means broadcast to all)

✅ All clients receive this message
✅ Java serialization converts the object to bytes
✅ Bytes are sent over TCP socket
✅ Receiving end deserializes back to Message object"
```

**C) PRIVATE Message:**
```
[In teacher window]: "@student1 This is private"
[Send]

"This creates a PRIVATE_MESSAGE:
- Type: PRIVATE_MESSAGE  
- Sender: teacher1
- Recipient: student1
- Content: 'This is private'

✅ Server checks recipient
✅ Only student1 receives it
✅ Other clients don't see it"
```

**Step 3: Show Serialization in Action**

```
"Let me explain Java Object Serialization:

[Reference code or diagram]

Client Side:
1. Create Message object
2. ObjectOutputStream.writeObject(message)
3. Object → Bytes → Network

Server Side:
1. Network → Bytes
2. ObjectInputStream.readObject()
3. Bytes → Message object

This allows us to send complex objects, not just strings."
```

**Step 4: Demonstrate Message Listener Pattern**

```
"The client uses a listener pattern:

[Show client code structure]

When a message arrives:
1. MessageListener.onMessageReceived() is called
2. Message is processed based on type
3. UI is updated accordingly

This is the Observer design pattern in action."
```

### **Key Points to Emphasize:**
- ✅ Object-oriented messaging vs raw strings
- ✅ Java Serialization mechanism
- ✅ Type-safe communication
- ✅ Extensibility (easy to add new message types)

### **Common Questions:**
**Q: Why not use JSON or XML?**
A: "Java serialization is simpler for Java-to-Java communication. JSON would be better for cross-platform systems. We could easily switch by changing the serialization layer."

**Q: Is serialization secure?**
A: "Basic serialization isn't encrypted. In production, we'd add SSL/TLS encryption for security."

---

## 📁 Member 4: File Transfer System (6 minutes)

### **Your Role:** Demonstrate binary file transfer over network

### **What to Say:**

```
"I'm responsible for the File Transfer System - uploading and downloading 
files over the network using binary streams."
```

### **Live Demonstration:**

**Step 1: Explain File Transfer Architecture**

```
"File transfer uses a separate TCP connection on port 5001. Why?

1. Keeps file traffic separate from chat traffic
2. Large files won't block chat messages
3. Can implement different protocols for each

File transfer uses binary streams, not object serialization:
- DataInputStream/DataOutputStream for metadata
- FileInputStream/FileOutputStream for file content
- 64KB buffer for efficient chunked transfer"
```

**Step 2: Teacher Uploads a Lecture**

```
[In teacher window, click "Upload File"]
[Select demo-lecture.txt]

"Watch what happens:

✅ Client sends: 'UPLOAD_LECTURE|demo-lecture.txt|1024' 
   (command, filename, file size)

✅ Client reads file in 64KB chunks

✅ Each chunk is sent over socket

✅ Server receives chunks and writes to:
   data/files/lectures/demo-lecture.txt

✅ Server broadcasts notification to all students

[Show student windows - notification appears]

✅ Students receive: 'New lecture available: demo-lecture.txt'"
```

**Step 3: Student Downloads the Lecture**

```
[In student1 window, click "Download Files"]
[Enter "demo-lecture.txt"]

"Now the reverse process:

✅ Student sends: 'DOWNLOAD_LECTURE|demo-lecture.txt'

✅ Server checks if file exists in lectures directory

✅ Server sends: 'FILE_EXISTS|1024' (file size)

✅ Server reads file in chunks

✅ Student receives chunks and writes to local file

✅ File appears in project folder

[Show downloaded file in file explorer]

✅ Success! The file was transferred successfully."
```

**Step 4: Explain the Transfer Process**

```
[Show code or diagram]

"The transfer happens in chunks for efficiency:

Upload Process:
1. Open FileInputStream on client
2. Read 64KB into buffer
3. Write buffer to socket
4. Repeat until EOF
5. Server writes to FileOutputStream

Download Process:
1. Server opens FileInputStream  
2. Reads 64KB chunks
3. Sends over socket
4. Client writes to FileOutputStream
5. File reconstructed on client side

This handles files of any size without loading entire file into memory."
```

**Step 5: Demonstrate Assignment Upload**

```
[In teacher window, click "Upload File" again]
[Select demo-assignment.txt]

"Same process, but files go to different directories:
✅ Lectures → data/files/lectures/
✅ Assignments → data/files/assignments/

Server knows where to store based on message type."
```

### **Key Points to Emphasize:**
- ✅ Binary transfer vs text transfer
- ✅ Chunked reading (memory efficient)
- ✅ Separate TCP connection (port 5001)
- ✅ File integrity (no corruption)
- ✅ Real-time notifications to students

### **Common Questions:**
**Q: What if the file is huge (1GB)?**
A: "Our 64KB buffer ensures we don't load the entire file into memory. We could transfer a 1GB file using only 64KB of RAM at a time."

**Q: How do you ensure file isn't corrupted?**
A: "TCP guarantees in-order, error-free delivery. For additional safety, we could add MD5/SHA checksums to verify file integrity."

**Q: Can students upload files?**
A: "Currently only teachers can upload. But the architecture supports bidirectional transfer - we just restrict it by role for this demo."

---

## 📡 Member 5: UDP Broadcasting (5 minutes)

### **Your Role:** Demonstrate connectionless UDP protocol

### **What to Say:**

```
"I'm responsible for UDP Broadcasting - sending urgent announcements 
to all clients using the UDP protocol. This demonstrates the difference 
between TCP and UDP."
```

### **Step 1: Explain TCP vs UDP**

```
"Let me explain the key differences:

TCP (what we've been using):
✅ Connection-oriented (handshake required)
✅ Reliable (guaranteed delivery)
✅ Ordered (packets arrive in order)
✅ Slower (overhead of reliability)
✅ Good for: Chat, File transfer

UDP (what I'll demonstrate now):
✅ Connectionless (no handshake)
✅ Best-effort (may lose packets)
✅ Unordered (packets may arrive out of order)
✅ Faster (minimal overhead)
✅ Good for: Live broadcasts, urgent alerts, streaming

For urgent announcements, speed is more important than 100% reliability."
```

### **Step 2: Demonstrate UDP Announcement**

```
[Make sure at least 2 clients are connected]

[In teacher window, type in message field]:
"URGENT: Class cancelled tomorrow due to weather"

[Click "Announcement" button]

✅ "Watch all client screens..."

[POP-UP appears on ALL clients simultaneously]

"📢 Announcement from Teacher
URGENT: Class cancelled tomorrow due to weather"

✅ "Notice how fast it appeared! This is UDP in action.
✅ All clients received it simultaneously via broadcast
✅ No TCP handshake overhead
✅ Fire-and-forget delivery"
```

**Step 3: Explain UDP Architecture**

```
[Show diagram or code]

"UDP Announcement system has three parts:

1. UDPAnnouncementServer (port 6000):
   - Listens for UDP packets
   - Maintains list of registered clients
   - Broadcasts announcements to all

2. UDPAnnouncementListener (each client):
   - Binds to a random port
   - Registers with server: 'REGISTER|[port]'
   - Listens for broadcasts

3. DatagramSocket & DatagramPacket:
   - UDP uses datagrams, not streams
   - Each packet is independent
   - No connection state maintained"
```

**Step 4: Show UDP Code Flow**

```
"Let me walk through what happened:

Teacher clicks 'Announcement':
1. Client creates DatagramPacket with message
2. Sends to server UDP port (6000)
3. No waiting for ACK - immediately returns

Server receives packet:
4. Server parses announcement
5. Loops through registered clients
6. Sends DatagramPacket to each client's port
7. Fire-and-forget - doesn't wait for confirmation

Each client:
8. UDPAnnouncementListener receives packet
9. Shows pop-up dialog immediately
10. Also logs to chat area

Total time: Less than 100ms for all clients!"
```

**Step 5: Demonstrate Multiple Announcements**

```
[Send another announcement]
"Quiz tomorrow at 10 AM"

[All clients get pop-up instantly]

✅ "See? Instant delivery to all clients
✅ This is why UDP is used for broadcasts
✅ No TCP connection overhead
✅ One packet reaches everyone"
```

**Step 6: Explain Registration Process**

```
"You might ask: How does server know where to send?

When client logs in:
1. UDPAnnouncementListener starts
2. Binds to random available port (e.g., 54321)
3. Sends REGISTER packet to server with port number
4. Server stores: [username, IP address, port]
5. Now server can send broadcasts to that port

When client disconnects:
- Can send UNREGISTER (optional)
- Or server can timeout inactive clients"
```

### **Key Points to Emphasize:**
- ✅ TCP vs UDP fundamental differences
- ✅ Datagram-based communication
- ✅ Broadcast/multicast capabilities
- ✅ Trade-off: Speed vs Reliability
- ✅ Real-world use cases (gaming, streaming, alerts)

### **Common Questions:**
**Q: What if a client doesn't receive the announcement?**
A: "That's the trade-off with UDP - we accept some packet loss for speed. For critical messages, we'd use TCP. For urgent but non-critical announcements, UDP's speed is worth the small risk."

**Q: Can you use UDP for chat?**
A: "You could, but you'd lose message ordering and reliability. Some gaming chats use UDP for speed, but most applications use TCP for messages and UDP only for position updates."

**Q: Why register? Can't you just broadcast to all?**
A: "We could use broadcast IP (255.255.255.255), but that's limited to local network. Our registration system allows targeted multicasting and works across networks."

---

## 🎓 Closing & Q&A Tips (2 minutes)

### **Closing Statement (Any member):**

```
"Thank you for watching our demonstration. Let me summarize what we showed:

✅ Member 1: Server architecture with 3 TCP/UDP servers on different ports
✅ Member 2: Concurrent client handling using multithreading  
✅ Member 3: Object-oriented message protocol with serialization
✅ Member 4: Binary file transfer system with chunked streaming
✅ Member 5: UDP broadcasting for instant announcements

EduNet demonstrates key networking concepts:
- Socket programming (TCP & UDP)
- Multithreading and concurrency
- Protocol design
- Binary vs text transmission
- Real-time communication

We're ready for questions!"
```

---

## 🎯 Q&A Preparation

### **Questions for Member 1 (Server/TCP):**
**Q: Why use ServerSocket instead of regular Socket?**
A: "ServerSocket is the listener - it waits for connections. When a client connects, ServerSocket.accept() creates a regular Socket for that specific connection."

**Q: What happens if the server crashes?**
A: "All client connections are lost because TCP requires active connection. Clients would need to reconnect. In production, we'd implement auto-reconnect logic."

**Q: Can the server handle 1000 clients?**
A: "Theoretically yes, but we'd hit OS thread limits. For high scalability, we'd use non-blocking I/O (NIO) or a thread pool instead of thread-per-client."

---

### **Questions for Member 2 (Multithreading):**
**Q: Why not use asynchronous I/O instead of threads?**
A: "Async I/O (NIO) is more scalable but complex. For educational purposes and moderate client loads, threads are simpler to understand and implement."

**Q: How do you prevent deadlocks?**
A: "We avoid nested synchronized blocks and use thread-safe collections like CopyOnWriteArrayList. Our locking is simple - mainly for the client list."

**Q: What's the maximum number of threads?**
A: "Depends on OS and JVM settings. Windows typically supports 2000-4000 threads. We could test this by connection flooding, but for a classroom app, 50-100 clients is realistic."

---

### **Questions for Member 3 (Protocol):**
**Q: Why not use Protocol Buffers or other serialization?**
A: "Java serialization is built-in and simple for Java-to-Java communication. Protocol Buffers would be better for cross-language compatibility and efficiency."

**Q: How do you version your protocol?**
A: "Currently we don't have versioning. In production, we'd add a version field to the Message class and handle backward compatibility."

**Q: Is it secure?**
A: "No encryption currently - it's plaintext over TCP. For production, we'd use SSL/TLS to encrypt the socket connection."

---

### **Questions for Member 4 (File Transfer):**
**Q: Why not use FTP?**
A: "FTP is a separate protocol. We wanted everything integrated into our application. Plus, implementing our own shows understanding of file I/O and network programming."

**Q: Can you transfer images or videos?**
A: "Yes! Our binary transfer works for any file type. We just use .txt files for demo simplicity."

**Q: How do you handle duplicate filenames?**
A: "Currently overwrites. We could add timestamps to filenames or implement version control."

---

### **Questions for Member 5 (UDP):**
**Q: When would you use UDP over TCP?**
A: "When speed matters more than reliability: live video streaming, online gaming (position updates), VoIP, live sports scores, IoT sensor data."

**Q: Can UDP be made reliable?**
A: "Yes, by implementing acknowledgments and retransmission at application level. This is what QUIC protocol does. But then you lose UDP's simplicity."

**Q: Why not use multicast instead of individual sends?**
A: "IP multicast requires router support and is complex to configure. Our registration system is simpler and works on any network."

---

## 📊 Demo Success Checklist

### **Before Starting:**
- [ ] Server starts successfully (3 ports shown)
- [ ] Test login with one client
- [ ] Test files exist and are accessible
- [ ] All team members know their speaking parts

### **During Demo:**
- [ ] Member 1: Server starts, explain TCP, connect 1 client
- [ ] Member 2: Connect 3+ clients, show concurrent messaging
- [ ] Member 3: Demonstrate different message types
- [ ] Member 4: Upload and download files successfully
- [ ] Member 5: Send UDP announcement, show pop-ups

### **After Demo:**
- [ ] Thank the audience
- [ ] Open for questions
- [ ] Each member ready to answer about their part

---

## 💡 Pro Tips

### **Presentation Tips:**
1. **Practice the demo 3 times beforehand** - know the flow
2. **Keep server terminal visible** - shows backend activity
3. **Speak while clicking** - explain what you're doing
4. **Don't rush** - give audience time to see results
5. **Point to evidence** - "See here on the screen..."

### **Handling Issues:**
- **If server crashes:** Stay calm, restart it, explain it's live code
- **If GUI freezes:** Have backup terminals ready
- **If network fails:** Show pre-recorded video or explain with diagrams

### **Team Coordination:**
- **Smooth transitions:** "Now I'll hand over to [Name] to show multithreading"
- **Support each other:** If someone forgets, help them
- **One laptop:** Designate one person to control demo, others explain

---

## 🎬 Time Management

| Section | Time | Who |
|---------|------|-----|
| Opening | 2 min | Any member |
| Member 1 - Server/TCP | 5 min | Member 1 |
| Member 2 - Multithreading | 5 min | Member 2 |
| Member 3 - Protocol | 5 min | Member 3 |
| Member 4 - File Transfer | 6 min | Member 4 |
| Member 5 - UDP | 5 min | Member 5 |
| Closing | 2 min | Any member |
| **Total** | **30 min** | |
| Q&A | 10-15 min | All |

---

## 🎯 Success Criteria

**You'll know your demo was successful if:**
✅ All 5 members clearly explained their part
✅ Server handled multiple concurrent clients
✅ File transfer worked (upload & download)
✅ UDP announcement reached all clients
✅ Audience understood TCP vs UDP difference
✅ You confidently answered questions

---

## 📚 Final Checklist Day-of-Demo

**30 Minutes Before:**
- [ ] Laptop charged, backup power available
- [ ] Project folder copied to desktop (easy access)
- [ ] Compiled successfully (run `start-server.bat` test)
- [ ] Test files created
- [ ] All guides printed/accessible
- [ ] Team meeting - review transitions

**5 Minutes Before:**
- [ ] Close all unnecessary applications
- [ ] Server terminal ready (not started yet)
- [ ] 3-4 terminal windows ready for clients
- [ ] Volume up (if using speakers)
- [ ] Deep breath - you've got this!

**During Presentation:**
- [ ] Speak clearly, don't rush
- [ ] Show evidence on screen
- [ ] Explain "what" and "why"
- [ ] Make eye contact with audience
- [ ] Support your team members

---

## 🚀 Good Luck!

Remember:
- **You built a working network application** - that's impressive!
- **Everyone makes mistakes** - just recover gracefully
- **The code works** - you've tested it
- **You know your parts** - you've studied the guides
- **Work as a team** - support each other

**You've got this! 💪**

---

## 📞 Emergency Contacts During Demo

If something breaks:
1. **Stay calm** - audience expects live code might glitch
2. **Restart cleanly** - kill Java processes, restart server
3. **Have backup** - show diagrams/code if demo fails
4. **Explain what would happen** - walk through process verbally

**Most importantly: Show confidence in what you built!**
