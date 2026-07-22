package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MusicManager {
    
    private final StuffPack plugin;
    private final Map<String, MusicTrack> tracks;
    private final File musicFolder;
    private final File dataFile;
    private final File resourcePackFile;
    private final NamespacedKey musicIdKey;
    private final Map<String, String> playingTracks;
    
    public MusicManager(StuffPack plugin) {
        this.plugin = plugin;
        this.tracks = new HashMap<>();
        this.musicFolder = new File(plugin.getDataFolder(), "music");
        this.dataFile = new File(plugin.getDataFolder(), "music.yml");
        this.resourcePackFile = new File(plugin.getDataFolder(), "resourcepack.zip");
        this.musicIdKey = new NamespacedKey(plugin, "music_id");
        this.playingTracks = new HashMap<>();
        
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }
        
        loadTracks();
        generateResourcePack();
        checkServerProperties();
    }
    
    private void checkServerProperties() {
        try {
            File serverProperties = new File(plugin.getDataFolder().getParentFile().getParentFile(), "server.properties");
            if (!serverProperties.exists()) {
                plugin.getLogger().warning("");
                plugin.getLogger().warning("============================================================");
                plugin.getLogger().warning("    ⚠️  无法找到 server.properties 文件！");
                plugin.getLogger().warning("    资源包需要在 server.properties 中配置");
                plugin.getLogger().warning("============================================================");
                plugin.getLogger().warning("");
                return;
            }
            
            Properties props = new Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(serverProperties)) {
                props.load(fis);
            }
            
            String resourcePack = props.getProperty("resource-pack", "");
            String requireResourcePack = props.getProperty("require-resource-pack", "");
            
            if (resourcePack == null || resourcePack.trim().isEmpty()) {
                plugin.getLogger().warning("");
                plugin.getLogger().warning("============================================================");
                plugin.getLogger().warning("    ⚠️  server.properties 中未配置资源包！");
                plugin.getLogger().warning("============================================================");
                plugin.getLogger().warning("    💡 建议在 server.properties 中添加以下配置：");
                plugin.getLogger().warning("       resource-pack=http://你的公网IP:端口/stuffpack/resourcepack.zip");
                plugin.getLogger().warning("       require-resource-pack=false");
                plugin.getLogger().warning("");
                plugin.getLogger().warning("    📦 资源包已生成: " + resourcePackFile.getName() + " (" + (resourcePackFile.length() / 1024) + " KB)");
                plugin.getLogger().warning("    📁 资源包位置: plugins/StuffPack/resourcepack.zip");
                plugin.getLogger().warning("============================================================");
                plugin.getLogger().warning("");
            } else {
                plugin.getLogger().info("");
                plugin.getLogger().info("============================================================");
                plugin.getLogger().info("    ✅ 资源包配置已检测到！");
                plugin.getLogger().info("============================================================");
                plugin.getLogger().info("    📥 资源包地址: " + resourcePack);
                plugin.getLogger().info("    🔒 强制加载: " + ("true".equalsIgnoreCase(requireResourcePack) ? "是" : "否"));
                plugin.getLogger().info("    📦 资源包大小: " + (resourcePackFile.length() / 1024) + " KB");
                plugin.getLogger().info("    🎵 音乐数量: " + tracks.size() + " 首");
                plugin.getLogger().info("============================================================");
                plugin.getLogger().info("");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检测 server.properties 时出错: " + e.getMessage());
        }
    }
    
    public NamespacedKey getMusicIdKey() {
        return musicIdKey;
    }
    
    public MusicTrack getTrack(String id) {
        return tracks.get(id);
    }
    
    public Collection<MusicTrack> getAllTracks() {
        return tracks.values();
    }
    
    public boolean hasTrack(String id) {
        return tracks.containsKey(id);
    }
    
    public String getPlayingTrack() {
        return playingTracks.get("global");
    }
    
    public void addTrack(String id, String name, File oggFile) {
        File destFile = new File(musicFolder, id + ".ogg");
        if (oggFile != null && oggFile.exists() && !oggFile.equals(destFile)) {
            try {
                java.nio.file.Files.copy(oggFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy music file: " + e.getMessage());
            }
        }
        tracks.put(id, new MusicTrack(id, name));
        saveTracks();
        generateResourcePack();
    }
    
    public void removeTrack(String id) {
        tracks.remove(id);
        File oggFile = new File(musicFolder, id + ".ogg");
        if (oggFile.exists()) {
            oggFile.delete();
        }
        saveTracks();
        generateResourcePack();
    }
    
    public ItemStack createMusicItem(String trackId) {
        if (!tracks.containsKey(trackId)) {
            return null;
        }
        
        MusicTrack track = tracks.get(trackId);
        ItemStack item = new ItemStack(Material.MUSIC_DISC_11);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6" + track.getName());
            meta.setLore(Arrays.asList("§7右键播放/暂停", "§7歌曲ID: " + trackId));
            meta.setCustomModelData(track.hashCode());
            
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(musicIdKey, PersistentDataType.STRING, trackId);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public String getMusicIdFromItem(ItemStack item) {
        if (item == null || item.getType() != Material.MUSIC_DISC_11) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(musicIdKey, PersistentDataType.STRING);
    }
    
    public void togglePlay(Player player, String trackId) {
        String currentTrack = playingTracks.get("global");
        
        if (currentTrack != null && currentTrack.equals(trackId)) {
            stopAllMusic();
            player.sendMessage("§7音乐已暂停");
        } else {
            playMusicToAll(trackId);
        }
    }
    
    public void playMusicToAll(String trackId) {
        if (!tracks.containsKey(trackId)) {
            return;
        }
        
        stopAllMusic();
        
        String soundKey = "stuffpack.music." + trackId;
        NamespacedKey nsKey = new NamespacedKey(NamespacedKey.MINECRAFT, "stuffpack.music." + trackId);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                java.lang.reflect.Method method = Player.class.getMethod("playSound", org.bukkit.Location.class, org.bukkit.NamespacedKey.class, SoundCategory.class, float.class, float.class);
                method.invoke(p, p.getLocation(), nsKey, SoundCategory.MUSIC, 1.0f, 1.0f);
            } catch (Exception e) {
                p.playSound(p.getLocation(), soundKey, SoundCategory.MUSIC, 1.0f, 1.0f);
            }
        }
        
        playingTracks.put("global", trackId);
        
        MusicTrack track = tracks.get(trackId);
        Bukkit.broadcastMessage("§a🎵 正在播放: §6" + track.getName());
    }
    
    public void stopMusic(Player player) {
        if (playingTracks.containsKey("global")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.stopSound(SoundCategory.MUSIC);
            }
            playingTracks.remove("global");
        }
    }
    
    public void stopAllMusic() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(SoundCategory.MUSIC);
        }
        playingTracks.clear();
    }
    
    public void clearResourcePack(Player player) {
        try {
            player.setResourcePack("");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clear resource pack for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    public File getMusicFolder() {
        return musicFolder;
    }
    
    public File getResourcePackFile() {
        return resourcePackFile;
    }
    
    public String getResourcePackUrl() {
        String configUrl = plugin.getConfig().getString("music.resourcepack-url", "");
        if (configUrl != null && !configUrl.isEmpty()) {
            return configUrl;
        }
        
        Plugin webServ = Bukkit.getPluginManager().getPlugin("WebServ");
        if (webServ != null && webServ.isEnabled()) {
            try {
                Object configManager = webServ.getClass().getMethod("getConfigManager").invoke(webServ);
                String host = (String) configManager.getClass().getMethod("getHost").invoke(configManager);
                int port = (int) configManager.getClass().getMethod("getPort").invoke(configManager);
                
                if ("0.0.0.0".equals(host)) {
                    host = "127.0.0.1";
                }
                
                return "http://" + host + ":" + port + "/stuffpack/resourcepack.zip";
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get WebServ URL: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public boolean isAutoSendResourcePack() {
        return plugin.getConfig().getBoolean("music.auto-send-resourcepack", false);
    }
    
    public boolean isForceResourcePack() {
        return plugin.getConfig().getBoolean("music.force-resourcepack", true);
    }
    
    public String getForceResourcePackMessage() {
        return plugin.getConfig().getString("music.force-resourcepack-message", "§c你必须接受资源包才能加入服务器!");
    }
    
    public void sendResourcePack(Player player) {
        String url = getResourcePackUrl();
        if (url == null || url.isEmpty()) {
            return;
        }
        
        try {
            player.setResourcePack(url, getResourcePackHash());
            plugin.getLogger().info("Sent resource pack to " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send resource pack to " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private String getResourcePackHash() {
        if (!resourcePackFile.exists()) {
            return "";
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(resourcePackFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, length);
                }
            }
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to calculate resource pack hash: " + e.getMessage());
            return "";
        }
    }
    
    public void handleResourcePackStatus(Player player, boolean accepted) {
        if (!isForceResourcePack()) {
            return;
        }
        
        if (!accepted) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.kickPlayer(getForceResourcePackMessage());
            });
        }
    }
    
    public void generateResourcePack() {
        try {
            if (resourcePackFile.exists()) {
                resourcePackFile.delete();
            }
            
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resourcePackFile))) {
                addZipEntry(zos, "pack.mcmeta", generatePackMcMeta());
                
                String soundsJson = generateSoundsJson();
                addZipEntry(zos, "assets/minecraft/sounds.json", soundsJson);
                
                for (MusicTrack track : tracks.values()) {
                    File oggFile = new File(musicFolder, track.getId() + ".ogg");
                    if (oggFile.exists()) {
                        addZipFile(zos, "assets/minecraft/sounds/stuffpack/" + track.getId() + ".ogg", oggFile);
                    }
                }
            }
            
            plugin.getLogger().info("Resource pack generated: " + resourcePackFile.getName() + " (" + tracks.size() + " tracks, " + (resourcePackFile.length() / 1024) + " KB)");
            plugin.getLogger().info("pack_format: " + plugin.getConfig().getInt("music.pack-format", 34));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate resource pack: " + e.getMessage());
        }
    }
    
    private String generatePackMcMeta() {
        int packFormat = plugin.getConfig().getInt("music.pack-format", 34);
        return "{\n" +
            "  \"pack\": {\n" +
            "    \"pack_format\": " + packFormat + ",\n" +
            "    \"description\": \"StuffPack Custom Music\"\n" +
            "  }\n" +
            "}";
    }
    
    private String generateSoundsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        
        boolean first = true;
        for (MusicTrack track : tracks.values()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            
            sb.append("  \"stuffpack.music.").append(track.getId()).append("\": {\n");
            sb.append("    \"category\": \"music\",\n");
            sb.append("    \"replace\": true,\n");
            sb.append("    \"sounds\": [\n");
            sb.append("      {\n");
            sb.append("        \"name\": \"stuffpack/").append(track.getId()).append("\",\n");
            sb.append("        \"stream\": true\n");
            sb.append("      }\n");
            sb.append("    ]\n");
            sb.append("  }");
        }
        
        sb.append("\n}");
        return sb.toString();
    }
    
    private void addZipEntry(ZipOutputStream zos, String path, String content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        zos.write(content.getBytes("UTF-8"));
        zos.closeEntry();
    }
    
    private void addZipFile(ZipOutputStream zos, String path, File file) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        
        zos.closeEntry();
    }
    
    private void loadTracks() {
        if (!dataFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            String name = config.getString(key + ".name", key);
            tracks.put(key, new MusicTrack(key, name));
        }
        
        plugin.getLogger().info("Loaded " + tracks.size() + " music tracks");
    }
    
    private void saveTracks() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (MusicTrack track : tracks.values()) {
            config.set(track.getId() + ".name", track.getName());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save music data: " + e.getMessage());
        }
    }
    
    public static class MusicTrack {
        private final String id;
        private String name;
        
        public MusicTrack(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
