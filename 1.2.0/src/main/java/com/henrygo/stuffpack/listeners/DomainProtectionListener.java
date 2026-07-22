package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DomainProtectionListener implements Listener {
    
    private final StuffPack plugin;
    
    public DomainProtectionListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getDomainManager().canBuild(player.getName(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§c你没有权限破坏此处方块");
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getDomainManager().canBuild(player.getName(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§c你没有权限在此处放置方块");
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        org.bukkit.Material type = event.getClickedBlock().getType();
        
        if (isContainer(type)) {
            if (!plugin.getDomainManager().canOpenChest(player.getName(), event.getClickedBlock().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§c你没有权限打开此容器");
            }
        } else if (isUseable(type)) {
            if (!plugin.getDomainManager().canUse(player.getName(), event.getClickedBlock().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§c你没有权限使用此物品");
            }
        }
    }
    
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getDomainManager().canBuild(player.getName(), event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§c你没有权限在此处放置液体");
        }
    }
    
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getDomainManager().canBuild(player.getName(), event.getBlockClicked().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§c你没有权限在此处使用桶");
        }
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        org.bukkit.entity.Entity remover = null;
        try {
            java.lang.reflect.Method method = event.getClass().getMethod("getRemover");
            remover = (org.bukkit.entity.Entity) method.invoke(event);
        } catch (Exception e) {
            try {
                java.lang.reflect.Method method = event.getClass().getMethod("getRemoverEntity");
                remover = (org.bukkit.entity.Entity) method.invoke(event);
            } catch (Exception ex) {
                return;
            }
        }
        
        if (remover instanceof Player) {
            Player player = (Player) remover;
            
            if (!plugin.getDomainManager().canBuild(player.getName(), event.getEntity().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§c你没有权限破坏此实体");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            
            Domain domain = plugin.getDomainManager().getDomainAt(event.getEntity().getLocation());
            if (domain != null) {
                if (!domain.isOwnerOrAdmin(player.getName()) && !domain.getPermissions().isVisitorPvp()) {
                    event.setCancelled(true);
                    player.sendMessage("§c此领地不允许PVP");
                }
            }
        }
    }
    
    private boolean isContainer(org.bukkit.Material type) {
        return type == org.bukkit.Material.CHEST ||
               type == org.bukkit.Material.TRAPPED_CHEST ||
               type == org.bukkit.Material.DISPENSER ||
               type == org.bukkit.Material.DROPPER ||
               type == org.bukkit.Material.FURNACE ||
               type == org.bukkit.Material.BLAST_FURNACE ||
               type == org.bukkit.Material.SMOKER ||
               type == org.bukkit.Material.HOPPER ||
               type == org.bukkit.Material.BARREL ||
               type == org.bukkit.Material.SHULKER_BOX ||
               type.name().endsWith("SHULKER_BOX");
    }
    
    private boolean isUseable(org.bukkit.Material type) {
        return type == org.bukkit.Material.LEVER ||
               type.name().endsWith("BUTTON") ||
               type.name().endsWith("DOOR") ||
               type.name().endsWith("TRAPDOOR") ||
               type.name().endsWith("FENCE_GATE") ||
               type == org.bukkit.Material.COMMAND_BLOCK ||
               type == org.bukkit.Material.REPEATING_COMMAND_BLOCK ||
               type == org.bukkit.Material.CHAIN_COMMAND_BLOCK ||
               type == org.bukkit.Material.NOTE_BLOCK ||
               type == org.bukkit.Material.JUKEBOX ||
               type == org.bukkit.Material.BEACON ||
               type == org.bukkit.Material.HOPPER ||
               type == org.bukkit.Material.DISPENSER ||
               type == org.bukkit.Material.DROPPER;
    }
}