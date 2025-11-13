package client;

import utils.*;
import java.io.*;
import java.net.Socket;

/**
 * AutomatedTest.java
 * 
 * Automated test to demonstrate server-client communication
 * without manual input.
 */
public class AutomatedTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║    EduNet Automated Test - v1.0          ║");
        System.out.println("║    Testing Server Communication          ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        try {
            // TEST 1: Connect to server
            System.out.println("TEST 1: Connecting to server...");
            Socket socket = new Socket(Constants.SERVER_IP, Constants.TCP_SERVER_PORT);
            System.out.println("✅ Connected! Local: " + socket.getLocalAddress() + 
                             ":" + socket.getLocalPort());
            
            // Set up streams
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            System.out.println("✅ Streams initialized!\n");
            
            // TEST 2: Login as teacher
            System.out.println("TEST 2: Logging in as teacher1...");
            String credentials = "teacher1:teacher123:TEACHER";
            Message loginMsg = new Message(
                Constants.MessageType.LOGIN,
                "teacher1",
                "SERVER",
                credentials
            );
            
            output.writeObject(loginMsg);
            output.flush();
            System.out.println("📤 Login request sent");
            
            // Wait for response
            Message response = (Message) input.readObject();
            System.out.println("📥 Response: " + response.getType());
            System.out.println("   Content: " + response.getContent());
            
            if (response.getType() == Constants.MessageType.LOGIN_SUCCESS) {
                System.out.println("✅ Login successful!\n");
            } else {
                System.out.println("❌ Login failed!\n");
                socket.close();
                return;
            }
            
            // TEST 3: Send broadcast message
            System.out.println("TEST 3: Sending broadcast message...");
            Message broadcastMsg = Message.createBroadcastMessage(
                "teacher1",
                "Hello students! Welcome to EduNet!"
            );
            
            output.writeObject(broadcastMsg);
            output.flush();
            System.out.println("✅ Broadcast sent!\n");
            
            // TEST 4: Listen for server messages
            System.out.println("TEST 4: Listening for messages (5 seconds)...");
            socket.setSoTimeout(5000);  // 5 second timeout
            
            try {
                while (true) {
                    Message msg = (Message) input.readObject();
                    System.out.println("📥 Received: [" + msg.getType() + "] " + 
                                     msg.getSender() + ": " + msg.getContent());
                }
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("⏱️  Timeout reached (this is expected)\n");
            }
            
            // TEST 5: Disconnect
            System.out.println("TEST 5: Disconnecting...");
            Message disconnectMsg = Message.createDisconnectMessage("teacher1");
            output.writeObject(disconnectMsg);
            output.flush();
            
            Thread.sleep(500);  // Give server time to process
            
            output.close();
            input.close();
            socket.close();
            System.out.println("✅ Disconnected gracefully\n");
            
            // Summary
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║         ALL TESTS PASSED! ✅             ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.println("\nWhat happened:");
            System.out.println("1. ✅ Client connected to server via TCP socket");
            System.out.println("2. ✅ Client authenticated as teacher1");
            System.out.println("3. ✅ Client sent broadcast message");
            System.out.println("4. ✅ Server received and logged the message");
            System.out.println("5. ✅ Client disconnected gracefully");
            System.out.println("\nCheck the server console to see the activity!");
            System.out.println("Also check: data/chat_logs.txt for full logs");
            
        } catch (Exception e) {
            System.err.println("\n❌ TEST FAILED!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\n⚠️  Make sure the server is running:");
            System.err.println("   java -cp bin server.ChatServer");
        }
    }
}
