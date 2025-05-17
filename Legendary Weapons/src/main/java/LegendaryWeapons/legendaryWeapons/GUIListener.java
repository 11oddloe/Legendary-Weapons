package LegendaryWeapons.legendaryWeapons;

import com.Items.ClassSelectionGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {
    private final ClassSelectionGUI classSelectionGUI;
    private final LegendaryWeapons plugin;

    public GUIListener(LegendaryWeapons plugin) {
        this.plugin = plugin;
        this.classSelectionGUI = plugin.getClassSelectionGUI();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (classSelectionGUI != null) {
            classSelectionGUI.handleClick(event);
        } else {
            plugin.getLogger().warning("ClassSelectionGUI is null during inventory click!");
        }
    }
}