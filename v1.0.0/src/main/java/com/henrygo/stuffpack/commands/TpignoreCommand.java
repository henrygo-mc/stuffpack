package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpignoreCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public TpignoreCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        Player player = (Player) sender;
        TeleportManager.TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getName());
        
        if (request == null) {
            sender.sendMessage("§c没有待处理的传送请求");
            return true;
        }
        
        plugin.getTeleportManager().ignoreRequest(player.getName());
        sender.sendMessage("§a已忽略传送请求");
        
        Player requester = Bukkit.getPlayer(request.getFrom());
        if (requester != null) {
            requester.sendMessage("§c你的传送请求被拒绝");
        }
        
        return true;
    }
}