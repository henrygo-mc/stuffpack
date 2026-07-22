package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerChatListener implements Listener {
    
    private final StuffPack plugin;
    
    public PlayerChatListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName().toLowerCase();
        
        String type = plugin.getDomainManager().getPendingMessageType(playerName);
        if (type != null) {
            String message = event.getMessage();
            event.setCancelled(true);
            
            plugin.getDomainManager().handlePendingMessage(playerName, message);
            
            if ("enter".equals(type)) {
                player.sendMessage("§a进入消息已设置为: " + message);
            } else if ("leave".equals(type)) {
                player.sendMessage("§a离开消息已设置为: " + message);
            }
        }
    }
}