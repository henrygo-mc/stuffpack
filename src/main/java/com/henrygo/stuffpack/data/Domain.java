package com.henrygo.stuffpack.data;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Domain {
    
    private String id;
    private int code;
    private String owner;
    private Set<String> admins;
    private Map<String, AdminPermissions> adminPermissions;
    private Location pos1;
    private Location pos2;
    private String name;
    private long createdAt;
    private boolean allowTeleport;
    private String enterMessage;
    private String leaveMessage;
    
    private Permissions permissions;
    private EnvironmentSettings environment;
    
    public Domain(String id, int code, String owner, Location pos1, Location pos2) {
        this.id = id;
        this.code = code;
        this.owner = owner.toLowerCase();
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.admins = new HashSet<>();
        this.adminPermissions = new HashMap<>();
        this.name = "领地" + id.substring(0, 8);
        this.createdAt = System.currentTimeMillis();
        this.allowTeleport = true;
        this.enterMessage = "§a进入领地: {name}";
        this.leaveMessage = "§7已离开领地: {name}";
        this.permissions = new Permissions();
        this.environment = new EnvironmentSettings();
    }
    
    public String getId() {
        return id;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public void setOwner(String owner) {
        this.owner = owner.toLowerCase();
    }
    
    public boolean isOwner(String playerName) {
        return owner.equalsIgnoreCase(playerName);
    }
    
    public Set<String> getAdmins() {
        return admins;
    }
    
    public void addAdmin(String admin) {
        admins.add(admin.toLowerCase());
    }
    
    public void removeAdmin(String admin) {
        admins.remove(admin.toLowerCase());
    }
    
    public boolean isAdmin(String playerName) {
        return admins.contains(playerName.toLowerCase());
    }
    
    public boolean isOwnerOrAdmin(String playerName) {
        return owner.equalsIgnoreCase(playerName) || admins.contains(playerName.toLowerCase());
    }
    
    public AdminPermissions getAdminPermissions(String adminName) {
        return adminPermissions.computeIfAbsent(adminName.toLowerCase(), k -> new AdminPermissions());
    }
    
    public void setAdminPermissions(String adminName, AdminPermissions perms) {
        adminPermissions.put(adminName.toLowerCase(), perms);
    }
    
    public void removeAdminPermissions(String adminName) {
        adminPermissions.remove(adminName.toLowerCase());
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public boolean isAllowTeleport() {
        return allowTeleport;
    }
    
    public void setAllowTeleport(boolean allowTeleport) {
        this.allowTeleport = allowTeleport;
    }
    
    public String getEnterMessage() {
        return enterMessage;
    }
    
    public void setEnterMessage(String enterMessage) {
        this.enterMessage = enterMessage;
    }
    
    public String getLeaveMessage() {
        return leaveMessage;
    }
    
    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }
    
    public Permissions getPermissions() {
        return permissions;
    }
    
    public EnvironmentSettings getEnvironment() {
        return environment;
    }
    
    public int getVolume() {
        World world = pos1.getWorld();
        if (world == null || !world.equals(pos2.getWorld())) {
            return 0;
        }
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
    
    public Location getCenter() {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return new Location(
            pos1.getWorld(),
            (minX + maxX) / 2.0 + 0.5,
            (minY + maxY) / 2.0 + 0.5,
            (minZ + maxZ) / 2.0 + 0.5
        );
    }
    
    public boolean contains(Location location) {
        if (!pos1.getWorld().equals(location.getWorld())) {
            return false;
        }
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
    
    public static class Permissions {
        private boolean visitorMove;
        private boolean visitorBuild;
        private boolean visitorUse;
        private boolean visitorOpenChest;
        private boolean visitorPvp;
        
        public Permissions() {
            this.visitorMove = true;
            this.visitorBuild = false;
            this.visitorUse = false;
            this.visitorOpenChest = false;
            this.visitorPvp = false;
        }
        
        public boolean isVisitorMove() {
            return visitorMove;
        }
        
        public void setVisitorMove(boolean visitorMove) {
            this.visitorMove = visitorMove;
        }
        
        public boolean isVisitorBuild() {
            return visitorBuild;
        }
        
        public void setVisitorBuild(boolean visitorBuild) {
            this.visitorBuild = visitorBuild;
        }
        
        public boolean isVisitorUse() {
            return visitorUse;
        }
        
        public void setVisitorUse(boolean visitorUse) {
            this.visitorUse = visitorUse;
        }
        
        public boolean isVisitorOpenChest() {
            return visitorOpenChest;
        }
        
        public void setVisitorOpenChest(boolean visitorOpenChest) {
            this.visitorOpenChest = visitorOpenChest;
        }
        
        public boolean isVisitorPvp() {
            return visitorPvp;
        }
        
        public void setVisitorPvp(boolean visitorPvp) {
            this.visitorPvp = visitorPvp;
        }
    }
    
    public static class EnvironmentSettings {
        
        private boolean mobSpawn;
        
        private boolean creeperExplosion;
        private boolean tntExplosion;
        private boolean tntBlockDamage;
        private boolean tntEntityDamage;
        private boolean tntChainReaction;
        private boolean tntFireSpread;
        private boolean endCrystalExplosion;
        private boolean witherExplosion;
        
        private boolean endermanGrief;
        private boolean pistonPush;
        private boolean pistonPull;
        private boolean mobGrief;
        private boolean gravityBlockFall;
        
        private boolean fireSpread;
        private boolean iceMelt;
        private boolean snowMelt;
        private boolean leafDecay;
        private boolean vineSpread;
        
        private boolean waterFlow;
        private boolean lavaFlow;
        
        private boolean cactusGrow;
        private boolean sugarcaneGrow;
        private boolean slimeDamage;
        
        public EnvironmentSettings() {
            this.mobSpawn = true;
            
            this.creeperExplosion = false;
            this.tntExplosion = false;
            this.tntBlockDamage = false;
            this.tntEntityDamage = true;
            this.tntChainReaction = false;
            this.tntFireSpread = true;
            this.endCrystalExplosion = false;
            this.witherExplosion = false;
            
            this.endermanGrief = false;
            this.pistonPush = false;
            this.pistonPull = false;
            this.mobGrief = false;
            this.gravityBlockFall = true;
            
            this.fireSpread = true;
            this.iceMelt = true;
            this.snowMelt = true;
            this.leafDecay = true;
            this.vineSpread = true;
            
            this.waterFlow = true;
            this.lavaFlow = true;
            
            this.cactusGrow = true;
            this.sugarcaneGrow = true;
            this.slimeDamage = true;
        }
        
        public boolean isMobSpawn() {
            return mobSpawn;
        }
        
        public void setMobSpawn(boolean mobSpawn) {
            this.mobSpawn = mobSpawn;
        }
        
        public boolean isCreeperExplosion() {
            return creeperExplosion;
        }
        
        public void setCreeperExplosion(boolean creeperExplosion) {
            this.creeperExplosion = creeperExplosion;
        }
        
        public boolean isTntExplosion() {
            return tntExplosion;
        }
        
        public void setTntExplosion(boolean tntExplosion) {
            this.tntExplosion = tntExplosion;
        }
        
        public boolean isTntBlockDamage() {
            return tntBlockDamage;
        }
        
        public void setTntBlockDamage(boolean tntBlockDamage) {
            this.tntBlockDamage = tntBlockDamage;
        }
        
        public boolean isTntEntityDamage() {
            return tntEntityDamage;
        }
        
        public void setTntEntityDamage(boolean tntEntityDamage) {
            this.tntEntityDamage = tntEntityDamage;
        }
        
        public boolean isTntChainReaction() {
            return tntChainReaction;
        }
        
        public void setTntChainReaction(boolean tntChainReaction) {
            this.tntChainReaction = tntChainReaction;
        }
        
        public boolean isTntFireSpread() {
            return tntFireSpread;
        }
        
        public void setTntFireSpread(boolean tntFireSpread) {
            this.tntFireSpread = tntFireSpread;
        }
        
        public boolean isEndermanGrief() {
            return endermanGrief;
        }
        
        public void setEndermanGrief(boolean endermanGrief) {
            this.endermanGrief = endermanGrief;
        }
        
        public boolean isFireSpread() {
            return fireSpread;
        }
        
        public void setFireSpread(boolean fireSpread) {
            this.fireSpread = fireSpread;
        }
        
        public boolean isLeafDecay() {
            return leafDecay;
        }
        
        public void setLeafDecay(boolean leafDecay) {
            this.leafDecay = leafDecay;
        }
        
        public boolean isVineSpread() {
            return vineSpread;
        }
        
        public void setVineSpread(boolean vineSpread) {
            this.vineSpread = vineSpread;
        }
        
        public boolean isWaterFlow() {
            return waterFlow;
        }
        
        public void setWaterFlow(boolean waterFlow) {
            this.waterFlow = waterFlow;
        }
        
        public boolean isLavaFlow() {
            return lavaFlow;
        }
        
        public void setLavaFlow(boolean lavaFlow) {
            this.lavaFlow = lavaFlow;
        }
        
        public boolean isEndCrystalExplosion() {
            return endCrystalExplosion;
        }
        
        public void setEndCrystalExplosion(boolean endCrystalExplosion) {
            this.endCrystalExplosion = endCrystalExplosion;
        }
        
        public boolean isWitherExplosion() {
            return witherExplosion;
        }
        
        public void setWitherExplosion(boolean witherExplosion) {
            this.witherExplosion = witherExplosion;
        }
        
        public boolean isPistonPush() {
            return pistonPush;
        }
        
        public void setPistonPush(boolean pistonPush) {
            this.pistonPush = pistonPush;
        }
        
        public boolean isPistonPull() {
            return pistonPull;
        }
        
        public void setPistonPull(boolean pistonPull) {
            this.pistonPull = pistonPull;
        }
        
        public boolean isMobGrief() {
            return mobGrief;
        }
        
        public void setMobGrief(boolean mobGrief) {
            this.mobGrief = mobGrief;
        }
        
        public boolean isGravityBlockFall() {
            return gravityBlockFall;
        }
        
        public void setGravityBlockFall(boolean gravityBlockFall) {
            this.gravityBlockFall = gravityBlockFall;
        }
        
        public boolean isIceMelt() {
            return iceMelt;
        }
        
        public void setIceMelt(boolean iceMelt) {
            this.iceMelt = iceMelt;
        }
        
        public boolean isSnowMelt() {
            return snowMelt;
        }
        
        public void setSnowMelt(boolean snowMelt) {
            this.snowMelt = snowMelt;
        }
        
        public boolean isCactusGrow() {
            return cactusGrow;
        }
        
        public void setCactusGrow(boolean cactusGrow) {
            this.cactusGrow = cactusGrow;
        }
        
        public boolean isSugarcaneGrow() {
            return sugarcaneGrow;
        }
        
        public void setSugarcaneGrow(boolean sugarcaneGrow) {
            this.sugarcaneGrow = sugarcaneGrow;
        }
        
        public boolean isSlimeDamage() {
            return slimeDamage;
        }
        
        public void setSlimeDamage(boolean slimeDamage) {
            this.slimeDamage = slimeDamage;
        }
    }
    
    public static class AdminPermissions {
        private boolean canBuild;
        private boolean canManagePermissions;
        private boolean canManageEnvironment;
        private boolean canManageAdmins;
        private boolean canTeleport;
        private boolean canDelete;
        
        public AdminPermissions() {
            this.canBuild = true;
            this.canManagePermissions = true;
            this.canManageEnvironment = true;
            this.canManageAdmins = false;
            this.canTeleport = true;
            this.canDelete = false;
        }
        
        public boolean isCanBuild() {
            return canBuild;
        }
        
        public void setCanBuild(boolean canBuild) {
            this.canBuild = canBuild;
        }
        
        public boolean isCanManagePermissions() {
            return canManagePermissions;
        }
        
        public void setCanManagePermissions(boolean canManagePermissions) {
            this.canManagePermissions = canManagePermissions;
        }
        
        public boolean isCanManageEnvironment() {
            return canManageEnvironment;
        }
        
        public void setCanManageEnvironment(boolean canManageEnvironment) {
            this.canManageEnvironment = canManageEnvironment;
        }
        
        public boolean isCanManageAdmins() {
            return canManageAdmins;
        }
        
        public void setCanManageAdmins(boolean canManageAdmins) {
            this.canManageAdmins = canManageAdmins;
        }
        
        public boolean isCanTeleport() {
            return canTeleport;
        }
        
        public void setCanTeleport(boolean canTeleport) {
            this.canTeleport = canTeleport;
        }
        
        public boolean isCanDelete() {
            return canDelete;
        }
        
        public void setCanDelete(boolean canDelete) {
            this.canDelete = canDelete;
        }
    }
}