package io.github.phateio.superspawnpoint;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SuperSpawnPoint extends JavaPlugin implements Listener {

    private final Map<UUID, Location> playerBedLocation = new HashMap<>();
    private final File dataFile = new File(getDataFolder(), "data.yml");

    @Override
    public void onEnable() {
        readData();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();
        Map<String, Location> data = new HashMap<>();
        playerBedLocation.forEach((k, v) -> data.put(k.toString(), v));
        yaml.set("data", data);
        try {
            yaml.save(dataFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void readData() {
        if (!dataFile.isFile()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection node = yaml.getConfigurationSection("data");
        if (node == null) return;
        node.getValues(false).forEach((uuid, loc) -> playerBedLocation.put(UUID.fromString(uuid), (Location) loc));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Location loc = playerBedLocation.remove(player.getUniqueId());
        if (loc == null) return;
        player.setBedSpawnLocation(loc, true);
        getServer().getConsoleSender().sendMessage(player.getName() + " bed spawn point is set to " + loc);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 5) return false;
        String playerName = args[0];
        OfflinePlayer offlinePlayer = getServer().getOfflinePlayerIfCached(playerName);
        if (offlinePlayer == null) {
            sender.sendMessage("OfflinePlayer " + playerName + " Not found.");
            return true;
        }
        String worldName = args[1];
        World world = getServer().getWorld(worldName);
        if (world == null) {
            String worldList = getServer().getWorlds().stream().map(WorldInfo::getName)
                    .collect(Collectors.joining(", ", "[", "]"));
            sender.sendMessage("world " + worldName + " not found. " + worldList);
            return true;
        }
        int x = Integer.parseInt(args[2]);
        int y = Integer.parseInt(args[3]);
        int z = Integer.parseInt(args[4]);

        Location loc = new Location(world, x, y, z).toBlockLocation();
        if (offlinePlayer.isOnline()) {
            //noinspection ConstantConditions
            offlinePlayer.getPlayer().setBedSpawnLocation(loc, true);
            sender.sendMessage("Player is online. set bed spawn point now.");
            return true;
        }

        playerBedLocation.put(offlinePlayer.getUniqueId(), loc);
        sender.sendMessage("player is offline. spawn point will set when player join.");
        return true;
    }
}
