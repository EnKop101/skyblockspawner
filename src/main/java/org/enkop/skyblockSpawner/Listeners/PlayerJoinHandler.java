package org.enkop.skyblockSpawner.Listeners;

import org.bukkit.event.Listener;
import org.enkop.skyblockSpawner.Configs.ConfigSettingsManager;
import org.enkop.skyblockSpawner.SkyblockSpawner;

public class PlayerJoinHandler implements Listener {
    private final SkyblockSpawner plugin;
    private final ConfigSettingsManager configSettingsManager;

    public PlayerJoinHandler(SkyblockSpawner _plugin, ConfigSettingsManager _configSettingsManager){
        this.plugin = _plugin;
        this.configSettingsManager = _configSettingsManager;
    }
    
}
