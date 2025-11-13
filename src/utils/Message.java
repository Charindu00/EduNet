package utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message.java
 * 
 * The universal communication object between client and server.
 * 
 * WHY SERIALIZABLE?
 * - We send this object through ObjectOutputStream over the network
 * - Java serialization converts objects to byte streams
 * - The receiving side deserializes bytes back to Message object
 * 
 * EVERY message in EduNet is a Message object!
 */
public class Message implements Serializable {
    
    // Serialization version - important for compatibility
    private static final long serialVersionUID = 1L;
    
    // ==================== FIELDS ====================
    
    private Constants.MessageType type;    // What kind of message is this?
    private String sender;                  // Who sent it?
    private String recipient;               // Who should receive it? (null = broadcast)
    private String content;                 // The actual message text
    private String timestamp;               // When was it sent?
    private Object data;                    // Extra data (file bytes, user lists, etc.)
    
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Main constructor - Creates a new message
     * 
     * @param type      Message category (LOGIN, CHAT, etc.)
     * @param sender    Username of sender
     * @param recipient Username of recipient (null for broadcast)
     * @param content   Message text
     */
    public Message(Constants.MessageType type, String sender, String recipient, String content) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.data = null;
    }
    
    /**
     * Constructor with extra data (for file transfers, user lists, etc.)
     */
    public Message(Constants.MessageType type, String sender, String recipient, String content, Object data) {
        this(type, sender, recipient, content);
        this.data = data;
    }
    
    /**
     * Simple constructor for system messages (no specific sender/recipient)
     */
    public Message(Constants.MessageType type, String content) {
        this(type, "SYSTEM", null, content);
    }
    
    
    // ==================== GETTERS & SETTERS ====================
    
    public Constants.MessageType getType() {
        return type;
    }
    
    public void setType(Constants.MessageType type) {
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if this is a broadcast message (no specific recipient)
     */
    public boolean isBroadcast() {
        return recipient == null || recipient.isEmpty() || recipient.equals("ALL");
    }
    
    /**
     * Check if this message is for a specific user
     */
    public boolean isPrivate() {
        return recipient != null && !recipient.isEmpty() && !recipient.equals("ALL");
    }
    
    /**
     * Format message for display in UI
     * Example: "[14:30:25] Teacher1: Hello class!"
     */
    public String getFormattedMessage() {
        String timeOnly = timestamp.substring(11); // Extract HH:mm:ss
        return String.format("[%s] %s: %s", timeOnly, sender, content);
    }
    
    /**
     * Format message for logging to file
     * Example: "2025-11-11 14:30:25 | CHAT | teacher1 -> ALL | Hello class!"
     */
    public String getLogFormat() {
        String recipientStr = (recipient == null || recipient.isEmpty()) ? "ALL" : recipient;
        return String.format("%s | %s | %s -> %s | %s", 
                             timestamp, type, sender, recipientStr, content);
    }
    
    /**
     * Create a reply message to this message
     */
    public Message createReply(String replyContent) {
        return new Message(Constants.MessageType.CHAT_PRIVATE, 
                          this.recipient,  // Reply sender is original recipient
                          this.sender,     // Reply recipient is original sender
                          replyContent);
    }
    
    
    // ==================== FACTORY METHODS ====================
    // These make creating common message types easier
    
    /**
     * Create a login request message
     */
    public static Message createLoginMessage(String username, String password, Constants.UserRole role) {
        String loginData = username + ":" + password + ":" + role;
        return new Message(Constants.MessageType.LOGIN, username, "SERVER", loginData);
    }
    
    /**
     * Create a broadcast chat message (teacher to all students)
     */
    public static Message createBroadcastMessage(String sender, String content) {
        return new Message(Constants.MessageType.CHAT_BROADCAST, sender, "ALL", content);
    }
    
    /**
     * Create a private chat message
     */
    public static Message createPrivateMessage(String sender, String recipient, String content) {
        return new Message(Constants.MessageType.CHAT_PRIVATE, sender, recipient, content);
    }
    
    /**
     * Create an announcement message (UDP broadcast)
     */
    public static Message createAnnouncement(String sender, String announcement) {
        return new Message(Constants.MessageType.ANNOUNCEMENT, sender, "ALL", announcement);
    }
    
    /**
     * Create a disconnect message
     */
    public static Message createDisconnectMessage(String username) {
        return new Message(Constants.MessageType.DISCONNECT, username, "SERVER", "User disconnecting");
    }
    
    
    // ==================== OVERRIDE METHODS ====================
    
    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        return String.format("Message[type=%s, from=%s, to=%s, content='%s']", 
                            type, sender, recipient, 
                            content.length() > 50 ? content.substring(0, 47) + "..." : content);
    }
    
    /**
     * Compare messages by timestamp and sender
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Message message = (Message) obj;
        return timestamp.equals(message.timestamp) && 
               sender.equals(message.sender) &&
               content.equals(message.content);
    }
    
    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + sender.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }
}
