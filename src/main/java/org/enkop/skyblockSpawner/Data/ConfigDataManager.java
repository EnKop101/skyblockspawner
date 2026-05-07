package org.enkop.skyblockSpawner.Data;

import org.bukkit.configuration.ConfigurationSection;
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
    private static final String ISLANDS_SPAWNED = "islands-spawned";
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
                return;
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Set defaults if missing
        if (!dataConfig.contains(ISLANDS_SPAWNED)){
            dataConfig.set(ISLANDS_SPAWNED, 0);
        }
        if (!dataConfig.contains(RELEASED_INDICES)){
            dataConfig.set(RELEASED_INDICES, new ArrayList<Integer>());
        }

        int islandsSpawned = Math.max(0, dataConfig.getInt(ISLANDS_SPAWNED, 0));
        ArrayList<Integer> releasedIndices = new ArrayList<>(dataConfig.getIntegerList("released-indices"));

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
        }
    }

    /**
     * Saves values that was changed by the plugin to the actual file.
     */
    public void update(){
        if (dataConfig != null) {
            dataConfig.set(ISLANDS_SPAWNED, configData.islandsSpawned());
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
