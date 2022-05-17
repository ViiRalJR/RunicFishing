package me.viiral.runicfishing.task;

import me.viiral.runicfishing.RunicFishing;
import org.bukkit.scheduler.BukkitRunnable;

public class FishSaveTask extends BukkitRunnable {

    private final RunicFishing main;

    public FishSaveTask(RunicFishing main) {
        this.main = main;
    }

    @Override
    public void run() {
        this.main.getFishingManager().save();
    }

}
