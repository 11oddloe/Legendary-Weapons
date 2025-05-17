package com.Items;

import LegendaryWeapons.legendaryWeapons.LegendaryWeapons;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Armor implements Listener {

    private final LegendaryWeapons plugin;
    private final Map<UUID, Long> flightCooldowns = new HashMap<>();
    private static final String GOLDEN_CROWN = "Golden Crown";
    public static final String DRAGON_HELMET = "Dragon Helmet";
    public static final String DRAGON_CHESTPLATE = "Dragon Chestplate";
    public static final String DRAGON_LEGGINGS = "Dragon Leggings";
    public static final String DRAGON_BOOTS = "Dragon Boots";

    public Armor(LegendaryWeapons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerEquip(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        // Check for Golden Crown
        if (helmet != null && isCustomArmor(helmet, GOLDEN_CROWN)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 100, 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
        }

        // Check for full Dragon set
        boolean fullSet =
                isCustomArmor(helmet, DRAGON_HELMET) &&
                isCustomArmor(chestplate, DRAGON_CHESTPLATE) &&
                isCustomArmor(leggings, DRAGON_LEGGINGS) &&
                isCustomArmor(boots, DRAGON_BOOTS);

        if (fullSet) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0));

            // Check if in lava and grant flight
            if (player.getLocation().getBlock().getType() == Material.LAVA ||
                    player.getLocation().getBlock().getType() == Material.LAVA_CAULDRON) {

                long now = System.currentTimeMillis();
                if (!flightCooldowns.containsKey(player.getUniqueId()) ||
                        now - flightCooldowns.get(player.getUniqueId()) > 300000) { // 5 minute cooldown

                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage(ChatColor.GOLD + "Dragon wings activate! You gain temporary flight!");

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                cancel();
                                return;
                            }

                            if (!player.isFlying() ||
                                    player.getLocation().getBlock().getType() != Material.LAVA &&
                                            player.getLocation().getBlock().getType() != Material.LAVA_CAULDRON) {

                                player.setAllowFlight(false);
                                player.setFlying(false);
                                flightCooldowns.put(player.getUniqueId(), now);
                                player.sendMessage(ChatColor.GOLD + "Your dragon wings cool down...");
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 200, 20); // 10 seconds flight
                }
            }
        }
    }


    private boolean isCustomArmor(ItemStack item, String name) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(name);
    }
}