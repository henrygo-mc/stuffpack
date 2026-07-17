package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

public class DataMigrator {

    private final StuffPack plugin;
    private final String[] oldPluginNames = {"DomPlugin", "dom-plugin", "domplugin"};

    public DataMigrator(StuffPack plugin) {
        this.plugin = plugin;
    }

    public void migrate() {
        File newDataFolder = plugin.getDataFolder();
        
        boolean hasNewData = newDataFolder.exists() && newDataFolder.list().length > 0;
        boolean foundOldData = false;
        
        for (String oldName : oldPluginNames) {
            File oldDataFolder = new File(plugin.getDataFolder().getParentFile(), oldName);
            
            if (oldDataFolder.exists() && oldDataFolder.isDirectory()) {
                plugin.getLogger().info("Found old data folder: " + oldName);
                foundOldData = true;
                
                if (hasNewData) {
                    plugin.getLogger().info("New data folder already exists, merging data...");
                    mergeFolder(oldDataFolder, newDataFolder);
                    plugin.getLogger().info("Successfully merged data from " + oldName);
                } else {
                    copyFolder(oldDataFolder, newDataFolder);
                    plugin.getLogger().info("Successfully migrated data from " + oldName);
                }
                return;
            }
        }
        
        if (!foundOldData) {
            plugin.getLogger().info("No old data folder found, starting fresh");
        }
    }

    private void mergeFolder(File source, File destination) {
        if (!destination.exists()) {
            destination.mkdirs();
        }

        File[] files = source.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            File destFile = new File(destination, file.getName());
            
            if (file.isDirectory()) {
                mergeFolder(file, destFile);
            } else {
                if (destFile.exists() && file.getName().endsWith(".yml")) {
                    mergeYamlFile(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }

    private void mergeYamlFile(File sourceFile, File destFile) {
        try {
            YamlConfiguration sourceConfig = YamlConfiguration.loadConfiguration(sourceFile);
            YamlConfiguration destConfig = YamlConfiguration.loadConfiguration(destFile);
            
            Set<String> sourceKeys = sourceConfig.getKeys(false);
            
            for (String key : sourceKeys) {
                if (!destConfig.contains(key)) {
                    destConfig.set(key, sourceConfig.get(key));
                    plugin.getLogger().info("Merged new entry '" + key + "' from " + sourceFile.getName());
                } else {
                    plugin.getLogger().info("Skipped existing entry '" + key + "' in " + sourceFile.getName());
                }
            }
            
            destConfig.save(destFile);
            plugin.getLogger().info("Merged file: " + destFile.getName());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to merge file: " + sourceFile.getName() + " - " + e.getMessage());
        }
    }

    private void copyFolder(File source, File destination) {
        if (!destination.exists()) {
            destination.mkdirs();
        }

        File[] files = source.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            File destFile = new File(destination, file.getName());
            
            if (file.isDirectory()) {
                copyFolder(file, destFile);
            } else {
                copyFile(file, destFile);
            }
        }
    }

    private void copyFile(File source, File destination) {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(destination).getChannel()) {
            
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            plugin.getLogger().info("Migrated file: " + source.getName());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to migrate file: " + source.getName() + " - " + e.getMessage());
        }
    }
}