package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntitySpawnListener implements Listener {
    
    private final StuffPack plugin;
    
    public EntitySpawnListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getLocation());
        
        if (domain != null && !domain.getEnvironment().isMobSpawn()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
            
            if (domain != null && !domain.getEnvironment().isEndermanGrief()) {
                event.setCancelled(true);
            }
        }
    }
}