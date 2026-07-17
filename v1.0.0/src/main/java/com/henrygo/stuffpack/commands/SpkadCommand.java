package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpkadCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public SpkadCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("stuffpack.admin")) {
            sender.sendMessage("§c你没有权限执行此命令");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /spkad <玩家> <内容>");
            return true;
        }
        
        String playerName = args[0];
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§c玩家不存在或不在线");
            return true;
        }
        
        Bukkit.broadcastMessage("<" + target.getName() + "> " + message.toString().trim());
        sender.sendMessage("§a已强制玩家 " + target.getName() + " 发言");
        return true;
    }
}