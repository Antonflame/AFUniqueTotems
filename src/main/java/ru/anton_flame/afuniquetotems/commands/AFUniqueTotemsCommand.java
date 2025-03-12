package ru.anton_flame.afuniquetotems.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.anton_flame.afuniquetotems.AFUniqueTotems;
import ru.anton_flame.afuniquetotems.utils.ConfigManager;
import ru.anton_flame.afuniquetotems.utils.Hex;

import java.util.*;
import java.util.stream.Collectors;

public class AFUniqueTotemsCommand implements CommandExecutor, TabCompleter {

    private final AFUniqueTotems plugin;
    public AFUniqueTotemsCommand(AFUniqueTotems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            sendHelp(commandSender);
            return false;
        }

        switch (strings[0].toLowerCase()) {
            case "reload": {
                if (!commandSender.hasPermission("afuniquetotems.reload")) {
                    commandSender.sendMessage(ConfigManager.noPermission);
                    return false;
                }

                plugin.reloadConfig();
                ConfigManager.setupConfigValues(plugin);
                commandSender.sendMessage(ConfigManager.reloaded);
                break;
            }

            case "give": {
                if (strings.length == 4) {
                    if (!commandSender.hasPermission("afuniquetotems.give")) {
                        commandSender.sendMessage(ConfigManager.noPermission);
                        return false;
                    }

                    Player player = Bukkit.getPlayer(strings[1]);
                    String totemType = strings[2];
                    int amount;

                    if (player == null) {
                        commandSender.sendMessage(ConfigManager.playerNotFound);
                        return false;
                    }

                    if (ConfigManager.totems.getString(totemType) == null) {
                        commandSender.sendMessage(ConfigManager.invalidTotemType);
                        return false;
                    }

                    try {
                        amount = Integer.parseInt(strings[3]);
                    } catch (NumberFormatException exception) {
                        commandSender.sendMessage(ConfigManager.invalidAmount);
                        return false;
                    }

                    ItemStack itemStack = new ItemStack(Material.TOTEM_OF_UNDYING, amount);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                    ConfigurationSection totemSettings = ConfigManager.totems.getConfigurationSection(totemType);

                    itemMeta.setDisplayName(Hex.color(totemSettings.getString("display_name")));
                    List<String> lore = totemSettings.getStringList("lore").stream()
                            .map(line -> line.replace("%uses%", String.valueOf(totemSettings.getInt("uses"))))
                            .collect(Collectors.toList());
                    itemMeta.setLore(Hex.color(lore));

                    switch (totemType.toLowerCase()) {
                        case "reusable_totem":
                            container.set(plugin.reusableTotemKey, PersistentDataType.INTEGER, totemSettings.getInt("uses"));
                            break;
                        case "effects_preserving_totem":
                            container.set(plugin.effectsPreservingTotemKey, PersistentDataType.INTEGER, 1);
                            break;
                        case "auto_totem":
                            container.set(plugin.autoTotemKey, PersistentDataType.INTEGER, 1);
                            break;
                        case "upgraded_totem":
                            container.set(plugin.upgradedTotemKey, PersistentDataType.STRING, String.join(", ", totemSettings.getStringList("effects")));
                        default:
                            break;
                    }

                    itemStack.setItemMeta(itemMeta);

                    player.getInventory().addItem(itemStack);
                    commandSender.sendMessage(ConfigManager.totemGiven
                            .replace("%type%", totemType)
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%player%", player.getName()));
                } else {
                    sendHelp(commandSender);
                }
                break;
            }

            default:
                sendHelp(commandSender);
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Arrays.asList("give", "reload");
        } else if (strings[0].equalsIgnoreCase("give")) {
            if (strings.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (strings.length == 3) {
                return new ArrayList<>(ConfigManager.totems.getKeys(false));
            } else if (strings.length == 4) {
                return Arrays.asList("1", "2", "3", "4", "5");
            }
        }

        return Collections.emptyList();
    }

    private void sendHelp(CommandSender commandSender) {
        for (String help : ConfigManager.help) {
            commandSender.sendMessage(help);
        }
    }
}
