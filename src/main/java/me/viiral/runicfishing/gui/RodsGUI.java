package me.viiral.runicfishing.gui;

import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.gui.model.GUI;
import me.viiral.runicfishing.gui.model.InventoryClick;
import me.viiral.runicfishing.gui.model.InventoryClose;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RodsGUI extends GUI {

    public RodsGUI(RunicFishing main) {
        super(main);

        this.setTitle(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("fishing-rods.gui.rods.title", " ")));
        this.setSize(main.getConfig().getInt("fishing-rods.gui.rods.size", 3) * 9);
    }

    @Override
    protected void buildInventory(Player player, Object... objects) {
        UUID uuid = player.getUniqueId();
        this.main.getConfig().getConfigurationSection("fishing-rods.gui.rods.items").getKeys(false).forEach(slotString ->
                {
                    String path = "fishing-rods.gui.rods.items." + slotString;

                    this.setItem(Integer.parseInt(slotString), this.main.getItemManager().getItemStackFromPathWithPlaceholders(path, player), event ->
                            {
                                int pointCost = this.main.getConfig().getInt(path + ".points-cost", 1);
                                if (pointCost > this.main.getFishingManager().getPlayerFishPoints(uuid)) {
                                    player.sendMessage(this.main.getMessageManager().getFishingRodsInsufficientPoints());

                                    player.closeInventory();
                                    return;
                                }

                                int moneyCost = this.main.getConfig().getInt(path + ".money-cost", 1);
                                if (moneyCost > this.main.getEconomy().getBalance(player)) {
                                    player.sendMessage(this.main.getMessageManager().getFishingRodsInsufficientMoney());

                                    player.closeInventory();
                                    return;
                                }

                                this.main.getFishingManager().chargePlayerFishPoints(player, pointCost);
                                this.main.getEconomy().withdrawPlayer(player, moneyCost);

                                String command = this.main.getConfig().getString(path + ".command", " ");
                                command = command.replace("%player%", player.getName());

                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            }
                    );
                }
        );

        if (main.getConfig().getBoolean("fishing-rods.gui.rods.fill-empty-slots", true)) this.fillInventory();
    }

    @Override
    protected InventoryClick mainInventoryClick() {
        return inventoryClick -> inventoryClick.setCancelled(true);
    }

    @Override
    protected InventoryClose inventoryClose() {
        return null;
    }

}
