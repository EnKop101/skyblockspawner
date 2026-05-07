package org.enkop.skyblockSpawner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.enkop.skyblockSpawner.Commands.AdminCommand;
import org.enkop.skyblockSpawner.Configs.ConfigSettingsManager;
import org.enkop.skyblockSpawner.Data.ConfigDataManager;
import org.enkop.skyblockSpawner.Utility.IslandGrid;

public final class SkyblockSpawner extends JavaPlugin {

    private boolean worldEditHooked = false;
    private boolean worldGuardHooked = false;
    private boolean essentialsHooked = false;

    private ConfigSettingsManager configSettingsManager;
    private ConfigDataManager configDataManager;
    private IslandGrid islandGrid;

    @Override
    public void onEnable() {
        this.configSettingsManager = new ConfigSettingsManager(this);
        this.configDataManager = new ConfigDataManager(this);
        this.islandGrid = new IslandGrid(this, configSettingsManager, configDataManager);

        // Hook into dependencies
        if (!hookWorldEdit()) {
            getLogger().severe("WorldEdit/FAWE not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!hookWorldGuard()) {
            getLogger().severe("WorldGuard not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        hookEssentials(); // soft depend - log but don't disable

        getLogger().info("==== SkyblockSpawner Enabled ====");
        getLogger().info("WorldEdit/FAWE: " + (worldEditHooked ? "OK" : "MISSING"));
        getLogger().info("WorldGuard:     " + (worldGuardHooked ? "OK" : "MISSING"));
        getLogger().info("Essentials:     " + (essentialsHooked ? "OK" : "not present (optional)"));

        getCommand("skyblockadmin").setExecutor(new AdminCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("SkyblockSpawner Disabled!");
    }

    // Checks if worldedit / fawe plugin is installed on the server and enabled
    private boolean hookWorldEdit() {
        Plugin fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");

        Plugin active = null;
        String type = null;

        if (fawe != null && fawe.isEnabled()) {
            active = fawe;
            type = "FastAsyncWorldEdit";
        } else if (we != null && we.isEnabled()) {
            active = we;
            type = "WorldEdit";
        }

        if (active == null) {
            return false;
        }

        // Sanity-check that the WorldEdit API is actually on the classpath.
        // Both WE and FAWE expose com.sk89q.worldedit.WorldEdit
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
        } catch (ClassNotFoundException e) {
            getLogger().severe(type + " is loaded but WorldEdit API classes are missing?");
            return false;
        }

        getLogger().info("Detected " + type + " v" + active.getDescription().getVersion());
        worldEditHooked = true;
        return true;
    }

    // Checks if WorldGuard plugin is installed on the server and enabled
    private boolean hookWorldGuard() {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg == null || !wg.isEnabled()) {
            return false;
        }
        getLogger().info("Detected WorldGuard v" + wg.getPluginMeta().getVersion());
        worldGuardHooked = true;
        return true;
    }

    // Checks if EssentielsX plugin is installed on the server and enabled
    private void hookEssentials() {
        Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null || !ess.isEnabled()) {
            essentialsHooked = false;
            return;
        }
        getLogger().info("Detected Essentials v" + ess.getPluginMeta().getVersion());
        essentialsHooked = true;
    }

    public boolean isWorldEditHooked() { return worldEditHooked; }
    public boolean isWorldGuardHooked() { return worldGuardHooked; }
    public boolean isEssentialsHooked() { return essentialsHooked; }

    public ConfigSettingsManager getConfigSettingsManager() { return configSettingsManager; }
    public ConfigDataManager getConfigDataManager() { return configDataManager; }
    public IslandGrid getIslandGrid() { return islandGrid; }
}
