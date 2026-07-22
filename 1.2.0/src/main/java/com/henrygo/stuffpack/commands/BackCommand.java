package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public BackCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getTeleportManager().teleportBack(player.getName())) {
            sender.sendMessage("§c没有可以返回的位置");
            return true;
        }
        
        sender.sendMessage("§a已返回上一个位置");
        return true;
    }
}