package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CoinEconomy {
    
    private final StuffPack plugin;
    private final Map<String, Long> coinBalances;
    private final File dataFile;
    private final long defaultCoins;
    
    public CoinEconomy(StuffPack plugin) {
        this.plugin = plugin;
        this.coinBalances = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "coins.yml");
        this.defaultCoins = plugin.getConfig().getLong("default_coins", 1000);
        loadCoins();
    }
    
    public long getBalance(String playerName) {
        return coinBalances.getOrDefault(playerName.toLowerCase(), 0L);
    }
    
    public void setBalance(String playerName, long amount) {
        coinBalances.put(playerName.toLowerCase(), amount);
    }
    
    public boolean addCoins(String playerName, long amount) {
        if (amount < 0) {
            return false;
        }
        coinBalances.put(playerName.toLowerCase(), getBalance(playerName) + amount);
        return true;
    }
    
    public boolean removeCoins(String playerName, long amount) {
        if (amount < 0) {
            return false;
        }
        long balance = getBalance(playerName);
        if (balance < amount) {
            return false;
        }
        coinBalances.put(playerName.toLowerCase(), balance - amount);
        return true;
    }
    
    public boolean transferCoins(String from, String to, long amount) {
        if (amount <= 0) {
            return false;
        }
        if (from.equalsIgnoreCase(to)) {
            return false;
        }
        if (getBalance(from) < amount) {
            return false;
        }
        removeCoins(from, amount);
        addCoins(to, amount);
        return true;
    }
    
    private void loadCoins() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            long balance = config.getLong(key);
            coinBalances.put(key.toLowerCase(), balance);
        }
    }
    
    public int getRank(String playerName) {
        long balance = getBalance(playerName);
        int rank = 1;
        
        for (Map.Entry<String, Long> entry : coinBalances.entrySet()) {
            if (entry.getValue() > balance) {
                rank++;
            }
        }
        
        return rank;
    }
    
    public void saveCoins() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Long> entry : coinBalances.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save coins: " + e.getMessage());
        }
    }
}