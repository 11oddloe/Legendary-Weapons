package LegendaryWeapons.legendaryWeapons;

import com.Items.Armor;
import com.Items.Items;
import com.Items.Commands;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemFlag;
import java.util.Arrays;
import java.util.UUID;

public class LegendaryWeapons extends JavaPlugin {

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

        instance = this;

        // Register event listeners
        getServer().getPluginManager().registerEvents(new Items(this), this);
        getServer().getPluginManager().registerEvents(new Armor(this), this);

        // Register commands
        getCommand("giveitem").setExecutor(new Commands());

        // Register recipes
        registerMagicSackRecipe();
        registerDragonArmorRecipes();

        getLogger().info("Legendary Weapons plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Legendary Weapons plugin disabled!");
        instance = null;
    }

    private void registerMagicSackRecipe() {
        ItemStack magicSack = new ItemStack(Material.BUNDLE);
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

    public static LegendaryWeapons getInstance() {
        return instance;
    }

    private void registerGoldenCrownRecipe() {
        // Create the Golden Crown item with netherite stats
        ItemStack goldenCrown = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = goldenCrown.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Golden Crown");
        meta.setUnbreakable(true);

        // Set armor toughness and knockback resistance like netherite
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                new AttributeModifier(UUID.randomUUID(), "armor_toughness", 3.0,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
        meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                new AttributeModifier(UUID.randomUUID(), "knockback_resistance", 0.1,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

        // Add protection equivalent to netherite
        meta.addEnchant(Enchantment.PROTECTION, 3, true);

        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Royal headwear with the strength of netherite",
                ChatColor.BLUE + "Grants regeneration and resistance"
        ));
        goldenCrown.setItemMeta(meta);

        // Create the recipe (now cheaper since we're not using netherite)
        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "golden_crown"),
                goldenCrown
        );

        recipe.shape("GGG", "GDG", "GGG");

        // G = Gold Block, D = Diamond Helmet
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('D', Material.GOLDEN_HELMET);

        getServer().addRecipe(recipe);
    }
}
