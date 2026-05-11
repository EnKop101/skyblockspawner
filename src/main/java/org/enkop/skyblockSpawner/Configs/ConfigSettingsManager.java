package org.enkop.skyblockSpawner.Configs;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.enkop.skyblockSpawner.SkyblockSpawner;

import java.io.File;

public class ConfigSettingsManager {
    private final SkyblockSpawner plugin;
    private volatile ConfigSettings configSettings;

    private static final String SCHEMATIC_FOLDER_NAME = "schematics";

    private static final String SECTION_NAME = "island-configuration";
    private static final String REGION_DIAMETER = "island-region-size";
    private static final String MIN_SPACING = "min-spacing";
    private static final String MAX_SPACING = "max-spacing";
    private static final String WORLD = "world";
    private static final String Y_LEVEL = "y-level";
    private static final String START_X = "start-x";
    private static final String START_Z = "start-z";
    private static final String SCHEMATIC_NAME = "schematic";

    public ConfigSettingsManager(SkyblockSpawner _plugin){
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
                throw new RuntimeException("Failed to configure config settings");
            }
        }

        FileConfiguration configFile = plugin.getConfig();
        ConfigurationSection islandConfigSection = configFile.getConfigurationSection(SECTION_NAME);
        if (islandConfigSection != null) {
            int regionDiameter = Math.max(0, islandConfigSection.getInt(REGION_DIAMETER, 1));
            int minSpacing = Math.max(1, islandConfigSection.getInt(MIN_SPACING, 1));
            int maxSpacing = Math.max(1, islandConfigSection.getInt(MAX_SPACING, 1));

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
                throw new RuntimeException("Failed to configure config settings");
            }

            double yLevel = Math.max(0.0, islandConfigSection.getDouble(Y_LEVEL, 0.0));

            String schematicName = islandConfigSection.getString(SCHEMATIC_NAME, null);
            if (schematicName == null){
                plugin.getLogger().severe("The schematic file name in the configuration file is missing!");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                throw new RuntimeException("Failed to configure config settings");
            }

            File schemFile = new File(customFolder, schematicName);
            if (!schemFile.exists()){
                plugin.getLogger().severe("The schematic file is missing from the folder! Make sure to put the .schem file in the schematics folder");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                throw new RuntimeException("Failed to configure config settings");
            }

            int startX = Math.max(0, islandConfigSection.getInt(START_X, 0));
            int startZ = Math.max(0, islandConfigSection.getInt(START_Z, 0));

            this.configSettings = new ConfigSettings(
                    regionDiameter,
                    minSpacing,
                    maxSpacing,
                    world,
                    yLevel,
                    startX,
                    startZ,
                    schemFile
            );
        }
    }
}
