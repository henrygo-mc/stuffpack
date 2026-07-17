package com.henrygo.stuffpack;

import com.henrygo.stuffpack.commands.*;
import com.henrygo.stuffpack.listeners.*;
import com.henrygo.stuffpack.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class StuffPack extends JavaPlugin {
    
    private CoinEconomy coinEconomy;
    private DomainManager domainManager;
    private TeleportManager teleportManager;
    private SelectionManager selectionManager;
    private LoginManager loginManager;
    private ChatInputListener chatInputListener;
    private ScoreboardManager scoreboardManager;
    private PictureManager pictureManager;
    private MessageManager messageManager;
    private FakePlayerManager fakePlayerManager;
    private PrivateDoorManager privateDoorManager;
    private PerformanceManager performanceManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        new DataMigrator(this).migrate();
        
        coinEconomy = new CoinEconomy(this);
        domainManager = new DomainManager(this);
        teleportManager = new TeleportManager(this);
        selectionManager = new SelectionManager(this);
        loginManager = new LoginManager(this);
        chatInputListener = new ChatInputListener(this);
        scoreboardManager = new ScoreboardManager(this);
        pictureManager = new PictureManager(this);
        messageManager = new MessageManager(this);
        fakePlayerManager = new FakePlayerManager(this);
        privateDoorManager = new PrivateDoorManager(this);
        performanceManager = new PerformanceManager(this);
        
        registerCommands();
        registerListeners();
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            fakePlayerManager.loadFakePlayers();
        }, 40L);
        
        getLogger().info("StuffPack enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        performanceManager.disable();
        fakePlayerManager.saveFakePlayers();
        fakePlayerManager.removeAllFakePlayers();
        domainManager.saveDomains();
        coinEconomy.saveCoins();
        coinEconomy.saveSignData();
        loginManager.saveData();
        pictureManager.savePictures();
        messageManager.saveMessages();
        privateDoorManager.saveDoors();
        getLogger().info("StuffPack disabled successfully!");
    }
    
    private void registerCommands() {
        getCommand("coin").setExecutor(new CoinCommand(this));
        getCommand("dom").setExecutor(new DomainCommand(this));
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpahere").setExecutor(new TpahereCommand(this));
        getCommand("tpaccept").setExecutor(new TpacceptCommand(this));
        getCommand("tpignore").setExecutor(new TpignoreCommand(this));
        getCommand("back").setExecutor(new BackCommand(this));
        getCommand("help").setExecutor(new HelpCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("reg").setExecutor(new RegisterCommand(this));
        getCommand("cp").setExecutor(new ChangePasswordCommand(this));
        getCommand("acp").setExecutor(new AdminChangePasswordCommand(this));
        getCommand("spkad").setExecutor(new SpkadCommand(this));
        getCommand("pic").setExecutor(new PictureCommand(this));
        getCommand("offms").setExecutor(new OfflineMessageCommand(this));
        getCommand("rd").setExecutor(new ReadMessageCommand(this));
        getCommand("msread").setExecutor(new MessageReadCommand(this));
        getCommand("fakeplayer").setExecutor(new FakePlayerCommand(this));
        getCommand("privatedoor").setExecutor(new PrivateDoorCommand(this));
    }
    
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new SelectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DomainProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DomainEnterListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(chatInputListener, this);
        Bukkit.getPluginManager().registerEvents(new DomainEnvironmentListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FakePlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PrivateDoorListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PictureListener(this), this);
        Bukkit.getPluginManager().registerEvents(performanceManager, this);
    }
    
    public CoinEconomy getCoinEconomy() {
        return coinEconomy;
    }
    
    public DomainManager getDomainManager() {
        return domainManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    public LoginManager getLoginManager() {
        return loginManager;
    }
    
    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public PictureManager getPictureManager() {
        return pictureManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public FakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }
    
    public PrivateDoorManager getPrivateDoorManager() {
        return privateDoorManager;
    }
    
    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }
}