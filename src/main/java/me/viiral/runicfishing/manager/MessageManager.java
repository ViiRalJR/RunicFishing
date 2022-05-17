package me.viiral.runicfishing.manager;

import lombok.Getter;
import me.viiral.runicfishing.RunicFishing;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class MessageManager {

    private final RunicFishing main;
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###,###", DecimalFormatSymbols.getInstance(Locale.US));

    private final List<String> fishingRodsHelp;
    private final String fishingRodsInvalidWorld;
    private final String fishingRodsCatchNotice;
    private final String fishingRodsNotHoldingUpgradable;
    private final String fishingRodsInvalidTier;
    private final String fishingRodsInvalidPlayer;
    private final String fishingRodsInsufficientMoney;
    private final String fishingRodsInsufficientPoints;
    private final String fishingRodsFishPointsNotice;
    private final String fishingRodsFishAmountNotice;
    private final String fishingRodsAutoStarted;
    private final String fishingRodsAutoStopped;

    private final String reloadNotice;

    public MessageManager(RunicFishing main){
        this.main = main;
        // Fishing Rods
        this.fishingRodsHelp = this.getMessageListFromPath("fishing-rods.messages.help");
        this.fishingRodsInvalidWorld = this.getMessageFromPath("fishing-rods.messages.invalid-world");
        this.fishingRodsCatchNotice = this.getMessageFromPath("fishing-rods.messages.catch-notice");
        this.fishingRodsNotHoldingUpgradable = this.getMessageFromPath("fishing-rods.messages.not-holding-upgradable");
        this.fishingRodsInvalidTier = this.getMessageFromPath("fishing-rods.messages.invalid-tier");
        this.fishingRodsInvalidPlayer = this.getMessageFromPath("fishing-rods.messages.invalid-player");
        this.fishingRodsInsufficientMoney = this.getMessageFromPath("fishing-rods.messages.insufficient-money");
        this.fishingRodsInsufficientPoints = this.getMessageFromPath("fishing-rods.messages.insufficient-points");
        this.fishingRodsFishPointsNotice = this.getMessageFromPath("fishing-rods.messages.fish-points-notice");
        this.fishingRodsFishAmountNotice = this.getMessageFromPath("fishing-rods.messages.fish-amount-notice");
        this.fishingRodsAutoStarted = this.getMessageFromPath("fishing-rods.auto-fishing.messages.started");
        this.fishingRodsAutoStopped = this.getMessageFromPath("fishing-rods.auto-fishing.messages.stopped");

        // Reload
        this.reloadNotice = this.getMessageFromPath("reload");
    }

    private String getMessageFromPath(String path) {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path, " "));
    }

    private List<String> getMessageListFromPath(String path) {
        List<String> message = new ArrayList<>();
        ((List<String>) this.main.getConfig().get(path)).forEach(line -> message.add(ChatColor.translateAlternateColorCodes('&', line)));
        return message;
    }

    public String getFormattedNumber(int amount) {
        return this.decimalFormat.format(amount);
    }

}
