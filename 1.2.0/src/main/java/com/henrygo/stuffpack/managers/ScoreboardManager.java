package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {
    
    private final StuffPack plugin;
    private int updateTaskId = -1;
    
    public ScoreboardManager(StuffPack plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.getLoginManager().isLoggedIn(player.getName())) {
                    updateScoreboard(player);
                }
            }
        }, 20L, 20L);
    }
    
    public void showScoreboard(Player player) {
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
        
        Objective objective = scoreboard.registerNewObjective("craftgo", "dummy", ChatColor.GOLD + "★ CraftGo ★");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        Team balanceTeam = scoreboard.registerNewTeam("balance");
        balanceTeam.addEntry(ChatColor.GRAY + "积分:");
        balanceTeam.setSuffix(ChatColor.GREEN + String.valueOf(plugin.getCoinEconomy().getBalance(player.getName())));
        
        Team rankTeam = scoreboard.registerNewTeam("rank");
        rankTeam.addEntry(ChatColor.GRAY + "排名:");
        rankTeam.setSuffix(ChatColor.BLUE + String.valueOf(plugin.getCoinEconomy().getRank(player.getName())));
        
        Team pingTeam = scoreboard.registerNewTeam("ping");
        pingTeam.addEntry(ChatColor.GRAY + "延迟:");
        pingTeam.setSuffix(getPingDisplay(player));
        
        Score spacer1 = objective.getScore(" ");
        spacer1.setScore(4);
        
        Score balanceScore = objective.getScore(ChatColor.GRAY + "积分:");
        balanceScore.setScore(3);
        
        Score rankScore = objective.getScore(ChatColor.GRAY + "排名:");
        rankScore.setScore(2);
        
        Score spacer2 = objective.getScore("  ");
        spacer2.setScore(1);
        
        Score pingScore = objective.getScore(ChatColor.GRAY + "延迟:");
        pingScore.setScore(0);
        
        player.setScoreboard(scoreboard);
    }
    
    private String getPingDisplay(Player player) {
        int ping = player.getPing();
        ChatColor color;
        if (ping < 50) {
            color = ChatColor.GREEN;
        } else if (ping < 100) {
            color = ChatColor.YELLOW;
        } else if (ping < 200) {
            color = ChatColor.GOLD;
        } else {
            color = ChatColor.RED;
        }
        return color + String.valueOf(ping) + "ms";
    }
    
    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) return;
        
        Team balanceTeam = scoreboard.getTeam("balance");
        if (balanceTeam != null) {
            balanceTeam.setSuffix(ChatColor.GREEN + String.valueOf(plugin.getCoinEconomy().getBalance(player.getName())));
        }
        
        Team rankTeam = scoreboard.getTeam("rank");
        if (rankTeam != null) {
            rankTeam.setSuffix(ChatColor.BLUE + String.valueOf(plugin.getCoinEconomy().getRank(player.getName())));
        }
        
        Team pingTeam = scoreboard.getTeam("ping");
        if (pingTeam != null) {
            pingTeam.setSuffix(getPingDisplay(player));
        }
    }
}