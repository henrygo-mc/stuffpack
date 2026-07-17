package com.henrygo.stuffpack.commands;

import com.henrygo.stuffpack.StuffPack;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {
    
    private final StuffPack plugin;
    
    public HelpCommand(StuffPack plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "========== StuffPack 帮助 ==========");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[登录系统]");
        sender.sendMessage(ChatColor.WHITE + "/reg <密码> <确认密码>" + ChatColor.GRAY + " - 注册账号");
        sender.sendMessage(ChatColor.WHITE + "/login <密码>" + ChatColor.GRAY + " - 登录账号");
        sender.sendMessage(ChatColor.WHITE + "/cp <旧密码> <新密码> <确认密码>" + ChatColor.GRAY + " - 修改密码");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[金币系统]");
        sender.sendMessage(ChatColor.WHITE + "/coin balance" + ChatColor.GRAY + " - 查看金币余额");
        sender.sendMessage(ChatColor.WHITE + "/coin pay <玩家> <金额>" + ChatColor.GRAY + " - 向玩家转账");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[领地系统]");
        sender.sendMessage(ChatColor.WHITE + "使用木杵" + ChatColor.GRAY + " - 左键设置点1，右键设置点2");
        sender.sendMessage(ChatColor.WHITE + "/dom create <名称>" + ChatColor.GRAY + " - 创建领地（消耗体积等量金币）");
        sender.sendMessage(ChatColor.WHITE + "/dom" + ChatColor.GRAY + " - 打开领地管理菜单");
        sender.sendMessage(ChatColor.WHITE + "/dom tp <ID/编号>" + ChatColor.GRAY + " - 传送到领地");
        sender.sendMessage(ChatColor.WHITE + "/dom addadmin <领地ID> <玩家>" + ChatColor.GRAY + " - 添加领地管理员");
        sender.sendMessage(ChatColor.WHITE + "/dom name <领地ID> <新名称>" + ChatColor.GRAY + " - 修改领地名称");
        sender.sendMessage(ChatColor.WHITE + "/dom move <领地ID/名称> <x偏移> <y偏移> <z偏移>" + ChatColor.GRAY + " - 移动领地（如 x+1 y-1 z+0）");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[传送系统]");
        sender.sendMessage(ChatColor.WHITE + "/tpa <玩家>" + ChatColor.GRAY + " - 请求传送到玩家身边");
        sender.sendMessage(ChatColor.WHITE + "/tpahere <玩家>" + ChatColor.GRAY + " - 请求玩家传送到你身边");
        sender.sendMessage(ChatColor.WHITE + "/tpaccept" + ChatColor.GRAY + " - 接受传送请求");
        sender.sendMessage(ChatColor.WHITE + "/tpignore" + ChatColor.GRAY + " - 忽略传送请求");
        sender.sendMessage(ChatColor.WHITE + "/back" + ChatColor.GRAY + " - 返回上一个位置");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[留言系统]");
        sender.sendMessage(ChatColor.WHITE + "/offms <玩家> <内容>" + ChatColor.GRAY + " - 给离线玩家留言");
        sender.sendMessage(ChatColor.WHITE + "/offms server <内容>" + ChatColor.GRAY + " - 向服务器发送留言");
        sender.sendMessage(ChatColor.WHITE + "/rd" + ChatColor.GRAY + " - 标记所有留言为已读");
        sender.sendMessage(ChatColor.WHITE + "/msread" + ChatColor.GRAY + " - 查看所有留言");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[图片系统]");
        sender.sendMessage(ChatColor.WHITE + "/pic give <URL> <是否可破坏>" + ChatColor.GRAY + " - 获取自定义图片地图");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[假玩家系统]");
        sender.sendMessage(ChatColor.WHITE + "/fakeplayer spawn <名称> [皮肤URL]" + ChatColor.GRAY + " - 生成假玩家");
        sender.sendMessage(ChatColor.WHITE + "/fakeplayer remove <名称>" + ChatColor.GRAY + " - 移除假玩家");
        sender.sendMessage(ChatColor.WHITE + "/fakeplayer list" + ChatColor.GRAY + " - 列出所有假玩家");
        
        sender.sendMessage(ChatColor.YELLOW + "\n[私人门系统]");
        sender.sendMessage(ChatColor.WHITE + "/privatedoor [数量]" + ChatColor.GRAY + " - 获取私人门（只有放置者可打开）");
        
        boolean isAdmin = sender.hasPermission("stuffpack.admin") || 
            (sender instanceof org.bukkit.entity.Player && ((org.bukkit.entity.Player) sender).isOp()) ||
            !(sender instanceof org.bukkit.entity.Player);
        
        if (isAdmin) {
            sender.sendMessage(ChatColor.YELLOW + "\n[管理员命令]");
            sender.sendMessage(ChatColor.WHITE + "/acp <玩家> <新密码>" + ChatColor.GRAY + " - 强制修改玩家密码");
            sender.sendMessage(ChatColor.WHITE + "/coin give <玩家> <金额>" + ChatColor.GRAY + " - 给予玩家金币");
            sender.sendMessage(ChatColor.WHITE + "/coin take <玩家> <金额>" + ChatColor.GRAY + " - 扣除玩家金币");
            sender.sendMessage(ChatColor.WHITE + "/coin balance <玩家>" + ChatColor.GRAY + " - 查看他人金币余额");
            sender.sendMessage(ChatColor.WHITE + "/dom ad <领地名称>" + ChatColor.GRAY + " - 强制进入任意领地后台管理");
            sender.sendMessage(ChatColor.WHITE + "/dom ad delete <领地名称>" + ChatColor.GRAY + " - 强制删除任意领地");
            sender.sendMessage(ChatColor.WHITE + "/spkad <玩家> <内容>" + ChatColor.GRAY + " - 强制玩家发言");
            sender.sendMessage(ChatColor.WHITE + "/fakeplayer spawn <名称> [皮肤URL]" + ChatColor.GRAY + " - 生成假玩家（需权限）");
        }
        
        sender.sendMessage(ChatColor.GOLD + "==================================");
        return true;
    }
}