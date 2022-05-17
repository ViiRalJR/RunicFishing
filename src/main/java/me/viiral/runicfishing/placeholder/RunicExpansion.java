package me.viiral.runicfishing.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.viiral.runicfishing.RunicFishing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RunicExpansion extends PlaceholderExpansion {

    private final RunicFishing main;

    public RunicExpansion(RunicFishing main) {
        this.main = main;
    }


    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return main.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "runicfishing";
    }

    @Override
    public @NotNull String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        String[] parts = identifier.split("_");

        if (parts.length == 1) {
            String partOne = parts[0];

            switch (partOne) {
                case "fishingpoints":
                    return main.getFishingManager().getPlayerFishPoints(player.getUniqueId()) + "";
                case "fishamount":
                    return main.getFishingManager().getPlayerFishAmount(player.getUniqueId()) + "";
            }
        }

        switch (identifier) {
            case "fishing_top_1_name": {
                return Bukkit.getOfflinePlayer(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(0))).getName();
            }
            case "fishing_top_2_name": {
                return Bukkit.getOfflinePlayer(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(1))).getName();
            }
            case "fishing_top_3_name": {
                return Bukkit.getOfflinePlayer(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(2))).getName();
            }
            case "fishing_top_1_points": {
                return main.getFishingManager().getPlayerFishPoints(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(0))) + "";
            }
            case "fishing_top_2_points": {
                return main.getFishingManager().getPlayerFishPoints(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(1))) + "";
            }
            case "fishing_top_3_points": {
                return main.getFishingManager().getPlayerFishPoints(UUID.fromString(main.getFishingManager().getPlayerLeaderBoardUUID().get(2))) + "";
            }
        }

        return "&4Error";
    }

}
