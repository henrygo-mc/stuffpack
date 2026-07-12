package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.managers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpacceptCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public TpacceptCommand(StuffPack plugin) {
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
        
        Player requester = Bukkit.getPlayer(request.getFrom());
        if (requester == null) {
            sender.sendMessage("§c请求者已离线");
            plugin.getTeleportManager().ignoreRequest(player.getName());
            return true;
        }
        
        plugin.getTeleportManager().acceptRequest(player.getName());
        
        if (request.getType() == TeleportManager.TeleportRequest.Type.TPA) {
            requester.sendMessage("§a传送请求已被接受");
            player.sendMessage("§a已允许 " + requester.getName() + " 传送到你身边");
        } else {
            player.sendMessage("§a你将被传送到 " + requester.getName() + " 身边");
            requester.sendMessage("§a" + player.getName() + " 接受了你的召唤请求");
        }
        
        return true;
    }
}