package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import com.henrygo.stuffpack.gui.DomainMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class ChatInputListener implements Listener {
    
    private final StuffPack plugin;
    private final Map<String, String> addingAdminDomain;
    
    public ChatInputListener(StuffPack plugin) {
        this.plugin = plugin;
        this.addingAdminDomain = new HashMap<>();
    }
    
    public void setAddingAdmin(String playerName, String domainId) {
        addingAdminDomain.put(playerName.toLowerCase(), domainId);
    }
    
    public void clearAddingAdmin(String playerName) {
        addingAdminDomain.remove(playerName.toLowerCase());
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName().toLowerCase();
        String domainId = addingAdminDomain.get(playerName);
        
        if (domainId != null) {
            event.setCancelled(true);
            String targetPlayerName = event.getMessage().trim();
            
            Domain domain = plugin.getDomainManager().getDomainById(domainId);
            if (domain == null) {
                player.sendMessage("§c领地不存在");
                clearAddingAdmin(player.getName());
                return;
            }
            
            if (targetPlayerName.isEmpty()) {
                player.sendMessage("§c请输入有效的玩家名称");
                return;
            }
            
            if (targetPlayerName.equalsIgnoreCase(domain.getOwner())) {
                player.sendMessage("§c领地主人不能作为管理员");
                return;
            }
            
            if (domain.isAdmin(targetPlayerName)) {
                player.sendMessage("§c该玩家已是管理员");
                return;
            }
            
            domain.addAdmin(targetPlayerName);
            plugin.getDomainManager().saveDomains();
            player.sendMessage("§a已添加管理员: " + targetPlayerName);
            clearAddingAdmin(player.getName());
            
            DomainMenuGUI gui = new DomainMenuGUI(plugin);
            gui.openAdminManage(player, domain);
        }
    }
}