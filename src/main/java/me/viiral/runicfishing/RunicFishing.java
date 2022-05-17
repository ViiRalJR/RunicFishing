package me.viiral.runicfishing;

import lombok.Getter;
import me.viiral.runicfishing.command.*;
import me.viiral.runicfishing.gui.model.GUIListener;
import me.viiral.runicfishing.listener.FishingListener;
import me.viiral.runicfishing.manager.*;
import me.viiral.runicfishing.placeholder.RunicExpansion;
import me.viiral.runicfishing.task.AutomaticFishingTask;
import me.viiral.runicfishing.task.FishSaveTask;
import me.viiral.runicfishing.task.FishTopSortTask;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

@Getter
public final class RunicFishing extends JavaPlugin {

    private static RunicFishing instance;

    private ItemManager itemManager;
    private GUIManager guiManager;
    private FishingManager fishingManager;
    private MessageManager messageManager;

    private BukkitTask fishSaveTask;
    private BukkitTask fishTopTask;
    private BukkitTask automaticFishingTask;

    public Economy economy;
    private RunicExpansion runicExpansion;

    public long enableTime;

    public RunicFishing() {
        instance = this;
    }


    @Override
    public void onEnable() {
        instance = this;
        this.enableTime = System.currentTimeMillis();
        this.reloadConfig();

        // COMMANDS
        this.getCommand("fishing").setExecutor(new FishingCommand(this));
        this.getCommand("fishing").setTabCompleter(new FishingTabCompleter(this));
        this.getCommand("rodupgrade").setExecutor(new RodUpgradeCommand(this));
        this.getCommand("rods").setExecutor(new RodsCommand(this));
        this.getCommand("freload").setExecutor(new ReloadCommand(this));

        // LISTENERS
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);

        this.setupEconomy();
        this.setupPlaceholders();
        Bukkit.getLogger().log(Level.INFO, "[RunicFishing] Loaded! Took" + (System.currentTimeMillis() - enableTime) + "ms");
    }

    @Override
    public void onDisable() {
        this.getFishingManager().save();
    }

    @Override
    public void reloadConfig(){
        this.saveDefaultConfig();
        super.reloadConfig();
        this.itemManager = new ItemManager(this);
        this.guiManager = new GUIManager();
        this.fishingManager = new FishingManager(this);
        this.messageManager = new MessageManager(this);
        this.automaticFishingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new AutomaticFishingTask(this), 10L, this.getConfig().getLong("fishing-rods.auto-fishing.interval", 5L) * 20L);
        this.fishSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new FishSaveTask(this), 10L, 10L * 60L * 20L);
        this.fishTopTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new FishTopSortTask(this), 10L, 10L * 60L * 20L);
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> registeredServiceProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null){
            Bukkit.getLogger().log(Level.INFO, "[RunicFishing] Failed to hook into Vault!");
        } else {
            this.economy = registeredServiceProvider.getProvider();
            Bukkit.getLogger().log(Level.INFO, "[RunicFishing] Successfully hooked into Vault!");
        }
    }

    private void setupPlaceholders() {
        try {
            this.runicExpansion = new RunicExpansion(this);
            this.runicExpansion.register();
            Bukkit.getLogger().log(Level.INFO, "[RunicFishing] Successfully registered placeholders");
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicFishing] Failed to register placeholders");
        }
    }
}
