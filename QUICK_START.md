# 🚀 EduNet Quick Start Guide

## Problem Fixed! ✅

**Issue:** The batch files weren't working because your Java files contain Unicode characters (emojis) that need UTF-8 encoding.

**Solution:** Files have been recompiled with UTF-8 encoding. Batch files updated to auto-compile if needed.

---

## ⚡ Quick Start (3 Steps)

### Step 1: Start Server
```
Double-click: start-server.bat
```
**Wait for these messages:**
```
✅ Server is running on port 5000
✅ File Transfer Server ready on port 5001
✅ UDP Announcement Server ready on port 6000
```

### Step 2: Start Client(s)
```
Double-click: start-client.bat
```
**The Login Window will appear!**

You can run this multiple times to open multiple clients.

### Step 3: Login and Test
**Use these test accounts:**

| Role | Username | Password |
|------|----------|----------|
| Teacher | teacher1 | teacher123 |
| Student | student1 | student123 |
| Admin | admin1 | admin123 |

---

## 🎯 Quick Demo Scenario

### Scenario: Teacher uploads file, Student downloads

1. **Server Terminal:** Keep it open and visible
2. **Client 1:** Login as `teacher1/teacher123/Teacher`
3. **Client 2:** Login as `student1/student123/Student`
4. **Teacher Window:** Click "Upload Lecture" → Select any .txt file
5. **Student Window:** Pop-up appears "New lecture available!"
6. **Student Window:** Click "Download Lecture" → Enter filename
7. **Success!** File downloads to project folder

---

## 🐛 If Something Goes Wrong

### Server Won't Start
```powershell
# Kill any running Java processes
taskkill /F /IM java.exe

# Wait 5 seconds, then try again
start-server.bat
```

### Client Won't Connect
- Make sure server is running FIRST
- Look for "✅ Server is running" message
- Check if firewall is blocking Java

### Need to Recompile
```powershell
# Open PowerShell in project folder
cd "C:\Users\user\OneDrive\Desktop\EduNet"
javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
```

---

## 📸 What You Should See

### Server Terminal:
```
╔════════════════════════════════════════════╗
║         EduNet Server - v1.0             ║
║   Educational Communication Platform      ║
╚════════════════════════════════════════════╝

✅ Server is running on port 5000
✅ File Transfer Server ready on port 5001
✅ UDP Announcement Server ready on port 6000
🔄 Waiting for next client...
```

### Login Window:
- Username field
- Password field
- Role dropdown (Teacher/Student/Admin)
- Login button

### After Login:
- Chat area (main panel)
- Message input box (bottom)
- Send button
- Online Users list (right side)
- Upload buttons (Teacher/Student)
- UDP Broadcast button

---

## 🎓 Complete Testing Guide

For detailed testing scenarios and viva preparation, see:
- **[GUI_TESTING_GUIDE.md](GUI_TESTING_GUIDE.md)** - Complete testing workflows

For network concepts and architecture, see:
- **[NETWORK_CONCEPTS_AND_FLOW.md](NETWORK_CONCEPTS_AND_FLOW.md)** - Network architecture

For individual member guides, see:
- **[docs/README.md](docs/README.md)** - Master guide index

---

## ✅ Everything Working Checklist

- [ ] Server starts without errors (3 ports shown)
- [ ] Client GUI appears when run
- [ ] Can login with test credentials
- [ ] Online users list updates
- [ ] Chat messages send/receive
- [ ] File upload works (teacher)
- [ ] File download works (student)
- [ ] UDP broadcasts show pop-ups

---

**Your server is currently running! 🎉**

Open another PowerShell or double-click `start-client.bat` to start testing!
