package LegendaryWeapons.legendaryWeapons;

import com.Items.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemFlag;
import java.util.Arrays;

public class LegendaryWeapons extends JavaPlugin {
    private PlayerClasses playerClasses;
    private ClassSelectionGUI classSelectionGUI;
    private static LegendaryWeapons instance;

    // Dragon armor constants
    private static final String DRAGON_HELMET = "Dragon Helmet";
    private static final String DRAGON_CHESTPLATE = "Dragon Chestplate";
    private static final String DRAGON_LEGGINGS = "Dragon Leggings";
    private static final String DRAGON_BOOTS = "Dragon Boots";

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning("Plugin is already initialized!");
            return;
        }

        saveDefaultConfig();
        reloadConfig();
        instance = this;

        // Initialize core components first
        this.playerClasses = new PlayerClasses(this);
        this.classSelectionGUI = new ClassSelectionGUI(this, playerClasses);
        Items items = new Items(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(items, this);
        getServer().getPluginManager().registerEvents(new Armor(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, items), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Register commands and recipes
        getCommand("giveitem").setExecutor(new Commands());
        registerMagicSackRecipe();
        registerDragonArmorRecipes();

        getLogger().info("Legendary Weapons plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Legendary Weapons plugin disabled!");
        instance = null;
    }

    public PlayerClasses getPlayerClasses() {
        return playerClasses;
    }

    public ClassSelectionGUI getClassSelectionGUI() {
        return classSelectionGUI;
    }

    private void registerMagicSackRecipe() {
        ItemStack magicSack = new ItemStack(Material.INK_SAC);
        ItemMeta meta = magicSack.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Magic Sack of Legend");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Contains a random legendary weapon",
                ChatColor.DARK_GRAY + "Right-click to open"
        ));

        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        magicSack.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "magic_sack"),
                magicSack
        );

        recipe.shape("DND", "GCG", "DND");
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('C', Material.BARREL);

        getServer().addRecipe(recipe);
        getLogger().info("Magic Sack recipe registered");
    }

    private void registerDragonArmorRecipes() {
        registerDragonPiece("helmet", Material.NETHERITE_HELMET, DRAGON_HELMET);
        registerDragonPiece("chestplate", Material.NETHERITE_CHESTPLATE, DRAGON_CHESTPLATE);
        registerDragonPiece("leggings", Material.NETHERITE_LEGGINGS, DRAGON_LEGGINGS);
        registerDragonPiece("boots", Material.NETHERITE_BOOTS, DRAGON_BOOTS);
    }

    private void registerDragonPiece(String type, Material baseItem, String displayName) {
        ItemStack result = new ItemStack(baseItem);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + displayName);
        meta.setUnbreakable(true);
        result.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "dragon_" + type),
                result
        );

        recipe.shape("NNN", "NAN", "NNN");
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('A', baseItem);

        getServer().addRecipe(recipe);
        getLogger().info("Registered Dragon " + type + " recipe");
    }
}