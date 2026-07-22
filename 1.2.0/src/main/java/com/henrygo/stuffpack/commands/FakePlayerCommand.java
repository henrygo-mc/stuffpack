package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FakePlayerCommand implements CommandExecutor {

    private final StuffPack plugin;

    public FakePlayerCommand(StuffPack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute this command");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("stuffpack.fakeplayer")) {
            player.sendMessage("§cYou don't have permission to use this command");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /fakeplayer <spawn/remove/list/cmd> [name] [skinUrl|command]");
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "spawn":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fakeplayer spawn <name> [skinUrl]");
                    return true;
                }
                String name = args[1];
                String skinUrl = args.length > 2 ? args[2] : null;
                
                Player fakePlayer = plugin.getFakePlayerManager().spawnFakePlayer(name, player.getLocation(), skinUrl);
                if (fakePlayer != null) {
                    player.sendMessage("§a假玩家 " + name + " 已生成!");
                } else {
                    player.sendMessage("§c假玩家 " + name + " 生成失败! 请查看控制台日志获取详细错误信息。");
                }
                break;

            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /fakeplayer remove <name>");
                    return true;
                }
                name = args[1];
                if (plugin.getFakePlayerManager().removeFakePlayer(name)) {
                    player.sendMessage("§a假玩家 " + name + " 已移除!");
                } else {
                    player.sendMessage("§c假玩家 " + name + " 不存在!");
                }
                break;

            case "list":
                player.sendMessage("§6===== 假玩家列表 =====");
                for (String fakeName : plugin.getFakePlayerManager().getAllFakePlayers().keySet()) {
                    player.sendMessage("§a- " + fakeName);
                }
                if (plugin.getFakePlayerManager().getAllFakePlayers().isEmpty()) {
                    player.sendMessage("§c没有假玩家");
                }
                break;

            case "cmd":
            case "command":
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /fakeplayer cmd <name> <command>");
                    player.sendMessage("§7示例: /fakeplayer cmd npc1 /help");
                    return true;
                }
                name = args[1];
                StringBuilder cmdBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) cmdBuilder.append(" ");
                    cmdBuilder.append(args[i]);
                }
                String cmd = cmdBuilder.toString();
                
                if (plugin.getFakePlayerManager().dispatchCommand(name, cmd)) {
                    player.sendMessage("§a已让假玩家 " + name + " 执行命令: " + cmd);
                } else {
                    player.sendMessage("§c命令执行失败! 请检查假玩家是否存在。");
                }
                break;

            default:
                player.sendMessage("§cUnknown action. Use: spawn, remove, list, cmd");
        }

        return true;
    }
}