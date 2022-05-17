package me.viiral.runicfishing.gui.model;

import me.viiral.runicfishing.RunicFishing;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ConcurrentModificationException;
import java.util.UUID;

public class GUIListener implements Listener {

    private final RunicFishing main;

    public GUIListener(RunicFishing main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        try {
            HumanEntity humanEntity = event.getWhoClicked();
            if (!(humanEntity instanceof Player)) return;
            Player player = (Player) humanEntity;
            UUID uuid = player.getUniqueId();
            GUI gui = main.getGuiManager().getPlayerGui(uuid);
            if (gui == null) return;
            if (!event.getView().getTitle().equals(gui.getTitle())) return;
            if (gui.getInventoryClick() != null) gui.getInventoryClick().click(event);
            InventoryClick inventoryClick = gui.getClick(event.getRawSlot());
            if (inventoryClick != null) inventoryClick.click(event);
        } catch (ConcurrentModificationException ignored) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player)) return;
        Player player = (Player) humanEntity;
        UUID uuid = player.getUniqueId();
        GUI gui = main.getGuiManager().removePlayerGui(uuid);
        if (gui == null) return;
        if (!event.getView().getTitle().equals(gui.getTitle())) return;
        InventoryClose inventoryClose = gui.getInventoryClose();
        if (inventoryClose != null) inventoryClose.close(event);
    }

}
