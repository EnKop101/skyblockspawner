package org.enkop.skyblockSpawner.Utility;

import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.min;
import org.enkop.skyblockSpawner.Configs.ConfigSettings;
import org.enkop.skyblockSpawner.Configs.ConfigSettingsManager;
import org.enkop.skyblockSpawner.Data.ConfigData;
import org.enkop.skyblockSpawner.Data.ConfigDataManager;
import org.enkop.skyblockSpawner.SkyblockSpawner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

// TODO ADD A RESET FUNCTION THAT RELEASES THE INDICES DELETES EVERYTHING AND SETS THEM ON THE NEXT SPAWN POINT
// TODO ADD A FUNCTION TO HAVE THEM TURN ON AND OFF RAID PROTECTION WITH A 12HR COOLDOWN BETWEEN EACH SWITCH
// TODO REDUCE THE SIZE OF THE DIAMETER OF THE REGION TO LIKE 350 IN TOTAL.

public class IslandGrid {
    private final SkyblockSpawner plugin;
    private final ConfigSettingsManager configSettingsManager;
    private final ConfigDataManager configDataManager;

    private volatile ConfigSettings configSettings;
    private volatile ConfigData configData;

    private final Random random = new Random();

    public IslandGrid(SkyblockSpawner _plugin, ConfigSettingsManager _configSettingsManager, ConfigDataManager _configDataManager){
        this.plugin = _plugin;
        this.configSettingsManager = _configSettingsManager;
        this.configDataManager = _configDataManager;

        this.configSettings = configSettingsManager.getConfigSettings();
        this.configData = configDataManager.getConfigData();
    }

    public IslandCoords allocateNext(){
        int index = configData.islandIndex();

        IslandCoords coords = computeCenter(index);

        configData.setIslandIndex(index + 1);
        configDataManager.update();

        return coords;
    }

    public IslandCoords computeCenter(int index) {
        int pitch = configSettings.regionSize() + configSettings.maxSpacing();
        int jitterRange = (configSettings.maxSpacing() - configSettings.minSpacing()) / 2;

        int[] gc = spiralCoords(index);
        int gridCenterX = configSettings.startX() + gc[0] * pitch + pitch / 2;
        int gridCenterZ = configSettings.startZ() + gc[1] * pitch + pitch / 2;

        int jitterX = random.nextInt(jitterRange * 2 + 1) - jitterRange;
        int jitterZ = random.nextInt(jitterRange * 2 + 1) - jitterRange;

        return new IslandCoords(gridCenterX + jitterX, gridCenterZ + jitterZ);
    }

    public static int[] spiralCoords(int index) {
        if (index == 0) return new int[]{0, 0};

        // Find which "ring" of the spiral we're on
        int ring = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);

        // Length of one side of this ring (minus 1)
        int sideLength = 2 * ring;

        // Index within the ring
        int ringStart = (2 * ring - 1) * (2 * ring - 1);
        int ringIndex = index - ringStart;

        // Which side of the ring (0=right, 1=top, 2=left, 3=bottom)
        int side = ringIndex / sideLength;
        int sideOffset = ringIndex % sideLength;

        int col, row;
        row = switch (side) {
            case 0 -> {
                col = ring;
                yield -ring + 1 + sideOffset;
            }
            case 1 -> {
                col = ring - 1 - sideOffset;
                yield ring;
            }
            case 2 -> {
                col = -ring;
                yield ring - 1 - sideOffset;
            }
            case 3 -> {
                col = -ring + 1 + sideOffset;
                yield -ring;
            }
            default -> {
                col = 0;
                yield 0;
            }
        };

        return new int[]{col, row};
    }

    public void createRegion(Player player, String regionName, IslandCoords coords, World weWorld){
        // 768x768 region centered on the island coords
        int quarterSize = configSettings.regionSize() / 4;

        BlockVector3 min = BlockVector3.at(
                coords.x() - quarterSize,
                -64,
                coords.z() - quarterSize
        );

        BlockVector3 max = BlockVector3.at(
                coords.x() + quarterSize,
                320,
                coords.z() + quarterSize
        );

        ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);

        DefaultDomain owners = region.getOwners();
        owners.addPlayer(player.getUniqueId());
        region.setOwners(owners);

        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.GREET_TITLE,  "Island of " + player.getName());
        region.setPriority(10);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(weWorld);

        if (regions != null) {
            regions.addRegion(region);
        }

    }

    public void spawnIsland(Player player, IslandCoords coords){
        try {
            ClipboardFormat format = ClipboardFormats.findByFile(configSettings.schematic());
            Clipboard clipboard;

            try (FileInputStream fis = new FileInputStream(configSettings.schematic())) {
                clipboard = format.getReader(fis).read();
            }

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(configSettings.world());
            BlockVector3 pasteAt = BlockVector3.at(coords.x(), configSettings.yLevel(), coords.z());

            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .build()) {

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pasteAt)
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
            }

            // Teleport and messaging must run on the main thread
            TaskManager.taskManager().sync(() -> {
                double x = coords.x();
                double y = configSettings.yLevel();
                double z = coords.z();

                player.teleport(new Location(configSettings.world(), x, y, z));

                player.sendMessage(ChatColor.GREEN + "Allocated island #" + configData.islandIndex() +
                        " at (" + coords.x() + ", " + coords.z() + ")");
                player.sendMessage(ChatColor.GREEN + "Island spawned and region created successfully!");
                String islandName = "Island-" + configData.islandIndex();
                createRegion(player, islandName, coords, weWorld);

                return null;
            });

        } catch (FileNotFoundException e) {
            TaskManager.taskManager().sync(() -> {
                player.sendMessage(ChatColor.RED + "Schematic file not found: " + configSettings.schematic());
                return null;
            });
            e.printStackTrace();
        } catch (IOException | WorldEditException e) {
            TaskManager.taskManager().sync(() -> {
                player.sendMessage(ChatColor.RED + "Error spawning island: " + e.getMessage());
                return null;
            });
            e.printStackTrace();
        }
    }
}