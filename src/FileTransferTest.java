import client.FileTransferClient;
import utils.Constants.UserRole;
import java.io.File;

/**
 * FileTransferTest.java
 * 
 * Automated test for file transfer functionality.
 * Tests upload and download operations.
 */
public class FileTransferTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║    EduNet File Transfer Test Suite        ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // Test 1: Teacher uploads a lecture
        System.out.println("TEST 1: Teacher Upload Lecture");
        System.out.println("================================");
        testTeacherUpload();
        
        // Wait a bit
        sleep(2000);
        
        // Test 2: Student downloads the lecture
        System.out.println("\nTEST 2: Student Download Lecture");
        System.out.println("==================================");
        testStudentDownload();
        
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║         All Tests Completed!               ║");
        System.out.println("╚════════════════════════════════════════════╝");
    }
    
    /**
     * Test teacher file upload
     */
    private static void testTeacherUpload() {
        try {
            // Create file transfer client as teacher
            FileTransferClient ftClient = new FileTransferClient("teacher1", UserRole.TEACHER);
            
            // Select test file
            File testFile = new File("test-lecture.txt");
            
            if (!testFile.exists()) {
                System.out.println("❌ Test file not found: " + testFile.getAbsolutePath());
                System.out.println("   Creating test file...");
                createTestFile(testFile);
            }
            
            System.out.println("📤 Uploading: " + testFile.getName());
            System.out.println("   Size: " + testFile.length() + " bytes");
            
            // Upload with progress tracking
            boolean success = ftClient.uploadFile(testFile, (message, percentage) -> {
                if (percentage >= 0) {
                    System.out.println("   Progress: " + percentage + "% - " + message);
                }
            });
            
            if (success) {
                System.out.println("✅ Upload successful!");
                
                // Verify file exists on server
                File serverFile = new File("data/files/lectures/" + testFile.getName());
                if (serverFile.exists()) {
                    System.out.println("✅ File verified on server: " + serverFile.getAbsolutePath());
                } else {
                    System.out.println("❌ File not found on server!");
                }
            } else {
                System.out.println("❌ Upload failed!");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during upload test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test student file download
     */
    private static void testStudentDownload() {
        try {
            // Create file transfer client as student
            FileTransferClient ftClient = new FileTransferClient("student1", UserRole.STUDENT);
            
            // Download destination
            File downloadFile = new File("downloaded-lecture.txt");
            
            // Delete if exists from previous test
            if (downloadFile.exists()) {
                downloadFile.delete();
            }
            
            System.out.println("📥 Downloading: test-lecture.txt");
            System.out.println("   To: " + downloadFile.getAbsolutePath());
            
            // Download with progress tracking
            boolean success = ftClient.downloadFile("test-lecture.txt", downloadFile, (message, percentage) -> {
                if (percentage >= 0) {
                    System.out.println("   Progress: " + percentage + "% - " + message);
                }
            });
            
            if (success) {
                System.out.println("✅ Download successful!");
                
                // Verify downloaded file
                if (downloadFile.exists()) {
                    System.out.println("✅ File verified: " + downloadFile.getAbsolutePath());
                    System.out.println("   Size: " + downloadFile.length() + " bytes");
                } else {
                    System.out.println("❌ Downloaded file not found!");
                }
            } else {
                System.out.println("❌ Download failed!");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during download test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a test file
     */
    private static void createTestFile(File file) {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write("EduNet File Transfer Test\n");
            writer.write("=========================\n\n");
            writer.write("This is a test file for demonstrating file transfer.\n");
            writer.write("It contains some sample text data.\n");
            writer.close();
            System.out.println("✅ Test file created: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("❌ Failed to create test file: " + e.getMessage());
        }
    }
    
    /**
     * Sleep helper
     */
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
