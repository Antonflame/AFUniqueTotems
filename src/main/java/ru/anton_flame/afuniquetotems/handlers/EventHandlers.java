package ru.anton_flame.afuniquetotems.handlers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.anton_flame.afuniquetotems.AFUniqueTotems;
import ru.anton_flame.afuniquetotems.utils.ConfigManager;
import ru.anton_flame.afuniquetotems.utils.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventHandlers implements Listener {

    private final AFUniqueTotems plugin;
    public EventHandlers(AFUniqueTotems plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack totem = findTotemInHands(player.getInventory());

        if (totem != null) {
            if (hasKey(totem, plugin.reusableTotemKey)) {
                ItemMeta totemMeta = totem.getItemMeta();
                PersistentDataContainer container = totemMeta.getPersistentDataContainer();
                ConfigurationSection totemSettings = ConfigManager.totems.getConfigurationSection("reusable_totem");
                int uses = container.getOrDefault(plugin.reusableTotemKey, PersistentDataType.INTEGER, 0);

                List<String> lore = new ArrayList<>();
                if (uses > 1) {
                    uses--;
                    int finalUses = uses;
                    lore = totemSettings.getStringList("lore").stream()
                            .map(line -> line.replace("%uses%", String.valueOf(finalUses)))
                            .collect(Collectors.toList());
                } else if (uses == 1 && totem.getAmount() >= 1) {
                    if (totem.getAmount() > 1) uses = totemSettings.getInt("uses");
                    if (totem.getAmount() == 1) uses = 0;
                    totem.setAmount(totem.getAmount() - 1);
                    lore = totemSettings.getStringList("lore").stream()
                            .map(line -> line.replace("%uses%", String.valueOf(totemSettings.getInt("uses"))))
                            .collect(Collectors.toList());
                }

                container.set(plugin.reusableTotemKey, PersistentDataType.INTEGER, uses);
                totemMeta.setLore(Hex.color(lore));
                totem.setItemMeta(totemMeta);

                if (player.getInventory().getItemInMainHand().equals(totem)) {
                    player.getInventory().setItemInMainHand(totem);
                } else if (player.getInventory().getItemInOffHand().equals(totem)) {
                    player.getInventory().setItemInOffHand(totem);
                }

                player.sendMessage(ConfigManager.reusableTotemUsed.replace("%uses%", String.valueOf(uses)));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = ((Player) event.getEntity()).getPlayer();
        if (player == null) return;

        boolean hasTotem = false;
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            if (findTotemInHands(player.getInventory()) == null) {
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                        if (hasKey(item, plugin.autoTotemKey)) {
                            item.setAmount(item.getAmount() - 1);
                            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
                            player.sendMessage(ConfigManager.autoTotemUsed);
                            hasTotem = true;
                            break;
                        }
                    }
                }
            }

            ItemStack totem = findTotemInHands(player.getInventory());
            if (totem != null && hasKey(totem, plugin.effectsPreservingTotemKey)) {
                totem.setAmount(totem.getAmount() - 1);

                player.sendMessage(ConfigManager.effectsPreservingTotemUsed.replace("%effects%", player.getActivePotionEffects().stream()
                        .map(PotionEffect::getType)
                        .map(PotionEffectType::getName)
                        .collect(Collectors.joining(", "))));
                hasTotem = true;
            }
        }

        if (hasTotem) {
            event.setCancelled(true);
            player.setHealth(1.0);

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 800, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

            player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        }
    }

    private ItemStack findTotemInHands(PlayerInventory inventory) {
        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING) {
            return mainHand;
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
            return offHand;
        }

        return null;
    }

    private boolean hasKey(ItemStack item, NamespacedKey key) {
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.INTEGER);
    }
}
