package com.Items;

import LegendaryWeapons.legendaryWeapons.LegendaryWeapons;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerClasses {
    private final LegendaryWeapons plugin;
    private static final String CLASS_METADATA = "playerClass";

    public PlayerClasses(LegendaryWeapons plugin) {
        this.plugin = plugin;
    }

    public enum PlayerClass {
        DEFAULT("Default", "No special abilities"),
        WARRIOR("Warrior", "Bonus melee damage"),
        ARCHER("Archer", "Bonus ranged damage"),
        WARDEN_COMMANDER("Warden Commander", "Permanent glowing, double max health"),
        SKELETON_ARCHER("Skeleton Archer", "Permanent Speed I, faster bow charging"),
        ILLUSIONEER("Illusioneer", "Infinite totem with 1min cooldown, spawns clones"),
        MINER("Miner", "1 block tall, Haste III, 8 max hearts"),
        COSMICO("Cosmico", "Speed II & Invisibility after using Cosmic Katana"),
        GODS_APPRENTICE("God's Apprentice", "Regen I, no fall damage, double damage in Nether"),
        DEMON_KIND("Demon Kind", "Fire Res I, Speed II in Nether, Weakness in Overworld"),
        POSEIDONS_APPRENTICE("Poseidon's Apprentice", "Water breathing, dolphin's grace, splash attack"),
        DEATHBRINGER("Deathbringer", "Strength II in darkness, burns in daylight"),
        SHULKSTER("Shulkster", "Resistance I, spawns with shulker box"),
        ALLAY("Allay", "0.5 blocks tall, creative flight, 6 max hearts"),
        GOLEM("Golem", "3 blocks tall, Slowness I, 40 max hearts");

        private final String displayName;
        private final String description;

        PlayerClass(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public PlayerClass getPlayerClass(Player player) {
        if (player.hasMetadata(CLASS_METADATA)) {
            return PlayerClass.valueOf(player.getMetadata(CLASS_METADATA).get(0).asString());
        }
        return PlayerClass.DEFAULT;
    }

    private void setEntityScale(Entity entity, float scale) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA);
            packet.getIntegers().write(0, entity.getEntityId());

            WrappedDataWatcher watcher = new WrappedDataWatcher();
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Float.class);
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(11, serializer), scale);

            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            // Send to all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set entity scale: " + e.getMessage());
        }
    }

    private void clearClassEffects(Player player) {
        // Reset health
        player.setMaxHealth(20);
        player.setHealth(20);

        // Remove all potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Reset size
        setEntityScale(player, 1.0f);

        // Clear flight if any
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    public void setPlayerClass(Player player, PlayerClass playerClass) {
        // Remove previous class effects
        clearClassEffects(player);

        // Apply new class effects
        switch (playerClass) {
            case WARDEN_COMMANDER:
                applyWardenCommanderEffects(player);
                break;
            case WARRIOR:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
            case ARCHER:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        Integer.MAX_VALUE,
                        1,
                        false, false, false
                ));
                break;
            case SKELETON_ARCHER:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
            case ILLUSIONEER:
                // Give totem on first selection (handled elsewhere)
                break;
            case MINER:
                player.setMaxHealth(16);
                player.setHealth(16);
                setEntityScale(player, 0.5f);
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE,
                        Integer.MAX_VALUE,
                        2,
                        false, false, false
                ));
                break;
            case COSMICO:
                // Effects applied when using Cosmic Katana
                break;
            case GODS_APPRENTICE:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
            case DEMON_KIND:
                updateDemonKindEffects(player);
                break;
            case POSEIDONS_APPRENTICE:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WATER_BREATHING,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DOLPHINS_GRACE,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
            case DEATHBRINGER:
                updateDeathbringerEffects(player);
                break;
            case SHULKSTER:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
            case ALLAY:
                player.setMaxHealth(12);
                player.setHealth(12);
                setEntityScale(player, 0.25f);
                player.setAllowFlight(true);
                break;
            case GOLEM:
                player.setMaxHealth(80);
                player.setHealth(80);
                setEntityScale(player, 3.0f);
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        Integer.MAX_VALUE,
                        0,
                        false, false, false
                ));
                break;
        }

        // Store class in metadata and config
        player.setMetadata(CLASS_METADATA, new FixedMetadataValue(plugin, playerClass.name()));
        plugin.getConfig().set("players." + player.getUniqueId() + ".class", playerClass.name());
        plugin.saveConfig();

        player.sendMessage(ChatColor.GOLD + "You are now a " + playerClass.getDisplayName() + "!");
    }

    public void updateDemonKindEffects(Player player) {
        if (getPlayerClass(player) != PlayerClass.DEMON_KIND) return;

        if (player.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    false, false, false
            ));
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    Integer.MAX_VALUE,
                    1,
                    false, false, false
            ));
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    false, false, false
            ));
        } else {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    Integer.MAX_VALUE,
                    0,
                    false, false, false
            ));
        }
    }

    public void updateDeathbringerEffects(Player player) {
        if (getPlayerClass(player) != PlayerClass.DEATHBRINGER) return;

        if (player.getLocation().getBlock().getLightLevel() <= 4) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.STRENGTH,
                    Integer.MAX_VALUE,
                    1,
                    false, false, false
            ));
        } else if (player.getWorld().getTime() < 12300 || player.getWorld().getTime() > 23850) {
            player.setFireTicks(100);
        }
    }

    private void applyWardenCommanderEffects(Player player) {
        double newMaxHealth = 40.0;
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(newMaxHealth);
        }
        player.setHealth(newMaxHealth);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                Integer.MAX_VALUE,
                0,
                false,
                false,
                false
        ));
    }

    public void loadPlayerClass(Player player) {
        String className = plugin.getConfig().getString("players." + player.getUniqueId() + ".class");
        if (className != null) {
            try {
                PlayerClass playerClass = PlayerClass.valueOf(className);
                setPlayerClass(player, playerClass);
            } catch (IllegalArgumentException e) {
                // Invalid class in config
            }
        }
    }
}