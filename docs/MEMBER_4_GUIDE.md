# 👤 MEMBER 4: Binary File Transfer & Streaming

## 📋 Complete Viva Preparation Guide

**Role:** File Transfer System Specialist  
**Focus:** Binary Data Streaming, Progress Tracking, Separate Port Architecture  
**Files:** `FileTransferHandler.java`, `FileTransferClient.java`, UI Integration  
**Lines of Code:** ~950 lines  
**Complexity:** ⭐⭐⭐⭐⭐ (Very High)

---

## 📚 PART 1: Network Concepts

### 1.1 Binary vs Text Data Transfer

```
TEXT DATA:
──────────
"Hello World" → Bytes: 48 65 6C 6C 6F 20 57 6F 72 6C 64
✅ Human-readable
✅ Easy to debug
❌ Inefficient for files
❌ Encoding issues

BINARY DATA:
────────────
Raw bytes: FF D8 FF E0 00 10 4A 46 49 46...
✅ Efficient (no encoding)
✅ Preserves exact data
✅ Works for any file type
❌ Not human-readable
```

### 1.2 File Transfer Protocol

```
UPLOAD FLOW:
────────────
Client → Server: "UPLOAD"
Client → Server: username (String)
Client → Server: role (String)
Client → Server: filename (String)
Client → Server: fileSize (long)
Client → Server: [raw bytes...]
Client ← Server: "SUCCESS" or "ERROR"

DOWNLOAD FLOW:
──────────────
Client → Server: "DOWNLOAD"
Client → Server: username (String)
Client → Server: role (String)
Client → Server: filename (String)
Client ← Server: "SUCCESS" or "ERROR"
Client ← Server: fileSize (long)
Client ← Server: [raw bytes...]
```

### 1.3 Why Separate Port?

```
SINGLE PORT (5000 for everything):
──────────────────────────────────
Chat + Files on same port
Large file upload (100 MB) → Blocks chat!
❌ Users can't send messages during file transfer

MULTIPLE PORTS:
───────────────
Port 5000: Chat (always responsive)
Port 5001: File Transfer (independent)
✅ Chat works during file transfers
✅ Multiple simultaneous file transfers
✅ Different timeout settings
✅ Better resource management
```

### 1.4 Chunked Transfer

```
WHY NOT LOAD ENTIRE FILE INTO MEMORY?
──────────────────────────────────────
100 MB file → Load all → OutOfMemoryError!

CHUNKED TRANSFER (64 KB at a time):
────────────────────────────────────
Read 64 KB → Send → Read 64 KB → Send → ...
✅ Low memory usage (only 64 KB in memory)
✅ Works for files larger than RAM
✅ Progress tracking possible
✅ Can resume on failure
```

---

## 🔧 PART 2: Implementation Details

### 2.1 FileTransferHandler.java (Server Side)

```java
package server;

import utils.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FileTransferHandler - Server-side file transfer service
 * 
 * Runs on port 5001 (separate from main chat server)
 * Handles both uploads and downloads
 */
public class FileTransferHandler extends Thread {
    
    private ServerSocket serverSocket;
    private ChatServer chatServer;
    private boolean running;
    private final int port = Constants.TCP_FILE_PORT;  // 5001
    
    public FileTransferHandler(ChatServer chatServer) {
        this.chatServer = chatServer;
        this.running = false;
    }
    
    @Override
    public void run() {
        try {
            // Create ServerSocket on port 5001
            serverSocket = new ServerSocket(port);
            running = true;
            
            Logger.info("File Transfer Server started on port " + port);
            System.out.println("📁 File Transfer Server ready on port " + port);
            
            // Accept file transfer connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    String clientIP = clientSocket.getInetAddress().getHostAddress();
                    Logger.info("File transfer connection from " + clientIP);
                    
                    // Handle in separate thread (allows multiple simultaneous transfers)
                    new Thread(() -> handleFileTransfer(clientSocket)).start();
                    
                } catch (IOException e) {
                    if (running) {
                        Logger.error("Error accepting file transfer connection", e);
                    }
                }
            }
            
        } catch (IOException e) {
            Logger.error("Failed to start file transfer server", e);
            System.err.println("❌ File Transfer Server failed to start!");
        }
    }
    
    /**
     * Handle single file transfer (upload or download)
     */
    private void handleFileTransfer(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            
            // Read metadata
            String action = dis.readUTF();      // "UPLOAD" or "DOWNLOAD"
            String username = dis.readUTF();    // Who is transferring
            String roleStr = dis.readUTF();     // Their role
            Constants.UserRole role = Constants.UserRole.valueOf(roleStr);
            
            Logger.info("File transfer: " + action + " from " + username);
            
            if (action.equals("UPLOAD")) {
                handleUpload(dis, dos, username, role);
            } else if (action.equals("DOWNLOAD")) {
                handleDownload(dis, dos, username, role);
            } else {
                dos.writeUTF("ERROR: Unknown action");
            }
            
        } catch (IOException e) {
            Logger.error("File transfer error", e);
        } finally {
            // Clean up
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Handle file UPLOAD from client
     */
    private void handleUpload(DataInputStream dis, DataOutputStream dos, 
                              String username, Constants.UserRole role) {
        FileOutputStream fos = null;
        
        try {
            // Read file metadata
            String filename = dis.readUTF();
            long fileSize = dis.readLong();
            
            Logger.file(username + " uploading: " + filename + 
                       " (" + FileUtils.formatFileSize(fileSize) + ")");
            
            // Determine save path based on role
            String savePath;
            if (role == Constants.UserRole.TEACHER) {
                // Teachers upload lectures
                savePath = FileUtils.getLectureFilePath(filename);
            } else {
                // Students upload assignments
                savePath = FileUtils.getAssignmentFilePath(filename);
            }
            
            // Create file and prepare to receive
            fos = new FileOutputStream(savePath);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];  // 64 KB
            
            long totalReceived = 0;
            int bytesRead;
            
            // Receive file in chunks
            while (totalReceived < fileSize) {
                bytesRead = dis.read(buffer);
                if (bytesRead == -1) break;  // Connection closed
                
                fos.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
                
                // Progress logging (every 10%)
                int progress = (int) ((totalReceived * 100) / fileSize);
                if (progress % 10 == 0) {
                    Logger.debug("Upload progress: " + progress + "%");
                }
            }
            
            fos.flush();
            
            // Verify complete
            if (totalReceived == fileSize) {
                dos.writeUTF("SUCCESS");
                Logger.file("Upload complete: " + filename);
                
                // Notify all clients about new file
                notifyNewFile(filename, username, role);
            } else {
                dos.writeUTF("ERROR: Incomplete transfer");
                Logger.error("Upload incomplete: " + totalReceived + "/" + fileSize);
            }
            
        } catch (IOException e) {
            Logger.error("Upload error", e);
            try {
                dos.writeUTF("ERROR: " + e.getMessage());
            } catch (IOException ex) {
                // Can't send error message
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Handle file DOWNLOAD to client
     */
    private void handleDownload(DataInputStream dis, DataOutputStream dos,
                                String username, Constants.UserRole role) {
        FileInputStream fis = null;
        
        try {
            // Read filename
            String filename = dis.readUTF();
            
            Logger.file(username + " downloading: " + filename);
            
            // Determine file location based on role
            String filePath;
            if (role == Constants.UserRole.STUDENT) {
                // Students download lectures
                filePath = FileUtils.getLectureFilePath(filename);
            } else {
                // Teachers can download anything
                filePath = FileUtils.getLectureFilePath(filename);
            }
            
            File file = new File(filePath);
            
            // Check if file exists
            if (!file.exists()) {
                dos.writeUTF("ERROR: File not found");
                Logger.error("File not found: " + filename);
                return;
            }
            
            // Send success and file metadata
            dos.writeUTF("SUCCESS");
            dos.writeLong(file.length());
            dos.flush();
            
            // Send file data in chunks
            fis = new FileInputStream(file);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            long totalSent = 0;
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
            }
            
            dos.flush();
            
            Logger.file("Download complete: " + filename + 
                       " (" + FileUtils.formatFileSize(totalSent) + ")");
            
        } catch (IOException e) {
            Logger.error("Download error", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    /**
     * Notify all clients about new file
     */
    private void notifyNewFile(String filename, String uploader, Constants.UserRole role) {
        String fileType = role == Constants.UserRole.TEACHER ? "lecture" : "assignment";
        String notification = "New " + fileType + " uploaded by " + uploader + ": " + filename;
        
        Message msg = new Message(
            Constants.MessageType.FILE_NOTIFICATION,
            "SERVER",
            "ALL",
            notification
        );
        
        chatServer.broadcastMessage(msg);
    }
    
    /**
     * Stop file transfer server
     */
    public void stopServer() {
        try {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.error("Error stopping file transfer server", e);
        }
    }
}
```

---

### 2.2 FileTransferClient.java (Client Side)

```java
package client;

import utils.*;
import java.io.*;
import java.net.Socket;

/**
 * FileTransferClient - Client-side file transfer
 * 
 * Handles uploads and downloads with progress tracking
 */
public class FileTransferClient {
    
    private String username;
    private Constants.UserRole role;
    
    /**
     * Progress callback interface
     */
    public interface ProgressListener {
        void onProgress(String message, int percentage);
    }
    
    public FileTransferClient(String username, Constants.UserRole role) {
        this.username = username;
        this.role = role;
    }
    
    /**
     * Upload file to server
     */
    public boolean uploadFile(File file, ProgressListener progressListener) {
        if (!file.exists() || !file.isFile()) {
            notifyProgress(progressListener, "Error: File does not exist", 0);
            return false;
        }
        
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileInputStream fis = null;
        
        try {
            // Connect to file transfer port
            notifyProgress(progressListener, "Connecting to server...", 0);
            socket = new Socket(Constants.SERVER_IP, Constants.TCP_FILE_PORT);
            
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            
            // Send metadata
            notifyProgress(progressListener, "Sending file information...", 5);
            dos.writeUTF("UPLOAD");
            dos.writeUTF(username);
            dos.writeUTF(role.toString());
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            dos.flush();
            
            Logger.file("Uploading: " + file.getName());
            
            // Send file data in chunks
            fis = new FileInputStream(file);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            long totalBytes = file.length();
            long sentBytes = 0;
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
                sentBytes += bytesRead;
                
                // Calculate progress
                int progress = (int) ((sentBytes * 100) / totalBytes);
                notifyProgress(progressListener, 
                    "Uploading: " + FileUtils.formatFileSize(sentBytes) + 
                    " / " + FileUtils.formatFileSize(totalBytes),
                    progress
                );
            }
            
            dos.flush();
            
            // Wait for confirmation
            String response = dis.readUTF();
            
            if (response.equals("SUCCESS")) {
                notifyProgress(progressListener, "Upload complete!", 100);
                Logger.file("Upload successful: " + file.getName());
                return true;
            } else {
                notifyProgress(progressListener, "Upload failed: " + response, 0);
                return false;
            }
            
        } catch (IOException e) {
            notifyProgress(progressListener, "Error: " + e.getMessage(), 0);
            Logger.error("Upload error", e);
            return false;
            
        } finally {
            // Cleanup
            try {
                if (fis != null) fis.close();
                if (dos != null) dos.close();
                if (dis != null) dis.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Download file from server
     */
    public boolean downloadFile(String filename, File saveLocation, 
                                ProgressListener progressListener) {
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        
        try {
            // Connect
            notifyProgress(progressListener, "Connecting to server...", 0);
            socket = new Socket(Constants.SERVER_IP, Constants.TCP_FILE_PORT);
            
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            
            // Send request
            notifyProgress(progressListener, "Requesting file...", 5);
            dos.writeUTF("DOWNLOAD");
            dos.writeUTF(username);
            dos.writeUTF(role.toString());
            dos.writeUTF(filename);
            dos.flush();
            
            // Wait for response
            String response = dis.readUTF();
            
            if (!response.equals("SUCCESS")) {
                notifyProgress(progressListener, "Error: " + response, 0);
                return false;
            }
            
            // Read file size
            long fileSize = dis.readLong();
            
            Logger.file("Downloading: " + filename + 
                       " (" + FileUtils.formatFileSize(fileSize) + ")");
            
            // Receive file data
            fos = new FileOutputStream(saveLocation);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            long totalReceived = 0;
            int bytesRead;
            
            while (totalReceived < fileSize) {
                bytesRead = dis.read(buffer);
                if (bytesRead == -1) break;
                
                fos.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
                
                // Calculate progress
                int progress = (int) ((totalReceived * 100) / fileSize);
                notifyProgress(progressListener,
                    "Downloading: " + FileUtils.formatFileSize(totalReceived) +
                    " / " + FileUtils.formatFileSize(fileSize),
                    progress
                );
            }
            
            fos.flush();
            
            // Verify complete
            if (totalReceived == fileSize) {
                notifyProgress(progressListener, "Download complete!", 100);
                Logger.file("Download successful: " + filename);
                return true;
            } else {
                notifyProgress(progressListener, "Download incomplete", 0);
                return false;
            }
            
        } catch (IOException e) {
            notifyProgress(progressListener, "Error: " + e.getMessage(), 0);
            Logger.error("Download error", e);
            return false;
            
        } finally {
            // Cleanup
            try {
                if (fos != null) fos.close();
                if (dos != null) dos.close();
                if (dis != null) dis.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Notify progress listener
     */
    private void notifyProgress(ProgressListener listener, String message, int percentage) {
        if (listener != null) {
            listener.onProgress(message, percentage);
        }
    }
}
```

---

## 🎓 PART 3: Viva Questions

**Q1: Why use DataInputStream/DataOutputStream instead of ObjectInputStream/ObjectOutputStream?**
```
A: Efficiency and control!

ObjectOutputStream:
- Serializes entire objects
- Adds overhead (class metadata, etc.)
- Good for complex objects
- Used for Message objects

DataInputStream/DataOutputStream:
- Direct primitive type transfer
- writeUTF(), writeLong(), etc.
- No overhead
- Much faster for simple data
- Perfect for file metadata

For files:
Metadata (filename, size) → DataOutputStream
File content → Raw bytes → write()
```

**Q2: Explain chunked file transfer.**
```
A: Transfer file in small pieces (chunks)

WHY?
────
100 MB file:
- Can't load entire file into RAM (OutOfMemoryError)
- Can't send all at once (blocks network)

SOLUTION: 64 KB chunks
────────────────────────
Read 64 KB → Send → Read 64 KB → Send → ...

Benefits:
✅ Low memory (only 64 KB in RAM)
✅ Works for files > available RAM
✅ Progress tracking possible
✅ Can pause/resume
✅ Multiple transfers don't block each other

Code:
byte[] buffer = new byte[65536];  // 64 KB
while ((bytesRead = fis.read(buffer)) > 0) {
    dos.write(buffer, 0, bytesRead);
}
```

**Q3: Why separate port for file transfer?**
```
A: Prevents blocking and provides isolation!

SCENARIO: Single port (5000)
────────────────────────────
Student uploads 100 MB file (takes 30 seconds)
During this: All chat messages BLOCKED!
❌ No one can chat for 30 seconds!

SOLUTION: Separate ports
────────────────────────
Port 5000 (Chat): Always responsive, lightweight
Port 5001 (Files): Independent, can be slow

Benefits:
✅ Chat works during file transfers
✅ Multiple simultaneous file transfers
✅ Different settings (timeouts, buffers)
✅ Failures don't affect each other
```

---

## ✅ Key Takeaways

1. **Binary transfer = raw bytes, efficient**
2. **DataInputStream/DataOutputStream for metadata**
3. **Chunked transfer = 64 KB pieces**
4. **Separate port = independent operation**
5. **Progress tracking via callbacks**
6. **Always close resources (finally block)**
7. **Verify transfer completeness (bytes sent = bytes received)**

**File transfer is complex but powerful! Master these concepts! 🚀**
