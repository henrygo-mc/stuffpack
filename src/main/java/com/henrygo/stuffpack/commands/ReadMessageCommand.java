package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReadMessageCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public ReadMessageCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getMessageManager().markAsRead(player.getName());
        player.sendMessage("§aAll unread messages marked as read!");
        
        return true;
    }
}