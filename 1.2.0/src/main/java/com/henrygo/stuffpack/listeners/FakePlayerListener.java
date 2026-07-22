package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.gui.FakePlayerGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class FakePlayerListener implements Listener {

    private final StuffPack plugin;
    private final FakePlayerGUI gui;

    public FakePlayerListener(StuffPack plugin) {
        this.plugin = plugin;
        this.gui = new FakePlayerGUI(plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player clickedPlayer = (Player) event.getRightClicked();
        
        if (!plugin.getFakePlayerManager().isFakePlayer(clickedPlayer)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        
        if (player.isOp()) {
            gui.openAdminMenu(player, clickedPlayer.getName());
        } else {
            String cmd = plugin.getFakePlayerManager().getLeftClickCommand(clickedPlayer.getName());
            if (cmd != null && !cmd.isEmpty()) {
                plugin.getFakePlayerManager().executeCommandAsPlayer(player, cmd);
            } else {
                gui.openNormalMenu(player, clickedPlayer.getName());
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player clickedPlayer = (Player) event.getEntity();
        
        if (!plugin.getFakePlayerManager().isFakePlayer(clickedPlayer)) {
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getDamager();
        
        if (player.isOp()) {
            gui.openAdminMenu(player, clickedPlayer.getName());
        } else {
            String cmd = plugin.getFakePlayerManager().getLeftClickCommand(clickedPlayer.getName());
            if (cmd != null && !cmd.isEmpty()) {
                plugin.getFakePlayerManager().executeCommandAsPlayer(player, cmd);
            } else {
                gui.openNormalMenu(player, clickedPlayer.getName());
            }
        }
    }
}