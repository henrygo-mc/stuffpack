package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpahereCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public TpahereCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§c用法: /tpahere <玩家>");
            return true;
        }
        
        Player from = (Player) sender;
        Player to = Bukkit.getPlayer(args[0]);
        
        if (to == null) {
            sender.sendMessage("§c玩家不存在");
            return true;
        }
        
        if (from.equals(to)) {
            sender.sendMessage("§c不能召唤自己");
            return true;
        }
        
        plugin.getTeleportManager().sendTpahereRequest(from.getName(), to.getName());
        from.sendMessage("§a已向 " + to.getName() + " 发送召唤请求，请等待对方接受");
        to.sendMessage("§a" + from.getName() + " 请求你传送到他身边");
        to.sendMessage("§a输入 /tpaccept 接受，或 /tpignore 忽略");
        return true;
    }
}