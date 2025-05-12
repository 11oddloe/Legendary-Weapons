package com.Items;

import LegendaryWeapons.legendaryWeapons.LegendaryWeapons;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    @EventHandler
    public void onCraftGoldenCrown(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CRAFTING_TABLE) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.GOLDEN_HELMET) return;

        // Check for 8 gold blocks around the crafting table
        Location tableLoc = event.getClickedBlock().getLocation();
        int goldBlocks = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Skip the crafting table itself
                if (tableLoc.clone().add(x, 0, z).getBlock().getType() == Material.GOLD_BLOCK) {
                    goldBlocks++;
                }
            }
        }

        if (goldBlocks >= 8) {
            // Remove the gold blocks
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    tableLoc.clone().add(x, 0, z).getBlock().setType(Material.AIR);
                }
            }

            // Replace the golden helmet with golden crown
            ItemStack goldenCrown = createArmorItem(Material.GOLDEN_HELMET, GOLDEN_CROWN);
            player.getInventory().setItemInMainHand(goldenCrown);
            player.sendMessage(ChatColor.GOLD + "You've crafted the Golden Crown!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private boolean isCustomArmor(ItemStack item, String name) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        return ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(name);
    }

    private ItemStack createArmorItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + name);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}