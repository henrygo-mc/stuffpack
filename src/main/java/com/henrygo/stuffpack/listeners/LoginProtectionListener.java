package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class LoginProtectionListener implements Listener {
    
    private final StuffPack plugin;
    
    public LoginProtectionListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getLoginManager().isLoggedIn(player.getName())) {
            return;
        }
        
        String command = event.getMessage().toLowerCase();
        
        if (command.startsWith("/login") || 
            command.startsWith("/reg") || 
            command.startsWith("/register")) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "请先登录或注册");
        player.sendMessage(ChatColor.YELLOW + "/reg <密码> <确认密码> - 注册");
        player.sendMessage(ChatColor.YELLOW + "/login <密码> - 登录");
    }
    
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "请先登录或注册");
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            if (event.hasExplicitlyChangedPosition()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            plugin.getLoginManager().applyLoginState(player);
            player.sendMessage("§c请登录账号");
            player.sendMessage("§e/login <密码>");
        }
    }
}