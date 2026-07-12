package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Picture;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class PictureListener implements Listener {
    
    private final StuffPack plugin;
    
    public PictureListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        
        ItemFrame frame = (ItemFrame) event.getEntity();
        
        Picture picture = plugin.getPictureManager().getPictureAt(frame.getLocation());
        if (picture == null) {
            return;
        }
        
        if (!picture.isAllowBreak()) {
            event.setCancelled(true);
            
            if (event instanceof HangingBreakByEntityEvent) {
                HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent) event;
                if (entityEvent.getRemover() instanceof Player) {
                    Player player = (Player) entityEvent.getRemover();
                    if (!player.hasPermission("stuffpack.picture.admin")) {
                        player.sendMessage("§c此画像不可破坏");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        
        ItemFrame frame = (ItemFrame) event.getEntity();
        
        Picture picture = plugin.getPictureManager().getPictureAt(frame.getLocation());
        if (picture == null) {
            return;
        }
        
        if (!picture.isAllowBreak()) {
            event.setCancelled(true);
            
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
                if (entityEvent.getDamager() instanceof Player) {
                    Player player = (Player) entityEvent.getDamager();
                    if (!player.hasPermission("stuffpack.picture.admin")) {
                        player.sendMessage("§c此画像不可破坏");
                    }
                }
            }
        }
    }
}