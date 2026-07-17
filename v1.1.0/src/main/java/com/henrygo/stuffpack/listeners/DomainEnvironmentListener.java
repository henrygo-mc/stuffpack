package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DomainEnvironmentListener implements Listener {
    
    private final StuffPack plugin;
    
    public DomainEnvironmentListener(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
        if (domain != null && !domain.getEnvironment().isFireSpread()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
        if (domain != null) {
            String type = event.getBlock().getType().name();
            if (type.contains("ICE") && !domain.getEnvironment().isIceMelt()) {
                event.setCancelled(true);
            } else if ((type.contains("SNOW") || type.contains("FROSTED")) && !domain.getEnvironment().isSnowMelt()) {
                event.setCancelled(true);
            } else if (type.contains("LEAVES") && !domain.getEnvironment().isLeafDecay()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getToBlock().getLocation());
        if (domain != null) {
            String type = event.getBlock().getType().name();
            if (type.contains("WATER") && !domain.getEnvironment().isWaterFlow()) {
                event.setCancelled(true);
            } else if (type.contains("LAVA") && !domain.getEnvironment().isLavaFlow()) {
                event.setCancelled(true);
            } else if ((type.contains("SAND") || type.contains("GRAVEL") || type.contains("CONCRETE_POWDER")) && !domain.getEnvironment().isGravityBlockFall()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
        if (domain != null) {
            String type = event.getNewState().getType().name();
            if (type.contains("CACTUS") && !domain.getEnvironment().isCactusGrow()) {
                event.setCancelled(true);
            } else if (type.contains("SUGAR_CANE") && !domain.getEnvironment().isSugarcaneGrow()) {
                event.setCancelled(true);
            } else if (type.contains("VINE") && !domain.getEnvironment().isVineSpread()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Domain domain = plugin.getDomainManager().getDomainAt(event.getBlock().getLocation());
        if (domain != null) {
            Entity entity = event.getEntity();
            
            if (entity instanceof Enderman && !domain.getEnvironment().isEndermanGrief()) {
                event.setCancelled(true);
            } else if (entity instanceof org.bukkit.entity.Slime && !domain.getEnvironment().isSlimeDamage()) {
                event.setCancelled(true);
            } else if (!domain.getEnvironment().isMobGrief()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }
        
        Domain domain = plugin.getDomainManager().getDomainAt(event.blockList().get(0).getLocation());
        if (domain != null) {
            Entity entity = event.getEntity();
            
            if (entity instanceof org.bukkit.entity.Creeper && !domain.getEnvironment().isCreeperExplosion()) {
                event.blockList().clear();
            } else if (entity instanceof TNTPrimed) {
                if (!domain.getEnvironment().isTntExplosion()) {
                    event.blockList().clear();
                } else if (!domain.getEnvironment().isTntBlockDamage()) {
                    event.blockList().clear();
                }
            } else if (entity instanceof EnderCrystal && !domain.getEnvironment().isEndCrystalExplosion()) {
                event.blockList().clear();
            } else if (entity instanceof Wither && !domain.getEnvironment().isWitherExplosion()) {
                event.blockList().clear();
            }
        }
    }
}