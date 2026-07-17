package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public CoinCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§c用法: /coin <give|take|pay|balance> [player] [amount]");
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "balance":
                return handleBalance(sender, args);
            default:
                sender.sendMessage("§c未知操作: " + action);
                sender.sendMessage("§c可用操作: give, take, pay, balance");
                return true;
        }
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stuffpack.coin.give")) {
            sender.sendMessage("§c没有权限执行此命令");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§c用法: /coin give <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c玩家不存在");
            return true;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c金额必须是数字");
            return true;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return true;
        }
        
        plugin.getCoinEconomy().addCoins(target.getName(), amount);
        sender.sendMessage("§a成功给予 " + target.getName() + " " + amount + " 金币");
        target.sendMessage("§a你获得了 " + amount + " 金币");
        plugin.getScoreboardManager().updateScoreboard(target);
        return true;
    }
    
    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("stuffpack.coin.take")) {
            sender.sendMessage("§c没有权限执行此命令");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§c用法: /coin take <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§c玩家不存在");
            return true;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c金额必须是数字");
            return true;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return true;
        }
        
        if (!plugin.getCoinEconomy().removeCoins(target.getName(), amount)) {
            sender.sendMessage("§c玩家金币不足");
            return true;
        }
        
        sender.sendMessage("§a成功扣除 " + target.getName() + " " + amount + " 金币");
        target.sendMessage("§c你被扣除了 " + amount + " 金币");
        plugin.getScoreboardManager().updateScoreboard(target);
        return true;
    }
    
    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("§c用法: /coin pay <player> <amount>");
            return true;
        }
        
        Player from = (Player) sender;
        Player to = Bukkit.getPlayer(args[1]);
        
        if (to == null) {
            sender.sendMessage("§c玩家不存在");
            return true;
        }
        
        if (from.equals(to)) {
            sender.sendMessage("§c不能给自己转账");
            return true;
        }
        
        long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c金额必须是数字");
            return true;
        }
        
        if (amount <= 0) {
            sender.sendMessage("§c金额必须大于0");
            return true;
        }
        
        if (!plugin.getCoinEconomy().transferCoins(from.getName(), to.getName(), amount)) {
            sender.sendMessage("§c金币不足");
            return true;
        }
        
        from.sendMessage("§a成功转账给 " + to.getName() + " " + amount + " 金币");
        to.sendMessage("§a你收到了 " + from.getName() + " 转账的 " + amount + " 金币");
        plugin.getScoreboardManager().updateScoreboard(from);
        plugin.getScoreboardManager().updateScoreboard(to);
        return true;
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        String targetName;
        
        if (args.length >= 2) {
            if (!sender.hasPermission("stuffpack.coin.balance.other")) {
                sender.sendMessage("§c没有权限查看他人余额");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c玩家不存在");
                return true;
            }
            targetName = target.getName();
        } else if (sender instanceof Player) {
            targetName = sender.getName();
        } else {
            sender.sendMessage("§c用法: /coin balance [player]");
            return true;
        }
        
        long balance = plugin.getCoinEconomy().getBalance(targetName);
        sender.sendMessage("§a" + targetName + " 的金币余额: " + balance);
        return true;
    }
}