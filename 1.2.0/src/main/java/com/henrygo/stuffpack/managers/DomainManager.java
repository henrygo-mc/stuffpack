package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DomainManager {
    
    private final StuffPack plugin;
    private final Map<String, Domain> domains;
    private final File dataFile;
    private final Map<String, List<Domain>> playerDomains;
    private final Map<Integer, Domain> domainsByCode;
    private int nextCode;
    private final Map<String, String> pendingMessageTypes;
    private final Map<String, String> pendingDomainIds;
    
    public DomainManager(StuffPack plugin) {
        this.plugin = plugin;
        this.domains = new HashMap<>();
        this.playerDomains = new HashMap<>();
        this.domainsByCode = new HashMap<>();
        this.nextCode = 1;
        this.pendingMessageTypes = new HashMap<>();
        this.pendingDomainIds = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "domains.yml");
        loadDomains();
    }
    
    public Domain createDomain(String owner, Location pos1, Location pos2) {
        String id = UUID.randomUUID().toString();
        Domain domain = new Domain(id, nextCode++, owner, pos1, pos2);
        domains.put(id, domain);
        domainsByCode.put(domain.getCode(), domain);
        
        playerDomains.computeIfAbsent(owner.toLowerCase(), k -> new ArrayList<>()).add(domain);
        saveDomains();
        return domain;
    }
    
    public boolean deleteDomain(String id) {
        Domain domain = domains.remove(id);
        if (domain != null) {
            domainsByCode.remove(domain.getCode());
            
            List<Domain> list = playerDomains.get(domain.getOwner());
            if (list != null) {
                list.remove(domain);
            }
            saveDomains();
            return true;
        }
        return false;
    }
    
    public Domain getDomain(String id) {
        return domains.get(id);
    }
    
    public Domain getDomainById(String id) {
        return domains.get(id);
    }
    
    public Domain getDomainByCode(int code) {
        return domainsByCode.get(code);
    }
    
    public Domain getDomainAt(Location location) {
        for (Domain domain : domains.values()) {
            if (domain.contains(location)) {
                return domain;
            }
        }
        return null;
    }
    
    public List<Domain> getDomainsByOwner(String owner) {
        return playerDomains.getOrDefault(owner.toLowerCase(), Collections.emptyList());
    }
    
    public Collection<Domain> getAllDomains() {
        return domains.values();
    }
    
    public boolean canBuild(String playerName, Location location) {
        Domain domain = getDomainAt(location);
        if (domain == null) {
            return true;
        }
        if (domain.isOwner(playerName)) {
            return true;
        }
        if (domain.isAdmin(playerName)) {
            return domain.getAdminPermissions(playerName).isCanBuild();
        }
        return false;
    }
    
    public boolean canUse(String playerName, Location location) {
        Domain domain = getDomainAt(location);
        if (domain == null) {
            return true;
        }
        if (domain.isOwner(playerName)) {
            return true;
        }
        if (domain.isAdmin(playerName)) {
            return domain.getAdminPermissions(playerName).isCanBuild();
        }
        return domain.getPermissions().isVisitorUse();
    }
    
    public boolean canOpenChest(String playerName, Location location) {
        Domain domain = getDomainAt(location);
        if (domain == null) {
            return true;
        }
        if (domain.isOwner(playerName)) {
            return true;
        }
        if (domain.isAdmin(playerName)) {
            return domain.getAdminPermissions(playerName).isCanBuild();
        }
        return domain.getPermissions().isVisitorOpenChest();
    }
    
    public boolean canMove(String playerName, Location location) {
        Domain domain = getDomainAt(location);
        if (domain == null) {
            return true;
        }
        if (domain.isOwner(playerName)) {
            return true;
        }
        if (domain.isAdmin(playerName)) {
            return true;
        }
        return domain.getPermissions().isVisitorMove();
    }
    
    private void loadDomains() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        for (String id : config.getKeys(false)) {
            try {
                String owner = config.getString(id + ".owner");
                if (owner == null) continue;
                
                World world = plugin.getServer().getWorld(config.getString(id + ".world"));
                if (world == null) continue;
                
                Location pos1 = new Location(
                    world,
                    config.getInt(id + ".pos1.x"),
                    config.getInt(id + ".pos1.y"),
                    config.getInt(id + ".pos1.z")
                );
                Location pos2 = new Location(
                    world,
                    config.getInt(id + ".pos2.x"),
                    config.getInt(id + ".pos2.y"),
                    config.getInt(id + ".pos2.z")
                );
                
                int code = config.getInt(id + ".code", 0);
                if (code <= 0) {
                    code = nextCode++;
                }
                Domain domain = new Domain(id, code, owner, pos1, pos2);
                domain.setName(config.getString(id + ".name", domain.getName()));
                domain.setAllowTeleport(config.getBoolean(id + ".allowTeleport", true));
                
                List<String> admins = config.getStringList(id + ".admins");
                for (String admin : admins) {
                    domain.addAdmin(admin);
                    
                    Domain.AdminPermissions adminPerms = new Domain.AdminPermissions();
                    adminPerms.setCanBuild(config.getBoolean(id + ".adminPermissions." + admin + ".canBuild", true));
                    adminPerms.setCanManagePermissions(config.getBoolean(id + ".adminPermissions." + admin + ".canManagePermissions", true));
                    adminPerms.setCanManageEnvironment(config.getBoolean(id + ".adminPermissions." + admin + ".canManageEnvironment", true));
                    adminPerms.setCanManageAdmins(config.getBoolean(id + ".adminPermissions." + admin + ".canManageAdmins", false));
                    adminPerms.setCanTeleport(config.getBoolean(id + ".adminPermissions." + admin + ".canTeleport", true));
                    adminPerms.setCanDelete(config.getBoolean(id + ".adminPermissions." + admin + ".canDelete", false));
                    domain.setAdminPermissions(admin, adminPerms);
                }
                
                domain.getPermissions().setVisitorMove(config.getBoolean(id + ".permissions.visitorMove", true));
                domain.getPermissions().setVisitorBuild(config.getBoolean(id + ".permissions.visitorBuild", false));
                domain.getPermissions().setVisitorUse(config.getBoolean(id + ".permissions.visitorUse", false));
                domain.getPermissions().setVisitorOpenChest(config.getBoolean(id + ".permissions.visitorOpenChest", false));
                domain.getPermissions().setVisitorPvp(config.getBoolean(id + ".permissions.visitorPvp", false));
                
                domain.getEnvironment().setMobSpawn(config.getBoolean(id + ".environment.mobSpawn", true));
                
                domain.getEnvironment().setCreeperExplosion(config.getBoolean(id + ".environment.creeperExplosion", false));
                domain.getEnvironment().setTntExplosion(config.getBoolean(id + ".environment.tntExplosion", false));
                domain.getEnvironment().setTntBlockDamage(config.getBoolean(id + ".environment.tntBlockDamage", false));
                domain.getEnvironment().setTntEntityDamage(config.getBoolean(id + ".environment.tntEntityDamage", true));
                domain.getEnvironment().setTntChainReaction(config.getBoolean(id + ".environment.tntChainReaction", false));
                domain.getEnvironment().setTntFireSpread(config.getBoolean(id + ".environment.tntFireSpread", true));
                domain.getEnvironment().setEndCrystalExplosion(config.getBoolean(id + ".environment.endCrystalExplosion", false));
                domain.getEnvironment().setWitherExplosion(config.getBoolean(id + ".environment.witherExplosion", false));
                
                domain.getEnvironment().setEndermanGrief(config.getBoolean(id + ".environment.endermanGrief", false));
                domain.getEnvironment().setPistonPush(config.getBoolean(id + ".environment.pistonPush", false));
                domain.getEnvironment().setPistonPull(config.getBoolean(id + ".environment.pistonPull", false));
                domain.getEnvironment().setMobGrief(config.getBoolean(id + ".environment.mobGrief", false));
                domain.getEnvironment().setGravityBlockFall(config.getBoolean(id + ".environment.gravityBlockFall", true));
                
                domain.getEnvironment().setFireSpread(config.getBoolean(id + ".environment.fireSpread", true));
                domain.getEnvironment().setIceMelt(config.getBoolean(id + ".environment.iceMelt", true));
                domain.getEnvironment().setSnowMelt(config.getBoolean(id + ".environment.snowMelt", true));
                domain.getEnvironment().setLeafDecay(config.getBoolean(id + ".environment.leafDecay", true));
                domain.getEnvironment().setVineSpread(config.getBoolean(id + ".environment.vineSpread", true));
                
                domain.getEnvironment().setWaterFlow(config.getBoolean(id + ".environment.waterFlow", true));
                domain.getEnvironment().setLavaFlow(config.getBoolean(id + ".environment.lavaFlow", true));
                
                domain.getEnvironment().setCactusGrow(config.getBoolean(id + ".environment.cactusGrow", true));
                domain.getEnvironment().setSugarcaneGrow(config.getBoolean(id + ".environment.sugarcaneGrow", true));
                domain.getEnvironment().setSlimeDamage(config.getBoolean(id + ".environment.slimeDamage", true));
                
                domain.setEnterMessage(config.getString(id + ".enterMessage", "§a进入领地: {name}"));
                domain.setLeaveMessage(config.getString(id + ".leaveMessage", "§7已离开领地: {name}"));
                
                domains.put(id, domain);
                domainsByCode.put(domain.getCode(), domain);
                playerDomains.computeIfAbsent(owner.toLowerCase(), k -> new ArrayList<>()).add(domain);
                
                if (domain.getCode() >= nextCode) {
                    nextCode = domain.getCode() + 1;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load domain " + id + ": " + e.getMessage());
            }
        }
        
        fixDuplicateCodes();
    }
    
    private void fixDuplicateCodes() {
        Set<Integer> usedCodes = new HashSet<>();
        boolean needSave = false;
        
        for (Domain domain : domains.values()) {
            int code = domain.getCode();
            if (code <= 0 || usedCodes.contains(code)) {
                int newCode = nextCode++;
                domain.setCode(newCode);
                needSave = true;
                plugin.getLogger().info("Fixed duplicate/invalid code for domain " + domain.getId() + ": " + code + " -> " + newCode);
            }
            usedCodes.add(domain.getCode());
        }
        
        if (needSave) {
            domainsByCode.clear();
            for (Domain domain : domains.values()) {
                domainsByCode.put(domain.getCode(), domain);
            }
            saveDomains();
        }
    }
    
    public void saveDomains() {
        YamlConfiguration config = new YamlConfiguration();
        for (Domain domain : domains.values()) {
            String id = domain.getId();
            config.set(id + ".owner", domain.getOwner());
            config.set(id + ".code", domain.getCode());
            config.set(id + ".allowTeleport", domain.isAllowTeleport());
            config.set(id + ".world", domain.getPos1().getWorld().getName());
            config.set(id + ".pos1.x", domain.getPos1().getBlockX());
            config.set(id + ".pos1.y", domain.getPos1().getBlockY());
            config.set(id + ".pos1.z", domain.getPos1().getBlockZ());
            config.set(id + ".pos2.x", domain.getPos2().getBlockX());
            config.set(id + ".pos2.y", domain.getPos2().getBlockY());
            config.set(id + ".pos2.z", domain.getPos2().getBlockZ());
            config.set(id + ".name", domain.getName());
            
            List<String> admins = new ArrayList<>(domain.getAdmins());
            config.set(id + ".admins", admins);
            
            for (String admin : admins) {
                Domain.AdminPermissions adminPerms = domain.getAdminPermissions(admin);
                config.set(id + ".adminPermissions." + admin + ".canBuild", adminPerms.isCanBuild());
                config.set(id + ".adminPermissions." + admin + ".canManagePermissions", adminPerms.isCanManagePermissions());
                config.set(id + ".adminPermissions." + admin + ".canManageEnvironment", adminPerms.isCanManageEnvironment());
                config.set(id + ".adminPermissions." + admin + ".canManageAdmins", adminPerms.isCanManageAdmins());
                config.set(id + ".adminPermissions." + admin + ".canTeleport", adminPerms.isCanTeleport());
                config.set(id + ".adminPermissions." + admin + ".canDelete", adminPerms.isCanDelete());
            }
            
            config.set(id + ".permissions.visitorMove", domain.getPermissions().isVisitorMove());
            config.set(id + ".permissions.visitorBuild", domain.getPermissions().isVisitorBuild());
            config.set(id + ".permissions.visitorUse", domain.getPermissions().isVisitorUse());
            config.set(id + ".permissions.visitorOpenChest", domain.getPermissions().isVisitorOpenChest());
            config.set(id + ".permissions.visitorPvp", domain.getPermissions().isVisitorPvp());
            
            config.set(id + ".environment.mobSpawn", domain.getEnvironment().isMobSpawn());
            
            config.set(id + ".environment.creeperExplosion", domain.getEnvironment().isCreeperExplosion());
            config.set(id + ".environment.tntExplosion", domain.getEnvironment().isTntExplosion());
            config.set(id + ".environment.tntBlockDamage", domain.getEnvironment().isTntBlockDamage());
            config.set(id + ".environment.tntEntityDamage", domain.getEnvironment().isTntEntityDamage());
            config.set(id + ".environment.tntChainReaction", domain.getEnvironment().isTntChainReaction());
            config.set(id + ".environment.tntFireSpread", domain.getEnvironment().isTntFireSpread());
            config.set(id + ".environment.endCrystalExplosion", domain.getEnvironment().isEndCrystalExplosion());
            config.set(id + ".environment.witherExplosion", domain.getEnvironment().isWitherExplosion());
            
            config.set(id + ".environment.endermanGrief", domain.getEnvironment().isEndermanGrief());
            config.set(id + ".environment.pistonPush", domain.getEnvironment().isPistonPush());
            config.set(id + ".environment.pistonPull", domain.getEnvironment().isPistonPull());
            config.set(id + ".environment.mobGrief", domain.getEnvironment().isMobGrief());
            config.set(id + ".environment.gravityBlockFall", domain.getEnvironment().isGravityBlockFall());
            
            config.set(id + ".environment.fireSpread", domain.getEnvironment().isFireSpread());
            config.set(id + ".environment.iceMelt", domain.getEnvironment().isIceMelt());
            config.set(id + ".environment.snowMelt", domain.getEnvironment().isSnowMelt());
            config.set(id + ".environment.leafDecay", domain.getEnvironment().isLeafDecay());
            config.set(id + ".environment.vineSpread", domain.getEnvironment().isVineSpread());
            
            config.set(id + ".environment.waterFlow", domain.getEnvironment().isWaterFlow());
            config.set(id + ".environment.lavaFlow", domain.getEnvironment().isLavaFlow());
            
            config.set(id + ".environment.cactusGrow", domain.getEnvironment().isCactusGrow());
            config.set(id + ".environment.sugarcaneGrow", domain.getEnvironment().isSugarcaneGrow());
            config.set(id + ".environment.slimeDamage", domain.getEnvironment().isSlimeDamage());
            
            config.set(id + ".enterMessage", domain.getEnterMessage());
            config.set(id + ".leaveMessage", domain.getLeaveMessage());
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save domains: " + e.getMessage());
        }
    }
    
    public void setPendingMessageType(String playerName, String type) {
        pendingMessageTypes.put(playerName.toLowerCase(), type);
    }
    
    public void setPendingDomainId(String playerName, String domainId) {
        pendingDomainIds.put(playerName.toLowerCase(), domainId);
    }
    
    public String getPendingMessageType(String playerName) {
        return pendingMessageTypes.get(playerName.toLowerCase());
    }
    
    public String getPendingDomainId(String playerName) {
        return pendingDomainIds.get(playerName.toLowerCase());
    }
    
    public void clearPendingMessage(String playerName) {
        pendingMessageTypes.remove(playerName.toLowerCase());
        pendingDomainIds.remove(playerName.toLowerCase());
    }
    
    public void moveDomain(Domain domain, int dx, int dy, int dz) {
        World world = domain.getPos1().getWorld();
        
        int minX = Math.min(domain.getPos1().getBlockX(), domain.getPos2().getBlockX());
        int maxX = Math.max(domain.getPos1().getBlockX(), domain.getPos2().getBlockX());
        int minY = Math.min(domain.getPos1().getBlockY(), domain.getPos2().getBlockY());
        int maxY = Math.max(domain.getPos1().getBlockY(), domain.getPos2().getBlockY());
        int minZ = Math.min(domain.getPos1().getBlockZ(), domain.getPos2().getBlockZ());
        int maxZ = Math.max(domain.getPos1().getBlockZ(), domain.getPos2().getBlockZ());
        
        Map<org.bukkit.util.Vector, org.bukkit.block.data.BlockData> blockDataMap = new HashMap<>();
        Map<org.bukkit.util.Vector, org.bukkit.block.BlockState> blockStateMap = new HashMap<>();
        Map<org.bukkit.util.Vector, org.bukkit.entity.Entity> entitiesToMove = new HashMap<>();
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != org.bukkit.Material.AIR) {
                        org.bukkit.util.Vector vec = new org.bukkit.util.Vector(x, y, z);
                        blockDataMap.put(vec, block.getBlockData().clone());
                        blockStateMap.put(vec, block.getState());
                    }
                }
            }
        }
        
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            Location loc = entity.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            
            if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                if (!(entity instanceof org.bukkit.entity.Player)) {
                    entitiesToMove.put(new org.bukkit.util.Vector(x, y, z), entity);
                }
            }
        }
        
        List<org.bukkit.util.Vector> solidBlocks = new ArrayList<>();
        List<org.bukkit.util.Vector> attachedBlocks = new ArrayList<>();
        
        for (org.bukkit.util.Vector vec : blockDataMap.keySet()) {
            org.bukkit.block.data.BlockData data = blockDataMap.get(vec);
            if (isAttachedBlock(data)) {
                attachedBlocks.add(vec);
            } else {
                solidBlocks.add(vec);
            }
        }
        
        if (dy >= 0) {
            solidBlocks.sort((a, b) -> Integer.compare(a.getBlockY(), b.getBlockY()));
            attachedBlocks.sort((a, b) -> Integer.compare(a.getBlockY(), b.getBlockY()));
        } else {
            solidBlocks.sort((a, b) -> Integer.compare(b.getBlockY(), a.getBlockY()));
            attachedBlocks.sort((a, b) -> Integer.compare(b.getBlockY(), a.getBlockY()));
        }
        
        for (org.bukkit.util.Vector vec : solidBlocks) {
            placeBlock(world, vec, dx, dy, dz, blockDataMap.get(vec), blockStateMap.get(vec));
        }
        
        for (org.bukkit.util.Vector vec : attachedBlocks) {
            placeBlock(world, vec, dx, dy, dz, blockDataMap.get(vec), blockStateMap.get(vec));
        }
        
        List<org.bukkit.util.Vector> allKeys = new ArrayList<>(blockDataMap.keySet());
        if (dy < 0) {
            allKeys.sort((a, b) -> Integer.compare(a.getBlockY(), b.getBlockY()));
        } else {
            allKeys.sort((a, b) -> Integer.compare(b.getBlockY(), a.getBlockY()));
        }
        
        for (org.bukkit.util.Vector vec : allKeys) {
            world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ()).setType(org.bukkit.Material.AIR);
        }
        
        for (org.bukkit.entity.Entity entity : entitiesToMove.values()) {
            Location oldLoc = entity.getLocation();
            Location newLoc = new Location(
                world,
                oldLoc.getX() + dx,
                oldLoc.getY() + dy,
                oldLoc.getZ() + dz,
                oldLoc.getYaw(),
                oldLoc.getPitch()
            );
            entity.teleport(newLoc);
        }
        
        Location newPos1 = new Location(
            world,
            domain.getPos1().getBlockX() + dx,
            domain.getPos1().getBlockY() + dy,
            domain.getPos1().getBlockZ() + dz
        );
        Location newPos2 = new Location(
            world,
            domain.getPos2().getBlockX() + dx,
            domain.getPos2().getBlockY() + dy,
            domain.getPos2().getBlockZ() + dz
        );
        
        domain.setPos1(newPos1);
        domain.setPos2(newPos2);
    }
    
    private boolean isAttachedBlock(org.bukkit.block.data.BlockData data) {
        String name = data.getMaterial().name();
        
        if (name.contains("FENCE") || name.contains("WALL") || name.contains("GLASS_PANE") || 
            name.contains("IRON_BARS") || name.contains("CHAIN")) {
            return false;
        }
        
        if (data instanceof org.bukkit.block.data.Bisected) {
            return false;
        }
        
        return name.contains("TORCH") || name.contains("BUTTON") || name.contains("LEVER") || 
               name.contains("LADDER") || name.contains("WALL_SIGN") || name.contains("HEAD") ||
               name.contains("BANNER") || name.contains("PICTURE") || name.contains("TRIPWIRE_HOOK") ||
               name.contains("RAIL") || name.contains("REPEATER") || name.contains("COMPARATOR") ||
               name.contains("REDSTONE_WIRE") || name.contains("REDSTONE_TORCH");
    }
    
    private void placeBlock(World world, org.bukkit.util.Vector vec, int dx, int dy, int dz,
                           org.bukkit.block.data.BlockData data, org.bukkit.block.BlockState state) {
        int newX = vec.getBlockX() + dx;
        int newY = vec.getBlockY() + dy;
        int newZ = vec.getBlockZ() + dz;
        
        org.bukkit.block.Block newBlock = world.getBlockAt(newX, newY, newZ);
        
        if (state instanceof org.bukkit.block.Container) {
            newBlock.setBlockData(data, false);
            org.bukkit.block.Container oldContainer = (org.bukkit.block.Container) state;
            org.bukkit.block.Container newContainer = (org.bukkit.block.Container) newBlock.getState();
            
            org.bukkit.inventory.Inventory oldInv = oldContainer.getInventory();
            org.bukkit.inventory.Inventory newInv = newContainer.getInventory();
            
            for (int i = 0; i < oldInv.getSize(); i++) {
                newInv.setItem(i, oldInv.getItem(i));
            }
            newContainer.update(true, false);
        } else if (state instanceof org.bukkit.block.Sign) {
            newBlock.setBlockData(data, false);
            org.bukkit.block.Sign oldSign = (org.bukkit.block.Sign) state;
            org.bukkit.block.Sign newSign = (org.bukkit.block.Sign) newBlock.getState();
            
            for (int i = 0; i < 4; i++) {
                newSign.setLine(i, oldSign.getLine(i));
            }
            newSign.update(true, false);
        } else {
            newBlock.setBlockData(data, false);
        }
    }
    
    public boolean handlePendingMessage(String playerName, String message) {
        String type = pendingMessageTypes.get(playerName.toLowerCase());
        String domainId = pendingDomainIds.get(playerName.toLowerCase());
        
        if (type == null || domainId == null) {
            return false;
        }
        
        Domain domain = domains.get(domainId);
        if (domain == null) {
            clearPendingMessage(playerName);
            return false;
        }
        
        if ("enter".equals(type)) {
            domain.setEnterMessage(message);
        } else if ("leave".equals(type)) {
            domain.setLeaveMessage(message);
        }
        
        saveDomains();
        clearPendingMessage(playerName);
        return true;
    }
}