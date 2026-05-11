package org.enkop.skyblockSpawner.Data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.enkop.skyblockSpawner.SkyblockSpawner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigDataManager {
    private final SkyblockSpawner plugin;
    private volatile ConfigData configData;

    private FileConfiguration dataConfig;
    private File dataFile;

    private static final String DATA_FILE_NAME = "data.yml";
    private static final String ISLAND_INDEX = "island-index";
    private static final String RELEASED_INDICES = "released-indices";

    public ConfigDataManager(SkyblockSpawner _plugin){
        this.plugin = _plugin;
        loadDataConfig();
    }

    public ConfigData getConfigData() { return configData; }
    public FileConfiguration getDataConfig() { return dataConfig; }

    public void loadDataConfig(){
        dataFile = new File(plugin.getDataFolder(), DATA_FILE_NAME);

        if (!dataFile.exists()) {
            // Create the file with defaults
            dataFile.getParentFile().mkdirs();

            try {
                dataFile.createNewFile();
            } catch (IOException e){
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
                plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                throw new RuntimeException("Failed to configure config data");
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Set defaults if missing
        if (!dataConfig.contains(ISLAND_INDEX)){
            dataConfig.set(ISLAND_INDEX, 1);
        }
        if (!dataConfig.contains(RELEASED_INDICES)){
            dataConfig.set(RELEASED_INDICES, new ArrayList<Integer>());
        }

        int islandsSpawned = Math.max(1, dataConfig.getInt(ISLAND_INDEX, 1));
        ArrayList<Integer> releasedIndices = new ArrayList<>(dataConfig.getIntegerList(RELEASED_INDICES));

        this.configData = new ConfigData(
                islandsSpawned,
                releasedIndices
        );

        saveDataConfig();
    }

    public void saveDataConfig() {
        if (dataConfig == null || dataFile == null) return;
        try {
            dataConfig.save(dataFile);
        } catch (IOException e){
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            throw new RuntimeException("Failed to configure config data");
        }
    }

    /**
     * Saves values that was changed by the plugin to the actual file.
     */
    public void update(){
        if (dataConfig != null) {
            dataConfig.set(ISLAND_INDEX, configData.islandIndex());
            dataConfig.set(RELEASED_INDICES, configData.releasedIndices());

            try {
                dataConfig.save(dataFile);
            } catch (IOException e){
                plugin.getLogger().warning("Could not save updated values to data.yml: " + e.getMessage());
            }
        }
        else {
            plugin.getLogger().warning("dataConfig was null which means it could not update the values.");
        }
    }
}
