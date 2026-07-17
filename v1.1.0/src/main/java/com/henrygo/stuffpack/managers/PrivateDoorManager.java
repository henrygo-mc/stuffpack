package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrivateDoorManager {

    private final StuffPack plugin;
    private final Map<String, String> doorOwners;
    private final File dataFile;

    public PrivateDoorManager(StuffPack plugin) {
        this.plugin = plugin;
        this.doorOwners = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "privatedoors.yml");
        loadDoors();
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length != 4) return null;
        return new Location(
            Bukkit.getWorld(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3])
        );
    }

    public void addDoor(Location loc, String playerName) {
        doorOwners.put(locationToString(loc), playerName.toLowerCase());
        saveDoors();
    }

    public void removeDoor(Location loc) {
        doorOwners.remove(locationToString(loc));
        saveDoors();
    }

    public String getOwner(Location loc) {
        return doorOwners.get(locationToString(loc));
    }

    public boolean isPrivateDoor(Location loc) {
        return doorOwners.containsKey(locationToString(loc));
    }

    public boolean canOpen(Location loc, String playerName) {
        String owner = getOwner(loc);
        if (owner == null) return true;
        return owner.equalsIgnoreCase(playerName);
    }

    private void loadDoors() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            doorOwners.put(key, config.getString(key));
        }
    }

    public void saveDoors() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, String> entry : doorOwners.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save private doors: " + e.getMessage());
        }
    }
}