package server;

import utils.Constants;
import utils.Logger;
import java.net.*;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * UDPAnnouncementServer.java
 * 
 * Handles UDP-based announcements from teachers to all students.
 * 
 * KEY CONCEPTS DEMONSTRATED:
 * - UDP (User Datagram Protocol) communication
 * - Connectionless broadcasting
 * - DatagramSocket and DatagramPacket
 * - Multicast/broadcast to multiple clients
 * 
 * UDP vs TCP:
 * - No connection establishment (faster)
 * - No delivery guarantee (fire and forget)
 * - No ordering guarantee
 * - Lower overhead
 * - Perfect for announcements that don't need reliability
 */
public class UDPAnnouncementServer implements Runnable {
    
    private DatagramSocket socket;
    private boolean running;
    private Thread serverThread;
    
    // List of registered student addresses (who want to receive announcements)
    private CopyOnWriteArrayList<InetSocketAddress> registeredClients;
    
    /**
     * Constructor
     */
    public UDPAnnouncementServer() {
        this.registeredClients = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Start the UDP announcement server
     */
    public void start() {
        try {
            socket = new DatagramSocket(Constants.UDP_PORT);
            running = true;
            
            serverThread = new Thread(this, "UDP-Announcement-Server");
            serverThread.start();
            
            Logger.info("UDP Announcement Server started on port " + Constants.UDP_PORT);
            System.out.println("📢 UDP Announcement Server ready on port " + Constants.UDP_PORT);
            
        } catch (SocketException e) {
            Logger.error("Failed to start UDP server: " + e.getMessage());
            System.err.println("❌ UDP server start failed: " + e.getMessage());
        }
    }
    
    /**
     * Main server loop - listens for incoming UDP messages
     */
    @Override
    public void run() {
        Logger.info("UDP server listening for announcements...");
        
        byte[] buffer = new byte[Constants.FILE_BUFFER_SIZE];
        
        while (running) {
            try {
                // Receive UDP packet
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Extract message
                String message = new String(packet.getData(), 0, packet.getLength());
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();
                
                Logger.debug("UDP: Received from " + senderAddress + ":" + senderPort);
                
                // Process message
                handleMessage(message, senderAddress, senderPort);
                
            } catch (IOException e) {
                if (running) {
                    Logger.error("UDP receive error: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handle incoming UDP message
     */
    private void handleMessage(String message, InetAddress sender, int port) {
        // Message format: "ACTION|DATA"
        String[] parts = message.split("\\|", 2);
        
        if (parts.length < 1) {
            return;
        }
        
        String action = parts[0];
        
        switch (action) {
            case "REGISTER":
                // Student wants to register for announcements
                handleRegistration(sender, port);
                break;
                
            case "UNREGISTER":
                // Student wants to unregister
                handleUnregistration(sender, port);
                break;
                
            case "ANNOUNCEMENT":
                // Teacher is sending an announcement
                if (parts.length == 2) {
                    String announcement = parts[1];
                    broadcastAnnouncement(announcement, sender);
                }
                break;
                
            default:
                Logger.debug("UDP: Unknown action: " + action);
        }
    }
    
    /**
     * Register a student to receive announcements
     */
    private void handleRegistration(InetAddress address, int port) {
        InetSocketAddress clientAddress = new InetSocketAddress(address, port);
        
        // Add if not already registered
        if (!registeredClients.contains(clientAddress)) {
            registeredClients.add(clientAddress);
            Logger.info("UDP: Student registered: " + address + ":" + port);
            System.out.println("📢 Student registered for announcements: " + address);
            
            // Send confirmation
            sendMessage("REGISTERED|You are now receiving announcements", clientAddress);
        }
    }
    
    /**
     * Unregister a student
     */
    private void handleUnregistration(InetAddress address, int port) {
        InetSocketAddress clientAddress = new InetSocketAddress(address, port);
        
        if (registeredClients.remove(clientAddress)) {
            Logger.info("UDP: Student unregistered: " + address + ":" + port);
            System.out.println("📢 Student unregistered: " + address);
        }
    }
    
    /**
     * Broadcast announcement to all registered students
     */
    private void broadcastAnnouncement(String announcement, InetAddress sender) {
        Logger.info("UDP: Broadcasting announcement from " + sender);
        Logger.admin("ANNOUNCEMENT: " + announcement);
        
        System.out.println("📢 Broadcasting announcement to " + registeredClients.size() + " students");
        
        int successCount = 0;
        int failCount = 0;
        
        // Send to all registered clients
        for (InetSocketAddress clientAddress : registeredClients) {
            String message = "ANNOUNCEMENT|" + announcement;
            boolean sent = sendMessage(message, clientAddress);
            
            if (sent) {
                successCount++;
            } else {
                failCount++;
            }
        }
        
        Logger.info("UDP: Announcement delivered to " + successCount + " students (" + failCount + " failed)");
        System.out.println("   ✅ Delivered to " + successCount + " students");
        
        if (failCount > 0) {
            System.out.println("   ⚠️  " + failCount + " delivery failures");
        }
    }
    
    /**
     * Send UDP message to specific address
     */
    private boolean sendMessage(String message, InetSocketAddress destination) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(
                data, 
                data.length, 
                destination.getAddress(), 
                destination.getPort()
            );
            
            socket.send(packet);
            return true;
            
        } catch (IOException e) {
            Logger.error("Failed to send UDP message to " + destination + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get number of registered clients
     */
    public int getRegisteredClientCount() {
        return registeredClients.size();
    }
    
    /**
     * Stop the UDP server
     */
    public void stop() {
        Logger.info("Stopping UDP Announcement Server...");
        
        running = false;
        
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        
        if (serverThread != null) {
            try {
                serverThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        registeredClients.clear();
        Logger.info("UDP Announcement Server stopped");
        System.out.println("📢 UDP Announcement Server stopped");
    }
}
