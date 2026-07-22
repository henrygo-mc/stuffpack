package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SelectionListener implements Listener {
    
    private final StuffPack plugin;
    
    public SelectionListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (player.getInventory().getItem(EquipmentSlot.HAND) == null) {
            return;
        }
        
        if (player.getInventory().getItem(EquipmentSlot.HAND).getType() != Material.WOODEN_HOE) {
            return;
        }
        
        if (event.getClickedBlock() == null) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos1(player.getName(), event.getClickedBlock().getLocation());
            player.sendMessage("§a已设置点1: " + 
                event.getClickedBlock().getX() + ", " + 
                event.getClickedBlock().getY() + ", " + 
                event.getClickedBlock().getZ());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos2(player.getName(), event.getClickedBlock().getLocation());
            player.sendMessage("§a已设置点2: " + 
                event.getClickedBlock().getX() + ", " + 
                event.getClickedBlock().getY() + ", " + 
                event.getClickedBlock().getZ());
            
            if (plugin.getSelectionManager().hasCompleteSelection(player.getName())) {
                plugin.getSelectionManager().showSelection(player);
                
                int volume = calculateVolume(
                    plugin.getSelectionManager().getPos1(player.getName()),
                    plugin.getSelectionManager().getPos2(player.getName())
                );
                player.sendMessage("§a选择完成! 体积: " + volume + " 方块, 需要 " + volume + " 金币");
                player.sendMessage("§a输入 /dom create <名称> 创建领地");
            }
        }
    }
    
    private int calculateVolume(org.bukkit.Location pos1, org.bukkit.Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
}