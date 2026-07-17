package com.henrygo.stuffpack.managers;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {
    
    private final StuffPack plugin;
    
    public ScoreboardManager(StuffPack plugin) {
        this.plugin = plugin;
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
        
        Score spacer1 = objective.getScore(" ");
        spacer1.setScore(3);
        
        Score balanceScore = objective.getScore(ChatColor.GRAY + "积分:");
        balanceScore.setScore(2);
        
        Score rankScore = objective.getScore(ChatColor.GRAY + "排名:");
        rankScore.setScore(1);
        
        Score spacer2 = objective.getScore("");
        spacer2.setScore(0);
        
        player.setScoreboard(scoreboard);
    }
    
    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        
        Team balanceTeam = scoreboard.getTeam("balance");
        if (balanceTeam != null) {
            balanceTeam.setSuffix(ChatColor.GREEN + String.valueOf(plugin.getCoinEconomy().getBalance(player.getName())));
        }
        
        Team rankTeam = scoreboard.getTeam("rank");
        if (rankTeam != null) {
            rankTeam.setSuffix(ChatColor.BLUE + String.valueOf(plugin.getCoinEconomy().getRank(player.getName())));
        }
    }
}