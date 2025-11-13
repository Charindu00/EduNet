# 🖥️ EduNet GUI Testing Guide

**Complete Step-by-Step Guide to Run and Demonstrate EduNet Application**

---

## 📋 Table of Contents
1. [Pre-Demo Setup](#pre-demo-setup)
2. [Starting the Application](#starting-the-application)
3. [Testing Each Role](#testing-each-role)
4. [Feature Demonstration Scenarios](#feature-demonstration-scenarios)
5. [Troubleshooting](#troubleshooting)

---

## 🔧 Pre-Demo Setup

### Step 1: Verify Files are Compiled
Before running, make sure your `.class` files are in the `bin` folder.

**Option A: Quick Check**
```powershell
# Navigate to project folder
cd "C:\Users\user\OneDrive\Desktop\EduNet"

# Check if bin folder has compiled classes
dir bin\server
dir bin\client\ui
```

**Option B: Recompile if Needed**
```powershell
# Compile all Java files
javac -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
```

### Step 2: Prepare Test Files
Make sure you have test files in the data folders:

```powershell
# Check lecture files
dir data\files\lectures

# Check assignment files  
dir data\files\assignments
```

If empty, create test files:
```powershell
# Create a test lecture
echo "This is a sample lecture on Java Programming" > data\files\lectures\java-basics.txt

# Create a test assignment
echo "Assignment: Create a simple calculator program" > data\files\assignments\assignment1.txt
```

### Step 3: Verify User Accounts
Check `data\users.txt` has these accounts:
- **Teacher:** `teacher1` / `teacher123`
- **Student:** `student1` / `student123`
- **Admin:** `admin1` / `admin123`

---

## 🚀 Starting the Application

### Step 1: Start the Server (ALWAYS FIRST!)

**Method 1: Using Batch File (Easiest)**
```
1. Double-click: start-server.bat
2. Wait for message: "✅ Server started on port 5000"
3. You should also see:
   - "✅ File Transfer Server started on port 5001"
   - "✅ UDP Announcement Server started on port 6000"
```

**Method 2: Using PowerShell**
```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
java -cp bin server.ChatServer
```

**✅ Server is Ready When You See:**
```
========================================
       🌐 EduNet Server
========================================
✅ Chat Server started on port 5000
✅ File Transfer Server started on port 5001
✅ UDP Announcement Server started on port 6000
📡 Waiting for connections...
```

### Step 2: Start Client(s) (After Server is Running)

**Method 1: Using Batch File (Easiest)**
```
1. Double-click: start-client.bat
2. LoginWindow GUI will appear
```

**Method 2: Using PowerShell**
```powershell
# Open NEW PowerShell window
cd "C:\Users\user\OneDrive\Desktop\EduNet"
java -cp bin client.ui.LoginWindow
```

**💡 TIP: Open Multiple Clients**
- Run the batch file multiple times, OR
- Open multiple PowerShell windows and run the command in each
- This lets you test chat between users!

---

## 👥 Testing Each Role

### 🎓 TEACHER ROLE - Complete Testing Workflow

#### **Step 1: Login as Teacher**
```
Username: teacher1
Password: teacher123
Role: Select "Teacher" from dropdown
Click: Login
```

#### **Step 2: Teacher Window Features**

**A. Chat with Students (Broadcast)**
```
1. Type in message box: "Hello class! Today's topic is networking"
2. Click "Send" or press Enter
3. ✅ Message appears in your chat area
4. ✅ If students are online, they receive it
```

**B. Send Private Message**
```
1. Look at "Online Users" list (right side)
2. Select a student (e.g., student1)
3. Type: "@student1 Great work on your assignment!"
4. Click "Send"
5. ✅ Only that student receives the message
```

**C. Upload Lecture File**
```
1. Click "Upload Lecture" button
2. File chooser dialog opens
3. Select a text file from your computer
4. ✅ Success message appears
5. ✅ Server console shows: "📚 Lecture uploaded: filename.txt by teacher1"
6. ✅ ALL online students get notification: "📢 New lecture available!"
```

**D. Upload Assignment**
```
1. Click "Upload Assignment" button
2. Select a text file
3. ✅ Success message appears
4. ✅ Server shows: "📝 Assignment uploaded: filename.txt by teacher1"
5. ✅ Students get notification: "📢 New assignment posted!"
```

**E. Send UDP Announcement**
```
1. Type in message box: "IMPORTANT: Quiz tomorrow at 10 AM"
2. Click "UDP Broadcast" button
3. ✅ Pop-up confirms: "Announcement sent"
4. ✅ ALL clients (even offline-then-online) receive broadcast
```

**F. View Online Users**
```
1. Look at right panel "Online Users"
2. ✅ Shows: student1, student2, admin1 (whoever is online)
3. ✅ Updates in real-time when users join/leave
```

---

### 📚 STUDENT ROLE - Complete Testing Workflow

#### **Step 1: Login as Student**
```
Username: student1
Password: student123
Role: Select "Student" from dropdown
Click: Login
```

#### **Step 2: Student Window Features**

**A. Receive Teacher Messages**
```
1. ✅ When teacher sends broadcast, you see it immediately
2. ✅ When teacher sends you "@student1 message", you see it
3. Messages appear in chat area with timestamp
```

**B. Reply to Teacher**
```
1. Type: "Thank you teacher! I have a question about sockets"
2. Click "Send"
3. ✅ Message sent to server
4. ✅ Teacher receives it
```

**C. Chat with Other Students**
```
1. Type: "@student2 Hey! Want to study together?"
2. Click "Send"
3. ✅ Only student2 receives it (if online)
```

**D. Download Lecture Files**
```
1. Click "Download Lecture" button
2. Dialog asks: "Enter lecture filename:"
3. Type: "java-basics.txt" (or whatever teacher uploaded)
4. Click OK
5. ✅ File downloads to project root folder
6. ✅ Success message: "Lecture downloaded successfully!"
7. ✅ Check: file appears in your project folder
```

**E. Download Assignment Files**
```
1. Click "Download Assignment" button
2. Enter filename: "assignment1.txt"
3. ✅ File downloads
4. ✅ Success message appears
```

**F. Receive UDP Announcements**
```
1. ✅ Automatic! When teacher sends UDP broadcast
2. Pop-up appears: "📢 Announcement: IMPORTANT: Quiz tomorrow at 10 AM"
3. Click OK to dismiss
```

**G. View Online Users**
```
1. Right panel shows all online users
2. ✅ See: teacher1, student2, student3, admin1, etc.
```

---

### 🔧 ADMIN ROLE - Complete Testing Workflow

#### **Step 1: Login as Admin**
```
Username: admin1
Password: admin123
Role: Select "Admin" from dropdown
Click: Login
```

#### **Step 2: Admin Dashboard Features**

**A. View All Online Users**
```
1. ✅ Admin window shows complete user list
2. ✅ See counts: Teachers (1), Students (3), Admins (1)
3. ✅ Real-time updates when users join/leave
```

**B. Monitor All Messages**
```
1. ✅ Admin can see ALL messages in the system
2. ✅ Broadcasts from teachers
3. ✅ Private messages between users (monitoring capability)
4. ✅ System notifications
```

**C. Send Admin Announcements**
```
1. Type: "SYSTEM MAINTENANCE: Server restart at 11 PM"
2. Click "Send Admin Broadcast" (or similar button)
3. ✅ ALL users receive message
4. ✅ Message marked as [ADMIN]
```

**D. UDP Broadcast**
```
1. Type important announcement
2. Click "UDP Broadcast" button
3. ✅ Sent to ALL clients via UDP
```

**E. View System Statistics**
```
✅ Admin dashboard shows:
- Total online users
- Messages sent today
- Files uploaded/downloaded
- Server uptime
```

---

## 🎬 Feature Demonstration Scenarios

### Scenario 1: Teacher Uploads and Student Downloads (FILE TRANSFER)

**Setup:** 
- 1 Server running
- 1 Teacher client (teacher1)
- 1 Student client (student1)

**Steps:**
```
1. TEACHER: Login as teacher1
2. STUDENT: Login as student1
3. TEACHER: Click "Upload Lecture"
4. TEACHER: Select file "networking-basics.txt"
5. ✅ STUDENT sees pop-up: "📢 New lecture available: networking-basics.txt"
6. STUDENT: Click "Download Lecture"
7. STUDENT: Enter "networking-basics.txt"
8. ✅ STUDENT: File appears in project folder
9. ✅ SERVER console shows: "📚 Lecture uploaded" and "📤 student1 downloaded"
```

**What This Demonstrates:**
- TCP file transfer (port 5001)
- Binary file streaming
- Server-client communication
- Real-time notifications

---

### Scenario 2: Group Chat Communication (TCP CHAT)

**Setup:**
- 1 Server running
- 1 Teacher client (teacher1)
- 2 Student clients (student1, student2)

**Steps:**
```
1. ALL: Login with respective credentials
2. TEACHER: Type "Hello everyone! Welcome to EduNet"
3. ✅ BOTH STUDENTS: See message instantly in chat area
4. STUDENT1: Type "Hello teacher!"
5. ✅ TEACHER and STUDENT2: See student1's message
6. STUDENT2: Type "Hi teacher and student1!"
7. ✅ ALL: See student2's message
8. CHECK: Online Users list updates on all clients
```

**What This Demonstrates:**
- TCP socket communication (port 5000)
- Broadcast messaging
- Multi-client handling
- Real-time synchronization

---

### Scenario 3: Private Messaging (TARGETED TCP)

**Setup:**
- 1 Server running
- 1 Teacher (teacher1)
- 2 Students (student1, student2)

**Steps:**
```
1. TEACHER: Type "@student1 Excellent work on your project!"
2. ✅ STUDENT1: Sees private message
3. ✅ STUDENT2: Does NOT see the message
4. STUDENT1: Type "@teacher1 Thank you so much!"
5. ✅ TEACHER: Sees private reply
6. ✅ STUDENT2: Does NOT see this exchange
```

**What This Demonstrates:**
- Message routing
- Client identification
- Targeted communication
- Server message filtering

---

### Scenario 4: UDP Announcements (UDP BROADCAST)

**Setup:**
- 1 Server running
- Multiple clients (teacher, students)

**Steps:**
```
1. ALL CLIENTS: Login and wait on main window
2. TEACHER: Type "URGENT: Class cancelled tomorrow"
3. TEACHER: Click "UDP Broadcast" button
4. ✅ ALL CLIENTS: Instant pop-up appears (even if minimized)
5. ✅ Message delivered via UDP (different from TCP chat)
6. ADMIN: Can also send UDP broadcasts for system announcements
```

**What This Demonstrates:**
- UDP datagram transmission (port 6000)
- Fire-and-forget protocol
- Broadcast to all network clients
- UDP vs TCP differences

---

### Scenario 5: Multi-User File Operations (CONCURRENT FILE TRANSFER)

**Setup:**
- 1 Server running
- 1 Teacher (teacher1)
- 3 Students (student1, student2, student3)

**Steps:**
```
1. TEACHER: Upload lecture "chapter1.txt"
2. ✅ ALL STUDENTS: Get notification
3. STUDENT1: Start downloading "chapter1.txt"
4. STUDENT2: Simultaneously download "chapter1.txt"
5. STUDENT3: Simultaneously download "chapter1.txt"
6. ✅ ALL: Download completes successfully
7. ✅ SERVER: Handles all 3 downloads concurrently (multithreading)
8. TEACHER: Upload another file "chapter2.txt" WHILE downloads happening
9. ✅ All operations complete without blocking
```

**What This Demonstrates:**
- Multithreading (multiple ClientHandlers)
- Concurrent file transfers
- Thread-safe file access
- Non-blocking I/O operations

---

### Scenario 6: Admin Monitoring (ADMIN DASHBOARD)

**Setup:**
- 1 Server running
- 1 Admin (admin1)
- 1 Teacher (teacher1)
- 2 Students (student1, student2)

**Steps:**
```
1. ADMIN: Login and observe dashboard
2. TEACHER: Send broadcast: "Today's topic is TCP/IP"
3. ✅ ADMIN: Sees message in monitoring panel
4. STUDENT1: Send private "@student2 Let's team up"
5. ✅ ADMIN: Can see private message (monitoring)
6. TEACHER: Upload file
7. ✅ ADMIN: Dashboard shows file upload notification
8. ✅ ADMIN: View statistics updates in real-time
```

**What This Demonstrates:**
- Admin privileges
- System monitoring
- Message interception (for admin)
- Real-time statistics

---

## 🎯 Demo Presentation Checklist

### Before Demo:
- [ ] Server compiled and ready (`bin/server/` has .class files)
- [ ] Client compiled and ready (`bin/client/` has .class files)
- [ ] Test files in `data/files/lectures/` and `data/files/assignments/`
- [ ] User credentials confirmed in `data/users.txt`
- [ ] Multiple PowerShell windows ready (or multiple computers)

### During Demo - Show These Key Features:

**1. Network Concepts (5 mins)**
- [ ] Show server starting (3 ports: 5000, 5001, 6000)
- [ ] Explain TCP vs UDP
- [ ] Show server console logging connections

**2. TCP Chat (3 mins)**
- [ ] Login multiple users
- [ ] Send broadcast messages
- [ ] Send private messages
- [ ] Show real-time delivery

**3. File Transfer (5 mins)**
- [ ] Teacher uploads lecture
- [ ] Student receives notification
- [ ] Student downloads file
- [ ] Show file appears in folder
- [ ] Demonstrate concurrent downloads

**4. UDP Announcements (2 mins)**
- [ ] Send UDP broadcast
- [ ] Show instant pop-ups on all clients
- [ ] Explain fire-and-forget protocol

**5. Admin Features (3 mins)**
- [ ] Show admin dashboard
- [ ] Monitor online users
- [ ] View system messages
- [ ] Send admin announcements

**6. Multithreading (2 mins)**
- [ ] Show multiple clients connected simultaneously
- [ ] Demonstrate non-blocking operations
- [ ] Explain thread-per-client model

---

## 🐛 Troubleshooting

### Problem: "Address already in use" Error
**Solution:**
```powershell
# Kill Java processes
taskkill /F /IM java.exe
# Wait 5 seconds
# Restart server
```

### Problem: Client Can't Connect
**Checklist:**
1. Is server running? (Check for "✅ Server started" message)
2. Are you using correct IP? (localhost or 127.0.0.1)
3. Is firewall blocking Java?
4. Try restarting both server and client

### Problem: File Download Fails
**Solution:**
1. Check file exists in `data/files/lectures/` or `data/files/assignments/`
2. Use exact filename (case-sensitive)
3. Check server console for error messages
4. Ensure file transfer server started (port 5001)

### Problem: UDP Announcements Not Received
**Solution:**
1. Check UDP server started (port 6000)
2. Verify UDPAnnouncementListener is running on clients
3. Check firewall allows UDP on port 6000
4. Try restarting clients

### Problem: GUI Not Appearing
**Solution:**
```powershell
# Verify Java GUI support
java -version
# Should show Java 8 or higher

# Recompile GUI classes
javac -d bin -sourcepath src src\client\ui\*.java

# Try running directly
java -cp bin client.ui.LoginWindow
```

### Problem: "ClassNotFoundException"
**Solution:**
```powershell
# Recompile everything
cd "C:\Users\user\OneDrive\Desktop\EduNet"
javac -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java

# Verify .class files exist
dir bin\server
dir bin\client
dir bin\client\ui
dir bin\utils
```

---

## 🎓 Quick Start Command Reference

### Start Server:
```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
java -cp bin server.ChatServer
```

### Start Client:
```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
java -cp bin client.ui.LoginWindow
```

### Recompile All:
```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
javac -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
```

### Kill All Java:
```powershell
taskkill /F /IM java.exe
```

---

## 📸 Demo Screenshots to Capture

1. **Server Console** - Showing all 3 servers started
2. **Login Window** - Clean login interface
3. **Teacher Window** - With message sending
4. **Student Window** - With file download success
5. **Admin Dashboard** - Showing online users
6. **UDP Announcement Pop-up** - Showing broadcast message
7. **File Upload Success** - Confirmation dialog
8. **Online Users List** - Real-time updates
9. **Chat Messages** - Multiple users chatting
10. **Downloaded File** - In file explorer

---

## 💡 Pro Tips for Viva Demo

1. **Start server FIRST, always!**
2. **Have 3-4 client windows ready** (different roles)
3. **Prepare test files beforehand** (small text files)
4. **Know your test account credentials by heart**
5. **Practice the demo flow 2-3 times before viva**
6. **Keep server console visible** to show backend logging
7. **Explain what's happening at network level** while demoing
8. **Have backup plan** if network fails (pre-recorded video?)
9. **Know which port does what** (5000=TCP, 5001=File, 6000=UDP)
10. **Be ready to answer "Why did you use TCP here and UDP there?"**

---

## ✅ Final Pre-Demo Checklist

**30 Minutes Before Demo:**
- [ ] Compile all files
- [ ] Test server starts without errors
- [ ] Test at least 1 client can connect
- [ ] Test file upload/download works
- [ ] Test UDP broadcast works
- [ ] Close unnecessary applications
- [ ] Have this guide open for reference

**5 Minutes Before Demo:**
- [ ] Start server
- [ ] Have 2-3 client batch files ready to double-click
- [ ] Have test files ready in `data/files/`
- [ ] Deep breath! You got this! 💪

---

## 🎉 Good Luck with Your Demo!

**Remember:** You've built a complete network application with:
- ✅ TCP Socket Programming
- ✅ UDP Broadcasting
- ✅ Multithreading
- ✅ File Transfer
- ✅ GUI Interface
- ✅ Real-time Communication

**You've got this!** 🚀
