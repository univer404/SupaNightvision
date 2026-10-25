package com.supanightvision;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SupaNightvision extends JavaPlugin implements Listener, CommandExecutor {
    private static final int EFFECT_DURATION = Integer.MAX_VALUE;
    private final Set<UUID> enabledPlayers = new HashSet<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private File dataFile;
    private YamlConfiguration data;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPlayerData();

        PluginCommand command = getCommand("nightvision");
        if (command == null) {
            getLogger().severe("The nightvision command is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (enabledPlayers.contains(player.getUniqueId())) {
                applyNightVision(player);
            }
        }
        getLogger().info("SupaNightvision enabled.");
    }

    @Override
    public void onDisable() {
        savePlayerData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(getConfig().getString("messages.player-only", "<#A8A8A8>ᴏɴʟʏ ᴘʟᴀʏᴇʀs ᴄᴀɴ ᴜsᴇ ᴛʜɪs ᴄᴏᴍᴍᴀɴᴅ.")));
            return true;
        }
        if (!player.hasPermission("supanightvision.use")) {
            player.sendMessage(color(getConfig().getString("messages.no-permission", "<#A8A8A8>ʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪssɪᴏɴ.")));
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (enabledPlayers.remove(uuid)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            savePlayerData();
            sendStatus(player, "messages.deactivated", "<#A8A8A8>ɴɪɢʜᴛ ᴠɪsɪᴏɴ ᴅᴇᴀᴄᴛɪᴠᴀᴛᴇᴅ");
        } else {
            enabledPlayers.add(uuid);
            applyNightVision(player);
            savePlayerData();
            sendStatus(player, "messages.activated", "<#A8A8A8>ɴɪɢʜᴛ ᴠɪsɪᴏɴ ᴀᴄᴛɪᴠᴀᴛᴇᴅ");
        }
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (enabledPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(this, () -> {
                if (player.isOnline() && enabledPlayers.contains(player.getUniqueId())) {
                    applyNightVision(player);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.MILK_BUCKET) {
            return;
        }
        Player player = event.getPlayer();
        if (enabledPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(this, () -> {
                if (player.isOnline() && enabledPlayers.contains(player.getUniqueId())) {
                    applyNightVision(player);
                }
            });
        }
    }

    private void applyNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.NIGHT_VISION, EFFECT_DURATION, 0, false, false, false
        ));
    }

    private void sendStatus(Player player, String configPath, String fallback) {
        Component message = color(getConfig().getString(configPath, fallback));
        if (getConfig().getBoolean("messages.chat", true)) {
            player.sendMessage(message);
        }
        if (getConfig().getBoolean("messages.actionbar", true)) {
            player.sendActionBar(message);
        }
    }

    private Component color(String text) {
        return text == null || text.isBlank() ? Component.empty() : miniMessage.deserialize(text);
    }

    private void loadPlayerData() {
        dataFile = new File(getDataFolder(), "players.yml");
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create the plugin data folder.");
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String value : data.getStringList("enabled-players")) {
            try {
                enabledPlayers.add(UUID.fromString(value));
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Ignoring invalid player UUID in players.yml: " + value);
            }
        }
    }

    private void savePlayerData() {
        if (dataFile == null || data == null) {
            return;
        }
        data.set("enabled-players", enabledPlayers.stream().map(UUID::toString).sorted().toList());
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            getLogger().severe("Could not save players.yml: " + exception.getMessage());
        }
    }
}
