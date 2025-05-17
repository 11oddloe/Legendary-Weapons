package com.Items;

import LegendaryWeapons.legendaryWeapons.LegendaryWeapons;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;

import java.util.*;

import static org.bukkit.Material.*;
import static org.bukkit.Sound.*;

public class Items implements Listener {
    public final LegendaryWeapons plugin;
    private final PlayerClasses playerClasses;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> sneakingStart = new HashMap<>();
    private final Map<Player, Long> lastAbsorptionTime = new HashMap<>();

    // Item name constants
    public static final String COSMIC_KATANA = "Cosmic Katana";
    public static final String THUNDER_STRIKER = "Thunder Striker";
    public static final String STAR_STAFF = "Star Staff";
    public static final String EARTH_WAND = "Earth Wand";
    public static final String POSEIDONS_TRIDENT = "Poseidon's Trident";
    public static final String DRAGON_WINGS = "Dragon Wings";
    public static final String ICE_BOW = "Ice Bow";
    public static final String MAGIC_SACK = "Magic Sack of Legend";
    public static final String LICH_STAFF = "Lich Staff";
    public static final String CALLING_OF_THE_WARDEN = "Calling of the Warden";
    public static final String ENHANCEMENT_ORB = "Enhancement Orb";
    public static final String LIFE_DRAINER = "Life Drainer";
    public static final String CLASS_SELECTOR = "Class Selector";
    public static final String ILLUSIONEER_TOTEM = "Illusioneer's Totem";

    public Items(LegendaryWeapons plugin) {
        this.plugin = plugin;
        this.playerClasses = plugin.getPlayerClasses();
    }

    /* ========== EVENT HANDLERS ========== */

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity)) return;
        if (!(event.getEntity() instanceof Player)) return;

        LivingEntity damager = (LivingEntity) event.getDamager();
        Player player = (Player) event.getEntity();

        if (damager instanceof Player) {
            Player attacker = (Player) damager;
            Player target = (Player) event.getEntity();
            ItemStack item = attacker.getInventory().getItemInMainHand();
            String name = item != null && item.hasItemMeta() ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : "";

            // Cosmico class ability
            if (playerClasses.getPlayerClass(attacker) == PlayerClasses.PlayerClass.COSMICO &&
                    name.equals(COSMIC_KATANA)) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
            }

            // Poseidon's Apprentice ability
            if (playerClasses.getPlayerClass(attacker) == PlayerClasses.PlayerClass.POSEIDONS_APPRENTICE &&
                    name.equals(POSEIDONS_TRIDENT)) {
                Location loc = event.getEntity().getLocation();
                loc.getWorld().createExplosion(loc, 2f, false, false);
                for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
                    if (nearby instanceof LivingEntity && !nearby.equals(attacker)) {
                        ((LivingEntity) nearby).damage(10, attacker);
                    }
                }
            }

            // Gods Apprentice vs Demon Kind interactions
            if (playerClasses.getPlayerClass(attacker) == PlayerClasses.PlayerClass.GODS_APPRENTICE &&
                    target.getWorld().getEnvironment() == World.Environment.NETHER) {
                event.setDamage(event.getDamage() * 2);
            }

            if (playerClasses.getPlayerClass(attacker) == PlayerClasses.PlayerClass.DEMON_KIND &&
                    playerClasses.getPlayerClass(target) == PlayerClasses.PlayerClass.GODS_APPRENTICE) {
                event.setDamage(event.getDamage() * 0.5);
            }
        }

        if (damager.hasMetadata("summoner")) {
            UUID summonerUUID = (UUID) damager.getMetadata("summoner").get(0).value();
            if (summonerUUID.equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!isCustomItem(item)) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (name.equals(CLASS_SELECTOR)) {
            event.setCancelled(true);
            plugin.getClassSelectionGUI().open(player);
            return;
        }
        // In your onInteract method:
        System.out.println("Item interacted: " + (item != null ? item.getType() : "null"));
        if (item != null && item.hasItemMeta()) {
            System.out.println("Item name: " + item.getItemMeta().getDisplayName());
        }

        switch (name) {
            case COSMIC_KATANA:
                useCosmicKatana(player);
                break;
            case STAR_STAFF:
                useStarStaff(player);
                break;
            case EARTH_WAND:
                useEarthWand(player);
                break;
            case POSEIDONS_TRIDENT:
                usePoseidonsTrident(player);
                break;
            case LICH_STAFF:
                useLichStaff(player);
                break;
            case CALLING_OF_THE_WARDEN:
                useCallingOfTheWarden(player);
                break;
            case MAGIC_SACK:
                event.setCancelled(true);
                giveRandomLegendaryItem(player);
                item.setAmount(item.getAmount() - 1);
                break;
            case ILLUSIONEER_TOTEM:
                if (playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ILLUSIONEER) {
                    useIllusioneerTotem(player);
                }
                break;
        }
    }

    private void useIllusioneerTotem(Player player) {
        if (checkCooldown(player, "illusioneer_totem", 60)) return;

        // Spawn 4 clones around the player
        for (int i = 0; i < 4; i++) {
            Location loc = player.getLocation().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1);
            ArmorStand clone = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            clone.setVisible(true);
            clone.setSmall(true);
            clone.setGravity(false);
            clone.setInvulnerable(true);
            clone.setCustomName(player.getName() + "'s Clone");
            clone.setCustomNameVisible(true);

            // Remove after 10 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    clone.remove();
                }
            }.runTaskLater(plugin, 200);
        }

        player.sendMessage(ChatColor.GOLD + "You spawn illusionary clones!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCustomItem(item)) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (name.equals(THUNDER_STRIKER)) {
            handleThunderStriker(event, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCustomItem(item)) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());



        // Life Drainer - Healing on Critical Hits
        if (name.equals(LIFE_DRAINER)) {
            if (player.getFallDistance() > 0 && !player.isOnGround() && !player.isSneaking()) {
                double health = player.getHealth();
                double newHealth = Math.min(player.getMaxHealth(), health + 4);
                player.setHealth(newHealth);

                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onPlayerHeldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GODS_APPRENTICE &&
                event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }

        player = (Player) event.getEntity();
        ItemStack offhand = player.getInventory().getItemInOffHand();

        if (offhand != null && isCustomItem(offhand)) {
            String name = ChatColor.stripColor(offhand.getItemMeta().getDisplayName());
            if (name.equals(ENHANCEMENT_ORB)) {
                if (player.getHealth() - event.getFinalDamage() <= 10) {
                    long now = System.currentTimeMillis();
                    if (!lastAbsorptionTime.containsKey(player) || now - lastAbsorptionTime.get(player) > 60000) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 2));
                        lastAbsorptionTime.put(player, now);
                        player.sendMessage(ChatColor.GOLD + "Enhancement Orb activates, protecting you!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTick(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack offhand = player.getInventory().getItemInOffHand();

        if (offhand != null && isCustomItem(offhand)) {
            String name = ChatColor.stripColor(offhand.getItemMeta().getDisplayName());
            if (name.equals(ENHANCEMENT_ORB)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        if (!isCustomItem(bow) || !bow.getType().equals(BOW)) return;

        String bowName = ChatColor.stripColor(bow.getItemMeta().getDisplayName());
        if (bowName.equals(ICE_BOW)) {
            addIceTrail((Arrow) event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Arrow arrow = (Arrow) event.getEntity();
        Player shooter = (Player) arrow.getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        if (!isCustomItem(bow) || !bow.getType().equals(BOW)) return;

        String bowName = ChatColor.stripColor(bow.getItemMeta().getDisplayName());
        if (bowName.equals(ICE_BOW)) {
            handleIceBowHit(arrow, event);
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack chest = player.getInventory().getChestplate();

        if (!isCustomItem(chest) || !chest.getType().equals(ELYTRA)) return;

        String name = ChatColor.stripColor(chest.getItemMeta().getDisplayName());
        if (!name.equals(DRAGON_WINGS)) return;

        if (event.isSneaking()) {
            sneakingStart.put(player.getUniqueId(), System.currentTimeMillis());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isSneaking() || !player.isOnline()) {
                        cancel();
                        return;
                    }
                    long start = sneakingStart.getOrDefault(player.getUniqueId(), 0L);
                    if (start == 0L) {
                        cancel();
                        return;
                    }
                    if (System.currentTimeMillis() - start >= 5000) {
                        if (!player.isOnGround()) {
                            player.sendMessage(ChatColor.RED + "You must be on the ground to use Dragon Wings.");
                            cancel();
                            return;
                        }
                        if (checkCooldown(player, "dragon_wings", 30)) {
                            cancel();
                            return;
                        }
                        player.setVelocity(new Vector(0, 4, 0));
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
                        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 50);
                        sneakingStart.remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 20);
        } else {
            sneakingStart.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isCustomItem(item)) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name.equals(MAGIC_SACK)) {
            event.setCancelled(true);
            giveRandomLegendaryItem(event.getPlayer());
            item.setAmount(item.getAmount() - 1);
        }
    }

    /* ========== ITEM ABILITIES ========== */

    private void useCosmicKatana(Player player) {
        if (checkCooldown(player, "cosmic_katana", 10)) return;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
        Vector dashVector = player.getLocation().getDirection().normalize().multiply(2);
        Location startLoc = player.getLocation().clone();
        Set<UUID> damagedEntities = new HashSet<>();

        new BukkitRunnable() {
            int ticks = 0;
            double distanceTraveled = 0;
            Location lastLocation = startLoc.clone();

            @Override
            public void run() {
                if (ticks++ >= 10) {
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 5);
                    cancel();
                    return;
                }

                player.setVelocity(dashVector);
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10);

                double distanceThisTick = player.getLocation().distance(lastLocation);
                distanceTraveled += distanceThisTick;
                lastLocation = player.getLocation().clone();

                for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
                    if (entity instanceof LivingEntity && !entity.equals(player) && !damagedEntities.contains(entity.getUniqueId())) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector());

                        if (toEntity.normalize().dot(dashVector.normalize()) > 0.33) {
                            livingEntity.damage(8.0, player);
                            damagedEntities.add(entity.getUniqueId());
                            Vector knockback = dashVector.clone().multiply(0.5).setY(0.3);
                            livingEntity.setVelocity(knockback);
                            livingEntity.getWorld().spawnParticle(
                                    Particle.DAMAGE_INDICATOR,
                                    livingEntity.getLocation().add(0, 1, 0),
                                    5
                            );
                        }
                    }
                }

                if (ticks % 2 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.CRIT,
                            player.getLocation().add(0, 1, 0),
                            3,
                            0.3, 0.3, 0.3, 0.1
                    );
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void useStarStaff(Player player) {
        if (checkCooldown(player, "star_staff", 20)) return;

        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.8f);

        ArmorStand proj = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        proj.setVisible(false);
        proj.setGravity(false);
        proj.setMarker(true);
        proj.setCustomName("Star Staff Projectile");
        proj.setCustomNameVisible(false);

        new BukkitRunnable() {
            int life = 0;

            @Override
            public void run() {
                if (life++ > 40 || proj.isDead()) {
                    explode(proj.getLocation());
                    proj.remove();
                    cancel();
                    return;
                }

                proj.teleport(proj.getLocation().add(dir));
                player.getWorld().spawnParticle(Particle.EXPLOSION, proj.getLocation(), 15, 0.1, 0.1, 0.1, 0.05);

                proj.getNearbyEntities(1.0, 1.0, 1.0).forEach(entity -> {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        ((LivingEntity) entity).damage(12.0, player);
                    }
                });
            }

            private void explode(Location loc) {
                loc.getWorld().createExplosion(loc, 7f, false, false);
                loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, (int) 1.5);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void useEarthWand(Player player) {
        if (checkCooldown(player, "earth_wand", 30)) return;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 4));

        Location center = player.getLocation();
        int radius = 12;
        Map<Location, Material> originalBlocks = new HashMap<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared >= (radius - 1) * (radius - 1) && distanceSquared <= radius * radius) {
                        Location loc = center.clone().add(x, y, z);
                        originalBlocks.put(loc, loc.getBlock().getType());
                        loc.getBlock().setType(OBSIDIAN);
                    }
                }
            }
        }

        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 30;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            player.getWorld().spawnParticle(
                    Particle.BLOCK,
                    center.clone().add(x, 0, z),
                    10,
                    OBSIDIAN.createBlockData()
            );
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            originalBlocks.forEach((loc, material) -> {
                loc.getBlock().setType(material);
                loc.getWorld().spawnParticle(
                        Particle.BLOCK,
                        loc,
                        5,
                        material.createBlockData()
                );
            });
            player.getWorld().playSound(center, Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
        }, 300L);
    }

    private void usePoseidonsTrident(Player player) {
        if (!player.isSneaking()) return;
        if (checkCooldown(player, "poseidons_trident", 10)) return;

        Vector launch = player.getLocation().getDirection().normalize();

        // Boost effect when in water or rain
        if (player.isInWater() || player.getWorld().hasStorm()) {
            launch.multiply(5);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 1f, 1f);
        } else {
            launch.multiply(3);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1f, 1f);
        }

        launch.setY(1.5);
        player.setVelocity(launch);
        player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 20);

        Particle particle = (player.isInWater() || player.getWorld().hasStorm())
                ? Particle.SPLASH
                : Particle.CLOUD;
        player.getWorld().spawnParticle(particle, player.getLocation(), 30);
    }

    private void useLichStaff(Player player) {
        if (checkCooldown(player, "lich_staff", 120)) return;

        Location spawnLoc = player.getLocation().add(0, 1, 0);
        player.getWorld().playSound(spawnLoc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 30, 1, 1, 1, 0.1);

        for (int i = 0; i < 2; i++) {
            Location offsetLoc = spawnLoc.clone().add(
                    (Math.random() - 0.5) * 2,
                    0,
                    (Math.random() - 0.5) * 2
            );

            Skeleton skeleton = (Skeleton) player.getWorld().spawnEntity(offsetLoc, EntityType.SKELETON);
            skeleton.getEquipment().setItemInMainHand(new ItemStack(BOW));
            skeleton.getEquipment().setItemInMainHandDropChance(0.0f);
            skeleton.setCustomName(player.getName() + "'s Skeleton");
            skeleton.setCustomNameVisible(true);
            skeleton.setRemoveWhenFarAway(true);

            // Set the summoner metadata
            skeleton.setMetadata("summoner", new FixedMetadataValue(plugin, player.getUniqueId()));

            // Immediate target finding with proper filtering
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Entity nearby : skeleton.getNearbyEntities(15, 15, 15)) {
                        if (nearby instanceof LivingEntity &&
                                !nearby.equals(player) &&
                                !nearby.equals(skeleton) &&
                                (!(nearby instanceof Tameable) ||
                                        !((Tameable) nearby).isTamed() ||
                                        !player.equals(((Tameable) nearby).getOwner()))) {
                            skeleton.setTarget((LivingEntity) nearby);
                            break; // Target the first valid entity found
                        }
                    }
                }
            }.runTaskLater(plugin, 1L); // 1 tick delay to ensure proper spawning

            // Despawn after time
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (skeleton.isValid()) {
                        skeleton.getWorld().spawnParticle(Particle.SMOKE, skeleton.getLocation(), 10);
                        skeleton.remove();
                    }
                }
            }.runTaskLater(plugin, 1200L);
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "You summon skeleton archers to fight for you!");
    }

    private void useCallingOfTheWarden(Player player) {
        if (checkCooldown(player, "calling_of_the_warden", 60)) return;

        Location spawnLoc = player.getLocation().add(0, 1, 0);
        player.getWorld().playSound(spawnLoc, ENTITY_WARDEN_STEP, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, spawnLoc, 30, 1, 1, 1, 0.1);

        Zombie zombie = (Zombie) player.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
        zombie.getEquipment().setItemInMainHand(new ItemStack(IRON_SWORD));
        zombie.getEquipment().setItemInMainHandDropChance(0.0f);
        zombie.setCustomName(ChatColor.DARK_PURPLE + "Warden's Minion");
        zombie.setCustomNameVisible(true);
        zombie.setRemoveWhenFarAway(true);
        zombie.setMaxHealth(40);
        zombie.setHealth(40);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));

        // Set the summoner metadata
        zombie.setMetadata("summoner", new FixedMetadataValue(plugin, player.getUniqueId()));

        // Immediate target finding
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity nearby : zombie.getNearbyEntities(15, 15, 15)) {
                    if (nearby instanceof LivingEntity &&
                            !nearby.equals(player) &&
                            !nearby.equals(zombie) &&
                            (!(nearby instanceof Tameable) ||
                                    !((Tameable) nearby).isTamed() ||
                                    !player.equals(((Tameable) nearby).getOwner()))) {
                        zombie.setTarget((LivingEntity) nearby);
                        break; // Target the first valid entity found
                    }
                }
            }
        }.runTaskLater(plugin, 1L); // 1 tick delay to ensure proper spawning

        // Visual effects
        new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isDead()) {
                    cancel();
                    return;
                }
                zombie.getWorld().spawnParticle(Particle.SQUID_INK, zombie.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.1);
            }
        }.runTaskTimer(plugin, 0, 10);

        // Despawn after time
        new BukkitRunnable() {
            @Override
            public void run() {
                if (zombie.isValid()) {
                    zombie.getWorld().spawnParticle(Particle.SMOKE, zombie.getLocation(), 10);
                    zombie.remove();
                }
            }
        }.runTaskLater(plugin, 1200L);

        player.sendMessage(ChatColor.DARK_PURPLE + "You summon a warden's minion to fight for you!");
    }

    private void handleThunderStriker(EntityDamageByEntityEvent event, Player player) {
        try {
            if (player.getFallDistance() > 12) {
                LivingEntity target = (LivingEntity) event.getEntity();
                Location targetLoc = target.getLocation();
                World world = targetLoc.getWorld();

                double bonusDamage = Math.min(10, (player.getFallDistance() - 12) * 1);
                event.setDamage(event.getDamage() + bonusDamage);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (world != null) {
                            world.createExplosion(
                                    targetLoc,
                                    3.0f,
                                    false,
                                    false,
                                    player
                            );
                            world.spawnParticle(Particle.EXPLOSION, targetLoc, 3);
                        }
                    }
                }.runTask(plugin);

                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.RESISTANCE,
                        40,
                        4,
                        false,
                        false
                ));

                player.getWorld().spawnParticle(Particle.CRIT, targetLoc, 20);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            plugin.getLogger().warning("Error in Thunder Striker ability: " + e.getMessage());
        }
    }

    private void addIceTrail(Arrow arrow) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) {
                    cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(
                        Particle.SNOWFLAKE,
                        arrow.getLocation(),
                        2,
                        0.1, 0.1, 0.1,
                        0.01
                );
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void handleIceBowHit(Arrow arrow, ProjectileHitEvent event) {
        arrow.getWorld().playSound(
                arrow.getLocation(),
                Sound.BLOCK_GLASS_BREAK,
                0.8f,
                1.2f
        );

        arrow.getWorld().spawnParticle(
                Particle.ITEM_SNOWBALL,
                arrow.getLocation(),
                10,
                0.3, 0.3, 0.3,
                0.1
        );

        if (event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getHitEntity();
            if (Math.random() < 0.3) {
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS,
                        60,
                        4,
                        false,
                        true
                ));

                target.getWorld().spawnParticle(
                        Particle.BLOCK,
                        target.getLocation().add(0, 1, 0),
                        15,
                        0.5, 0.5, 0.5,
                        0,
                        new ItemStack(ICE)
                );
            }
        }
    }


    /* ========== UTILITY METHODS ========== */

    private void giveRandomLegendaryItem(Player player) {
        ItemStack[] legendaryItems = {
                createItem(NETHERITE_SWORD, COSMIC_KATANA),
                createItem(NETHERITE_AXE, THUNDER_STRIKER),
                createItem(BLAZE_ROD, STAR_STAFF),
                createItem(STICK, EARTH_WAND),
                createItem(TRIDENT, POSEIDONS_TRIDENT),
                createItem(BOW, ICE_BOW),
                createItem(ELYTRA, DRAGON_WINGS),
                createItem(BONE, LICH_STAFF),
                createItem(NETHER_STAR, CALLING_OF_THE_WARDEN),
                createItem(NETHERITE_INGOT, ENHANCEMENT_ORB),
                createItem(IRON_SWORD, LIFE_DRAINER)
        };

        Random random = new Random();
        ItemStack reward = legendaryItems[random.nextInt(legendaryItems.length)].clone();

        ItemMeta meta = reward.getItemMeta();
        meta.setUnbreakable(true);
        reward.setItemMeta(meta);
        reward.setAmount(1);

        player.getInventory().addItem(reward);
        player.playSound(player.getLocation(), ITEM_BUNDLE_DROP_CONTENTS, 1.0f, 1.0f);
        player.spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "The magic sack contained: " +
                ChatColor.stripColor(reward.getItemMeta().getDisplayName()));
    }

    public ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private boolean checkCooldown(Player player, String key, int seconds) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldown = seconds * 1000L;
        String mapKey = id + key;
        if (cooldowns.containsKey(mapKey)) {
            long elapsed = now - cooldowns.get(mapKey);
            if (elapsed < cooldown) {
                player.sendMessage(ChatColor.RED + "Ability on cooldown. " +
                        (cooldown - elapsed) / 1000 + "s remaining.");
                return true;
            }
        }
        cooldowns.put(mapKey, now);
        return false;
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals(COSMIC_KATANA) ||
                name.equals(THUNDER_STRIKER) ||
                name.equals(STAR_STAFF) ||
                name.equals(EARTH_WAND) ||
                name.equals(POSEIDONS_TRIDENT) ||
                name.equals(DRAGON_WINGS) ||
                name.equals(ICE_BOW) ||
                name.equals(LICH_STAFF) ||
                name.equals(CALLING_OF_THE_WARDEN) ||
                name.equals(ENHANCEMENT_ORB) ||
                name.equals(LIFE_DRAINER) ||
                name.equals(MAGIC_SACK);
    }
    public ItemStack createClassSelector() {
        ItemStack selector = new ItemStack(Material.BOOK);
        ItemMeta meta = selector.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + CLASS_SELECTOR);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Right-click to choose your class",
                ChatColor.GRAY + "Gain special abilities and bonuses"
        ));
        selector.setItemMeta(meta);
        return selector;
    }
}