package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {
    
    private final StuffPack plugin;
    
    public ExplosionListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
        
        if (domain != null && !domain.getEnvironment().isTntExplosion()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }
        
        org.bukkit.Location firstBlock = event.blockList().get(0).getLocation();
        Domain domain = plugin.getDomainManager().getDomainAt(firstBlock);
        
        if (domain != null && !domain.getEnvironment().isCreeperExplosion()) {
            event.blockList().clear();
        }
    }
}