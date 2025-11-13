# 🎓 EduNet - Educational Communication Platform

A Java-based network programming project demonstrating TCP/UDP communication, multithreading, and file I/O in an educational context.

---

## 📋 Project Overview

**EduNet** is a client-server application that enables teachers and students to communicate within a local classroom network. It demonstrates core networking concepts:

- ✅ **TCP Sockets** - Client-server chat communication
- ✅ **UDP Datagrams** - Broadcast announcements
- ✅ **Multithreading** - Concurrent client handling
- ✅ **File I/O** - File transfers and logging
- ✅ **Java Swing** - Simple GUI interface

---

## 🏗️ Architecture

```
Server (TCP Port 5000)
    ↓
ClientHandler Threads (one per client)
    ↓
Teachers ← → Server ← → Students
    ↓
Admin Dashboard (monitors all)
```

---

## 📁 Project Structure

```
EduNet/
├── src/
│   ├── utils/              # Shared utilities
│   │   ├── Constants.java  # Configuration & enums
│   │   ├── Message.java    # Communication protocol
│   │   ├── User.java       # User model
│   │   ├── Logger.java     # Logging system
│   │   └── FileUtils.java  # File operations
│   ├── server/             # Server-side logic
│   └── client/             # Client-side + UI
├── data/                   # Runtime data
│   ├── users.txt           # User credentials
│   ├── chat_logs.txt       # Activity logs
│   └── files/              # Uploaded files
└── docs/                   # Documentation
```

---

## 🚀 Getting Started

### Prerequisites
- Java JDK 8 or higher
- No external libraries required (pure Java!)

### Running the Application

1. **Start the Server:**
   ```bash
   cd src
   javac -d ../bin server/*.java utils/*.java
   java -cp ../bin server.ChatServer
   ```

2. **Start a Client:**
   ```bash
   java -cp ../bin client.ChatClient
   ```

3. **Login with default accounts:**
   - Teacher: `teacher1` / `teacher123`
   - Student: `student1` / `student123`
   - Admin: `admin1` / `admin123`

---

## 👥 Module Distribution

| Module | Owner | Key Concepts |
|--------|-------|--------------|
| 1. Server & Login | Member 1 | TCP, Multithreading |
| 2. Chat System | Member 2 | TCP Streams, Swing |
| 3. File Transfer | Member 3 | File I/O, TCP |
| 4. Announcements | Member 4 | UDP Broadcasting |
| 5. Admin Dashboard | Member 5 | Monitoring, Logs |

---

## 🎯 Features

### For Teachers 👨‍🏫
- Broadcast messages to all students
- Send private messages
- Upload lecture files
- Send UDP announcements

### For Students 👨‍🎓
- Chat with teacher
- Download lecture files
- Upload assignments
- Receive announcements

### For Admins 🔧
- Monitor all connections
- View chat logs
- See file transfers
- Disconnect users

---

## 🔧 Technical Details

### Network Configuration
- **TCP Server Port:** 5000
- **TCP File Port:** 5001
- **UDP Port:** 6000
- **Server IP:** 127.0.0.1 (localhost)

### Communication Protocol
Messages use the `Message` class with:
- Type (LOGIN, CHAT, FILE_TRANSFER, etc.)
- Sender / Recipient
- Content
- Timestamp

### Threading Model
- Server: Main thread + one ClientHandler thread per client
- Client: Main thread (UI) + reader thread (listen for messages)

---

## 📊 Implementation Status

- [x] Phase 1: Foundation (utils package) ✅
- [ ] Phase 2: Server & Login System
- [ ] Phase 3: Chat System
- [ ] Phase 4: File Transfer
- [ ] Phase 5: Announcements
- [ ] Phase 6: Admin Dashboard
- [ ] Phase 7: UI Polish
- [ ] Phase 8: Testing & Documentation

---

## 📝 Notes

- User credentials stored in `data/users.txt` (file-based, not database)
- All activity logged to `data/chat_logs.txt`
- Files stored in `data/files/lectures/` and `data/files/assignments/`

---

## 👨‍💻 Development Team

- Member 1: Server & Login System
- Member 2: Chat System
- Member 3: File Distribution
- Member 4: Announcement System
- Member 5: Admin Dashboard

---

## 📄 License

Educational project for university coursework.

---

**Built with ❤️ for learning Network Programming in Java**
