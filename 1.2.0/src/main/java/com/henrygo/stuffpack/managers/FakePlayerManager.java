package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.*;
import java.util.*;

public class FakePlayerManager {

    private final StuffPack plugin;
    private final Map<String, Player> fakePlayers;
    private final Map<String, String> leftClickCommands;
    private final Set<UUID> fakePlayerUuids;
    private final Map<String, String> fakePlayerSkins;
    private final Map<String, Location> fakePlayerLocations;
    private int lookTaskId = -1;
    private int hideTabTaskId = -1;

    public FakePlayerManager(StuffPack plugin) {
        this.plugin = plugin;
        this.fakePlayers = new HashMap<>();
        this.leftClickCommands = new HashMap<>();
        this.fakePlayerUuids = new HashSet<>();
        this.fakePlayerSkins = new HashMap<>();
        this.fakePlayerLocations = new HashMap<>();
        startLookTask();
        startHideTabTask();
    }
    
    public boolean isFakePlayer(UUID uuid) {
        return fakePlayerUuids.contains(uuid);
    }
    
    public boolean isFakePlayer(String name) {
        return fakePlayers.containsKey(name);
    }
    
    public boolean isFakePlayer(Player player) {
        if (fakePlayerUuids.contains(player.getUniqueId())) {
            return true;
        }
        return fakePlayers.containsValue(player);
    }
    
    private void startHideTabTask() {
        hideTabTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (String name : fakePlayers.keySet()) {
                hideFromTabList(name);
            }
        }, 100L, 600L);
    }
    
    public void setLeftClickCommand(String fakePlayerName, String command) {
        leftClickCommands.put(fakePlayerName, command);
        saveFakePlayers();
    }
    
    public String getLeftClickCommand(String fakePlayerName) {
        return leftClickCommands.get(fakePlayerName);
    }
    
    public boolean executeCommandAsPlayer(Player player, String command) {
        boolean wasOp = player.isOp();
        try {
            if (!wasOp) {
                player.setOp(true);
            }
            return Bukkit.dispatchCommand(player, command);
        } finally {
            if (!wasOp) {
                player.setOp(false);
            }
        }
    }

    private void startLookTask() {
        lookTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player fakePlayer : fakePlayers.values()) {
                Player nearest = findNearestPlayer(fakePlayer, 10);
                if (nearest != null) {
                    lookAtPlayer(fakePlayer, nearest);
                }
            }
        }, 20L, 20L);
    }

    private Player findNearestPlayer(Player fakePlayer, double range) {
        Player nearest = null;
        double nearestDist = range * range;
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(fakePlayer.getUniqueId())) continue;
            if (fakePlayers.containsValue(p)) continue;
            if (!p.getWorld().equals(fakePlayer.getWorld())) continue;
            
            double dist = p.getLocation().distanceSquared(fakePlayer.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        
        return nearest;
    }

    private void lookAtPlayer(Player fakePlayer, Player target) {
        try {
            Location from = fakePlayer.getLocation();
            Location to = target.getLocation();
            
            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double dy = to.getY() + 1.62 - (from.getY() + fakePlayer.getEyeHeight());
            
            double distance = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) -(Math.atan2(dy, distance) * 180.0 / Math.PI);
            
            fakePlayer.setRotation(yaw, pitch);
        } catch (Exception e) {
        }
    }

    public Player spawnFakePlayer(String name, Location location, String skinUrl) {
        if (fakePlayers.containsKey(name)) {
            removeFakePlayer(name);
        }

        try {
            Object craftServer = Bukkit.getServer();
            Method getServerMethod = craftServer.getClass().getMethod("getServer");
            Object minecraftServer = getServerMethod.invoke(craftServer);

            Object serverLevel = getServerLevel(location);

            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            UUID uuid = UUID.randomUUID();
            fakePlayerUuids.add(uuid);
            Object gameProfile = gameProfileClass.getConstructor(UUID.class, String.class)
                    .newInstance(uuid, name);

            if (skinUrl != null && !skinUrl.isEmpty()) {
                setSkinToGameProfile(gameProfile, skinUrl);
            }

            Class<?> clientInfoClass = Class.forName("net.minecraft.server.level.ClientInformation");
            Object clientInfo = clientInfoClass.getMethod("createDefault").invoke(null);

            Class<?> entityPlayerClass = Class.forName("net.minecraft.server.level.ServerPlayer");
            Constructor<?> playerConstructor = entityPlayerClass.getConstructor(
                    Class.forName("net.minecraft.server.MinecraftServer"),
                    Class.forName("net.minecraft.server.level.ServerLevel"),
                    gameProfileClass,
                    clientInfoClass
            );
            Object entityPlayer = playerConstructor.newInstance(minecraftServer, serverLevel, gameProfile, clientInfo);

            setEntityPositionRaw(entityPlayer, entityPlayerClass, location);

            Class<?> connectionClass = Class.forName("net.minecraft.network.Connection");
            Class<?> packetFlowClass = Class.forName("net.minecraft.network.protocol.PacketFlow");
            Object serverBound = null;
            for (Object constant : ((Enum[]) packetFlowClass.getEnumConstants())) {
                if (constant.toString().equals("SERVERBOUND")) {
                    serverBound = constant;
                    break;
                }
            }
            Object connection = connectionClass.getConstructor(packetFlowClass).newInstance(serverBound);

            setupChannel(connection);

            Class<?> cookieClass = Class.forName("net.minecraft.server.network.CommonListenerCookie");
            Object cookie = cookieClass.getMethod("createInitial", gameProfileClass, boolean.class)
                    .invoke(null, gameProfile, false);

            Method getPlayerListMethod = minecraftServer.getClass().getMethod("getPlayerList");
            Object playerList = getPlayerListMethod.invoke(minecraftServer);

            Method placeNewPlayerMethod = playerList.getClass().getMethod("placeNewPlayer",
                    connectionClass,
                    entityPlayerClass,
                    cookieClass);
            
            placeNewPlayerMethod.invoke(playerList, connection, entityPlayer, cookie);

            Player bukkitPlayer = Bukkit.getPlayer(uuid);
            if (bukkitPlayer == null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getUniqueId().equals(uuid)) {
                        bukkitPlayer = p;
                        break;
                    }
                }
            }

            if (bukkitPlayer != null) {
                Location spawnLoc = location.clone();
                spawnLoc.setPitch(0);
                bukkitPlayer.teleport(spawnLoc);
                
                bukkitPlayer.setOp(true);
                
                try {
                    bukkitPlayer.setGameMode(org.bukkit.GameMode.CREATIVE);
                } catch (Exception e) {
                }
                
                try {
                    bukkitPlayer.setInvulnerable(true);
                } catch (Exception e) {
                }
                
                try {
                    setSkinViaPlayerProfile(bukkitPlayer, skinUrl);
                } catch (Exception e) {
                }
                
                fakePlayers.put(name, bukkitPlayer);
                if (skinUrl != null && !skinUrl.isEmpty()) {
                    fakePlayerSkins.put(name, skinUrl);
                }
                fakePlayerLocations.put(name, location);
                log("Spawned fake player: " + name);
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    hideFromTabList(name);
                }, 20L);
                
                return bukkitPlayer;
            } else {
                log("WARNING: Failed to get Bukkit Player for fake player: " + name);
                return null;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("========== FAILED TO SPAWN FAKE PLAYER: " + name + " ==========");
            plugin.getLogger().severe("Error: " + e.getMessage());
            Throwable cause = e;
            int depth = 0;
            while (cause != null && depth < 10) {
                plugin.getLogger().severe("Cause " + depth + ": " + cause.getClass().getName() + ": " + cause.getMessage());
                for (StackTraceElement ste : cause.getStackTrace()) {
                    if (ste.toString().contains("henrygo") || ste.toString().contains("stuffpack")) {
                        plugin.getLogger().severe("  at " + ste);
                    }
                }
                cause = cause.getCause();
                depth++;
            }
            e.printStackTrace();
            plugin.getLogger().severe("==========================================================");
            return null;
        }
    }

    public void hideFromTabList(String name) {
        Player fakePlayer = fakePlayers.get(name);
        if (fakePlayer == null) return;
        
        try {
            Object entityPlayer = getEntityPlayer(fakePlayer);
            Object removePacket = createPlayerInfoRemovePacket(entityPlayer);
            
            if (removePacket == null) {
                return;
            }
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().equals(fakePlayer.getUniqueId())) continue;
                try {
                    sendPacketToPlayer(p, removePacket);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    public void hideAllFromPlayer(Player player) {
        for (Player fakePlayer : fakePlayers.values()) {
            if (player.getUniqueId().equals(fakePlayer.getUniqueId())) continue;
            try {
                Object entityPlayer = getEntityPlayer(fakePlayer);
                Object removePacket = createPlayerInfoRemovePacket(entityPlayer);
                if (removePacket != null) {
                    sendPacketToPlayer(player, removePacket);
                }
            } catch (Exception e) {
            }
        }
    }

    private Object createPlayerInfoRemovePacket(Object entityPlayer) throws Exception {
        Class<?> playerInfoPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
        
        Class<?> actionClass = null;
        Object removePlayerAction = null;
        
        for (Class<?> c : playerInfoPacketClass.getDeclaredClasses()) {
            if (c.isEnum()) {
                for (Object constant : c.getEnumConstants()) {
                    String name = constant.toString();
                    if (name.equals("REMOVE_PLAYER") || name.equals("REMOVE")) {
                        actionClass = c;
                        removePlayerAction = constant;
                        break;
                    }
                }
                if (actionClass != null) break;
            }
        }
        
        if (actionClass == null || removePlayerAction == null) {
            return null;
        }
        
        for (Constructor<?> c : playerInfoPacketClass.getConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length == 2) {
                try {
                    if (params[1].isAssignableFrom(entityPlayer.getClass())) {
                        return c.newInstance(removePlayerAction, entityPlayer);
                    } else if (params[1].isAssignableFrom(List.class) || params[1].isAssignableFrom(Collection.class)) {
                        return c.newInstance(removePlayerAction, Collections.singletonList(entityPlayer));
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        
        return null;
    }

    private void sendPacketToPlayer(Player player, Object packet) throws Exception {
        Object craftPlayer = player;
        Method getHandleMethod = craftPlayer.getClass().getMethod("getHandle");
        Object entityPlayer = getHandleMethod.invoke(craftPlayer);
        
        Field connectionField = findConnectionField(entityPlayer.getClass());
        connectionField.setAccessible(true);
        Object connection = connectionField.get(entityPlayer);
        
        Class<?> connectionClass = connection.getClass();
        Method sendMethod = null;
        for (Method m : connectionClass.getMethods()) {
            if (m.getName().equals("send") && m.getParameterCount() == 1) {
                sendMethod = m;
                break;
            }
        }
        
        if (sendMethod != null) {
            sendMethod.invoke(connection, packet);
        }
    }

    private Field findConnectionField(Class<?> entityPlayerClass) {
        Class<?> current = entityPlayerClass;
        while (current != null) {
            for (Field f : current.getDeclaredFields()) {
                if (f.getType().getName().contains("ServerGamePacketListener") || 
                    f.getType().getName().contains("PacketListener") ||
                    f.getName().equals("connection")) {
                    return f;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private void setEntityPositionRaw(Object entity, Class<?> entityClass, Location location) throws Exception {
        try {
            Method setPosRawMethod = entityClass.getMethod("setPosRaw", double.class, double.class, double.class);
            setPosRawMethod.invoke(entity, location.getX(), location.getY(), location.getZ());
            log("Used setPosRaw(double, double, double)");
        } catch (NoSuchMethodException e) {
            try {
                Method setPosRawMethod = entityClass.getMethod("setPosRaw", double.class, double.class, double.class, boolean.class);
                setPosRawMethod.invoke(entity, location.getX(), location.getY(), location.getZ(), false);
                log("Used setPosRaw(double, double, double, boolean)");
            } catch (NoSuchMethodException e2) {
                log("setPosRaw not found");
            }
        }

        try {
            Method setRotMethod = entityClass.getMethod("setRot", float.class, float.class);
            setRotMethod.invoke(entity, location.getYaw(), location.getPitch());
            log("Used setRot(float, float)");
        } catch (NoSuchMethodException e) {
            log("setRot not found");
        }
    }

    private void setSkinToGameProfile(Object gameProfile, String skinUrl) throws Exception {
        String skinValue = getSkinValue(skinUrl);
        if (skinValue == null) {
            log("Skin value is null, skipping skin setup");
            return;
        }

        log("Trying to set skin to GameProfile...");
        
        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
        Object property = propertyClass.getConstructor(String.class, String.class)
                .newInstance("textures", skinValue);

        Class<?> gameProfileClass = gameProfile.getClass();
        
        Method getPropertiesMethod = null;
        try {
            getPropertiesMethod = gameProfileClass.getMethod("getProperties");
            Object properties = getPropertiesMethod.invoke(gameProfile);
            log("Got properties via getProperties(): " + properties.getClass().getName());
            
            if (tryPutSkin(properties, property)) {
                log("Skin set successfully via getProperties()");
                return;
            }
        } catch (NoSuchMethodException e) {
            log("getProperties() not found");
        }
        
        Field propertiesField = null;
        for (Field f : gameProfileClass.getDeclaredFields()) {
            String typeName = f.getType().getName();
            if (typeName.contains("PropertyMap") || typeName.contains("Multimap") || typeName.contains("properties")) {
                propertiesField = f;
                break;
            }
        }
        if (propertiesField == null) {
            try {
                propertiesField = gameProfileClass.getDeclaredField("properties");
            } catch (NoSuchFieldException e) {
                log("properties field not found by name");
            }
        }

        if (propertiesField != null) {
            propertiesField.setAccessible(true);
            Object properties = propertiesField.get(gameProfile);
            log("Got properties via field: " + properties.getClass().getName());
            
            if (tryPutSkin(properties, property)) {
                log("Skin set successfully via field");
                return;
            }
            
            log("Direct put failed, trying to create new PropertyMap...");
            try {
                Class<?> propertyMapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");
                Constructor<?>[] constructors = propertyMapClass.getDeclaredConstructors();
                log("PropertyMap constructors found: " + constructors.length);
                for (Constructor<?> c : constructors) {
                    log("  Constructor: " + c + ", params: " + c.getParameterCount());
                }
                
                Object newPropertyMap = null;
                
                for (Constructor<?> c : constructors) {
                    try {
                        c.setAccessible(true);
                        Object[] params = new Object[c.getParameterCount()];
                        for (int i = 0; i < params.length; i++) {
                            Class<?> paramType = c.getParameterTypes()[i];
                            if (paramType.isPrimitive()) {
                                if (paramType == boolean.class) params[i] = false;
                                else if (paramType == int.class) params[i] = 0;
                                else if (paramType == long.class) params[i] = 0L;
                                else params[i] = 0;
                            } else {
                                params[i] = null;
                            }
                        }
                        newPropertyMap = c.newInstance(params);
                        log("Created new PropertyMap via constructor: " + c);
                        break;
                    } catch (Exception ex) {
                        log("  Constructor failed: " + ex.getMessage());
                    }
                }
                
                if (newPropertyMap == null) {
                    log("Trying to use Guava HashMultimap...");
                    try {
                        Class<?> hashMultimapClass = Class.forName("com.google.common.collect.HashMultimap");
                        Method createMethod = hashMultimapClass.getMethod("create");
                        newPropertyMap = createMethod.invoke(null);
                        log("Created HashMultimap");
                    } catch (Exception ex) {
                        log("HashMultimap create failed: " + ex.getMessage());
                    }
                }
                
                if (newPropertyMap != null) {
                    if (tryPutSkin(newPropertyMap, property)) {
                        propertiesField.set(gameProfile, newPropertyMap);
                        log("Replaced GameProfile.properties with new map");
                        return;
                    }
                }
            } catch (Exception e) {
                log("Failed to create new PropertyMap: " + e.getMessage());
            }
        }

        plugin.getLogger().warning("[FakePlayer] Could not set skin property - all methods failed");
    }

    private boolean tryPutSkin(Object properties, Object property) {
        Class<?> propClass = properties.getClass();

        for (Method m : propClass.getMethods()) {
            if (m.getName().equals("put") && m.getParameterCount() == 2) {
                try {
                    m.invoke(properties, "textures", property);
                    log("Set skin via put(String, Property)");
                    return true;
                } catch (Exception e) {
                    log("put failed: " + e.getMessage());
                }
            }
        }

        for (Method m : propClass.getMethods()) {
            if (m.getName().equals("putAll") && m.getParameterCount() == 2) {
                try {
                    m.invoke(properties, "textures", Collections.singletonList(property));
                    log("Set skin via putAll(String, Collection)");
                    return true;
                } catch (Exception e) {
                    log("putAll failed: " + e.getMessage());
                }
            }
        }

        return false;
    }

    private void setSkinViaPlayerProfile(Player player, String skinUrl) throws Exception {
        if (skinUrl == null || skinUrl.isEmpty()) return;
        
        log("Trying to set skin via Paper PlayerProfile API...");
        
        try {
            Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Method getProfileMethod = player.getClass().getMethod("getPlayerProfile");
            Object profile = getProfileMethod.invoke(player);
            log("Got PlayerProfile: " + profile.getClass().getName());
            
            Class<?> propertyClass = Class.forName("org.bukkit.profile.PlayerTextures$Skin");
            Method getTexturesMethod = playerProfileClass.getMethod("getTextures");
            Object textures = getTexturesMethod.invoke(profile);
            log("Got textures");
            
            Method setSkinUrlMethod = textures.getClass().getMethod("setSkin", java.net.URL.class);
            setSkinUrlMethod.invoke(textures, new java.net.URL(skinUrl));
            log("Set skin URL via PlayerTextures");
            
            Method setTexturesMethod = playerProfileClass.getMethod("setTextures", textures.getClass());
            setTexturesMethod.invoke(profile, textures);
            log("Updated textures on profile");
            
            Method setPlayerProfileMethod = player.getClass().getMethod("setPlayerProfile", playerProfileClass);
            setPlayerProfileMethod.invoke(player, profile);
            log("Set PlayerProfile on player");
            
        } catch (Exception e) {
            log("PlayerProfile API failed: " + e.getMessage());
            log("  Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
        }
    }

    private Object getServerLevel(Location location) throws Exception {
        Object craftWorld = location.getWorld();
        Class<?> craftWorldClass = craftWorld.getClass();
        Method getHandleMethod = craftWorldClass.getMethod("getHandle");
        return getHandleMethod.invoke(craftWorld);
    }
    
    private Object getEntityPlayer(Player player) throws Exception {
        Method getHandleMethod = player.getClass().getMethod("getHandle");
        return getHandleMethod.invoke(player);
    }

    private String getSkinValue(String skinUrl) {
        try {
            long timestamp = System.currentTimeMillis();
            String json = "{" +
                "\"timestamp\":" + timestamp + "," +
                "\"profileId\":\"" + UUID.randomUUID().toString().replace("-", "") + "\"," +
                "\"profileName\":\"FakePlayer\"," +
                "\"signatureRequired\":false," +
                "\"textures\":{" +
                    "\"SKIN\":{" +
                        "\"url\":\"" + skinUrl + "\"," +
                        "\"metadata\":{\"model\":\"slim\"}" +
                    "}" +
                "}" +
            "}";
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
            return encoder.encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            log("Failed to encode skin value: " + e.getMessage());
            return null;
        }
    }

    private void setupChannel(Object connection) {
        try {
            Class<?> embeddedChannelClass = Class.forName("io.netty.channel.embedded.EmbeddedChannel");
            Object channel = embeddedChannelClass.getDeclaredConstructor().newInstance();
            log("Created EmbeddedChannel");

            Field channelField = null;
            Class<?> current = connection.getClass();
            while (current != null) {
                for (Field f : current.getDeclaredFields()) {
                    if (f.getType().getName().contains("Channel")) {
                        channelField = f;
                        break;
                    }
                }
                if (channelField != null) break;
                current = current.getSuperclass();
            }
            
            if (channelField != null) {
                channelField.setAccessible(true);
                channelField.set(connection, channel);
                log("Set channel field on Connection");
            }
        } catch (Exception e) {
            log("Failed to setup channel: " + e.getMessage());
        }
    }

    private void log(String msg) {
        plugin.getLogger().info("[FakePlayer] " + msg);
    }

    public boolean removeFakePlayer(String name) {
        Player player = fakePlayers.remove(name);
        if (player != null) {
            fakePlayerUuids.remove(player.getUniqueId());
            try {
                player.kickPlayer("");
                log("Removed fake player: " + name);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to remove fake player: " + e.getMessage());
            }
        }
        return false;
    }

    public void removeAllFakePlayers() {
        for (String name : new ArrayList<>(fakePlayers.keySet())) {
            removeFakePlayer(name);
        }
    }

    public Map<String, Player> getAllFakePlayers() {
        return new HashMap<>(fakePlayers);
    }

    public boolean dispatchCommand(String name, String command) {
        Player player = fakePlayers.get(name);
        if (player == null) {
            log("Fake player not found: " + name);
            return false;
        }
        
        try {
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            
            boolean result = Bukkit.dispatchCommand(player, command);
            log("Dispatched command '" + command + "' for " + name + " result: " + result);
            return result;
        } catch (Exception e) {
            log("Failed to dispatch command: " + e.getMessage());
            return false;
        }
    }
    
    public void saveFakePlayers() {
        try {
            org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
            config.set("fakePlayers", null);
            
            int i = 0;
            for (Map.Entry<String, Player> entry : fakePlayers.entrySet()) {
                String name = entry.getKey();
                Player player = entry.getValue();
                Location loc = player.getLocation();
                String skin = fakePlayerSkins.getOrDefault(name, "");
                String cmd = leftClickCommands.getOrDefault(name, "");
                
                String path = "fakePlayers." + i;
                config.set(path + ".name", name);
                config.set(path + ".world", loc.getWorld().getName());
                config.set(path + ".x", loc.getX());
                config.set(path + ".y", loc.getY());
                config.set(path + ".z", loc.getZ());
                config.set(path + ".yaw", (double) loc.getYaw());
                config.set(path + ".pitch", (double) loc.getPitch());
                config.set(path + ".skin", skin);
                config.set(path + ".leftClickCommand", cmd);
                i++;
            }
            
            plugin.saveConfig();
            log("Saved " + i + " fake players");
        } catch (Exception e) {
            log("Failed to save fake players: " + e.getMessage());
        }
    }
    
    public void loadFakePlayers() {
        try {
            org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
            if (!config.contains("fakePlayers")) {
                log("No saved fake players found");
                return;
            }
            
            int count = 0;
            for (String key : config.getConfigurationSection("fakePlayers").getKeys(false)) {
                String path = "fakePlayers." + key;
                String name = config.getString(path + ".name");
                String worldName = config.getString(path + ".world");
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                float yaw = (float) config.getDouble(path + ".yaw");
                float pitch = (float) config.getDouble(path + ".pitch");
                String skin = config.getString(path + ".skin", "");
                String cmd = config.getString(path + ".leftClickCommand", "");
                
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    log("World not found for fake player " + name + ": " + worldName);
                    continue;
                }
                
                Location loc = new Location(world, x, y, z, yaw, pitch);
                fakePlayerSkins.put(name, skin);
                fakePlayerLocations.put(name, loc);
                if (cmd != null && !cmd.isEmpty()) {
                    leftClickCommands.put(name, cmd);
                }
                
                Player player = spawnFakePlayer(name, loc, skin);
                if (player != null) {
                    count++;
                }
            }
            
            log("Loaded " + count + " fake players");
        } catch (Exception e) {
            log("Failed to load fake players: " + e.getMessage());
        }
    }
}