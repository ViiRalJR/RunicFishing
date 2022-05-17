package me.viiral.runicfishing.command;

import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.gui.RodUpgradeGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RodUpgradeCommand implements CommandExecutor {

    private final RunicFishing main;

    public RodUpgradeCommand(RunicFishing main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getType() != Material.FISHING_ROD && !main.getItemManager().hasNbtCompound(itemInHand, "distant")) {
            player.sendMessage(this.main.getMessageManager().getFishingRodsNotHoldingUpgradable());
        } else {
            new RodUpgradeGUI(main).open(player, player.getInventory().getHeldItemSlot());
        }

        return true;
    }

}
