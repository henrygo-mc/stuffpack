package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public RegisterCommand(StuffPack plugin) {
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
        
        if (plugin.getLoginManager().isRegistered(player.getName())) {
            player.sendMessage("§c你已经注册了，请使用 /login <密码> 登录");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§c用法: /reg <密码> <确认密码>");
            return true;
        }
        
        String password = args[0];
        String confirmPassword = args[1];
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "";
        
        if (plugin.getLoginManager().register(player.getName(), password, confirmPassword)) {
            plugin.getLoginManager().login(player.getName(), password, ip);
            plugin.getPerformanceManager().onPlayerJoin(player);
            plugin.getLoginManager().applyLoggedInState(player);
            player.sendMessage("§a注册成功!");
        } else {
            player.sendMessage("§c两次输入的密码不一致");
        }
        
        return true;
    }
}