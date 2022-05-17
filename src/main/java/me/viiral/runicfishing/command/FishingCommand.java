package me.viiral.runicfishing.command;

import com.google.common.util.concurrent.AtomicDouble;
import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.gui.FishingShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

public class FishingCommand implements CommandExecutor {
    // -------------------------------------------- //
    // LOCAL FIELDS
    // -------------------------------------------- //

    private final RunicFishing main;

    // -------------------------------------------- //
    // CONSTRUCT
    // -------------------------------------------- //

    public FishingCommand(RunicFishing main) {
        this.main = main;
    }

    // -------------------------------------------- //
    // OVERRIDES
    // -------------------------------------------- //

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            this.main.getMessageManager().getFishingRodsHelp().forEach(sender::sendMessage);
            return true;
        }

        switch (args[0]) {
            case "shop": {
                if (!(sender instanceof Player)) return true;

                new FishingShopGUI(this.main).open((Player) sender);

                return true;
            }
            case "top": {
                this.main.getFishingManager().getPlayerLeaderBoard().forEach(sender::sendMessage);
                return true;
            }
            case "givepoints": {
                if(args.length == 3) {
                    Player p = Bukkit.getPlayerExact(args[1]);
                    if(p == null) {
                        sender.sendMessage("Player is null.");
                        return true;
                    }
                    try {
                        Integer.parseInt(args[2]);
                    } catch (NumberFormatException exc) {
                        sender.sendMessage("Not a number.");
                        return true;
                    }
                    AtomicDouble points = main.getFishingManager().getPlayerStatistics().get(p.getUniqueId()).getPoints();
                    points.set(points.get() + Integer.parseInt(args[2]));

                    sender.sendMessage("Set " + args[1] + "'s points to " + args[2]);
                    return true;
                }
                if(!(sender instanceof Player)) {
                    sender.sendMessage("You need to be a player.");
                    return true;
                }
                AtomicDouble points = main.getFishingManager().getPlayerStatistics().get(((Player) sender).getUniqueId()).getPoints();

                points.set(points.get() + Integer.parseInt(args[1]));
                sender.sendMessage("Set your points to " + args[1]);
                return true;
            }
            case "rod": {
                if (args.length < 3) break;

                int fishingRodTier;
                try {
                    fishingRodTier = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(this.main.getMessageManager().getFishingRodsInvalidTier());
                    return true;
                }

                Player player = Bukkit.getPlayer(args[2]);
                if (player == null) {
                    sender.sendMessage(this.main.getMessageManager().getFishingRodsInvalidPlayer());
                    return true;
                }

                ItemStack fishingRod;
                if (args.length < 4) {
                    fishingRod = this.main.getItemManager().getFishingRod(fishingRodTier, 0, 0, 0, 0);
                } else {
                    boolean maxed = Boolean.parseBoolean(args[3]);

                    if (maxed) {
                        fishingRod = this.main.getItemManager().getFishingRod(fishingRodTier, 5, 5, 5, 5);
                    } else {
                        fishingRod = this.main.getItemManager().getFishingRod(fishingRodTier, 0, 0, 0, 0);
                    }
                }

                if (fishingRod == null) {
                    sender.sendMessage(this.main.getMessageManager().getFishingRodsInvalidTier());
                    return true;
                }

                player.getInventory().addItem(fishingRod);

                return true;
            }
            case "points": {

                if (args.length != 2) break;

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (offlinePlayer == null) {
                    sender.sendMessage(this.main.getMessageManager().getFishingRodsInvalidPlayer());
                    return true;
                }

                String pointsNotice = this.main.getMessageManager().getFishingRodsFishPointsNotice();
                pointsNotice = pointsNotice.replace("%player%", offlinePlayer.getName())
                        .replace("%points%", this.main.getFishingManager().getPlayerFishPoints(offlinePlayer.getUniqueId()) + "");
                sender.sendMessage(pointsNotice);

                return true;
            }
            case "fish": {
                if (args.length != 2) break;

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (offlinePlayer == null) {
                    sender.sendMessage(this.main.getMessageManager().getFishingRodsInvalidPlayer());
                    return true;
                }

                String pointsNotice = this.main.getMessageManager().getFishingRodsFishAmountNotice();
                pointsNotice = pointsNotice.replace("%player%", offlinePlayer.getName())
                        .replace("%amount%", this.main.getFishingManager().getPlayerFishAmount(offlinePlayer.getUniqueId()) + "");
                sender.sendMessage(pointsNotice);

                return true;
            }
        }

        this.main.getMessageManager().getFishingRodsHelp().forEach(sender::sendMessage);
        return true;
    }

}
