package me.viiral.runicfishing.listener;


import me.viiral.runicfishing.Pair;
import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class FishingListener implements Listener {

    private final RunicFishing main;

    public FishingListener(RunicFishing main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        // Getting player ...
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getType() != Material.FISHING_ROD) return;

        ItemManager itemManager = main.getItemManager();
        if (!itemManager.hasNbtCompound(itemInHand, "tier")) return;

        String world = player.getWorld().getName();
        if (!main.getConfig().getList("fishing-rods.worlds-permitted", new ArrayList<>()).contains(world)) {
            player.sendMessage(this.main.getMessageManager().getFishingRodsInvalidWorld());
            event.setCancelled(true);
            return;
        }

        if (this.main.getFishingManager().removePlayerAutoFishing(uuid)) {
            player.sendMessage(this.main.getMessageManager().getFishingRodsAutoStopped());
        }

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this.main, () ->
            {
                if (event.getHook().getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                    this.main.getFishingManager().addPlayerAutoFishing(uuid, new Pair<>(player.getLocation(), event.getHook().getLocation()));
                    player.sendMessage(this.main.getMessageManager().getFishingRodsAutoStarted());
                }
            }, 20L);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerFishEvent(PlayerJoinEvent event) {
        this.main.getFishingManager().removePlayerAutoFishing(event.getPlayer().getUniqueId());
    }

}

