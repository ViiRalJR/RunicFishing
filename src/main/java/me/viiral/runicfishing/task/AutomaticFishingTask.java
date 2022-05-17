package me.viiral.runicfishing.task;

import lombok.Getter;
import me.viiral.runicfishing.FishTier;
import me.viiral.runicfishing.Pair;
import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.manager.FishingManager;
import me.viiral.runicfishing.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class AutomaticFishingTask extends BukkitRunnable {

    private final RunicFishing main;

    public AutomaticFishingTask(RunicFishing main) {
        this.main = main;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, Pair<Location, Location>> entry : this.main.getFishingManager().getPlayersAutoFishing().entrySet()) {
            FishingManager fishingManager = this.main.getFishingManager();
            ItemManager itemManager = this.main.getItemManager();

            UUID uuid = entry.getKey();
            Location location = entry.getValue().getLeft();
            Player player = Bukkit.getPlayer(uuid);
            if (this.hasMoved(location, player.getLocation())) {
                fishingManager.removePlayerAutoFishing(uuid);
                player.sendMessage(this.main.getMessageManager().getFishingRodsAutoStopped());
                continue;
            }

            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand.getType() != Material.FISHING_ROD || !itemManager.hasNbtCompound(itemInHand, "tier")) {
                fishingManager.removePlayerAutoFishing(uuid);
                player.sendMessage(this.main.getMessageManager().getFishingRodsAutoStopped());
                continue;
            }

            List<FishTier> fishCaught = new ArrayList<>();
            int randomPick = ThreadLocalRandom.current().nextInt(0, 100);

            int crateFinder = itemManager.getNBTTagInt(itemInHand, "crateFinder");
            if (crateFinder != 0 && randomPick <= fishingManager.getCrateFinderChances().get(crateFinder)) {
                String crateFinderCommand = fishingManager.getCrateFinderCommand();
                Bukkit.getScheduler().runTask(this.main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), crateFinderCommand.replace("%player%", player.getName())));
            }

            int fishingRodTier = itemManager.getNBTTagInt(itemInHand, "tier");
            int luck = itemManager.getNBTTagInt(itemInHand, "luck");
            int fishNet = itemManager.getNBTTagInt(itemInHand, "fishNet");
            if (fishNet != 0 && randomPick <= fishingManager.getFishNetChances().get(fishNet)) {
                for (int i = 0; i < fishingManager.getFishNetAmount(); i++) {
                    randomPick = ThreadLocalRandom.current().nextInt(0, 100);
                    fishCaught.add(fishingManager.getCatch(luck, fishingRodTier, randomPick));
                }
            } else {
                fishCaught.add(fishingManager.getCatch(luck, fishingRodTier, randomPick));
            }

            final AtomicInteger fishPoints = new AtomicInteger(0);
            final AtomicInteger fishMoney = new AtomicInteger(0);
            final StringBuilder fishDesc = new StringBuilder();

            int doubleCatch = itemManager.getNBTTagInt(itemInHand, "doubleCatch");
            if (doubleCatch != 0 && randomPick <= fishingManager.getDoubleCatchChances().get(doubleCatch)) {
                if (fishCaught.size() == 1) {
                    FishTier fishTier = fishCaught.get(0);
                    fishPoints.addAndGet(2 * fishTier.getPoints());
                    fishMoney.addAndGet(2 * fishTier.getMoney());

                    fishDesc.append("2x ").append(fishTier.getName());
                } else {
                    for (int i = 0; i < fishCaught.size(); i++) {
                        FishTier fishTier = fishCaught.get(i);
                        fishPoints.addAndGet(2 * fishTier.getPoints());
                        fishMoney.addAndGet(2 * fishTier.getMoney());

                        if (i == fishCaught.size() - 1) {
                            fishDesc.append("2x ").append(fishTier.getName());
                        } else if (i == fishCaught.size() - 2) {
                            fishDesc.append("2x ").append(fishTier.getName()).append(" and ");
                        } else {
                            fishDesc.append("2x ").append(fishTier.getName()).append(", ");
                        }
                    }
                }
            } else {
                if (fishCaught.size() == 1) {
                    FishTier fishTier = fishCaught.get(0);
                    fishPoints.addAndGet(fishTier.getPoints());
                    fishMoney.addAndGet(fishTier.getMoney());

                    fishDesc.append("1x ").append(fishTier.getName());
                } else {
                    for (int i = 0; i < fishCaught.size(); i++) {
                        FishTier fishTier = fishCaught.get(i);
                        fishPoints.addAndGet(fishTier.getPoints());
                        fishMoney.addAndGet(fishTier.getMoney());

                        if (i == fishCaught.size() - 1) {
                            fishDesc.append("1x ").append(fishTier.getName());
                        } else if (i == fishCaught.size() - 2) {
                            fishDesc.append("1x ").append(fishTier.getName()).append(" and ");
                        } else {
                            fishDesc.append("1x ").append(fishTier.getName()).append(", ");
                        }
                    }
                }
            }

            String notice = this.main.getMessageManager().getFishingRodsCatchNotice();
            notice = notice.replace("%amount%", "1x")
                    .replace("%fish%", fishDesc.toString())
                    .replace("%points%", fishPoints.get() + "")
                    .replace("%money%", fishMoney.get() + "");
            player.sendMessage(notice);

            main.getEconomy().depositPlayer(player, fishMoney.get());

            fishingManager.incrementPlayerStatistics(player, fishPoints.get());

            player.getWorld().playEffect(entry.getValue().getRight(), Effect.SPLASH, 0);
        }
    }

    private boolean hasMoved(Location stored, Location now) {
        return stored.getX() != now.getX() || stored.getY() != now.getY() || stored.getZ() != now.getZ();
    }

}
