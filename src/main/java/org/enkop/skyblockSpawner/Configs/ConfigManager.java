package org.enkop.skyblockSpawner.Configs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.enkop.skyblockSpawner.SkyblockSpawner;

import java.io.File;

public class ConfigManager {
    private final SkyblockSpawner plugin;
    private volatile ConfigSettings configSettings;

    private static final String SCHEMATIC_FOLDER_NAME = "schematics";

    private static final String SECTION_NAME = "island-configuration";
    private static final String REGION_DIAMETER = "island-region-diameter";
    private static final String SPACING = "spacing";
    private static final String MAX_COLUMNS_PER_ROW = "max-columns-per-row";
    private static final String WORLD = "world";
    private static final String Y_LEVEL = "y-level";
    private static final String SCHEMATIC_NAME = "schematic";
    private static final String CURRENT_COLUMN = "current-column";
    private static final String CURRENT_ROW = "current-row";

    public ConfigManager(SkyblockSpawner _plugin){
        this.plugin = _plugin;
        reload();
    }

    public ConfigSettings getConfigSettings() { return configSettings; };

    /**
     * Loads all the values from the config.yml file.
     * Does a few error handling checks on a few of the values.
     * Recreates a new instance of the class {@link ConfigSettings}.
     */
    public void reload(){
        plugin.saveDefaultConfig();
        plugin.reloadConfig();


        File customFolder = new File(plugin.getDataFolder(), SCHEMATIC_FOLDER_NAME);
        if (!customFolder.exists()){
            if (customFolder.mkdirs()){
                plugin.getLogger().info("Schematics folder created " + SCHEMATIC_FOLDER_NAME);
            }
            else {
                plugin.getLogger().severe("Failed to create the schematics folder. Stopping plugin as it is a requirement!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }
        }

        FileConfiguration configFile = plugin.getConfig();
        ConfigurationSection islandConfigSection = configFile.getConfigurationSection(SECTION_NAME);
        if (islandConfigSection != null) {
            int regionDiameter = Math.max(0, islandConfigSection.getInt(REGION_DIAMETER, 1));
            int spacing = Math.max(0, islandConfigSection.getInt(SPACING, 0));
            int maxColumnsPerRow = Math.max(1, islandConfigSection.getInt(MAX_COLUMNS_PER_ROW, 1));

            String worldName = islandConfigSection.getString(WORLD, null);
            if (worldName == null){
                plugin.getLogger().severe("The world name is missing in the configuration!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null){
                plugin.getLogger().severe("The world name doesn't match any world that exists!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            double yLevel = Math.max(0.0, islandConfigSection.getDouble(Y_LEVEL, 0.0));

            String schematicName = islandConfigSection.getString(SCHEMATIC_NAME, null);
            if (schematicName == null){
                plugin.getLogger().severe("The schematic file name in the configuration file is missing!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            File schemFile = new File(customFolder, schematicName);
            if (!schemFile.exists()){
                plugin.getLogger().severe("The schematic file is missing from the folder! Make sure to put the .schem file in the schematics folder");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }

            int currentColumn = Math.max(1, islandConfigSection.getInt(CURRENT_COLUMN, 1));
            int currentRow = Math.max(1, islandConfigSection.getInt(CURRENT_ROW, 1));

            this.configSettings = new ConfigSettings(
                    regionDiameter,
                    spacing,
                    maxColumnsPerRow,
                    world,
                    yLevel,
                    schemFile,
                    currentColumn,
                    currentRow
            );
        }
    }

    /**
     * Saves values that was changed by the plugin to the actual file.
     */
    public void update(){
        FileConfiguration configFile = plugin.getConfig();
        ConfigurationSection islandConfigSection = configFile.getConfigurationSection(SECTION_NAME);
        if (islandConfigSection != null) {
            islandConfigSection.set(CURRENT_COLUMN, configSettings.currentColumn());
            islandConfigSection.set(CURRENT_ROW, configSettings.currentRow());
        }
        plugin.saveConfig();
    }
}
