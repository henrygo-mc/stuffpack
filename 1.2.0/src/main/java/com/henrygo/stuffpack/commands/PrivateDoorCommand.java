package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PrivateDoorCommand implements CommandExecutor {

    private final StuffPack plugin;

    public PrivateDoorCommand(StuffPack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以执行此命令");
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage("§c你没有权限使用此命令");
            return true;
        }

        int amount = 1;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                player.sendMessage("§c无效的数量");
                return true;
            }
        }

        ItemStack privateDoor = createPrivateDoor(amount);
        player.getInventory().addItem(privateDoor);
        player.sendMessage("§a已获得 " + amount + " 个私人门！");

        return true;
    }

    private ItemStack createPrivateDoor(int amount) {
        ItemStack door = new ItemStack(Material.OAK_DOOR, amount);
        ItemMeta meta = door.getItemMeta();
        
        meta.setDisplayName("§d私人门");
        meta.setLore(Arrays.asList(
            "§7只有放置者可以打开",
            "§7其他人无法开启"
        ));
        
        door.setItemMeta(meta);
        return door;
    }
}