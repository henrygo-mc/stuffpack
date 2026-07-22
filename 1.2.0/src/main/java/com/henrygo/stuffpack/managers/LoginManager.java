package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginManager {
    
    private final StuffPack plugin;
    private final File dataFile;
    private final Map<String, String> playerPasswords;
    private final Map<String, Location> offlineLocations;
    private final Map<String, Boolean> loggedInPlayers;
    private final Map<String, String> playerIPs;
    private final Map<String, Long> sessionExpiry;
    private long sessionTimeout = 3600000;
    
    public LoginManager(StuffPack plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.playerPasswords = new HashMap<>();
        this.offlineLocations = new HashMap<>();
        this.loggedInPlayers = new HashMap<>();
        this.playerIPs = new HashMap<>();
        this.sessionExpiry = new HashMap<>();
        loadData();
        sessionTimeout = plugin.getConfig().getLong("login.sessionTimeout", 3600000);
    }
    
    public boolean hasValidSession(String playerName, String ip) {
        playerName = playerName.toLowerCase();
        String storedIP = playerIPs.get(playerName);
        Long expiry = sessionExpiry.get(playerName);
        
        if (storedIP == null || expiry == null) {
            return false;
        }
        
        if (!storedIP.equals(ip)) {
            return false;
        }
        
        return System.currentTimeMillis() < expiry;
    }
    
    public void createSession(String playerName, String ip) {
        playerName = playerName.toLowerCase();
        playerIPs.put(playerName, ip);
        sessionExpiry.put(playerName, System.currentTimeMillis() + sessionTimeout);
    }
    
    public void invalidateSession(String playerName) {
        playerName = playerName.toLowerCase();
        sessionExpiry.remove(playerName);
        playerIPs.remove(playerName);
    }
    
    public boolean isRegistered(String playerName) {
        return playerPasswords.containsKey(playerName.toLowerCase());
    }
    
    public boolean isLoggedIn(String playerName) {
        return loggedInPlayers.getOrDefault(playerName.toLowerCase(), false);
    }
    
    public boolean register(String playerName, String password, String confirmPassword) {
        playerName = playerName.toLowerCase();
        
        if (isRegistered(playerName)) {
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            return false;
        }
        
        playerPasswords.put(playerName, hashPassword(password));
        saveData();
        return true;
    }
    
    public boolean login(String playerName, String password, String ip) {
        playerName = playerName.toLowerCase();
        
        if (!isRegistered(playerName)) {
            return false;
        }
        
        String storedHash = playerPasswords.get(playerName);
        if (storedHash == null) {
            return false;
        }
        
        if (!storedHash.equals(hashPassword(password))) {
            return false;
        }
        
        loggedInPlayers.put(playerName, true);
        if (ip != null) {
            createSession(playerName, ip);
        }
        return true;
    }
    
    public boolean login(String playerName, String password) {
        return login(playerName, password, null);
    }
    
    public boolean changePassword(String playerName, String oldPassword, String newPassword, String confirmPassword) {
        playerName = playerName.toLowerCase();
        
        if (!isRegistered(playerName)) {
            return false;
        }
        
        String storedHash = playerPasswords.get(playerName);
        if (!storedHash.equals(hashPassword(oldPassword))) {
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }
        
        playerPasswords.put(playerName, hashPassword(newPassword));
        saveData();
        return true;
    }
    
    public boolean adminChangePassword(String adminName, String playerName, String newPassword) {
        playerName = playerName.toLowerCase();
        
        if (!isRegistered(playerName)) {
            return false;
        }
        
        playerPasswords.put(playerName, hashPassword(newPassword));
        saveData();
        
        Player target = Bukkit.getPlayer(playerName);
        if (target != null) {
            target.kickPlayer(ChatColor.RED + "管理员已重置你的密码，请使用新密码登录");
        }
        
        return true;
    }
    
    public void setOfflineLocation(String playerName, Location location) {
        offlineLocations.put(playerName.toLowerCase(), location);
        saveData();
    }
    
    public Location getOfflineLocation(String playerName) {
        return offlineLocations.get(playerName.toLowerCase());
    }
    
    public void logout(String playerName) {
        loggedInPlayers.remove(playerName.toLowerCase());
    }
    
    public void setLoggedIn(String playerName) {
        loggedInPlayers.put(playerName.toLowerCase(), true);
    }
    
    public void applyLoginState(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 10));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFallDistance(0);
        
        player.sendMessage(ChatColor.RED + "请登录或注册");
        player.sendMessage(ChatColor.YELLOW + "/reg <密码> <确认密码> - 注册");
        player.sendMessage(ChatColor.YELLOW + "/login <密码> - 登录");
    }
    
    public void applyLoggedInState(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        
        Location offlineLoc = getOfflineLocation(player.getName());
        if (offlineLoc != null && offlineLoc.getWorld() != null) {
            player.teleport(offlineLoc);
        } else {
            Location spawn = player.getWorld().getSpawnLocation();
            player.teleport(spawn);
        }
        
        player.sendMessage(ChatColor.GREEN + "登录成功!");
        plugin.getScoreboardManager().showScoreboard(player);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        if (config.getConfigurationSection("passwords") != null) {
            for (String key : config.getConfigurationSection("passwords").getKeys(false)) {
                playerPasswords.put(key.toLowerCase(), config.getString("passwords." + key));
            }
        }
        
        if (config.getConfigurationSection("locations") != null) {
            for (String key : config.getConfigurationSection("locations").getKeys(false)) {
                String worldName = config.getString("locations." + key + ".world");
                double x = config.getDouble("locations." + key + ".x");
                double y = config.getDouble("locations." + key + ".y");
                double z = config.getDouble("locations." + key + ".z");
                float yaw = (float) config.getDouble("locations." + key + ".yaw");
                float pitch = (float) config.getDouble("locations." + key + ".pitch");
                
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    offlineLocations.put(key.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
                }
            }
        }
    }
    
    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, String> entry : playerPasswords.entrySet()) {
            config.set("passwords." + entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<String, Location> entry : offlineLocations.entrySet()) {
            Location loc = entry.getValue();
            config.set("locations." + entry.getKey() + ".world", loc.getWorld().getName());
            config.set("locations." + entry.getKey() + ".x", loc.getX());
            config.set("locations." + entry.getKey() + ".y", loc.getY());
            config.set("locations." + entry.getKey() + ".z", loc.getZ());
            config.set("locations." + entry.getKey() + ".yaw", loc.getYaw());
            config.set("locations." + entry.getKey() + ".pitch", loc.getPitch());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }
}