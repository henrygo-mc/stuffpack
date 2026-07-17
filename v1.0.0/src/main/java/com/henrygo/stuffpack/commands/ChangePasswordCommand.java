package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public ChangePasswordCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!plugin.getLoginManager().isLoggedIn(player.getName())) {
            player.sendMessage("§c请先登录");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage("§c用法: /cp <旧密码> <新密码> <确认新密码>");
            return true;
        }
        
        String oldPassword = args[0];
        String newPassword = args[1];
        String confirmPassword = args[2];
        
        if (plugin.getLoginManager().changePassword(player.getName(), oldPassword, newPassword, confirmPassword)) {
            player.sendMessage("§a密码修改成功");
        } else {
            player.sendMessage("§c密码修改失败，旧密码错误或新密码不一致");
        }
        
        return true;
    }
}