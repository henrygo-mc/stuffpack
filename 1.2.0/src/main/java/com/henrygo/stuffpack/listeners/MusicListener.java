package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MusicListener implements Listener {

    private final StuffPack plugin;

    public MusicListener(StuffPack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String musicId = plugin.getMusicManager().getMusicIdFromItem(item);
        if (musicId != null) {
            event.setCancelled(true);
            plugin.getMusicManager().togglePlay(player, musicId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMusicManager().stopMusic(event.getPlayer());
    }
}
