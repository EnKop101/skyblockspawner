package org.enkop.skyblockSpawner.Listeners;

import org.bukkit.event.Listener;
import org.enkop.skyblockSpawner.Configs.ConfigManager;
import org.enkop.skyblockSpawner.SkyblockSpawner;

public class PlayerJoinHandler implements Listener {
    private final SkyblockSpawner plugin;
    private final ConfigManager configManager;

    public PlayerJoinHandler(SkyblockSpawner _plugin, ConfigManager _configManager){
        this.plugin = _plugin;
        this.configManager = _configManager;
    }
    
}
