package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.MessageData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageReadCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public MessageReadCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getMessageManager().showAllMessages(player);
        
        return true;
    }
}