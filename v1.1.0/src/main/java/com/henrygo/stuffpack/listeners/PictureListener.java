package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class PictureListener implements Listener {

    private final StuffPack plugin;

    public PictureListener(StuffPack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }

        ItemFrame frame = (ItemFrame) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.FILLED_MAP) {
            String pictureId = plugin.getPictureManager().getPictureIdFromItem(item);
            if (pictureId != null) {
                plugin.getPictureManager().updatePictureLocation(pictureId, frame.getLocation());
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }

        ItemFrame frame = (ItemFrame) event.getEntity();
        ItemStack item = frame.getItem();

        if (item != null && item.getType() == Material.FILLED_MAP) {
            String pictureId = plugin.getPictureManager().getPictureIdFromItem(item);
            if (pictureId != null) {
                plugin.getPictureManager().updatePictureLocation(pictureId, null);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        ItemFrame frame = (ItemFrame) event.getEntity();
        ItemStack item = frame.getItem();

        if (item != null && item.getType() == Material.FILLED_MAP) {
            String pictureId = plugin.getPictureManager().getPictureIdFromItem(item);
            if (pictureId != null) {
                plugin.getPictureManager().updatePictureLocation(pictureId, null);
            }
        }
    }
}