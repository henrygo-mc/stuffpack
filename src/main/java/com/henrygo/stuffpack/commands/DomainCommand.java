package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Domain;
import com.henrygo.stuffpack.gui.DomainMenuGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DomainCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    private final DomainMenuGUI gui;
    
    public DomainCommand(StuffPack plugin) {
        this.plugin = plugin;
        this.gui = new DomainMenuGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            gui.openMainMenu(player);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "create":
                return handleCreate(player, args);
            case "tp":
                return handleTp(player, args);
            case "addadmin":
                return handleAddAdmin(player, args);
            case "name":
                return handleName(player, args);
            case "ad":
                return handleAdmin(player, args);
            default:
                gui.openMainMenu(player);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        String playerName = player.getName();
        
        if (args.length < 2) {
            player.sendMessage("§c用法: /dom create <名称>");
            return true;
        }
        
        String name = args[1];
        
        if (!plugin.getSelectionManager().hasCompleteSelection(playerName)) {
            player.sendMessage("§c请先用木杵选择两个对角点");
            return true;
        }
        
        if (!plugin.getSelectionManager().hasSameWorld(playerName)) {
            player.sendMessage("§c两个点必须在同一个世界");
            return true;
        }
        
        for (Domain existing : plugin.getDomainManager().getAllDomains()) {
            if (existing.getName().equals(name)) {
                player.sendMessage("§c该领地名称已存在");
                return true;
            }
        }
        
        Location pos1 = plugin.getSelectionManager().getPos1(playerName);
        Location pos2 = plugin.getSelectionManager().getPos2(playerName);
        
        Domain existing = plugin.getDomainManager().getDomainAt(pos1);
        if (existing != null) {
            player.sendMessage("§c该区域已经被圈地了");
            return true;
        }
        
        existing = plugin.getDomainManager().getDomainAt(pos2);
        if (existing != null) {
            player.sendMessage("§c该区域已经被圈地了");
            return true;
        }
        
        int volume = calculateVolume(pos1, pos2);
        long cost = volume;
        
        if (plugin.getCoinEconomy().getBalance(playerName) < cost) {
            player.sendMessage("§c金币不足，需要 " + cost + " 金币");
            return true;
        }
        
        plugin.getCoinEconomy().removeCoins(playerName, cost);
        Domain domain = plugin.getDomainManager().createDomain(playerName, pos1, pos2);
        domain.setName(name);
        plugin.getDomainManager().saveDomains();
        plugin.getSelectionManager().clearSelection(playerName);
        
        player.sendMessage("§a领地创建成功!");
        player.sendMessage("§a领地ID: " + domain.getId().substring(0, 8));
        player.sendMessage("§a领地名称: " + domain.getName());
        player.sendMessage("§a体积: " + volume + " 方块");
        player.sendMessage("§a花费: " + cost + " 金币");
        plugin.getScoreboardManager().updateScoreboard(player);
        return true;
    }
    
    private int calculateVolume(Location pos1, Location pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
    
    private boolean handleTp(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /dom tp <领地ID/编号>");
            return true;
        }
        
        String target = args[1];
        Domain domain = null;
        
        try {
            int code = Integer.parseInt(target);
            domain = plugin.getDomainManager().getDomainByCode(code);
        } catch (NumberFormatException e) {
            domain = plugin.getDomainManager().getDomain(target);
        }
        
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            return true;
        }
        
        if (!domain.isAllowTeleport()) {
            player.sendMessage("§c该领地已关闭传送功能");
            return true;
        }
        
        plugin.getTeleportManager().savePreviousLocation(player.getName(), player.getLocation());
        player.teleport(domain.getCenter());
        player.sendMessage("§a已传送到领地 " + domain.getName() + " (编号: " + domain.getCode() + ")");
        return true;
    }
    
    private boolean handleAddAdmin(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /dom addadmin <领地ID> <玩家>");
            return true;
        }
        
        String id = args[1];
        Player target = Bukkit.getPlayer(args[2]);
        
        Domain domain = plugin.getDomainManager().getDomain(id);
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            return true;
        }
        
        if (!domain.isOwner(player.getName())) {
            player.sendMessage("§c你不是该领地的主人");
            return true;
        }
        
        if (target == null) {
            player.sendMessage("§c玩家不存在");
            return true;
        }
        
        if (domain.isOwner(target.getName())) {
            player.sendMessage("§c不能将主人设置为管理员");
            return true;
        }
        
        if (domain.isAdmin(target.getName())) {
            player.sendMessage("§c该玩家已经是管理员");
            return true;
        }
        
        domain.addAdmin(target.getName());
        plugin.getDomainManager().saveDomains();
        player.sendMessage("§a成功添加管理员: " + target.getName());
        target.sendMessage("§a你已成为领地 " + domain.getName() + " 的管理员");
        return true;
    }
    
    private boolean handleName(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /dom name <领地ID> <新名称>");
            return true;
        }
        
        String id = args[1];
        String name = args[2];
        
        Domain domain = plugin.getDomainManager().getDomain(id);
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            return true;
        }
        
        if (!domain.isOwnerOrAdmin(player.getName())) {
            player.sendMessage("§c你不是该领地的主人或管理员");
            return true;
        }
        
        for (Domain existing : plugin.getDomainManager().getAllDomains()) {
            if (!existing.getId().equals(id) && existing.getName().equals(name)) {
                player.sendMessage("§c该领地名称已存在");
                return true;
            }
        }
        
        domain.setName(name);
        plugin.getDomainManager().saveDomains();
        player.sendMessage("§a领地名称已修改为: " + name);
        return true;
    }
    
    private boolean handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("stuffpack.admin")) {
            player.sendMessage("§c你没有权限执行此命令");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§c用法: /dom ad <领地名称> 或 /dom ad delete <领地名称>");
            return true;
        }
        
        if (args.length >= 3 && "delete".equalsIgnoreCase(args[1])) {
            String domainName = args[2];
            Domain domain = findDomainByName(domainName);
            
            if (domain == null) {
                player.sendMessage("§c领地不存在");
                return true;
            }
            
            plugin.getDomainManager().deleteDomain(domain.getId());
            player.sendMessage("§a已强制删除领地: " + domain.getName());
            return true;
        }
        
        String domainName = args[1];
        Domain domain = findDomainByName(domainName);
        
        if (domain == null) {
            player.sendMessage("§c领地不存在");
            return true;
        }
        
        gui.openDomainManage(player, domain);
        player.sendMessage("§a已强制进入领地后台管理: " + domain.getName());
        return true;
    }
    
    private Domain findDomainByName(String name) {
        for (Domain domain : plugin.getDomainManager().getAllDomains()) {
            if (domain.getName().equalsIgnoreCase(name)) {
                return domain;
            }
        }
        return null;
    }
}