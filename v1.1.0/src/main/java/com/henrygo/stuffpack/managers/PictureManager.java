package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Picture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class PictureManager {
    
    private final StuffPack plugin;
    private final Map<String, Picture> pictures;
    private final File dataFile;
    private NamespacedKey pictureIdKey;
    
    public PictureManager(StuffPack plugin) {
        this.plugin = plugin;
        this.pictures = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "pictures.yml");
        this.pictureIdKey = new NamespacedKey(plugin, "picture_id");
        loadPictures();
    }
    
    public NamespacedKey getPictureIdKey() {
        return pictureIdKey;
    }
    
    public void createPictureAsync(String url, Location location, boolean allowBreak, java.util.function.Consumer<Picture> callback, UUID owner) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL imageUrl = new URL(url);
                BufferedImage image = ImageIO.read(imageUrl);
                
                if (image == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
                    return;
                }
                
                int width = image.getWidth();
                int height = image.getHeight();
                int pieces = calculatePieces(width, height);
                
                Picture picture = new Picture(url, location, allowBreak, width, height, pieces, owner);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    pictures.put(picture.getId(), picture);
                    List<ItemStack> mapItems = renderPicture(picture, image);
                    savePictures();
                    
                    Player player = Bukkit.getPlayer(owner);
                    if (player != null && player.isOnline()) {
                        for (ItemStack mapItem : mapItems) {
                            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(mapItem);
                            if (!leftover.isEmpty()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
                            }
                        }
                        player.sendMessage("§a图片地图已放入你的背包!");
                    }
                    
                    callback.accept(picture);
                });
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to download image: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(null));
            }
        });
    }
    
    private int calculatePieces(int width, int height) {
        int mapSize = 128;
        int cols = (width + mapSize - 1) / mapSize;
        int rows = (height + mapSize - 1) / mapSize;
        return cols * rows;
    }
    
    private List<ItemStack> renderPicture(Picture picture, BufferedImage image) {
        int mapSize = 128;
        int cols = (picture.getWidth() + mapSize - 1) / mapSize;
        int rows = (picture.getHeight() + mapSize - 1) / mapSize;
        
        picture.setCols(cols);
        picture.setRows(rows);
        
        Location loc = picture.getLocation();
        World world = loc.getWorld();
        
        List<Integer> mapIds = new ArrayList<>();
        List<ItemStack> mapItems = new ArrayList<>();
        
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
                map.setLocked(true);
                
                MapRenderer renderer = createImageRenderer(subImage);
                map.addRenderer(renderer);
                
                mapIds.add(map.getId());
                
                ItemStack mapItem = createPictureMapItem(map, picture.getId());
                mapItems.add(mapItem);
            }
        }
        
        picture.setMapIds(mapIds);
        return mapItems;
    }
    
    private ItemStack createPictureMapItem(MapView map, String pictureId) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapView(map);
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(pictureIdKey, PersistentDataType.STRING, pictureId);
        
        mapItem.setItemMeta(meta);
        return mapItem;
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
                            int rgb = image.getRGB(srcX, srcY);
                            int alpha = (rgb >> 24) & 0xFF;
                            
                            if (alpha > 128) {
                                Color color = new Color(rgb);
                                canvas.setPixel(x, y, org.bukkit.map.MapPalette.matchColor(color));
                            }
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
    
    public Picture getPictureByMapId(int mapId) {
        for (Picture picture : pictures.values()) {
            if (picture.getMapIds().contains(mapId)) {
                return picture;
            }
        }
        return null;
    }
    
    public String getPictureIdFromItem(ItemStack item) {
        if (item == null || item.getType() != Material.FILLED_MAP) {
            return null;
        }
        
        MapMeta meta = (MapMeta) item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(pictureIdKey, PersistentDataType.STRING);
    }
    
    public void updatePictureLocation(String pictureId, Location newLocation) {
        Picture picture = pictures.get(pictureId);
        if (picture != null) {
            picture.setLocation(newLocation);
            savePictures();
        }
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
                String ownerStr = config.getString(key + ".owner");
                UUID owner = ownerStr != null ? UUID.fromString(ownerStr) : null;
                
                List<Integer> mapIds = (List<Integer>) config.getIntegerList(key + ".mapIds");
                int cols = config.getInt(key + ".cols", 1);
                int rows = config.getInt(key + ".rows", 1);
                
                Picture picture = new Picture(url, location, allowBreak, width, height, pieces, owner);
                picture.setMapIds(mapIds);
                picture.setCols(cols);
                picture.setRows(rows);
                
                pictures.put(key, picture);
                
                restorePictureRenderers(picture);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load picture " + key + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + pictures.size() + " pictures");
    }
    
    private void restorePictureRenderers(Picture picture) {
        String url = picture.getUrl();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL imageUrl = new URL(url);
                BufferedImage image = ImageIO.read(imageUrl);
                
                if (image == null) {
                    return;
                }
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    int mapSize = 128;
                    int cols = picture.getCols();
                    int rows = picture.getRows();
                    List<Integer> mapIds = picture.getMapIds();
                    
                    int index = 0;
                    for (int row = 0; row < rows; row++) {
                        for (int col = 0; col < cols; col++) {
                            if (index >= mapIds.size()) break;
                            
                            int x = col * mapSize;
                            int y = row * mapSize;
                            
                            BufferedImage subImage = image.getSubimage(
                                Math.min(x, image.getWidth() - 1),
                                Math.min(y, image.getHeight() - 1),
                                Math.min(mapSize, image.getWidth() - x),
                                Math.min(mapSize, image.getHeight() - y)
                            );
                            
                            try {
                                MapView map = Bukkit.getMap(mapIds.get(index));
                                if (map != null) {
                                    map.getRenderers().clear();
                                    map.setLocked(true);
                                    MapRenderer renderer = createImageRenderer(subImage);
                                    map.addRenderer(renderer);
                                }
                            } catch (Exception e) {
                                plugin.getLogger().warning("Failed to restore map " + mapIds.get(index) + ": " + e.getMessage());
                            }
                            
                            index++;
                        }
                    }
                    
                    plugin.getLogger().info("Restored renderers for picture: " + picture.getId());
                });
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to reload picture " + picture.getId() + ": " + e.getMessage());
            }
        });
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
            config.set(key + ".cols", picture.getCols());
            config.set(key + ".rows", picture.getRows());
            config.set(key + ".mapIds", picture.getMapIds());
            if (picture.getOwner() != null) {
                config.set(key + ".owner", picture.getOwner().toString());
            }
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save pictures: " + e.getMessage());
        }
    }
}