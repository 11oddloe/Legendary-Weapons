package LegendaryWeapons.legendaryWeapons;

import com.Items.Items;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {
    private final LegendaryWeapons plugin;
    private final Items items;

    public PlayerJoinListener(LegendaryWeapons plugin, Items items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Safe class loading
        if (plugin.getPlayerClasses() != null) {
            plugin.getPlayerClasses().loadPlayerClass(player);
        } else {
            plugin.getLogger().severe("Failed to load player class - PlayerClasses is null!");
        }

        // Give class selector on first join
        if (!player.hasPlayedBefore()) {
            try {
                ItemStack classSelector = items.createItem(Material.BOOK, Items.CLASS_SELECTOR);
                player.getInventory().addItem(classSelector);
                player.sendMessage(ChatColor.GOLD + "Welcome! You've received a Class Selector item.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to give class selector: " + e.getMessage());
            }
        }
    }
}