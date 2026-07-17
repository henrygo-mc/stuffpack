package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CoinEconomy {
    
    private final StuffPack plugin;
    private final Map<String, Long> coinBalances;
    private final Map<String, Integer> dailySignCount;
    private final File dataFile;
    private final File signDataFile;
    private final long defaultCoins;
    private final long signReward;
    private final int dailySignLimit;
    private String currentDate;
    
    public CoinEconomy(StuffPack plugin) {
        this.plugin = plugin;
        this.coinBalances = new HashMap<>();
        this.dailySignCount = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "coins.yml");
        this.signDataFile = new File(plugin.getDataFolder(), "signdata.yml");
        this.defaultCoins = plugin.getConfig().getLong("default_coins", 1000);
        this.signReward = plugin.getConfig().getLong("sign.reward", 100);
        this.dailySignLimit = plugin.getConfig().getInt("sign.dailyLimit", 1);
        this.currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        loadCoins();
        loadSignData();
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
    
    public int getTodaySignCount(String playerName) {
        checkDateChange();
        return dailySignCount.getOrDefault(playerName.toLowerCase(), 0);
    }
    
    public boolean canSign(String playerName) {
        return getTodaySignCount(playerName) < dailySignLimit;
    }
    
    public long doSign(String playerName) {
        playerName = playerName.toLowerCase();
        checkDateChange();
        
        int count = dailySignCount.getOrDefault(playerName, 0);
        if (count >= dailySignLimit) {
            return -1;
        }
        
        addCoins(playerName, signReward);
        dailySignCount.put(playerName, count + 1);
        saveSignData();
        saveCoins();
        return signReward;
    }
    
    public long getSignReward() {
        return signReward;
    }
    
    public int getDailySignLimit() {
        return dailySignLimit;
    }
    
    private void checkDateChange() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!today.equals(currentDate)) {
            currentDate = today;
            dailySignCount.clear();
            saveSignData();
        }
    }
    
    private void loadSignData() {
        if (!signDataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(signDataFile);
        String savedDate = config.getString("date", "");
        if (savedDate.equals(currentDate)) {
            for (String key : config.getConfigurationSection("players").getKeys(false)) {
                int count = config.getInt("players." + key);
                dailySignCount.put(key.toLowerCase(), count);
            }
        }
    }
    
    public void saveSignData() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("date", currentDate);
        for (Map.Entry<String, Integer> entry : dailySignCount.entrySet()) {
            config.set("players." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(signDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save sign data: " + e.getMessage());
        }
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