package org.enkop.skyblockSpawner.Commands;

import com.fastasyncworldedit.core.util.TaskManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.enkop.skyblockSpawner.Configs.ConfigSettings;
import org.enkop.skyblockSpawner.Configs.ConfigSettingsManager;
import org.enkop.skyblockSpawner.Data.ConfigData;
import org.enkop.skyblockSpawner.Data.ConfigDataManager;
import org.enkop.skyblockSpawner.SkyblockSpawner;
import org.enkop.skyblockSpawner.Utility.IslandCoords;
import org.enkop.skyblockSpawner.Utility.IslandGrid;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    private final SkyblockSpawner plugin;

    public AdminCommand(SkyblockSpawner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("skyblockspawner.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        IslandGrid grid = plugin.getIslandGrid();
        ConfigDataManager configDataManager = plugin.getConfigDataManager();
        ConfigSettingsManager configSettingsManager = plugin.getConfigSettingsManager();

        ConfigSettings configSettings = configSettingsManager.getConfigSettings();
        ConfigData configData = configDataManager.getConfigData();

        switch (args[0].toLowerCase()) {
            case "next" -> {
                IslandCoords coords = grid.allocateNext();
                int index = configData.islandIndex() - 1;

                TaskManager.taskManager().async(() -> {
                    grid.spawnIsland((Player) sender, coords);
                });
            }
            case "peek" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sba peek <index>");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    IslandCoords coords = grid.computeCenter(index);
                    sender.sendMessage(ChatColor.YELLOW + "Index " + index +
                            " -> (" + coords.x() + ", " + coords.z() + ")");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                }
            }

            case "info" -> {
                int spawned = configData.islandIndex();
                int regionSize = configSettings.regionSize();
                int minSpacing = configSettings.minSpacing();
                int maxSpacing = configSettings.maxSpacing();

                sender.sendMessage(ChatColor.GOLD + "=== SkyblockSpawner Info ===");
                sender.sendMessage(ChatColor.YELLOW + "Islands spawned: " + ChatColor.WHITE + spawned);
                sender.sendMessage(ChatColor.YELLOW + "Region size: " + ChatColor.WHITE + regionSize);
                sender.sendMessage(ChatColor.YELLOW + "Spacing: " + ChatColor.WHITE +
                        minSpacing + " to " + maxSpacing);
                sender.sendMessage(ChatColor.YELLOW + "Pitch: " + ChatColor.WHITE +
                        (regionSize + maxSpacing));
            }

            case "tp" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sba tp <index>");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    IslandCoords coords = grid.computeCenter(index);
                    String worldName = plugin.getConfig().getString("island-configuration.world", "world");

                    World world = configSettings.world();
                    if (world == null) {
                        sender.sendMessage(ChatColor.RED + "World not found: " + worldName);
                        return true;
                    }
                    int y = plugin.getConfig().getInt("island-configuration.y-level", 65);
                    Location loc = new Location(world, coords.x() + 0.5, y, coords.z() + 0.5);
                    player.teleport(loc);
                    player.sendMessage(ChatColor.GREEN + "Teleported to island #" + index);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                }
            }

            case "simulate" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /sba simulate <count>");
                    return true;
                }
                try {
                    int count = Math.min(Integer.parseInt(args[1]), 50); // safety cap
                    sender.sendMessage(ChatColor.GOLD + "Simulating " + count + " islands:");
                    for (int i = 0; i < count; i++) {
                        IslandCoords c = grid.computeCenter(i);
                        sender.sendMessage(ChatColor.GRAY + "#" + i + " -> (" + c.x() + ", " + c.z() + ")");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                }
            }

            case "reset" -> {
                if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(ChatColor.RED + "This will reset islands-spawned to 0.");
                    sender.sendMessage(ChatColor.RED + "Run: /sba reset confirm");
                    return true;
                }
                
                configData.setIslandIndex(0);
                configDataManager.update();
                sender.sendMessage(ChatColor.GREEN + "islands-spawned reset to 0.");
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== /sba commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/sba next " + ChatColor.GRAY + "- allocate next island");
        sender.sendMessage(ChatColor.YELLOW + "/sba peek <index> " + ChatColor.GRAY + "- preview coords");
        sender.sendMessage(ChatColor.YELLOW + "/sba tp <index> " + ChatColor.GRAY + "- teleport to island");
        sender.sendMessage(ChatColor.YELLOW + "/sba info " + ChatColor.GRAY + "- show config");
        sender.sendMessage(ChatColor.YELLOW + "/sba simulate <count> " + ChatColor.GRAY + "- preview many");
        sender.sendMessage(ChatColor.YELLOW + "/sba reset confirm " + ChatColor.GRAY + "- reset counter");
    }
}
