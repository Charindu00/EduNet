package client;

import utils.*;
import java.io.*;
import java.net.Socket;

/**
 * FileTransferClient.java
 * 
 * Handles file uploads and downloads for EduNet clients.
 * 
 * FEATURES:
 * - Upload files to server (teachers upload lectures, students upload assignments)
 * - Download files from server
 * - Progress tracking during transfer
 * - Separate TCP connection on dedicated port (5001)
 * 
 * PROTOCOL:
 * 1. Connect to server on FILE_TRANSFER_PORT
 * 2. Send metadata (username, action, filename, filesize)
 * 3. Transfer file data in chunks
 * 4. Receive confirmation
 * 
 * WHY SEPARATE PORT?
 * - Keeps file transfers separate from chat
 * - Large files won't block chat messages
 * - Can have different timeout settings
 */
public class FileTransferClient {
    
    // ==================== FIELDS ====================
    
    private String username;
    private Constants.UserRole role;
    
    
    // ==================== CONSTRUCTOR ====================
    
    public FileTransferClient(String username, Constants.UserRole role) {
        this.username = username;
        this.role = role;
    }
    
    
    // ==================== UPLOAD FILE ====================
    
    /**
     * Upload a file to the server
     * 
     * @param file              File to upload
     * @param progressListener  Callback for progress updates (can be null)
     * @return true if upload successful
     */
    public boolean uploadFile(File file, ProgressListener progressListener) {
        if (!file.exists() || !file.isFile()) {
            notifyProgress(progressListener, "Error: File does not exist");
            return false;
        }
        
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileInputStream fis = null;
        
        try {
            // Connect to file transfer port
            notifyProgress(progressListener, "Connecting to server...");
            socket = new Socket(Constants.SERVER_IP, Constants.TCP_FILE_PORT);
            
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            
            // Send metadata
            notifyProgress(progressListener, "Sending file information...");
            
            // Protocol: [ACTION][USERNAME][ROLE][FILENAME][FILESIZE]
            dos.writeUTF("UPLOAD");                    // Action
            dos.writeUTF(username);                    // Username
            dos.writeUTF(role.toString());             // Role
            dos.writeUTF(file.getName());              // Filename
            dos.writeLong(file.length());              // File size in bytes
            dos.flush();
            
            Logger.file("Starting upload: " + file.getName() + 
                       " (" + FileUtils.formatFileSize(file.length()) + ")");
            
            // Send file data in chunks
            fis = new FileInputStream(file);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];  // 64KB chunks
            
            long totalBytes = file.length();
            long sentBytes = 0;
            int bytesRead;
            
            notifyProgress(progressListener, "Uploading file...");
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
                sentBytes += bytesRead;
                
                // Calculate and report progress
                int progress = (int) ((sentBytes * 100) / totalBytes);
                notifyProgress(progressListener, "Uploading: " + progress + "%", progress);
            }
            
            dos.flush();
            
            // Wait for confirmation
            String response = dis.readUTF();
            
            if (response.equals("SUCCESS")) {
                notifyProgress(progressListener, "Upload complete!", 100);
                Logger.file("Upload successful: " + file.getName());
                return true;
            } else {
                notifyProgress(progressListener, "Upload failed: " + response);
                Logger.error("Upload failed: " + response);
                return false;
            }
            
        } catch (IOException e) {
            notifyProgress(progressListener, "Error: " + e.getMessage());
            Logger.error("File upload error", e);
            return false;
            
        } finally {
            // Clean up resources
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
    
    
    // ==================== DOWNLOAD FILE ====================
    
    /**
     * Download a file from the server
     * 
     * @param filename          Name of file to download
     * @param saveLocation      Where to save the file
     * @param progressListener  Callback for progress updates (can be null)
     * @return true if download successful
     */
    public boolean downloadFile(String filename, File saveLocation, ProgressListener progressListener) {
        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        
        try {
            // Connect to file transfer port
            notifyProgress(progressListener, "Connecting to server...");
            socket = new Socket(Constants.SERVER_IP, Constants.TCP_FILE_PORT);
            
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            
            // Send download request
            notifyProgress(progressListener, "Requesting file...");
            
            dos.writeUTF("DOWNLOAD");
            dos.writeUTF(username);
            dos.writeUTF(role.toString());
            dos.writeUTF(filename);
            dos.flush();
            
            // Wait for response
            String response = dis.readUTF();
            
            if (response.equals("FILE_NOT_FOUND")) {
                notifyProgress(progressListener, "File not found on server");
                return false;
            }
            
            if (!response.equals("OK")) {
                notifyProgress(progressListener, "Download failed: " + response);
                return false;
            }
            
            // Read file size
            long fileSize = dis.readLong();
            
            Logger.file("Starting download: " + filename + 
                       " (" + FileUtils.formatFileSize(fileSize) + ")");
            
            // Receive file data
            fos = new FileOutputStream(saveLocation);
            byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
            
            long totalBytes = fileSize;
            long receivedBytes = 0;
            int bytesRead;
            
            notifyProgress(progressListener, "Downloading file...");
            
            while (receivedBytes < totalBytes) {
                bytesRead = dis.read(buffer, 0, 
                    (int) Math.min(buffer.length, totalBytes - receivedBytes));
                
                if (bytesRead == -1) break;
                
                fos.write(buffer, 0, bytesRead);
                receivedBytes += bytesRead;
                
                // Calculate and report progress
                int progress = (int) ((receivedBytes * 100) / totalBytes);
                notifyProgress(progressListener, "Downloading: " + progress + "%", progress);
            }
            
            fos.flush();
            
            // Send confirmation
            dos.writeUTF("RECEIVED");
            dos.flush();
            
            notifyProgress(progressListener, "Download complete!", 100);
            Logger.file("Download successful: " + filename);
            return true;
            
        } catch (IOException e) {
            notifyProgress(progressListener, "Error: " + e.getMessage());
            Logger.error("File download error", e);
            return false;
            
        } finally {
            // Clean up resources
            try {
                if (fos != null) fos.close();
                if (dos != null) dos.close();
                if (dis != null) dis.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    
    // ==================== PROGRESS LISTENER ====================
    
    /**
     * Interface for progress updates
     * UI can implement this to show progress bars
     */
    public interface ProgressListener {
        void onProgress(String message, int percentage);
    }
    
    /**
     * Helper to notify progress listener
     */
    private void notifyProgress(ProgressListener listener, String message) {
        notifyProgress(listener, message, -1);
    }
    
    private void notifyProgress(ProgressListener listener, String message, int percentage) {
        if (listener != null) {
            listener.onProgress(message, percentage);
        }
        System.out.println("File Transfer: " + message);
    }
}
