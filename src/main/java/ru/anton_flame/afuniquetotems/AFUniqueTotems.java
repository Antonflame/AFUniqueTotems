package ru.anton_flame.afuniquetotems;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anton_flame.afuniquetotems.commands.AFUniqueTotemsCommand;
import ru.anton_flame.afuniquetotems.handlers.EventHandlers;
import ru.anton_flame.afuniquetotems.utils.ConfigManager;

public final class AFUniqueTotems extends JavaPlugin {

    public NamespacedKey reusableTotemKey = NamespacedKey.fromString("reusable_totem_uses");
    public NamespacedKey effectsPreservingTotemKey = NamespacedKey.fromString("preserving_effects_totem");
    public NamespacedKey autoTotemKey = NamespacedKey.fromString("auto_totem");
    public NamespacedKey upgradedTotemKey = NamespacedKey.fromString("upgraded_totem");

    @Override
    public void onEnable() {
        getLogger().info("Плагин был включен!");
        saveDefaultConfig();
        ConfigManager.setupConfigValues(this);
        Bukkit.getPluginManager().registerEvents(new EventHandlers(this), this);

        PluginCommand afUniqueTotemsCommand = getCommand("afuniquetotems");
        AFUniqueTotemsCommand afUniqueTotemsCommandClass = new AFUniqueTotemsCommand(this);
        afUniqueTotemsCommand.setExecutor(afUniqueTotemsCommandClass);
        afUniqueTotemsCommand.setTabCompleter(afUniqueTotemsCommandClass);
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин был выключен!");
    }
}
