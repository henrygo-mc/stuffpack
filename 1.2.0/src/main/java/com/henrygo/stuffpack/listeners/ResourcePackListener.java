package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.event.Listener;

public class ResourcePackListener implements Listener {

    private final StuffPack plugin;

    public ResourcePackListener(StuffPack plugin) {
        this.plugin = plugin;
    }
}
