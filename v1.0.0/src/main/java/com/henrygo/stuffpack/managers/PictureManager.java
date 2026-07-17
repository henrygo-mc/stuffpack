package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Picture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;

public class PictureManager {
    
    private final StuffPack plugin;
    private final Map<String, Picture> pictures;
    private final File dataFile;
    
    public PictureManager(StuffPack plugin) {
        this.plugin = plugin;
        this.pictures = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "pictures.yml");
        loadPictures();
    }
    
    public Picture createPicture(String url, Location location, boolean allowBreak) {
        try {
            URL imageUrl = new URL(url);
            BufferedImage image = ImageIO.read(imageUrl);
            
            if (image == null) {
                return null;
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            int pieces = calculatePieces(width, height);
            
            Picture picture = new Picture(url, location, allowBreak, width, height, pieces);
            pictures.put(picture.getId(), picture);
            
            renderPicture(picture, image);
            savePictures();
            
            return picture;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to download image: " + e.getMessage());
            return null;
        }
    }
    
    private int calculatePieces(int width, int height) {
        int mapSize = 128;
        int cols = (width + mapSize - 1) / mapSize;
        int rows = (height + mapSize - 1) / mapSize;
        return cols * rows;
    }
    
    private void renderPicture(Picture picture, BufferedImage image) {
        int mapSize = 128;
        int cols = (picture.getWidth() + mapSize - 1) / mapSize;
        int rows = (picture.getHeight() + mapSize - 1) / mapSize;
        
        Location loc = picture.getLocation();
        World world = loc.getWorld();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * mapSize;
                int y = row * mapSize;
                
                BufferedImage subImage = image.getSubimage(
                    Math.min(x, image.getWidth() - 1),
                    Math.min(y, image.getHeight() - 1),
                    Math.min(mapSize, image.getWidth() - x),
                    Math.min(mapSize, image.getHeight() - y)
                );
                
                MapView map = Bukkit.createMap(world);
                map.getRenderers().clear();
                map.setScale(MapView.Scale.NORMAL);
                
                MapRenderer renderer = createImageRenderer(subImage);
                map.addRenderer(renderer);
                
                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                meta.setMapView(map);
                mapItem.setItemMeta(meta);
                
                Location frameLoc = loc.add(col, -row, 0);
                ItemFrame frame = (ItemFrame) world.spawnEntity(frameLoc, EntityType.ITEM_FRAME);
                frame.setItem(mapItem);
                frame.setFixed(false);
                
                plugin.getLogger().info("Created picture frame at " + frameLoc);
            }
        }
    }
    
    private MapRenderer createImageRenderer(BufferedImage image) {
        return new MapRenderer() {
            @Override
            public void render(MapView map, org.bukkit.map.MapCanvas canvas, org.bukkit.entity.Player player) {
                int imgWidth = image.getWidth();
                int imgHeight = image.getHeight();
                
                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        int srcX = (x * imgWidth) / 128;
                        int srcY = (y * imgHeight) / 128;
                        
                        if (srcX < imgWidth && srcY < imgHeight) {
                            Color color = new Color(image.getRGB(srcX, srcY));
                            canvas.setPixel(x, y, org.bukkit.map.MapPalette.matchColor(color));
                        }
                    }
                }
            }
        };
    }
    
    public boolean deletePicture(String id) {
        Picture picture = pictures.remove(id);
        if (picture != null) {
            savePictures();
            return true;
        }
        return false;
    }
    
    public Picture getPicture(String id) {
        return pictures.get(id);
    }
    
    public Collection<Picture> getAllPictures() {
        return pictures.values();
    }
    
    public Picture getPictureAt(Location location) {
        for (Picture picture : pictures.values()) {
            Location picLoc = picture.getLocation();
            if (picLoc.getWorld().equals(location.getWorld()) &&
                picLoc.distanceSquared(location) < 25) {
                return picture;
            }
        }
        return null;
    }
    
    private void loadPictures() {
        if (!dataFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : config.getKeys(false)) {
            try {
                String url = config.getString(key + ".url");
                
                String worldName = config.getString(key + ".location.world");
                int x = config.getInt(key + ".location.x");
                int y = config.getInt(key + ".location.y");
                int z = config.getInt(key + ".location.z");
                
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    continue;
                }
                
                Location location = new Location(world, x, y, z);
                boolean allowBreak = config.getBoolean(key + ".allowBreak", true);
                int width = config.getInt(key + ".width", 128);
                int height = config.getInt(key + ".height", 128);
                int pieces = config.getInt(key + ".pieces", 1);
                
                Picture picture = new Picture(url, location, allowBreak, width, height, pieces);
                pictures.put(key, picture);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load picture " + key + ": " + e.getMessage());
            }
        }
    }
    
    public void savePictures() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Picture picture : pictures.values()) {
            String key = picture.getId();
            Location loc = picture.getLocation();
            
            config.set(key + ".url", picture.getUrl());
            config.set(key + ".location.world", loc.getWorld().getName());
            config.set(key + ".location.x", loc.getBlockX());
            config.set(key + ".location.y", loc.getBlockY());
            config.set(key + ".location.z", loc.getBlockZ());
            config.set(key + ".allowBreak", picture.isAllowBreak());
            config.set(key + ".width", picture.getWidth());
            config.set(key + ".height", picture.getHeight());
            config.set(key + ".pieces", picture.getPieces());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save pictures: " + e.getMessage());
        }
    }
}