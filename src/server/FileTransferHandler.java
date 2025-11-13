package server;

import utils.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FileTransferHandler.java
 * 
 * Server-side handler for file transfers.
 * Runs on a separate port (5001) from main chat server.
 * 
 * RESPONSIBILITIES:
 * - Accept file transfer connections
 * - Handle uploads (save to appropriate directory)
 * - Handle downloads (send requested files)
 * - Notify main server about new files
 * 
 * RUNS IN SEPARATE THREAD from main server!
 */
public class FileTransferHandler extends Thread {
    
    // ==================== FIELDS ====================
    
    private ServerSocket serverSocket;
    private ChatServer chatServer;
    private boolean running;
    private int port;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public FileTransferHandler(ChatServer chatServer) {
        this.chatServer = chatServer;
        this.port = Constants.TCP_FILE_PORT;
        this.running = false;
    }
    
    
    // ==================== START SERVER ====================
    
    @Override
    public void run() {
        try {
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
                    
                    // Handle this file transfer in a separate thread
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
    
    
    // ==================== HANDLE FILE TRANSFER ====================
    
    /**
     * Handle a single file transfer connection
     * This runs in a separate thread for each transfer
     */
    private void handleFileTransfer(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            
            // Read action (UPLOAD or DOWNLOAD)
            String action = dis.readUTF();
            String username = dis.readUTF();
            String roleStr = dis.readUTF();
            Constants.UserRole role = Constants.UserRole.valueOf(roleStr);
            
            Logger.info("File transfer request: " + action + " from " + username);
            
            if (action.equals("UPLOAD")) {
                handleUpload(dis, dos, username, role);
            } else if (action.equals("DOWNLOAD")) {
                handleDownload(dis, dos, username, role);
            } else {
                dos.writeUTF("ERROR: Unknown action");
                Logger.error("Unknown file transfer action: " + action);
            }
            
        } catch (IOException e) {
            Logger.error("File transfer error", e);
        } finally {
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    
    // ==================== HANDLE UPLOAD ====================
    
    /**
     * Handle file upload from client
     */
    private void handleUpload(DataInputStream dis, DataOutputStream dos, 
                              String username, Constants.UserRole role) {
        FileOutputStream fos = null;
        
        try {
            // Read metadata
            String filename = dis.readUTF();
            long fileSize = dis.readLong();
            
            Logger.file(username + " is uploading: " + filename + 
                       " (" + FileUtils.formatFileSize(fileSize) + ")");
            
            // Determine save location based on role
            String savePath;
            if (role == Constants.UserRole.TEACHER) {
                savePath = FileUtils.getLectureFilePath(filename);
            } else {
                savePath = FileUtils.getAssignmentFilePath(filename);
            }
            
            // Receive file data
            fos = new FileOutputStream(savePath);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            long totalBytes = fileSize;
            long receivedBytes = 0;
            int bytesRead;
            
            while (receivedBytes < totalBytes) {
                bytesRead = dis.read(buffer, 0, 
                    (int) Math.min(buffer.length, totalBytes - receivedBytes));
                
                if (bytesRead == -1) break;
                
                fos.write(buffer, 0, bytesRead);
                receivedBytes += bytesRead;
            }
            
            fos.flush();
            
            // Send success response
            dos.writeUTF("SUCCESS");
            dos.flush();
            
            Logger.file("Upload complete: " + filename + " from " + username);
            System.out.println("📥 " + username + " uploaded: " + filename);
            
            // Notify all clients about new file
            notifyNewFile(username, filename, role);
            
        } catch (IOException e) {
            Logger.error("Upload error", e);
            try {
                dos.writeUTF("ERROR: " + e.getMessage());
            } catch (IOException ex) {
                // Ignore
            }
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    
    // ==================== HANDLE DOWNLOAD ====================
    
    /**
     * Handle file download request from client
     */
    private void handleDownload(DataInputStream dis, DataOutputStream dos,
                               String username, Constants.UserRole role) {
        FileInputStream fis = null;
        
        try {
            // Read filename
            String filename = dis.readUTF();
            
            Logger.file(username + " is downloading: " + filename);
            
            // Find file (check both directories)
            File file = null;
            
            // Try lectures directory first
            File lectureFile = new File(Constants.LECTURES_DIR + filename);
            if (lectureFile.exists()) {
                file = lectureFile;
            } else {
                // Try assignments directory
                File assignmentFile = new File(Constants.ASSIGNMENTS_DIR + filename);
                if (assignmentFile.exists()) {
                    file = assignmentFile;
                }
            }
            
            // Check if file found
            if (file == null || !file.exists()) {
                dos.writeUTF("FILE_NOT_FOUND");
                dos.flush();
                Logger.error("File not found: " + filename);
                return;
            }
            
            // Send OK response and file size
            dos.writeUTF("OK");
            dos.writeLong(file.length());
            dos.flush();
            
            // Send file data
            fis = new FileInputStream(file);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }
            
            dos.flush();
            
            // Wait for confirmation
            String confirmation = dis.readUTF();
            
            if (confirmation.equals("RECEIVED")) {
                Logger.file("Download complete: " + filename + " to " + username);
                System.out.println("📤 " + username + " downloaded: " + filename);
            }
            
        } catch (IOException e) {
            Logger.error("Download error", e);
            try {
                dos.writeUTF("ERROR: " + e.getMessage());
            } catch (IOException ex) {
                // Ignore
            }
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    
    // ==================== NOTIFICATIONS ====================
    
    /**
     * Notify all connected clients about a new file
     */
    private void notifyNewFile(String uploader, String filename, Constants.UserRole role) {
        String fileType = (role == Constants.UserRole.TEACHER) ? "lecture" : "assignment";
        
        Message notification = new Message(
            Constants.MessageType.FILE_NOTIFICATION,
            "SERVER",
            "ALL",
            "📁 New " + fileType + " available: " + filename + " (uploaded by " + uploader + ")"
        );
        
        // Broadcast through main chat server
        chatServer.broadcastMessage(notification, null);
    }
    
    
    // ==================== SHUTDOWN ====================
    
    /**
     * Stop the file transfer server
     */
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.error("Error closing file transfer server", e);
        }
        Logger.info("File Transfer Server stopped");
    }
}
