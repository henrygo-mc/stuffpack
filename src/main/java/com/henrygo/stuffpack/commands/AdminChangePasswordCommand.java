package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminChangePasswordCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public AdminChangePasswordCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("stuffpack.admin.acp")) {
            sender.sendMessage("§c没有权限执行此命令");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§c用法: /acp <玩家> <新密码>");
            return true;
        }
        
        String playerName = args[0];
        String newPassword = args[1];
        
        if (plugin.getLoginManager().adminChangePassword(sender.getName(), playerName, newPassword)) {
            sender.sendMessage("§a已强制修改玩家 " + playerName + " 的密码");
        } else {
            sender.sendMessage("§c玩家不存在或未注册");
        }
        
        return true;
    }
}