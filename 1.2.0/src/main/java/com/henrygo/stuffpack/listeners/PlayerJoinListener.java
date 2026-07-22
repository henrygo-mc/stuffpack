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
        
        if (plugin.isFakePlayerEnabled() && plugin.getFakePlayerManager().isFakePlayer(player)) {
            return;
        }
        
        if (plugin.isFakePlayerEnabled()) {
            plugin.getFakePlayerManager().hideAllFromPlayer(player);
        }
        
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "";
        boolean hasValidSession = plugin.isLoginEnabled() && plugin.getLoginManager().hasValidSession(player.getName(), ip);
        
        if (plugin.isLoginEnabled()) {
            if (hasValidSession) {
                plugin.getLoginManager().setLoggedIn(player.getName());
                if (plugin.isPerformanceEnabled()) {
                    plugin.getPerformanceManager().onPlayerJoin(player);
                }
                player.sendMessage("§a会话有效，自动登录!");
                if (plugin.isCoinEnabled()) {
                    plugin.getScoreboardManager().showScoreboard(player);
                }
                if (plugin.isMessageEnabled()) {
                    plugin.getMessageManager().showUnreadMessages(player);
                }
                return;
            }
            
            if (!plugin.getLoginManager().isRegistered(player.getName())) {
                plugin.getLoginManager().applyLoginState(player);
                player.sendMessage("§c欢迎来到服务器!请注册账号");
                player.sendMessage("§e/reg <密码> <确认密码>");
            } else if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
                plugin.getLoginManager().applyLoginState(player);
                player.sendMessage("§c请登录账号");
                player.sendMessage("§e/login <密码>");
            }
            
            if (plugin.isMessageEnabled()) {
                plugin.getMessageManager().showUnreadMessages(player);
            }
        } else {
            if (plugin.isPerformanceEnabled()) {
                plugin.getPerformanceManager().onPlayerJoin(player);
            }
            if (plugin.isCoinEnabled()) {
                plugin.getScoreboardManager().showScoreboard(player);
            }
            if (plugin.isMessageEnabled()) {
                plugin.getMessageManager().showUnreadMessages(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.isFakePlayerEnabled() && plugin.getFakePlayerManager().isFakePlayer(player)) {
            return;
        }
        
        if (plugin.isLoginEnabled()) {
            if (plugin.getLoginManager().isLoggedIn(player.getName())) {
                plugin.getLoginManager().setOfflineLocation(player.getName(), player.getLocation());
                plugin.getLoginManager().logout(player.getName());
            }
        }
        
        if (plugin.isMusicEnabled()) {
            plugin.getMusicManager().stopMusic(player);
        }
    }
}
