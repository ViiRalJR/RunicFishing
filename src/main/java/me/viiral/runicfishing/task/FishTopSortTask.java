package me.viiral.runicfishing.task;

import me.viiral.runicfishing.RunicFishing;
import org.bukkit.scheduler.BukkitRunnable;

public class FishTopSortTask extends BukkitRunnable {

    private final RunicFishing main;

    public FishTopSortTask(RunicFishing main) {
        this.main = main;
    }

    @Override
    public void run() {
        this.main.getFishingManager().updateTop();
    }

}
