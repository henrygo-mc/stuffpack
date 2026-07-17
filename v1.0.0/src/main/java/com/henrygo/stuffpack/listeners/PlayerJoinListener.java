package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
    
    private final StuffPack plugin;
    
    public PlayerJoinListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLoginManager().isRegistered(player.getName())) {
            plugin.getLoginManager().applyLoginState(player);
            player.sendMessage("§c欢迎来到服务器!请注册账号");
            player.sendMessage("§e/reg <密码> <确认密码>");
        } else if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            plugin.getLoginManager().applyLoginState(player);
            player.sendMessage("§c请登录账号");
            player.sendMessage("§e/login <密码>");
        }
        
        plugin.getMessageManager().showUnreadMessages(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getLoginManager().isLoggedIn(player.getName())) {
            plugin.getLoginManager().setOfflineLocation(player.getName(), player.getLocation());
            plugin.getLoginManager().logout(player.getName());
        }
    }
}