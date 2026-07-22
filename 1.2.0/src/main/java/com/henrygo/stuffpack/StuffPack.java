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
    private MusicManager musicManager;
    
    private boolean loginEnabled;
    private boolean domainEnabled;
    private boolean coinEnabled;
    private boolean teleportEnabled;
    private boolean backEnabled;
    private boolean fakePlayerEnabled;
    private boolean pictureEnabled;
    private boolean messageEnabled;
    private boolean privateDoorEnabled;
    private boolean musicEnabled;
    private boolean performanceEnabled;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        loadModuleConfig();
        
        new DataMigrator(this).migrate();
        
        if (coinEnabled) {
            coinEconomy = new CoinEconomy(this);
            scoreboardManager = new ScoreboardManager(this);
        }
        
        if (domainEnabled) {
            domainManager = new DomainManager(this);
            selectionManager = new SelectionManager(this);
        }
        
        if (teleportEnabled) {
            teleportManager = new TeleportManager(this);
        }
        
        if (loginEnabled) {
            loginManager = new LoginManager(this);
            chatInputListener = new ChatInputListener(this);
        }
        
        if (pictureEnabled) {
            pictureManager = new PictureManager(this);
        }
        
        if (messageEnabled) {
            messageManager = new MessageManager(this);
        }
        
        if (fakePlayerEnabled) {
            fakePlayerManager = new FakePlayerManager(this);
        }
        
        if (privateDoorEnabled) {
            privateDoorManager = new PrivateDoorManager(this);
        }
        
        if (performanceEnabled) {
            performanceManager = new PerformanceManager(this);
        }
        
        if (musicEnabled) {
            musicManager = new MusicManager(this);
        }
        
        registerCommands();
        registerListeners();
        
        if (fakePlayerEnabled) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                fakePlayerManager.loadFakePlayers();
            }, 40L);
        }
        
        logEnabledModules();
        getLogger().info("StuffPack enabled successfully!");
    }
    
    private void loadModuleConfig() {
        loginEnabled = getConfig().getBoolean("modules.login", true);
        domainEnabled = getConfig().getBoolean("modules.domain", true);
        coinEnabled = getConfig().getBoolean("modules.coin", true);
        teleportEnabled = getConfig().getBoolean("modules.teleport", true);
        backEnabled = getConfig().getBoolean("modules.back", true);
        fakePlayerEnabled = getConfig().getBoolean("modules.fakeplayer", true);
        pictureEnabled = getConfig().getBoolean("modules.picture", true);
        messageEnabled = getConfig().getBoolean("modules.message", true);
        privateDoorEnabled = getConfig().getBoolean("modules.privatedoor", true);
        musicEnabled = getConfig().getBoolean("modules.music", true);
        performanceEnabled = getConfig().getBoolean("modules.performance", true);
    }
    
    private void logEnabledModules() {
        getLogger().info("=== 已启用模块 ===");
        if (loginEnabled) getLogger().info("  ✅ 登录系统");
        if (domainEnabled) getLogger().info("  ✅ 领地系统");
        if (coinEnabled) getLogger().info("  ✅ 经济/签到系统");
        if (teleportEnabled) getLogger().info("  ✅ TPA传送");
        if (backEnabled) getLogger().info("  ✅ 返回死亡点");
        if (fakePlayerEnabled) getLogger().info("  ✅ 假玩家系统");
        if (pictureEnabled) getLogger().info("  ✅ 图片地图");
        if (messageEnabled) getLogger().info("  ✅ 私信系统");
        if (privateDoorEnabled) getLogger().info("  ✅ 私密门系统");
        if (musicEnabled) getLogger().info("  ✅ 自定义音乐");
        if (performanceEnabled) getLogger().info("  ✅ 性能优化");
    }
    
    @Override
    public void onDisable() {
        if (performanceManager != null) {
            performanceManager.disable();
        }
        if (fakePlayerManager != null) {
            fakePlayerManager.saveFakePlayers();
            fakePlayerManager.removeAllFakePlayers();
        }
        if (domainManager != null) {
            domainManager.saveDomains();
        }
        if (coinEconomy != null) {
            coinEconomy.saveCoins();
            coinEconomy.saveSignData();
        }
        if (loginManager != null) {
            loginManager.saveData();
        }
        if (pictureManager != null) {
            pictureManager.savePictures();
        }
        if (messageManager != null) {
            messageManager.saveMessages();
        }
        if (privateDoorManager != null) {
            privateDoorManager.saveDoors();
        }
        if (musicManager != null) {
            musicManager.stopAllMusic();
        }
        getLogger().info("StuffPack disabled successfully!");
    }
    
    private void registerCommands() {
        if (coinEnabled) {
            getCommand("coin").setExecutor(new CoinCommand(this));
        }
        if (domainEnabled) {
            getCommand("dom").setExecutor(new DomainCommand(this));
        }
        if (teleportEnabled) {
            getCommand("tpa").setExecutor(new TpaCommand(this));
            getCommand("tpahere").setExecutor(new TpahereCommand(this));
            getCommand("tpaccept").setExecutor(new TpacceptCommand(this));
            getCommand("tpignore").setExecutor(new TpignoreCommand(this));
        }
        if (backEnabled) {
            getCommand("back").setExecutor(new BackCommand(this));
        }
        getCommand("help").setExecutor(new HelpCommand(this));
        if (loginEnabled) {
            getCommand("login").setExecutor(new LoginCommand(this));
            getCommand("reg").setExecutor(new RegisterCommand(this));
            getCommand("cp").setExecutor(new ChangePasswordCommand(this));
            getCommand("acp").setExecutor(new AdminChangePasswordCommand(this));
            getCommand("spkad").setExecutor(new SpkadCommand(this));
        }
        if (pictureEnabled) {
            getCommand("pic").setExecutor(new PictureCommand(this));
        }
        if (messageEnabled) {
            getCommand("offms").setExecutor(new OfflineMessageCommand(this));
            getCommand("rd").setExecutor(new ReadMessageCommand(this));
            getCommand("msread").setExecutor(new MessageReadCommand(this));
        }
        if (fakePlayerEnabled) {
            getCommand("fakeplayer").setExecutor(new FakePlayerCommand(this));
        }
        if (privateDoorEnabled) {
            getCommand("privatedoor").setExecutor(new PrivateDoorCommand(this));
        }
        if (musicEnabled) {
            getCommand("music").setExecutor(new MusicCommand(this));
        }
    }
    
    private void registerListeners() {
        if (domainEnabled) {
            Bukkit.getPluginManager().registerEvents(new SelectionListener(this), this);
            Bukkit.getPluginManager().registerEvents(new DomainProtectionListener(this), this);
            Bukkit.getPluginManager().registerEvents(new DomainEnterListener(this), this);
            Bukkit.getPluginManager().registerEvents(new DomainEnvironmentListener(this), this);
        }
        if (domainEnabled || fakePlayerEnabled) {
            Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(this), this);
        }
        if (backEnabled) {
            Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        }
        if (domainEnabled) {
            Bukkit.getPluginManager().registerEvents(new ExplosionListener(this), this);
        }
        if (domainEnabled) {
            Bukkit.getPluginManager().registerEvents(new InventoryClickListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        if (loginEnabled) {
            Bukkit.getPluginManager().registerEvents(new LoginProtectionListener(this), this);
            Bukkit.getPluginManager().registerEvents(chatInputListener, this);
        }
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this), this);
        if (fakePlayerEnabled) {
            Bukkit.getPluginManager().registerEvents(new FakePlayerListener(this), this);
        }
        if (privateDoorEnabled) {
            Bukkit.getPluginManager().registerEvents(new PrivateDoorListener(this), this);
        }
        if (pictureEnabled) {
            Bukkit.getPluginManager().registerEvents(new PictureListener(this), this);
        }
        if (musicEnabled) {
            Bukkit.getPluginManager().registerEvents(new MusicListener(this), this);
            Bukkit.getPluginManager().registerEvents(new ResourcePackListener(this), this);
        }
        if (performanceEnabled) {
            Bukkit.getPluginManager().registerEvents(performanceManager, this);
        }
    }
    
    public boolean isLoginEnabled() { return loginEnabled; }
    public boolean isDomainEnabled() { return domainEnabled; }
    public boolean isCoinEnabled() { return coinEnabled; }
    public boolean isTeleportEnabled() { return teleportEnabled; }
    public boolean isBackEnabled() { return backEnabled; }
    public boolean isFakePlayerEnabled() { return fakePlayerEnabled; }
    public boolean isPictureEnabled() { return pictureEnabled; }
    public boolean isMessageEnabled() { return messageEnabled; }
    public boolean isPrivateDoorEnabled() { return privateDoorEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isPerformanceEnabled() { return performanceEnabled; }
    
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
    
    public MusicManager getMusicManager() {
        return musicManager;
    }
}
