package com.Items;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("giveitem")) return false;
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /giveitem <player> <item>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        String itemKey = args[1].toLowerCase();
        ItemStack item ;

        switch (itemKey) {
            case "cosmic_katana":
                item = createItem(Material.NETHERITE_SWORD, "Cosmic Katana", "Right-click to dash forward.");
                break;
            case "thunder_striker":
                item = createItem(Material.NETHERITE_AXE, "Thunder Striker", "Deals more damage when falling.");
                break;
            case "star_staff":
                item = createItem(Material.BLAZE_ROD, "Star Staff", "Shoots a homing explosive projectile.");
                break;
            case "earth_wand":
                item = createItem(Material.STICK, "Earth Wand", "Grants resistance and creates obsidian shield.");
                break;
            case "poseidons_trident":
                item = createItem(Material.TRIDENT, "Poseidon's Trident", "Riptide when sneaking, otherwise normal.");
                break;
            case "dragon_wings":
                item = createItem(Material.ELYTRA, "Dragon Wings", "Sneak 10s to launch upward.");
                break;
            case "ice_bow":
                item = createItem(Material.BOW, "Ice Bow", "Chance to freeze hit entities.");
                break;
            case "magic_sack":
                item = createMagicSack();
                break;
            case "lich_staff":
                item = createItem(Material.BONE, "Lich Staff", "Right-click to summon skeleton archers.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown item: " + itemKey);
                return true;
        }

        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "Gave " + itemKey + " to " + target.getName());
        return true;
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(ChatColor.GRAY + line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMagicSack() {
        ItemStack sack = new ItemStack(Material.INK_SAC);
        ItemMeta meta = sack.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Magic Sack of Legend");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Contains a random legendary weapon");
        lore.add(ChatColor.DARK_GRAY + "Right-click to open");
        meta.setLore(lore);

        // Add enchantment glint (visual only)
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        sack.setItemMeta(meta);
        return sack;
    }
}