package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.MessageData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessageManager {
    
    private final StuffPack plugin;
    private final List<MessageData> messages;
    private final File dataFile;
    
    public MessageManager(StuffPack plugin) {
        this.plugin = plugin;
        this.messages = new ArrayList<>();
        this.dataFile = new File(plugin.getDataFolder(), "messages.yml");
        loadMessages();
    }
    
    public void sendMessage(String sender, String receiver, String content, boolean isServerMessage) {
        MessageData message = new MessageData(sender, receiver, content, isServerMessage);
        messages.add(message);
        saveMessages();
        
        Player receiverPlayer = Bukkit.getPlayer(receiver);
        if (receiverPlayer != null && receiverPlayer.isOnline()) {
            showMessageToPlayer(receiverPlayer, message);
        }
    }
    
    public List<MessageData> getUnreadMessages(String playerName) {
        return messages.stream()
                .filter(m -> m.getReceiver().equalsIgnoreCase(playerName) && !m.isRead())
                .collect(Collectors.toList());
    }
    
    public List<MessageData> getAllMessages(String playerName) {
        return messages.stream()
                .filter(m -> m.getReceiver().equalsIgnoreCase(playerName))
                .sorted(Comparator.comparing(MessageData::getTime).reversed())
                .collect(Collectors.toList());
    }
    
    public void markAsRead(String playerName) {
        messages.stream()
                .filter(m -> m.getReceiver().equalsIgnoreCase(playerName) && !m.isRead())
                .forEach(m -> m.setRead(true));
        saveMessages();
    }
    
    public void showUnreadMessages(Player player) {
        List<MessageData> unread = getUnreadMessages(player.getName());
        if (unread.isEmpty()) {
            return;
        }
        
        player.sendMessage("");
        player.sendMessage("§6§l═══════════════════════════════");
        player.sendMessage("§e你有 §c" + unread.size() + " §e条未读留言!");
        player.sendMessage("§6§l═══════════════════════════════");
        
        for (MessageData message : unread) {
            String senderPrefix = message.isServerMessage() ? "§4[服务器]" : "§b[" + message.getSender() + "]";
            player.sendMessage("");
            player.sendMessage(senderPrefix + " §f" + message.getFormattedTime());
            player.sendMessage("§a§l内容: §r" + message.getContent());
        }
        
        player.sendMessage("");
        player.sendMessage("§6使用 /rd 标记为已读");
        player.sendMessage("§6使用 /msread 查看所有留言");
        player.sendMessage("§6§l═══════════════════════════════");
        player.sendMessage("");
    }
    
    public void showMessageToPlayer(Player player, MessageData message) {
        player.sendMessage("");
        player.sendMessage("§6§l═══════════════════════════════");
        String senderPrefix = message.isServerMessage() ? "§4[服务器]" : "§b[" + message.getSender() + "]";
        player.sendMessage(senderPrefix + " §f给你发送了留言!");
        player.sendMessage("§a§l内容: §r" + message.getContent());
        player.sendMessage("§6使用 /rd 标记为已读");
        player.sendMessage("§6§l═══════════════════════════════");
        player.sendMessage("");
    }
    
    public void showAllMessages(Player player) {
        List<MessageData> allMessages = getAllMessages(player.getName());
        if (allMessages.isEmpty()) {
            player.sendMessage("§c你没有任何留言");
            return;
        }
        
        player.sendMessage("");
        player.sendMessage("§6§l═══════════════════════════════");
        player.sendMessage("§e所有留言 (共 " + allMessages.size() + " 条)");
        player.sendMessage("§6§l═══════════════════════════════");
        
        for (MessageData message : allMessages) {
            String senderPrefix = message.isServerMessage() ? "§4[服务器]" : "§b[" + message.getSender() + "]";
            String readStatus = message.isRead() ? "§7(已读)" : "§c(未读)";
            player.sendMessage("");
            player.sendMessage(senderPrefix + " §f" + message.getFormattedTime() + readStatus);
            player.sendMessage("§a内容: §r" + message.getContent());
        }
        
        player.sendMessage("");
        player.sendMessage("§6§l═══════════════════════════════");
        player.sendMessage("");
    }
    
    public void broadcastServerMessages() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<MessageData> unread = getUnreadMessages(player.getName());
            if (!unread.isEmpty()) {
                showUnreadMessages(player);
            }
        }
    }
    
    public List<MessageData> getServerMessages() {
        return messages.stream()
                .filter(m -> m.isServerMessage())
                .sorted(Comparator.comparing(MessageData::getTime).reversed())
                .collect(Collectors.toList());
    }
    
    private void loadMessages() {
        if (!dataFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            try {
                String sender = config.getString(key + ".sender");
                String receiver = config.getString(key + ".receiver");
                String content = config.getString(key + ".content");
                String timeStr = config.getString(key + ".time");
                boolean read = config.getBoolean(key + ".read", false);
                boolean isServerMessage = config.getBoolean(key + ".isServerMessage", false);
                
                MessageData message = new MessageData(sender, receiver, content, isServerMessage);
                message.setRead(read);
                
                if (timeStr != null) {
                    LocalDateTime time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    message.getTime().withNano(0);
                }
                
                messages.add(message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load message " + key + ": " + e.getMessage());
            }
        }
    }
    
    public void saveMessages() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (MessageData message : messages) {
            String key = message.getId();
            config.set(key + ".sender", message.getSender());
            config.set(key + ".receiver", message.getReceiver());
            config.set(key + ".content", message.getContent());
            config.set(key + ".time", message.getFormattedTime());
            config.set(key + ".read", message.isRead());
            config.set(key + ".isServerMessage", message.isServerMessage());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save messages: " + e.getMessage());
        }
    }
}