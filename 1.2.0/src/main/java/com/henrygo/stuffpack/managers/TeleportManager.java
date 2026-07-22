package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TeleportManager {
    
    private final StuffPack plugin;
    private final Map<String, TeleportRequest> pendingRequests;
    private final Map<String, Location> previousLocations;
    private final long requestTimeout;
    
    public TeleportManager(StuffPack plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.previousLocations = new HashMap<>();
        this.requestTimeout = plugin.getConfig().getLong("teleport_request_timeout", 60) * 20L;
    }
    
    public void sendTpaRequest(String from, String to) {
        pendingRequests.put(to.toLowerCase(), new TeleportRequest(from, to, TeleportRequest.Type.TPA));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(to.toLowerCase());
        }, requestTimeout);
    }
    
    public void sendTpahereRequest(String from, String to) {
        pendingRequests.put(to.toLowerCase(), new TeleportRequest(from, to, TeleportRequest.Type.TPAHERE));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(to.toLowerCase());
        }, requestTimeout);
    }
    
    public TeleportRequest getPendingRequest(String player) {
        return pendingRequests.get(player.toLowerCase());
    }
    
    public void acceptRequest(String player) {
        TeleportRequest request = pendingRequests.remove(player.toLowerCase());
        if (request == null) {
            return;
        }
        
        Player requester = plugin.getServer().getPlayer(request.getFrom());
        Player target = plugin.getServer().getPlayer(request.getTo());
        
        if (requester == null || target == null) {
            return;
        }
        
        if (request.getType() == TeleportRequest.Type.TPA) {
            savePreviousLocation(requester.getName(), requester.getLocation());
            requester.teleport(target.getLocation());
        } else {
            savePreviousLocation(target.getName(), target.getLocation());
            target.teleport(requester.getLocation());
        }
    }
    
    public void ignoreRequest(String player) {
        pendingRequests.remove(player.toLowerCase());
    }
    
    public void savePreviousLocation(String player, Location location) {
        previousLocations.put(player.toLowerCase(), location);
    }
    
    public Location getPreviousLocation(String player) {
        return previousLocations.get(player.toLowerCase());
    }
    
    public boolean teleportBack(String player) {
        Location location = previousLocations.get(player.toLowerCase());
        if (location == null) {
            return false;
        }
        
        Player p = plugin.getServer().getPlayer(player);
        if (p == null) {
            return false;
        }
        
        p.teleport(location);
        previousLocations.remove(player.toLowerCase());
        return true;
    }
    
    public static class TeleportRequest {
        private final String from;
        private final String to;
        private final Type type;
        
        public TeleportRequest(String from, String to, Type type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }
        
        public String getFrom() {
            return from;
        }
        
        public String getTo() {
            return to;
        }
        
        public Type getType() {
            return type;
        }
        
        public enum Type {
            TPA,
            TPAHERE
        }
    }
}