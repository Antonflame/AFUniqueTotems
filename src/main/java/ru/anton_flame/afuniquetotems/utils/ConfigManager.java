package ru.anton_flame.afuniquetotems.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    public static ConfigurationSection totems;
    public static String noPermission, reloaded, playerNotFound, invalidTotemType, invalidAmount, totemGiven, reusableTotemUsed, effectsPreservingTotemUsed, autoTotemUsed, upgradedTotemUsed;
    public static List<String> help;

    public static void setupConfigValues(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection messages = config.getConfigurationSection("messages");

        totems = config.getConfigurationSection("totems");
        noPermission = Hex.color(messages.getString("no_permission"));
        reloaded = Hex.color(messages.getString("reloaded"));
        playerNotFound = Hex.color(messages.getString("player_not_found"));
        invalidTotemType = Hex.color(messages.getString("invalid_totem_type"));
        invalidAmount = Hex.color(messages.getString("invalid_amount"));
        totemGiven = Hex.color(messages.getString("totem_given"));
        reusableTotemUsed = Hex.color(messages.getString("reusable_totem_used"));
        effectsPreservingTotemUsed = Hex.color(messages.getString("effects_preserving_totem_used"));
        autoTotemUsed = Hex.color(messages.getString("auto_totem_used"));
        upgradedTotemUsed = Hex.color(messages.getString("upgraded_totem_used"));
        help = Hex.color(messages.getStringList("help"));
    }
}
