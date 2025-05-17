package com.Items;

import LegendaryWeapons.legendaryWeapons.LegendaryWeapons;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassSelectionGUI {
    private final PlayerClasses playerClasses;
    private final LegendaryWeapons plugin;

    public ClassSelectionGUI(LegendaryWeapons plugin, PlayerClasses playerClasses) {
        this.plugin = plugin;
        this.playerClasses = playerClasses;
    }

    public void open(Player player) {
        try {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }

            Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Choose Your Class");
            plugin.getLogger().info("[DEBUG] Opening class selection for " + player.getName());

            // Change all these lines in the open() method:
            gui.setItem(0, createClassItem(Material.WOODEN_SWORD, PlayerClasses.PlayerClass.DEFAULT,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.DEFAULT));
            gui.setItem(2, createClassItem(Material.IRON_SWORD, PlayerClasses.PlayerClass.WARRIOR,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.WARRIOR));
            gui.setItem(4, createClassItem(Material.BOW, PlayerClasses.PlayerClass.ARCHER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ARCHER));
            gui.setItem(6, createClassItem(Material.NETHER_STAR, PlayerClasses.PlayerClass.WARDEN_COMMANDER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.WARDEN_COMMANDER));
            gui.setItem(8, createClassItem(Material.BONE, PlayerClasses.PlayerClass.SKELETON_ARCHER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.SKELETON_ARCHER));
            gui.setItem(11, createClassItem(Material.TOTEM_OF_UNDYING, PlayerClasses.PlayerClass.ILLUSIONEER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ILLUSIONEER));
            gui.setItem(13, createClassItem(Material.DIAMOND_PICKAXE, PlayerClasses.PlayerClass.MINER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.MINER));
            gui.setItem(15, createClassItem(Material.ENDER_EYE, PlayerClasses.PlayerClass.COSMICO,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.COSMICO));
            gui.setItem(17, createClassItem(Material.FEATHER, PlayerClasses.PlayerClass.GODS_APPRENTICE,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GODS_APPRENTICE));
            gui.setItem(18, createClassItem(Material.WITHER_SKELETON_SKULL, PlayerClasses.PlayerClass.DEMON_KIND,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.DEMON_KIND));
            gui.setItem(20, createClassItem(Material.TRIDENT, PlayerClasses.PlayerClass.POSEIDONS_APPRENTICE,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.POSEIDONS_APPRENTICE));
            gui.setItem(22, createClassItem(Material.IRON_HOE, PlayerClasses.PlayerClass.DEATHBRINGER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.DEATHBRINGER));
            gui.setItem(24, createClassItem(Material.SHULKER_SHELL, PlayerClasses.PlayerClass.SHULKSTER,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.SHULKSTER));
            gui.setItem(26, createClassItem(Material.ALLAY_SPAWN_EGG, PlayerClasses.PlayerClass.ALLAY,
                    playerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.ALLAY));

            // Fill empty slots with black stained glass panes
            ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);

            for (int i = 0; i < gui.getSize(); i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, filler);
                }
            }

            player.openInventory(gui);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening class selection GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ItemStack createClassItem(Material material, PlayerClasses.PlayerClass playerClass, boolean isSelected) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName((isSelected ? ChatColor.GREEN : ChatColor.WHITE) + playerClass.getDisplayName());
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + playerClass.getDescription(),
                "",
                isSelected ? ChatColor.GREEN + "✓ Currently Selected" : ChatColor.YELLOW + "Click to select"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        try {
            if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Choose Your Class")) {
                return;
            }

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta() || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                return;
            }

            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            plugin.getLogger().info("[DEBUG] Player clicked: " + displayName);

            for (PlayerClasses.PlayerClass playerClass : PlayerClasses.PlayerClass.values()) {
                if (playerClass.getDisplayName().equals(displayName)) {
                    playerClasses.setPlayerClass(player, playerClass);

                    // Give special items for certain classes
                    if (playerClass == PlayerClasses.PlayerClass.ILLUSIONEER) {
                        player.getInventory().addItem(createIllusioneerTotem());
                    } else if (playerClass == PlayerClasses.PlayerClass.SHULKSTER) {
                        player.getInventory().addItem(new ItemStack(Material.SHULKER_BOX));
                    }

                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.GOLD + "You are now a " + playerClass.getDisplayName() + "!");
                    return;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling class selection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ItemStack createIllusioneerTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Illusioneer's Totem");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Spawns clones when activated",
                ChatColor.DARK_GRAY + "1 minute cooldown"
        ));
        totem.setItemMeta(meta);
        return totem;
    }
}