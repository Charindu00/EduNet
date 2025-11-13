package utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileUtils.java
 * 
 * Helper class for all file operations in EduNet.
 * 
 * FUNCTIONS:
 * - Load/save user credentials
 * - List files in directories
 * - Copy/move files
 * - Get file info (size, extension, etc.)
 * - Validate file paths
 */
public class FileUtils {
    
    // ==================== USER FILE OPERATIONS ====================
    
    /**
     * Load all users from users.txt file
     * Format: username:password:role
     * 
     * Returns: List of User objects
     */
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(Constants.USERS_FILE);
        
        // If file doesn't exist, create it with default users
        if (!file.exists()) {
            createDefaultUsersFile();
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                try {
                    User user = User.fromFileFormat(line);
                    users.add(user);
                } catch (IllegalArgumentException e) {
                    Logger.error("Invalid user format in file: " + line, e);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to load users from file", e);
        }
        
        Logger.info("Loaded " + users.size() + " users from file");
        return users;
    }
    
    /**
     * Save users to file (for adding new users)
     */
    public static void saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(Constants.USERS_FILE))) {
            bw.write("# EduNet User Credentials\n");
            bw.write("# Format: username:password:role\n");
            bw.write("# Roles: TEACHER, STUDENT, ADMIN\n\n");
            
            for (User user : users) {
                bw.write(user.toFileFormat());
                bw.newLine();
            }
            
            Logger.info("Saved " + users.size() + " users to file");
        } catch (IOException e) {
            Logger.error("Failed to save users to file", e);
        }
    }
    
    /**
     * Create default users file with sample accounts
     */
    private static void createDefaultUsersFile() {
        try {
            // Create parent directory if needed
            File file = new File(Constants.USERS_FILE);
            file.getParentFile().mkdirs();
            
            // Create file with default users
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("# EduNet User Credentials\n");
                bw.write("# Format: username:password:role\n");
                bw.write("# Roles: TEACHER, STUDENT, ADMIN\n\n");
                
                // Default accounts
                bw.write("teacher1:teacher123:TEACHER\n");
                bw.write("teacher2:teacher123:TEACHER\n");
                bw.write("student1:student123:STUDENT\n");
                bw.write("student2:student123:STUDENT\n");
                bw.write("student3:student123:STUDENT\n");
                bw.write("student4:student123:STUDENT\n");
                bw.write("admin1:admin123:ADMIN\n");
            }
            
            Logger.info("Created default users file with 7 accounts");
        } catch (IOException e) {
            System.err.println("Failed to create default users file: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user - check if username/password match
     */
    public static User authenticateUser(String username, String password) {
        List<User> users = loadUsers();
        
        for (User user : users) {
            if (user.getUsername().equals(username) && user.checkPassword(password)) {
                Logger.info("User authenticated: " + username + " (Role: " + user.getRole() + ")");
                return user;
            }
        }
        
        Logger.error("Authentication failed for user: " + username);
        return null;
    }
    
    
    // ==================== FILE LISTING ====================
    
    /**
     * List all files in lectures directory
     */
    public static List<String> listLectureFiles() {
        return listFilesInDirectory(Constants.LECTURES_DIR);
    }
    
    /**
     * List all files in assignments directory
     */
    public static List<String> listAssignmentFiles() {
        return listFilesInDirectory(Constants.ASSIGNMENTS_DIR);
    }
    
    /**
     * List all files in a directory
     */
    public static List<String> listFilesInDirectory(String directoryPath) {
        List<String> fileNames = new ArrayList<>();
        File directory = new File(directoryPath);
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs();
            return fileNames;  // Empty list
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        
        // Sort alphabetically
        Collections.sort(fileNames);
        return fileNames;
    }
    
    /**
     * Get detailed file information
     */
    public static Map<String, Object> getFileInfo(String filePath) {
        Map<String, Object> info = new HashMap<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            info.put("exists", false);
            return info;
        }
        
        info.put("exists", true);
        info.put("name", file.getName());
        info.put("size", file.length());
        info.put("sizeFormatted", formatFileSize(file.length()));
        info.put("extension", getFileExtension(file.getName()));
        info.put("lastModified", new Date(file.lastModified()));
        info.put("isDirectory", file.isDirectory());
        info.put("absolutePath", file.getAbsolutePath());
        
        return info;
    }
    
    
    // ==================== FILE OPERATIONS ====================
    
    /**
     * Copy file from source to destination
     */
    public static boolean copyFile(String sourcePath, String destPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path dest = Paths.get(destPath);
            
            // Create parent directories if needed
            Files.createDirectories(dest.getParent());
            
            // Copy file
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            
            Logger.file("Copied file: " + sourcePath + " -> " + destPath);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to copy file: " + sourcePath, e);
            return false;
        }
    }
    
    /**
     * Delete a file
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            boolean deleted = file.delete();
            
            if (deleted) {
                Logger.file("Deleted file: " + filePath);
            }
            
            return deleted;
        } catch (Exception e) {
            Logger.error("Failed to delete file: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Check if file exists
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    /**
     * Create directory if it doesn't exist
     */
    public static void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
            Logger.info("Created directory: " + directoryPath);
        }
    }
    
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get file extension (e.g., "pdf", "txt", "jpg")
     */
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Format file size in human-readable format
     * Examples: "1.5 KB", "2.3 MB", "500 bytes"
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            double sizeInKB = sizeInBytes / 1024.0;
            return String.format("%.2f KB", sizeInKB);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", sizeInMB);
        } else {
            double sizeInGB = sizeInBytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.2f GB", sizeInGB);
        }
    }
    
    /**
     * Sanitize filename (remove invalid characters)
     */
    public static String sanitizeFilename(String filename) {
        // Remove or replace invalid characters
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Get file path for lecture upload
     */
    public static String getLectureFilePath(String filename) {
        ensureDirectoryExists(Constants.LECTURES_DIR);
        return Constants.LECTURES_DIR + sanitizeFilename(filename);
    }
    
    /**
     * Get file path for assignment upload
     */
    public static String getAssignmentFilePath(String filename) {
        ensureDirectoryExists(Constants.ASSIGNMENTS_DIR);
        return Constants.ASSIGNMENTS_DIR + sanitizeFilename(filename);
    }
    
    /**
     * Validate if file type is allowed (optional security check)
     */
    public static boolean isAllowedFileType(String filename) {
        String extension = getFileExtension(filename);
        
        // Allowed extensions (add more as needed)
        String[] allowedTypes = {
            "pdf", "txt", "doc", "docx", "ppt", "pptx",
            "xls", "xlsx", "jpg", "jpeg", "png", "gif",
            "zip", "rar", "java", "py", "cpp", "c"
        };
        
        for (String allowed : allowedTypes) {
            if (extension.equals(allowed)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Initialize all required directories
     */
    public static void initializeDirectories() {
        ensureDirectoryExists(Constants.DATA_DIR);
        ensureDirectoryExists(Constants.LECTURES_DIR);
        ensureDirectoryExists(Constants.ASSIGNMENTS_DIR);
        Logger.info("All directories initialized");
    }
}
