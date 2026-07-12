package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import com.henrygo.stuffpack.data.Picture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PictureCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public PictureCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("stuffpack.picture")) {
            player.sendMessage("§cYou don't have permission to use this command");
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage("§cUsage: /pic give <URL> <allowBreak(true/false)>");
            return true;
        }
        
        String url = args[1];
        boolean allowBreak;
        
        try {
            allowBreak = Boolean.parseBoolean(args[2]);
        } catch (Exception e) {
            player.sendMessage("§callowBreak must be true or false");
            return true;
        }
        
        Picture picture = plugin.getPictureManager().createPicture(url, player.getLocation(), allowBreak);
        
        if (picture != null) {
            player.sendMessage("§aPicture created successfully!");
            player.sendMessage("§aURL: " + url);
            player.sendMessage("§aSize: " + picture.getWidth() + "x" + picture.getHeight());
            player.sendMessage("§aPieces: " + picture.getPieces());
            player.sendMessage("§aAllow Break: " + (picture.isAllowBreak() ? "Yes" : "No"));
        } else {
            player.sendMessage("§cFailed to create picture, please check if the URL is valid");
        }
        
        return true;
    }
}