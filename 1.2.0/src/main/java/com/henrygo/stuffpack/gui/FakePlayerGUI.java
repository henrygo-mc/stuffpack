package com.henrygo.stuffpack.gui;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerGUI {
    
    private final StuffPack plugin;
    
    public FakePlayerGUI(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    public void openFakePlayerMenu(Player player, String fakePlayerName) {
        if (player.isOp()) {
            openAdminMenu(player, fakePlayerName);
        } else {
            openNormalMenu(player, fakePlayerName);
        }
    }
    
    public void openNormalMenu(Player player, String fakePlayerName) {
        Inventory inventory = Bukkit.createInventory(new FakePlayerInventoryHolder("normal_" + fakePlayerName), 27, ChatColor.GOLD + fakePlayerName);
        
        ItemStack headItem = createPlayerHead(fakePlayerName, ChatColor.WHITE + fakePlayerName,
            "NPC",
            "点击打招呼");
        inventory.setItem(13, headItem);
        
        ItemStack closeItem = createItem(Material.BARRIER, ChatColor.RED + "关闭");
        inventory.setItem(22, closeItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    public void openAdminMenu(Player player, String fakePlayerName) {
        Inventory inventory = Bukkit.createInventory(new FakePlayerInventoryHolder("admin_" + fakePlayerName), 54, ChatColor.RED + "管理: " + fakePlayerName);
        
        ItemStack infoItem = createPlayerHead(fakePlayerName, ChatColor.WHITE + "假玩家信息",
            "名称: " + fakePlayerName,
            "状态: 在线",
            "权限: OP",
            "游戏模式: 创造");
        inventory.setItem(10, infoItem);
        
        ItemStack teleportItem = createItem(Material.ENDER_PEARL, ChatColor.LIGHT_PURPLE + "传送",
            "传送到假玩家位置");
        inventory.setItem(12, teleportItem);
        
        ItemStack cmdItem = createItem(Material.COMMAND_BLOCK, ChatColor.YELLOW + "执行指令",
            "让假玩家执行指定指令");
        inventory.setItem(14, cmdItem);
        
        ItemStack setLeftClickItem = createItem(Material.STICK, ChatColor.GOLD + "设置左键指令",
            "设置玩家左键点击假玩家时执行的指令",
            "当前: " + getLeftClickCommandDisplay(fakePlayerName));
        inventory.setItem(16, setLeftClickItem);
        
        ItemStack viewModeItem = createItem(Material.PAINTING, ChatColor.AQUA + "查看普通界面",
            "切换到非OP玩家看到的界面");
        inventory.setItem(28, viewModeItem);
        
        ItemStack debugItem = createItem(Material.BLAZE_POWDER, ChatColor.LIGHT_PURPLE + "调试模式",
            "以普通玩家身份左键测试",
            "（会被强迫执行左键指令）");
        inventory.setItem(30, debugItem);
        
        ItemStack kickItem = createItem(Material.RED_CONCRETE, ChatColor.RED + "移除假玩家",
            "永久移除此假玩家");
        inventory.setItem(32, kickItem);
        
        ItemStack listItem = createItem(Material.CHEST, ChatColor.BLUE + "假玩家列表",
            "查看所有假玩家");
        inventory.setItem(34, listItem);
        
        ItemStack closeItem = createItem(Material.BARRIER, ChatColor.RED + "关闭");
        inventory.setItem(49, closeItem);
        
        fillBorder(inventory);
        player.openInventory(inventory);
    }
    
    private String getLeftClickCommandDisplay(String fakePlayerName) {
        String cmd = plugin.getFakePlayerManager().getLeftClickCommand(fakePlayerName);
        if (cmd == null || cmd.isEmpty()) {
            return "未设置";
        }
        return cmd.length() > 30 ? cmd.substring(0, 30) + "..." : cmd;
    }
    
    public void openFakePlayerList(Player player) {
        List<String> fakePlayers = new ArrayList<>(plugin.getFakePlayerManager().getAllFakePlayers().keySet());
        
        if (fakePlayers.isEmpty()) {
            player.sendMessage("§c没有假玩家");
            return;
        }
        
        int size = Math.min(((fakePlayers.size() + 8) / 9) * 9 + 9, 54);
        Inventory inventory = Bukkit.createInventory(new FakePlayerInventoryHolder("list"), size, ChatColor.GOLD + "假玩家列表");
        
        int slot = 0;
        for (String name : fakePlayers) {
            ItemStack playerItem = createPlayerHead(name, ChatColor.GREEN + name,
                "点击查看详情",
                "右键直接移除");
            inventory.setItem(slot++, playerItem);
        }
        
        ItemStack backItem = createItem(Material.ARROW, ChatColor.RED + "返回");
        inventory.setItem(size - 1, backItem);
        
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
    
    private ItemStack createPlayerHead(String playerName, String name, String... lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(name);
        
        try {
            meta.setOwner(playerName);
        } catch (Exception e) {
        }
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        meta.setLore(loreList);
        
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
    
    public static class FakePlayerInventoryHolder implements InventoryHolder {
        private final String type;
        
        public FakePlayerInventoryHolder(String type) {
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