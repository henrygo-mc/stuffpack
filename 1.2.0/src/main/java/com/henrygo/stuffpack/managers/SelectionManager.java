package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SelectionManager {
    
    private final StuffPack plugin;
    private final Map<String, Location> pos1;
    private final Map<String, Location> pos2;
    
    public SelectionManager(StuffPack plugin) {
        this.plugin = plugin;
        this.pos1 = new HashMap<>();
        this.pos2 = new HashMap<>();
    }
    
    public void setPos1(String playerName, Location location) {
        pos1.put(playerName.toLowerCase(), location);
    }
    
    public void setPos2(String playerName, Location location) {
        pos2.put(playerName.toLowerCase(), location);
    }
    
    public Location getPos1(String playerName) {
        return pos1.get(playerName.toLowerCase());
    }
    
    public Location getPos2(String playerName) {
        return pos2.get(playerName.toLowerCase());
    }
    
    public boolean hasCompleteSelection(String playerName) {
        return pos1.containsKey(playerName.toLowerCase()) && pos2.containsKey(playerName.toLowerCase());
    }
    
    public boolean hasSameWorld(String playerName) {
        Location p1 = pos1.get(playerName.toLowerCase());
        Location p2 = pos2.get(playerName.toLowerCase());
        return p1 != null && p2 != null && p1.getWorld() != null && p1.getWorld().equals(p2.getWorld());
    }
    
    public void clearSelection(String playerName) {
        pos1.remove(playerName.toLowerCase());
        pos2.remove(playerName.toLowerCase());
    }
    
    public void showSelection(Player player) {
        String playerName = player.getName();
        Location p1 = pos1.get(playerName.toLowerCase());
        Location p2 = pos2.get(playerName.toLowerCase());
        
        if (p1 == null || p2 == null) {
            return;
        }
        
        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                sendBlockEffect(player, new Location(p1.getWorld(), x, minY, z));
                sendBlockEffect(player, new Location(p1.getWorld(), x, maxY, z));
            }
        }
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                sendBlockEffect(player, new Location(p1.getWorld(), x, y, minZ));
                sendBlockEffect(player, new Location(p1.getWorld(), x, y, maxZ));
            }
        }
        
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                sendBlockEffect(player, new Location(p1.getWorld(), minX, y, z));
                sendBlockEffect(player, new Location(p1.getWorld(), maxX, y, z));
            }
        }
    }
    
    private void sendBlockEffect(Player player, Location location) {
        player.sendBlockChange(location, org.bukkit.Material.GLASS.createBlockData());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }, 20L);
    }
}