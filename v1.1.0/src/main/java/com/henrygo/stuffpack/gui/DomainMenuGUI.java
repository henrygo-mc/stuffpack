package com.henrygo.stuffpack.gui;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DomainMenuGUI {
    
    private final StuffPack plugin;
    
    public DomainMenuGUI(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("main"), 54, ChatColor.GOLD + "领地管理");
        
        ItemStack createItem = createItem(Material.WOODEN_HOE, ChatColor.GREEN + "创建领地", 
            "点击木杵左键设置点1", "右键设置点2", "然后使用 /dom create <名称> 创建");
        inventory.setItem(11, createItem);
        
        ItemStack listItem = createItem(Material.CHEST, ChatColor.BLUE + "我的领地", 
            "查看和管理你的领地");
        inventory.setItem(13, listItem);
        
        ItemStack helpItem = createItem(Material.BOOK, ChatColor.YELLOW + "帮助", 
            "查看所有命令说明");
        inventory.setItem(15, helpItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openDomainList(Player player) {
        List<Domain> domains = plugin.getDomainManager().getDomainsByOwner(player.getName());
        
        if (domains.isEmpty()) {
            player.sendMessage("§c你没有任何领地");
            return;
        }
        
        int size = Math.min(((domains.size() + 8) / 9) * 9 + 9, 54);
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("list"), size, ChatColor.GOLD + "我的领地");
        
        int slot = 0;
        for (Domain domain : domains) {
            ItemStack domainItem = createItem(Material.GRASS_BLOCK, ChatColor.GREEN + domain.getName(),
                "ID: " + domain.getId().substring(0, 8),
                "体积: " + domain.getVolume(),
                "点击查看详情");
            inventory.setItem(slot++, domainItem);
        }
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(size - 1, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openDomainManage(Player player, Domain domain) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("manage_" + domain.getId()), 54, ChatColor.GOLD + "领地: " + domain.getName());
        
        String ownerName = Bukkit.getPlayer(domain.getOwner()) != null 
            ? Bukkit.getPlayer(domain.getOwner()).getName() 
            : domain.getOwner();
        
        ItemStack infoItem = createItem(Material.PAPER, ChatColor.WHITE + "领地信息",
            "ID: " + domain.getId().substring(0, 8),
            "编号: " + domain.getCode(),
            "主人: " + ownerName,
            "体积: " + domain.getVolume());
        inventory.setItem(10, infoItem);
        
        ItemStack renameItem = createItem(Material.NAME_TAG, ChatColor.YELLOW + "重命名",
            "修改领地名称");
        inventory.setItem(12, renameItem);
        
        ItemStack deleteItem = createItem(Material.BARRIER, ChatColor.RED + "删除领地",
            "永久删除此领地");
        inventory.setItem(14, deleteItem);
        
        ItemStack tpItem = createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "传送",
            "传送到领地中心");
        inventory.setItem(16, tpItem);
        
        ItemStack teleportToggleItem = createToggleItem(Material.ENDER_PEARL, "允许传送", domain.isAllowTeleport());
        inventory.setItem(25, teleportToggleItem);
        
        ItemStack adminItem = createItem(Material.PLAYER_HEAD, ChatColor.AQUA + "管理员管理",
            "添加/移除管理员");
        inventory.setItem(28, adminItem);
        
        ItemStack permItem = createItem(Material.LEVER, ChatColor.GREEN + "权限设置",
            "设置访客权限");
        inventory.setItem(30, permItem);
        
        ItemStack envItem = createItem(Material.FIRE_CHARGE, ChatColor.GOLD + "环境设置",
            "设置领地环境");
        inventory.setItem(32, envItem);
        
        ItemStack messageItem = createItem(Material.OAK_SIGN, ChatColor.DARK_PURPLE + "消息设置",
            "自定义进入/离开提示");
        inventory.setItem(34, messageItem);
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(49, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openMessageManage(Player player, Domain domain) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("message_" + domain.getId()), 27, ChatColor.GOLD + "消息设置");
        
        inventory.setItem(10, createItem(Material.GREEN_CONCRETE, ChatColor.GREEN + "进入消息",
            "当前: " + domain.getEnterMessage().replace("§", "&")));
        inventory.setItem(16, createItem(Material.RED_CONCRETE, ChatColor.RED + "离开消息",
            "当前: " + domain.getLeaveMessage().replace("§", "&")));
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(22, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openAdminManage(Player player, Domain domain) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("admin_" + domain.getId()), 54, ChatColor.GOLD + "管理员管理");
        
        int slot = 0;
        for (String admin : domain.getAdmins()) {
            Player adminPlayer = Bukkit.getPlayer(admin);
            String name = adminPlayer != null ? adminPlayer.getName() : admin;
            
            ItemStack adminItem = createItem(Material.PLAYER_HEAD, ChatColor.AQUA + name,
                "左键: 管理权限", "右键: 移除管理员");
            inventory.setItem(slot++, adminItem);
        }
        
        ItemStack addItem = createItem(Material.EMERALD, ChatColor.GREEN + "添加管理员",
            "点击后输入玩家名称");
        inventory.setItem(45, addItem);
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(49, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openAdminPermissionManage(Player player, Domain domain, String adminName) {
        String lowerAdminName = adminName.toLowerCase();
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("adminperm_" + domain.getId() + "_" + lowerAdminName), 54, ChatColor.GOLD + adminName + " 的权限");
        
        Domain.AdminPermissions adminPerms = domain.getAdminPermissions(adminName);
        
        inventory.setItem(19, createToggleItem(Material.BRICKS, "建造", adminPerms.isCanBuild()));
        inventory.setItem(20, createToggleItem(Material.LEVER, "管理权限", adminPerms.isCanManagePermissions()));
        inventory.setItem(21, createToggleItem(Material.FIRE_CHARGE, "管理环境", adminPerms.isCanManageEnvironment()));
        inventory.setItem(22, createToggleItem(Material.PLAYER_HEAD, "管理管理员", adminPerms.isCanManageAdmins()));
        inventory.setItem(23, createToggleItem(Material.ENDER_PEARL, "传送", adminPerms.isCanTeleport()));
        inventory.setItem(24, createToggleItem(Material.BARRIER, "删除领地", adminPerms.isCanDelete()));
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(49, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openPermissionManage(Player player, Domain domain) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("perm_" + domain.getId()), 54, ChatColor.GOLD + "权限设置");
        
        Domain.Permissions perm = domain.getPermissions();
        
        inventory.setItem(0, createItem(Material.IRON_DOOR, ChatColor.YELLOW + "=== 基础权限 ==="));
        inventory.setItem(1, createToggleItem(Material.DIRT, "移动", perm.isVisitorMove()));
        inventory.setItem(2, createToggleItem(Material.BRICKS, "建造/破坏", perm.isVisitorBuild()));
        inventory.setItem(3, createToggleItem(Material.IRON_SWORD, "PVP", perm.isVisitorPvp()));
        
        inventory.setItem(9, createItem(Material.CHEST, ChatColor.GOLD + "=== 交互权限 ==="));
        inventory.setItem(10, createToggleItem(Material.CHEST, "打开箱子", perm.isVisitorOpenChest()));
        inventory.setItem(11, createToggleItem(Material.LEVER, "使用按钮/拉杆", perm.isVisitorUse()));
        inventory.setItem(12, createToggleItem(Material.OAK_BUTTON, "按钮", perm.isVisitorUseButton()));
        inventory.setItem(13, createToggleItem(Material.LEVER, "拉杆", perm.isVisitorUseLever()));
        inventory.setItem(14, createToggleItem(Material.STONE_PRESSURE_PLATE, "压力板", perm.isVisitorUsePressurePlate()));
        
        inventory.setItem(18, createItem(Material.OAK_DOOR, ChatColor.AQUA + "=== 门类权限 ==="));
        inventory.setItem(19, createToggleItem(Material.OAK_DOOR, "木门", perm.isVisitorOpenDoor()));
        inventory.setItem(20, createToggleItem(Material.IRON_DOOR, "铁门", perm.isVisitorOpenDoor()));
        inventory.setItem(21, createToggleItem(Material.OAK_TRAPDOOR, "活板门", perm.isVisitorOpenTrapdoor()));
        inventory.setItem(22, createToggleItem(Material.OAK_FENCE_GATE, "栅栏门", perm.isVisitorOpenFenceGate()));
        inventory.setItem(23, createToggleItem(Material.RED_BED, "睡觉", perm.isVisitorUseBed()));
        
        inventory.setItem(27, createItem(Material.APPLE, ChatColor.LIGHT_PURPLE + "=== 物品权限 ==="));
        inventory.setItem(28, createToggleItem(Material.APPLE, "丢弃物品", perm.isVisitorDropItem()));
        inventory.setItem(29, createToggleItem(Material.CHEST_MINECART, "拾取物品", perm.isVisitorPickupItem()));
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(49, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openEnvironmentManage(Player player, Domain domain) {
        Inventory inventory = Bukkit.createInventory(new DomainInventoryHolder("env_" + domain.getId()), 54, ChatColor.GOLD + "环境设置");
        
        Domain.EnvironmentSettings env = domain.getEnvironment();
        
        inventory.setItem(0, createItem(Material.GOLD_BLOCK, ChatColor.YELLOW + "=== 怪物设置 ==="));
        inventory.setItem(1, createToggleItem(Material.ZOMBIE_HEAD, "怪物生成", env.isMobSpawn()));
        
        inventory.setItem(9, createItem(Material.TNT, ChatColor.RED + "=== 爆炸设置 ==="));
        inventory.setItem(10, createToggleItem(Material.CREEPER_HEAD, "苦力怕爆炸", env.isCreeperExplosion()));
        inventory.setItem(11, createToggleItem(Material.TNT, "TNT爆炸", env.isTntExplosion()));
        inventory.setItem(12, createToggleItem(Material.ANVIL, "TNT方块伤害", env.isTntBlockDamage()));
        inventory.setItem(13, createToggleItem(Material.SKELETON_SKULL, "TNT实体伤害", env.isTntEntityDamage()));
        inventory.setItem(14, createToggleItem(Material.REDSTONE_TORCH, "TNT连锁爆炸", env.isTntChainReaction()));
        inventory.setItem(15, createToggleItem(Material.FIRE_CHARGE, "TNT火焰蔓延", env.isTntFireSpread()));
        inventory.setItem(16, createToggleItem(Material.END_CRYSTAL, "末影水晶爆炸", env.isEndCrystalExplosion()));
        inventory.setItem(17, createToggleItem(Material.WITHER_SKELETON_SKULL, "凋零爆炸", env.isWitherExplosion()));
        
        inventory.setItem(18, createItem(Material.IRON_GOLEM_SPAWN_EGG, ChatColor.AQUA + "=== 实体破坏 ==="));
        inventory.setItem(19, createToggleItem(Material.ENDERMAN_SPAWN_EGG, "末影人搬运", env.isEndermanGrief()));
        inventory.setItem(20, createToggleItem(Material.PISTON, "活塞推动", env.isPistonPush()));
        inventory.setItem(21, createToggleItem(Material.STICKY_PISTON, "粘性活塞拉动", env.isPistonPull()));
        inventory.setItem(22, createToggleItem(Material.SILVERFISH_SPAWN_EGG, "生物破坏", env.isMobGrief()));
        inventory.setItem(23, createToggleItem(Material.SAND, "重力方块下落", env.isGravityBlockFall()));
        
        inventory.setItem(27, createItem(Material.FLINT_AND_STEEL, ChatColor.GOLD + "=== 自然消失 ==="));
        inventory.setItem(28, createToggleItem(Material.FLINT_AND_STEEL, "火焰蔓延", env.isFireSpread()));
        inventory.setItem(29, createToggleItem(Material.ICE, "冰融化", env.isIceMelt()));
        inventory.setItem(30, createToggleItem(Material.SNOW_BLOCK, "雪融化", env.isSnowMelt()));
        inventory.setItem(31, createToggleItem(Material.OAK_LEAVES, "树叶腐烂", env.isLeafDecay()));
        inventory.setItem(32, createToggleItem(Material.VINE, "藤蔓蔓延", env.isVineSpread()));
        
        inventory.setItem(36, createItem(Material.WATER_BUCKET, ChatColor.BLUE + "=== 流体设置 ==="));
        inventory.setItem(37, createToggleItem(Material.WATER_BUCKET, "水流", env.isWaterFlow()));
        inventory.setItem(38, createToggleItem(Material.LAVA_BUCKET, "岩浆流", env.isLavaFlow()));
        
        inventory.setItem(45, createItem(Material.CACTUS, ChatColor.GREEN + "=== 植物生长 ==="));
        inventory.setItem(46, createToggleItem(Material.CACTUS, "仙人掌生长", env.isCactusGrow()));
        inventory.setItem(47, createToggleItem(Material.SUGAR_CANE, "甘蔗生长", env.isSugarcaneGrow()));
        inventory.setItem(48, createToggleItem(Material.SLIME_BALL, "史莱姆破坏", env.isSlimeDamage()));
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(53, backItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createToggleItem(Material material, String name, boolean value) {
        ItemStack item = new ItemStack(value ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((value ? ChatColor.GREEN : ChatColor.RED) + name);
        meta.setLore(List.of(ChatColor.GRAY + "当前: " + (value ? "开启" : "关闭")));
        item.setItemMeta(meta);
        return item;
    }
    
    private void fillBorder(Inventory inventory) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        border.setItemMeta(meta);
        
        int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, border);
                }
            }
        }
    }
    
    public static class DomainInventoryHolder implements InventoryHolder {
        private final String type;
        
        public DomainInventoryHolder(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}