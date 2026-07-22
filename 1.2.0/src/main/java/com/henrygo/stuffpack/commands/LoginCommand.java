package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public LoginCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (plugin.getLoginManager().isLoggedIn(player.getName())) {
            player.sendMessage("§c你已经登录了");
            return true;
        }
        
        if (!plugin.getLoginManager().isRegistered(player.getName())) {
            player.sendMessage("§c你还未注册，请使用 /reg <密码> <确认密码> 注册");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§c用法: /login <密码>");
            return true;
        }
        
        String password = args[0];
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "";
        
        if (plugin.getLoginManager().login(player.getName(), password, ip)) {
            plugin.getPerformanceManager().onPlayerJoin(player);
            plugin.getLoginManager().applyLoggedInState(player);
            if (plugin.isMusicEnabled() && plugin.getMusicManager().isAutoSendResourcePack()) {
                plugin.getMusicManager().sendResourcePack(player);
            }
        } else {
            player.sendMessage("§c密码错误");
        }
        
        return true;
    }
}