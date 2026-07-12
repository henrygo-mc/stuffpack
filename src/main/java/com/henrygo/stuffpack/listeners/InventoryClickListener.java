package com.henrygo.stuffpack.listeners;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import com.henrygo.stuffpack.gui.DomainMenuGUI;
import com.henrygo.stuffpack.gui.DomainMenuGUI.DomainInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryClickListener implements Listener {
    
    private final StuffPack plugin;
    private final DomainMenuGUI gui;
    
    public InventoryClickListener(StuffPack plugin) {
        this.plugin = plugin;
        this.gui = new DomainMenuGUI(plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        
        if (!(inventory.getHolder() instanceof DomainInventoryHolder)) {
            return;
        }
        
        DomainInventoryHolder holder = (DomainInventoryHolder) inventory.getHolder();
        String holderType = holder.getType();
        
        event.setCancelled(true);
        
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            Player player = (Player) event.getWhoClicked();
            player.getInventory().addItem(event.getCursor());
            event.getCursor().setType(Material.AIR);
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        String itemName = clickedItem.getItemMeta() != null && clickedItem.getItemMeta().hasDisplayName() 
            ? clickedItem.getItemMeta().getDisplayName() 
            : "";
        
        handleClick(player, holderType, event.getSlot(), itemName, event.getClick());
    }
    
    private void handleClick(Player player, String holderType, int slot, String itemName, ClickType clickType) {
        if (holderType.equals("main")) {
            handleMainMenuClick(player, itemName);
        } else if (holderType.equals("list")) {
            handleDomainListClick(player, slot);
        } else if (holderType.startsWith("manage_")) {
            String domainId = holderType.substring(7);
            handleDomainManageClick(player, domainId, itemName, slot);
        } else if (holderType.startsWith("admin_")) {
            String domainId = holderType.substring(6);
            handleAdminManageClick(player, domainId, slot, itemName, clickType);
        } else if (holderType.startsWith("perm_")) {
            String domainId = holderType.substring(5);
            handlePermissionClick(player, domainId, slot, itemName);
        } else if (holderType.startsWith("env_")) {
            String domainId = holderType.substring(4);
            handleEnvironmentClick(player, domainId, slot, itemName);
        } else if (holderType.startsWith("adminperm_")) {
            String[] parts = holderType.substring(10).split("_", 2);
            String domainId = parts[0];
            String adminName = parts[1];
            handleAdminPermissionClick(player, domainId, adminName, slot, itemName);
        } else if (holderType.startsWith("message_")) {
            String domainId = holderType.substring(8);
            handleMessageClick(player, domainId, slot, itemName);
        }
    }
    
    private void handleMainMenuClick(Player player, String itemName) {
        if (itemName.contains(ChatColor.GREEN + "创建领地")) {
            player.closeInventory();
            player.sendMessage("§a使用木杵左键设置点1，右键设置点2，然后使用 /dom create <名称> 创建领地");
        } else if (itemName.contains(ChatColor.BLUE + "我的领地")) {
            gui.openDomainList(player);
        } else if (itemName.contains(ChatColor.YELLOW + "帮助")) {
            player.closeInventory();
            player.performCommand("help");
        }
    }
    
    private void handleDomainListClick(Player player, int slot) {
        ItemStack item = player.getOpenInventory().getTopInventory().getItem(slot);
        
        if (item != null && item.getType() == Material.ARROW) {
            gui.openMainMenu(player);
            return;
        }
        
        List<Domain> domains = plugin.getDomainManager().getDomainsByOwner(player.getName());
        if (slot >= 0 && slot < domains.size()) {
            gui.openDomainManage(player, domains.get(slot));
        }
    }
    
    private void handleDomainManageClick(Player player, String domainId, String itemName, int slot) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.YELLOW + "重命名")) {
            player.closeInventory();
            player.sendMessage("§a请输入新的领地名称:");
        } else if (itemName.contains(ChatColor.RED + "删除领地")) {
            plugin.getDomainManager().deleteDomain(domain.getId());
            player.sendMessage("§a领地已删除");
            gui.openDomainList(player);
        } else if (itemName.contains(ChatColor.LIGHT_PURPLE + "传送")) {
            plugin.getTeleportManager().savePreviousLocation(player.getName(), player.getLocation());
            player.teleport(domain.getCenter());
            player.sendMessage("§a已传送到领地 " + domain.getName());
            player.closeInventory();
        } else if (itemName.contains(ChatColor.AQUA + "管理员管理")) {
            gui.openAdminManage(player, domain);
        } else if (itemName.contains(ChatColor.GREEN + "权限设置")) {
            gui.openPermissionManage(player, domain);
        } else if (itemName.contains(ChatColor.GOLD + "环境设置")) {
            gui.openEnvironmentManage(player, domain);
        } else if (itemName.contains(ChatColor.DARK_PURPLE + "消息设置")) {
            gui.openMessageManage(player, domain);
        } else if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openDomainList(player);
        } else if (slot == 25) {
            domain.setAllowTeleport(!domain.isAllowTeleport());
            plugin.getDomainManager().saveDomains();
            player.sendMessage("§a允许传送: " + (domain.isAllowTeleport() ? "开启" : "关闭"));
            gui.openDomainManage(player, domain);
        }
    }
    
    private void handleMessageClick(Player player, String domainId, int slot, String itemName) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.GREEN + "进入消息")) {
            player.closeInventory();
            player.sendMessage("§a请输入进入消息（使用 {name} 代表领地名称，§ 代表颜色代码）:");
            plugin.getDomainManager().setPendingMessageType(player.getName().toLowerCase(), "enter");
            plugin.getDomainManager().setPendingDomainId(player.getName().toLowerCase(), domainId);
        } else if (itemName.contains(ChatColor.RED + "离开消息")) {
            player.closeInventory();
            player.sendMessage("§a请输入离开消息（使用 {name} 代表领地名称，§ 代表颜色代码）:");
            plugin.getDomainManager().setPendingMessageType(player.getName().toLowerCase(), "leave");
            plugin.getDomainManager().setPendingDomainId(player.getName().toLowerCase(), domainId);
        } else if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openDomainManage(player, domain);
        }
    }
    
    private void handleAdminManageClick(Player player, String domainId, int slot, String itemName, ClickType clickType) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openDomainManage(player, domain);
            return;
        }
        
        if (itemName.contains(ChatColor.GREEN + "添加管理员")) {
            player.closeInventory();
            player.sendMessage("§a请输入要添加的玩家名称:");
            plugin.getChatInputListener().setAddingAdmin(player.getName(), domainId);
            return;
        }
        
        if (itemName.contains(ChatColor.AQUA.toString())) {
            String adminName = ChatColor.stripColor(itemName);
            String lowerAdminName = adminName.toLowerCase();
            
            if (domain.isAdmin(lowerAdminName)) {
                if (clickType == ClickType.RIGHT) {
                    domain.removeAdmin(lowerAdminName);
                    domain.removeAdminPermissions(lowerAdminName);
                    plugin.getDomainManager().saveDomains();
                    player.sendMessage("§a已移除管理员: " + adminName);
                    gui.openAdminManage(player, domain);
                } else {
                    gui.openAdminPermissionManage(player, domain, lowerAdminName);
                }
            }
        }
    }
    
    private void handlePermissionClick(Player player, String domainId, int slot, String itemName) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openDomainManage(player, domain);
            return;
        }
        
        Domain.Permissions perm = domain.getPermissions();
        boolean toggle = itemName.startsWith(ChatColor.GREEN + "");
        
        switch (slot) {
            case 19:
                perm.setVisitorMove(!toggle);
                break;
            case 20:
                perm.setVisitorBuild(!toggle);
                break;
            case 21:
                perm.setVisitorUse(!toggle);
                break;
            case 22:
                perm.setVisitorOpenChest(!toggle);
                break;
            case 23:
                perm.setVisitorPvp(!toggle);
                break;
        }
        
        plugin.getDomainManager().saveDomains();
        gui.openPermissionManage(player, domain);
    }
    
    private void handleEnvironmentClick(Player player, String domainId, int slot, String itemName) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openDomainManage(player, domain);
            return;
        }
        
        if (itemName.contains("===")) {
            return;
        }
        
        Domain.EnvironmentSettings env = domain.getEnvironment();
        boolean toggle = itemName.startsWith(ChatColor.GREEN + "");
        
        switch (slot) {
            case 1:
                env.setMobSpawn(!toggle);
                break;
            case 10:
                env.setCreeperExplosion(!toggle);
                break;
            case 11:
                env.setTntExplosion(!toggle);
                break;
            case 12:
                env.setTntBlockDamage(!toggle);
                break;
            case 13:
                env.setTntEntityDamage(!toggle);
                break;
            case 14:
                env.setTntChainReaction(!toggle);
                break;
            case 15:
                env.setTntFireSpread(!toggle);
                break;
            case 16:
                env.setEndCrystalExplosion(!toggle);
                break;
            case 17:
                env.setWitherExplosion(!toggle);
                break;
            case 19:
                env.setEndermanGrief(!toggle);
                break;
            case 20:
                env.setPistonPush(!toggle);
                break;
            case 21:
                env.setPistonPull(!toggle);
                break;
            case 22:
                env.setMobGrief(!toggle);
                break;
            case 23:
                env.setGravityBlockFall(!toggle);
                break;
            case 28:
                env.setFireSpread(!toggle);
                break;
            case 29:
                env.setIceMelt(!toggle);
                break;
            case 30:
                env.setSnowMelt(!toggle);
                break;
            case 31:
                env.setLeafDecay(!toggle);
                break;
            case 32:
                env.setVineSpread(!toggle);
                break;
            case 37:
                env.setWaterFlow(!toggle);
                break;
            case 38:
                env.setLavaFlow(!toggle);
                break;
            case 46:
                env.setCactusGrow(!toggle);
                break;
            case 47:
                env.setSugarcaneGrow(!toggle);
                break;
            case 48:
                env.setSlimeDamage(!toggle);
                break;
        }
        
        plugin.getDomainManager().saveDomains();
        gui.openEnvironmentManage(player, domain);
    }
    
    private void handleAdminPermissionClick(Player player, String domainId, String adminName, int slot, String itemName) {
        Domain domain = plugin.getDomainManager().getDomainById(domainId);
        if (domain == null) {
            player.closeInventory();
            return;
        }
        
        if (itemName.contains(ChatColor.RED + "返回")) {
            gui.openAdminManage(player, domain);
            return;
        }
        
        Domain.AdminPermissions adminPerms = domain.getAdminPermissions(adminName);
        boolean toggle = itemName.startsWith(ChatColor.GREEN + "");
        
        switch (slot) {
            case 19:
                adminPerms.setCanBuild(!toggle);
                break;
            case 20:
                adminPerms.setCanManagePermissions(!toggle);
                break;
            case 21:
                adminPerms.setCanManageEnvironment(!toggle);
                break;
            case 22:
                adminPerms.setCanManageAdmins(!toggle);
                break;
            case 23:
                adminPerms.setCanTeleport(!toggle);
                break;
            case 24:
                adminPerms.setCanDelete(!toggle);
                break;
        }
        
        plugin.getDomainManager().saveDomains();
        gui.openAdminPermissionManage(player, domain, adminName);
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        
        if (!(inventory.getHolder() instanceof DomainInventoryHolder)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            Player player = (Player) event.getWhoClicked();
            player.getInventory().addItem(event.getCursor());
            event.getCursor().setType(Material.AIR);
        }
    }
}