package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PerformanceManager implements Listener {
    
    private final StuffPack plugin;
    private final Map<UUID, Integer> originalViewDistances;
    private final Map<UUID, Integer> originalSimulationDistances;
    private final Map<UUID, Long> lastActivityTime;
    private final Map<UUID, Boolean> afkStatus;
    private final int afkTimeoutSeconds;
    private final int afkViewDistance;
    private final int afkSimulationDistance;
    private final boolean progressiveViewEnabled;
    private final boolean afkOptimizationEnabled;
    private int afkTaskId = -1;

    public PerformanceManager(StuffPack plugin) {
        this.plugin = plugin;
        this.originalViewDistances = new HashMap<>();
        this.originalSimulationDistances = new HashMap<>();
        this.lastActivityTime = new HashMap<>();
        this.afkStatus = new HashMap<>();
        
        this.progressiveViewEnabled = plugin.getConfig().getBoolean("performance.progressiveViewDistance", true);
        this.afkOptimizationEnabled = plugin.getConfig().getBoolean("performance.afkOptimization", true);
        this.afkTimeoutSeconds = plugin.getConfig().getInt("performance.afkTimeout", 120);
        this.afkViewDistance = plugin.getConfig().getInt("performance.afkViewDistance", 4);
        this.afkSimulationDistance = plugin.getConfig().getInt("performance.afkSimulationDistance", 4);
        
        if (afkOptimizationEnabled) {
            startAfkChecker();
        }
    }
    
    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        
        originalViewDistances.put(uuid, player.getViewDistance());
        originalSimulationDistances.put(uuid, player.getSimulationDistance());
        lastActivityTime.put(uuid, System.currentTimeMillis());
        afkStatus.put(uuid, false);
        
        if (progressiveViewEnabled) {
            startProgressiveViewDistance(player);
        }
    }
    
    private void startProgressiveViewDistance(Player player) {
        UUID uuid = player.getUniqueId();
        int originalView = originalViewDistances.getOrDefault(uuid, 10);
        int originalSim = originalSimulationDistances.getOrDefault(uuid, 10);
        
        int startView = Math.min(2, originalView);
        int startSim = Math.min(2, originalSim);
        
        try {
            player.setViewDistance(startView);
            player.setSimulationDistance(startSim);
        } catch (Exception e) {
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            increaseGradually(player, originalView, originalSim);
        }, 40L);
    }
    
    private void increaseGradually(Player player, int targetView, int targetSim) {
        if (!player.isOnline()) return;
        
        int currentView = player.getViewDistance();
        int currentSim = player.getSimulationDistance();
        
        if (currentView < targetView || currentSim < targetSim) {
            int nextView = Math.min(currentView + 1, targetView);
            int nextSim = Math.min(currentSim + 1, targetSim);
            
            try {
                player.setViewDistance(nextView);
                player.setSimulationDistance(nextSim);
            } catch (Exception e) {
            }
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                increaseGradually(player, targetView, targetSim);
            }, 20L);
        }
    }
    
    private void startAfkChecker() {
        afkTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = afkTimeoutSeconds * 1000L;
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getFakePlayerManager().isFakePlayer(player)) {
                    continue;
                }
                
                UUID uuid = player.getUniqueId();
                Long lastActive = lastActivityTime.get(uuid);
                
                if (lastActive == null) {
                    lastActivityTime.put(uuid, now);
                    continue;
                }
                
                boolean isAfk = (now - lastActive) > timeout;
                Boolean wasAfk = afkStatus.get(uuid);
                
                if (wasAfk == null || wasAfk != isAfk) {
                    afkStatus.put(uuid, isAfk);
                    
                    if (isAfk) {
                        applyAfkOptimization(player);
                    } else {
                        removeAfkOptimization(player);
                    }
                }
            }
        }, 200L, 200L);
    }
    
    private void applyAfkOptimization(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!originalViewDistances.containsKey(uuid)) {
            originalViewDistances.put(uuid, player.getViewDistance());
        }
        if (!originalSimulationDistances.containsKey(uuid)) {
            originalSimulationDistances.put(uuid, player.getSimulationDistance());
        }
        
        try {
            player.setViewDistance(afkViewDistance);
            player.setSimulationDistance(afkSimulationDistance);
        } catch (Exception e) {
        }
    }
    
    private void removeAfkOptimization(Player player) {
        UUID uuid = player.getUniqueId();
        
        Integer originalView = originalViewDistances.get(uuid);
        Integer originalSim = originalSimulationDistances.get(uuid);
        
        try {
            if (originalView != null) {
                player.setViewDistance(originalView);
            }
            if (originalSim != null) {
                player.setSimulationDistance(originalSim);
            }
        } catch (Exception e) {
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!afkOptimizationEnabled) return;
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            lastActivityTime.put(uuid, System.currentTimeMillis());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        originalViewDistances.remove(uuid);
        originalSimulationDistances.remove(uuid);
        lastActivityTime.remove(uuid);
        afkStatus.remove(uuid);
    }
    
    public boolean isAfk(Player player) {
        return afkStatus.getOrDefault(player.getUniqueId(), false);
    }
    
    public void disable() {
        if (afkTaskId != -1) {
            Bukkit.getScheduler().cancelTask(afkTaskId);
            afkTaskId = -1;
        }
    }
}
