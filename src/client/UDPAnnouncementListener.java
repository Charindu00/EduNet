package client;

import utils.Constants;
import java.net.*;
import java.io.IOException;

/**
 * UDPAnnouncementListener.java
 * 
 * Client-side UDP listener for receiving announcements from teachers.
 * 
 * KEY CONCEPTS:
 * - UDP receiving (DatagramSocket)
 * - Asynchronous message handling
 * - Callback interface for UI updates
 * 
 * ARCHITECTURE:
 * - Runs in background thread
 * - Listens on client-chosen port
 * - Registers with server to receive announcements
 * - Notifies UI via callback when announcement arrives
 */
public class UDPAnnouncementListener implements Runnable {
    
    private DatagramSocket socket;
    private boolean running;
    private Thread listenerThread;
    private AnnouncementCallback callback;
    private int localPort;
    
    /**
     * Callback interface for announcement notifications
     */
    public interface AnnouncementCallback {
        void onAnnouncementReceived(String announcement);
        void onRegistrationConfirmed(String message);
        void onError(String error);
    }
    
    /**
     * Constructor
     */
    public UDPAnnouncementListener(AnnouncementCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Start listening for UDP announcements
     */
    public void start() {
        try {
            // Create socket on any available port (OS assigns)
            socket = new DatagramSocket();
            localPort = socket.getLocalPort();
            running = true;
            
            // Start listener thread
            listenerThread = new Thread(this, "UDP-Announcement-Listener");
            listenerThread.setDaemon(true);  // Don't prevent JVM shutdown
            listenerThread.start();
            
            System.out.println("📢 UDP Listener started on port " + localPort);
            
            // Register with server
            registerWithServer();
            
        } catch (SocketException e) {
            System.err.println("❌ Failed to start UDP listener: " + e.getMessage());
            if (callback != null) {
                callback.onError("Failed to start UDP listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Register with server to receive announcements
     */
    private void registerWithServer() {
        try {
            // Send REGISTER message to server
            String message = "REGISTER";
            byte[] data = message.getBytes();
            
            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                serverAddress,
                Constants.UDP_PORT
            );
            
            socket.send(packet);
            System.out.println("📢 Registration request sent to server");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to register with server: " + e.getMessage());
            if (callback != null) {
                callback.onError("Failed to register: " + e.getMessage());
            }
        }
    }
    
    /**
     * Main listener loop - receives UDP packets
     */
    @Override
    public void run() {
        System.out.println("📢 Listening for announcements...");
        
        byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
        
        while (running) {
            try {
                // Wait for UDP packet
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Extract message
                String message = new String(packet.getData(), 0, packet.getLength());
                
                // Process message
                handleMessage(message);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ UDP receive error: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Receive error: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Handle received UDP message
     */
    private void handleMessage(String message) {
        // Message format: "ACTION|DATA"
        String[] parts = message.split("\\|", 2);
        
        if (parts.length < 1) {
            return;
        }
        
        String action = parts[0];
        
        switch (action) {
            case "REGISTERED":
                // Server confirmed registration
                if (parts.length == 2 && callback != null) {
                    callback.onRegistrationConfirmed(parts[1]);
                }
                System.out.println("✅ Registered for announcements");
                break;
                
            case "ANNOUNCEMENT":
                // Received announcement from teacher
                if (parts.length == 2) {
                    String announcement = parts[1];
                    System.out.println("📢 ANNOUNCEMENT: " + announcement);
                    
                    if (callback != null) {
                        callback.onAnnouncementReceived(announcement);
                    }
                }
                break;
                
            default:
                System.out.println("📢 Unknown UDP message: " + action);
        }
    }
    
    /**
     * Get local port
     */
    public int getLocalPort() {
        return localPort;
    }
    
    /**
     * Stop listening
     */
    public void stop() {
        System.out.println("📢 Stopping UDP listener...");
        
        // Unregister from server
        try {
            String message = "UNREGISTER";
            byte[] data = message.getBytes();
            
            InetAddress serverAddress = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                serverAddress,
                Constants.UDP_PORT
            );
            
            socket.send(packet);
            System.out.println("📢 Unregister request sent");
            
        } catch (IOException e) {
            System.err.println("⚠️  Failed to unregister: " + e.getMessage());
        }
        
        running = false;
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        if (listenerThread != null) {
            try {
                listenerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("📢 UDP listener stopped");
    }
}
