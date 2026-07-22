package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.managers.MusicManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class MusicCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public MusicCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                handleList(sender);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "add":
                handleAdd(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== 音乐系统命令 ===");
        sender.sendMessage("§e/music list §7- 查看所有音乐");
        sender.sendMessage("§e/music give <id> §7- 获得音乐物品");
        sender.sendMessage("§e/music add <id> <名称> §7- 添加音乐(OGG文件放plugins/StuffPack/music/下)");
        sender.sendMessage("§e/music remove <id> §7- 移除音乐");
        sender.sendMessage("§e/music reload §7- 重新生成资源包");
    }
    
    private void handleList(CommandSender sender) {
        MusicManager manager = plugin.getMusicManager();
        sender.sendMessage("§6=== 音乐列表 (" + manager.getAllTracks().size() + ") ===");
        for (MusicManager.MusicTrack track : manager.getAllTracks()) {
            sender.sendMessage("§e" + track.getId() + " §7- §f" + track.getName());
        }
    }
    
    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return;
        }
        
        if (!sender.hasPermission("stuffpack.music.give")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /music give <id>");
            return;
        }
        
        String trackId = args[1];
        if (!plugin.getMusicManager().hasTrack(trackId)) {
            sender.sendMessage("§c找不到音乐ID: " + trackId);
            return;
        }
        
        Player player = (Player) sender;
        ItemStack musicItem = plugin.getMusicManager().createMusicItem(trackId);
        
        if (musicItem != null) {
            player.getInventory().addItem(musicItem);
            sender.sendMessage("§a已获得音乐物品: " + plugin.getMusicManager().getTrack(trackId).getName());
        }
    }
    
    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stuffpack.music.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§c用法: /music add <id> <名称>");
            sender.sendMessage("§7请先将OGG文件放入 plugins/StuffPack/music/<id>.ogg");
            return;
        }
        
        String trackId = args[1];
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) nameBuilder.append(" ");
            nameBuilder.append(args[i]);
        }
        String trackName = nameBuilder.toString();
        
        File oggFile = new File(plugin.getMusicManager().getMusicFolder(), trackId + ".ogg");
        if (!oggFile.exists()) {
            sender.sendMessage("§c找不到OGG文件: " + oggFile.getPath());
            sender.sendMessage("§7请先将音乐文件放入 plugins/StuffPack/music/" + trackId + ".ogg");
            return;
        }
        
        plugin.getMusicManager().addTrack(trackId, trackName, oggFile);
        sender.sendMessage("§a已添加音乐: §e" + trackId + " §7- §f" + trackName);
        sender.sendMessage("§a资源包已自动重新生成");
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stuffpack.music.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /music remove <id>");
            return;
        }
        
        String trackId = args[1];
        if (!plugin.getMusicManager().hasTrack(trackId)) {
            sender.sendMessage("§c找不到音乐ID: " + trackId);
            return;
        }
        
        plugin.getMusicManager().removeTrack(trackId);
        sender.sendMessage("§a已移除音乐: " + trackId);
        sender.sendMessage("§a资源包已自动重新生成");
    }
    
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("stuffpack.music.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return;
        }
        
        plugin.getMusicManager().generateResourcePack();
        sender.sendMessage("§a资源包已重新生成");
    }
}
