package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class DomainEnterListener implements Listener {
    
    private final StuffPack plugin;
    private final Map<String, String> playerDomains;
    
    public DomainEnterListener(StuffPack plugin) {
        this.plugin = plugin;
        this.playerDomains = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Domain domain = plugin.getDomainManager().getDomainAt(event.getTo());
        
        if (domain != null) {
            String currentDomainId = playerDomains.get(player.getName().toLowerCase());
            
            if (currentDomainId == null || !currentDomainId.equals(domain.getId())) {
                if (currentDomainId != null) {
                    Domain oldDomain = plugin.getDomainManager().getDomain(currentDomainId);
                    if (oldDomain != null) {
                        String leaveMsg = oldDomain.getLeaveMessage().replace("{name}", oldDomain.getName());
                        player.sendActionBar(leaveMsg);
                    }
                }
                
                playerDomains.put(player.getName().toLowerCase(), domain.getId());
                
                String enterMsg = domain.getEnterMessage().replace("{name}", domain.getName());
                player.sendActionBar(enterMsg);
                
                showDomainBorder(player, domain);
                
                if (!domain.getPermissions().isVisitorMove() && !domain.isOwnerOrAdmin(player.getName())) {
                    event.setCancelled(true);
                    player.sendMessage("§c此领地不允许访客进入");
                    playerDomains.remove(player.getName().toLowerCase());
                    
                    String deniedMsg = "§c此领地不允许访客进入";
                    player.sendActionBar(deniedMsg);
                }
            }
        } else {
            String currentDomainId = playerDomains.get(player.getName().toLowerCase());
            if (currentDomainId != null) {
                Domain oldDomain = plugin.getDomainManager().getDomain(currentDomainId);
                if (oldDomain != null) {
                    String leaveMsg = oldDomain.getLeaveMessage().replace("{name}", oldDomain.getName());
                    player.sendActionBar(leaveMsg);
                }
                playerDomains.remove(player.getName().toLowerCase());
            }
        }
    }
    
    private void showDomainBorder(Player player, Domain domain) {
        Location pos1 = domain.getPos1();
        Location pos2 = domain.getPos2();
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        final int[] loop = {0};
        final org.bukkit.scheduler.BukkitTask[] taskRef = new org.bukkit.scheduler.BukkitTask[1];
        
        taskRef[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (loop[0] >= 3 || !player.isOnline()) {
                return;
            }
            
            for (int x = minX; x <= maxX; x += 2) {
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), x + 0.5, minY, minZ + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), x + 0.5, minY, maxZ + 0.5), 1);
            }
            for (int z = minZ; z <= maxZ; z += 2) {
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), minX + 0.5, minY, z + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), maxX + 0.5, minY, z + 0.5), 1);
            }
            
            for (int x = minX; x <= maxX; x += 2) {
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), x + 0.5, maxY, minZ + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), x + 0.5, maxY, maxZ + 0.5), 1);
            }
            for (int z = minZ; z <= maxZ; z += 2) {
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), minX + 0.5, maxY, z + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), maxX + 0.5, maxY, z + 0.5), 1);
            }
            
            for (int y = minY; y <= maxY; y += 2) {
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), minX + 0.5, y, minZ + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), maxX + 0.5, y, minZ + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), minX + 0.5, y, maxZ + 0.5), 1);
                player.spawnParticle(Particle.CLOUD, new Location(pos1.getWorld(), maxX + 0.5, y, maxZ + 0.5), 1);
            }
            
            loop[0]++;
            if (loop[0] >= 3 || !player.isOnline()) {
                taskRef[0].cancel();
            }
        }, 0, 2);
    }
}