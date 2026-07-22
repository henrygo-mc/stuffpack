package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PrivateDoorListener implements Listener {

    private final StuffPack plugin;

    public PrivateDoorListener(StuffPack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item == null || item.getItemMeta() == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().equals("§d私人门")) {
            Material placedType = event.getBlock().getType();
            
            if (placedType.name().endsWith("DOOR")) {
                plugin.getPrivateDoorManager().addDoor(event.getBlock().getLocation(), player.getName());
                
                org.bukkit.block.Block doorBlock = event.getBlock();
                if (isDoorTop(doorBlock)) {
                    org.bukkit.block.Block bottomDoor = doorBlock.getRelative(org.bukkit.block.BlockFace.DOWN);
                    if (bottomDoor.getType().name().endsWith("DOOR")) {
                        plugin.getPrivateDoorManager().addDoor(bottomDoor.getLocation(), player.getName());
                    }
                } else {
                    org.bukkit.block.Block topDoor = doorBlock.getRelative(org.bukkit.block.BlockFace.UP);
                    if (topDoor.getType().name().endsWith("DOOR")) {
                        plugin.getPrivateDoorManager().addDoor(topDoor.getLocation(), player.getName());
                    }
                }
                
                player.sendMessage("§a私人门已放置，只有你可以打开！");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Material type = event.getClickedBlock().getType();

        if (type.name().endsWith("DOOR")) {
            org.bukkit.block.Block clickedBlock = event.getClickedBlock();
            org.bukkit.block.Block otherHalf = null;
            
            if (isDoorTop(clickedBlock)) {
                otherHalf = clickedBlock.getRelative(org.bukkit.block.BlockFace.DOWN);
            } else {
                otherHalf = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP);
            }
            
            boolean isPrivate = plugin.getPrivateDoorManager().isPrivateDoor(clickedBlock.getLocation());
            String owner = plugin.getPrivateDoorManager().getOwner(clickedBlock.getLocation());
            
            if (!isPrivate && otherHalf != null && otherHalf.getType().name().endsWith("DOOR")) {
                isPrivate = plugin.getPrivateDoorManager().isPrivateDoor(otherHalf.getLocation());
                owner = plugin.getPrivateDoorManager().getOwner(otherHalf.getLocation());
            }
            
            if (isPrivate && owner != null) {
                if (!player.getName().equalsIgnoreCase(owner)) {
                    event.setCancelled(true);
                    player.sendMessage("§c这是私人门，只有放置者可以打开！");
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        if (type.name().endsWith("DOOR")) {
            org.bukkit.block.Block clickedBlock = event.getBlock();
            org.bukkit.block.Block otherHalf = null;
            
            if (isDoorTop(clickedBlock)) {
                otherHalf = clickedBlock.getRelative(org.bukkit.block.BlockFace.DOWN);
            } else {
                otherHalf = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP);
            }
            
            boolean isPrivate = plugin.getPrivateDoorManager().isPrivateDoor(clickedBlock.getLocation());
            String owner = plugin.getPrivateDoorManager().getOwner(clickedBlock.getLocation());
            
            if (!isPrivate && otherHalf != null && otherHalf.getType().name().endsWith("DOOR")) {
                isPrivate = plugin.getPrivateDoorManager().isPrivateDoor(otherHalf.getLocation());
                owner = plugin.getPrivateDoorManager().getOwner(otherHalf.getLocation());
            }
            
            if (isPrivate && owner != null) {
                if (!player.getName().equalsIgnoreCase(owner)) {
                    event.setCancelled(true);
                    player.sendMessage("§c这是私人门，只有放置者可以破坏！");
                    return;
                }

                plugin.getPrivateDoorManager().removeDoor(clickedBlock.getLocation());
                if (otherHalf != null && otherHalf.getType().name().endsWith("DOOR")) {
                    plugin.getPrivateDoorManager().removeDoor(otherHalf.getLocation());
                }
            }
        }
    }

    private boolean isDoorTop(org.bukkit.block.Block block) {
        try {
            Object blockState = block.getState();
            java.lang.reflect.Method getHalfMethod = blockState.getClass().getMethod("getHalf");
            Object half = getHalfMethod.invoke(blockState);
            return half.toString().equals("UPPER");
        } catch (Exception e) {
            return block.getY() % 2 == 1;
        }
    }
}