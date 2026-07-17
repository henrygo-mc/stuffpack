package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OfflineMessageCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public OfflineMessageCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /offms <player/server> <message>");
            return true;
        }
        
        String receiver = args[0];
        String content = String.join(" ", args).substring(receiver.length() + 1);
        
        String senderName;
        boolean isServerMessage = false;
        
        if (sender instanceof Player) {
            senderName = sender.getName();
        } else {
            senderName = "Server";
            isServerMessage = true;
        }
        
        if (receiver.equalsIgnoreCase("server")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getMessageManager().sendMessage(senderName, player.getName(), content, isServerMessage);
            }
            plugin.getLogger().info(senderName + " sent a message to the server: " + content);
            sender.sendMessage("§aMessage sent to all players");
            return true;
        }
        
        if (Bukkit.getOfflinePlayer(receiver).hasPlayedBefore()) {
            plugin.getMessageManager().sendMessage(senderName, receiver, content, isServerMessage);
            
            Player onlinePlayer = Bukkit.getPlayer(receiver);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                sender.sendMessage("§aMessage sent, player is online, notified immediately!");
            } else {
                sender.sendMessage("§aMessage saved, player will receive it when they come online!");
            }
            
            return true;
        } else {
            sender.sendMessage("§cPlayer does not exist or has never logged in");
            return true;
        }
    }
}