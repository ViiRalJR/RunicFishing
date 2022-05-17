package me.viiral.runicfishing.manager;

import lombok.Getter;
import lombok.SneakyThrows;
import me.viiral.runicfishing.FishTier;
import me.viiral.runicfishing.Pair;
import me.viiral.runicfishing.PlayerFishStats;
import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.enchants.EnchantCost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

@Getter
public class FishingManager {
    // -------------------------------------------- //
    // LOCAL FIELDS
    // -------------------------------------------- //

    private final RunicFishing main;

    private final File saveFile;
    private final YamlConfiguration saveFileConfig;

    private final Map<UUID, PlayerFishStats> playerStatistics = new HashMap<>();
    private final List<String> playerLeaderBoard = new ArrayList<>();
    private final List<String> playerLeaderBoardUUID = new LinkedList<>();

    // <Luck level, <Fishing Rod Tier, <Chance, <Chance, Fish Tier>>>> (yikes I know)
    private final Map<Integer, Map<Integer, Map<Integer, Map<Integer, FishTier>>>> fishCatchProbabilities = new TreeMap<>();
    private final Map<Integer, EnchantCost> luckCosts = new HashMap<>();

    private final Map<Integer, Integer> doubleCatchChances = new TreeMap<>();
    private final Map<Integer, EnchantCost> doubleCatchCosts = new HashMap<>();

    private final Map<Integer, Integer> crateFinderChances = new TreeMap<>();
    private final Map<Integer, EnchantCost> crateFinderCosts = new HashMap<>();
    private final String crateFinderCommand;

    private final Map<Integer, Integer> fishNetChances = new TreeMap<>();
    private final Map<Integer, EnchantCost> fishNetCosts = new HashMap<>();
    private final int fishNetAmount;

    private final Map<UUID, Pair<Location, Location>> playersAutoFishing = new HashMap<>();

    // -------------------------------------------- //
    // CONSTRUCT
    // -------------------------------------------- //

    public FishingManager(RunicFishing main) {
        this.main = main;
        this.saveFile = new File(main.getDataFolder(), "fishingData.yml");
        this.saveFileConfig = YamlConfiguration.loadConfiguration(saveFile);

        if (this.saveFileConfig.isSet("players")) {
            this.saveFileConfig.getConfigurationSection("players").getKeys(false).forEach(uuid ->
                    playerStatistics.put(UUID.fromString(uuid), new PlayerFishStats(this.saveFileConfig.getInt("players." + uuid + ".amount", 0),
                            this.saveFileConfig.getDouble("players." + uuid + ".points", 0.0D)))
            );
        }

        // Luck Probabilities
        main.getConfig().getConfigurationSection("fishing-rods.enchants.luck.levels").getKeys(false).forEach(key ->
                {
                    int level = Integer.parseInt(key);
                    String path = "fishing-rods.enchants.luck.levels." + level;

                    this.initializeCatchProbability(level, path);
                    this.luckCosts.put(level, new EnchantCost(main.getConfig().getInt(path + ".points-cost", 1),
                            main.getConfig().getInt(path + ".money-cost", 1)));
                }
        );

        // Double Catch
        main.getConfig().getConfigurationSection("fishing-rods.enchants.double-catch.levels").getKeys(false).forEach(key ->
                {
                    int level = Integer.parseInt(key);
                    String path = "fishing-rods.enchants.double-catch.levels." + level;

                    this.doubleCatchChances.put(level, main.getConfig().getInt(path + ".chance", 1));
                    this.doubleCatchCosts.put(level, new EnchantCost(main.getConfig().getInt(path + ".points-cost", 1),
                            main.getConfig().getInt(path + ".money-cost", 1)));
                }
        );

        // Crate Finder
        main.getConfig().getConfigurationSection("fishing-rods.enchants.crate-finder.levels").getKeys(false).forEach(key ->
                {
                    int level = Integer.parseInt(key);
                    String path = "fishing-rods.enchants.crate-finder.levels." + level;

                    this.crateFinderChances.put(level, main.getConfig().getInt(path + ".chance", 1));
                    this.crateFinderCosts.put(level, new EnchantCost(main.getConfig().getInt(path + ".points-cost", 1),
                            main.getConfig().getInt(path + ".money-cost", 1)));
                }
        );
        this.crateFinderCommand = main.getConfig().getString("fishing-rods.enchants.crate-finder.command", "");

        // Fish Net
        main.getConfig().getConfigurationSection("fishing-rods.enchants.fish-net.levels").getKeys(false).forEach(key ->
                {
                    int level = Integer.parseInt(key);
                    String path = "fishing-rods.enchants.fish-net.levels." + level;

                    this.fishNetChances.put(level, main.getConfig().getInt(path + ".chance", 1));
                    this.fishNetCosts.put(level, new EnchantCost(main.getConfig().getInt(path + ".points-cost", 1),
                            main.getConfig().getInt(path + ".money-cost", 1)));
                }
        );
        this.fishNetAmount = main.getConfig().getInt("fishing-rods.enchants.fish-net.fish-amount", 4);

        // Updating Top
        this.updateTop();
    }

    // -------------------------------------------- //
    // PROBABILITY
    // -------------------------------------------- //

    private void initializeCatchProbability(int luckLevel, String path) {
        Map<Integer, Map<Integer, Map<Integer, FishTier>>> fishingRodTiers = new TreeMap<>();

        for (String fishingRodTier : main.getConfig().getConfigurationSection(path + ".fishing-rod-tiers").getKeys(false)) {
            Map<Integer, Map<Integer, FishTier>> fishChances = new TreeMap<>();
            int currentFishChance = 0;

            String fishTypePath = path + ".fishing-rod-tiers." + fishingRodTier;
            for (String fishType : main.getConfig().getConfigurationSection(fishTypePath).getKeys(false)) {
                Map<Integer, FishTier> fishTierChances = new TreeMap<>();
                int currentFishTierChance = 0;

                String fishTierPath = fishTypePath + "." + fishType + ".fish-tiers";
                for (String fishTier : main.getConfig().getConfigurationSection(fishTierPath).getKeys(false)) {
                    int fishTierChance = main.getConfig().getInt(fishTierPath + "." + fishTier, 1);
                    String fishTierName = main.getConfig().getString("fishing-rods.fish-tiers." + fishType + ".tiers." + fishTier + ".name", " ");
                    int fishTierPoints = main.getConfig().getInt("fishing-rods.fish-tiers." + fishType + ".tiers." + fishTier + ".points", 1);
                    int fishTierMoney = main.getConfig().getInt("fishing-rods.fish-tiers." + fishType + ".tiers." + fishTier + ".money", 1);

                    currentFishTierChance += fishTierChance;
                    fishTierChances.put(currentFishTierChance, new FishTier(ChatColor.translateAlternateColorCodes('&', fishTierName), fishTierPoints, fishTierMoney));
                }

                int fishChance = main.getConfig().getInt(fishTypePath + "." + fishType + ".chance", 1);

                currentFishChance += fishChance;
                fishChances.put(currentFishChance, fishTierChances);
            }

            int parsedFishingRodTier = Integer.parseInt(fishingRodTier);
            fishingRodTiers.put(parsedFishingRodTier, fishChances);
        }

        this.fishCatchProbabilities.put(luckLevel, fishingRodTiers);
    }

    public FishTier getCatch(int luckLevel, int fishingRodTier, int randomPick) {
        for (Map.Entry<Integer, Map<Integer, FishTier>> fishChanceEntry : this.fishCatchProbabilities.get(luckLevel).get(fishingRodTier).entrySet()) {
            if (randomPick > fishChanceEntry.getKey()) continue;

            randomPick = ThreadLocalRandom.current().nextInt(0, 100);
            for (Map.Entry<Integer, FishTier> fishTierChanceEntry : fishChanceEntry.getValue().entrySet()) {
                if (randomPick < fishTierChanceEntry.getKey()) return fishTierChanceEntry.getValue();
            }
        }

        return new FishTier("Fish", 1, 1);
    }

    // -------------------------------------------- //
    // FUNCTION
    // -------------------------------------------- //

    public int getPlayerFishAmount(UUID uuid) {
        if (!this.playerStatistics.containsKey(uuid)) this.playerStatistics.put(uuid, new PlayerFishStats(0, 0));
        return this.playerStatistics.get(uuid).getAmount().get();
    }

    public int getPlayerFishPoints(UUID uuid) {
        if (!this.playerStatistics.containsKey(uuid)) this.playerStatistics.put(uuid, new PlayerFishStats(0, 0));
        return this.playerStatistics.get(uuid).getPoints().intValue();
    }

    public void incrementPlayerStatistics(Player player, int fishPoints) {
        UUID uuid = player.getUniqueId();
        if (!this.playerStatistics.containsKey(uuid)) {
            this.playerStatistics.put(uuid, new PlayerFishStats(1, fishPoints));
        } else {
            this.playerStatistics.get(uuid).updateValues(1, fishPoints);
        }
    }

    public void chargePlayerFishPoints(Player player, int fishPoints) {
        UUID uuid = player.getUniqueId();
        if (!this.playerStatistics.containsKey(uuid)) {
            this.playerStatistics.put(uuid, new PlayerFishStats(1, fishPoints));
        } else {
            this.playerStatistics.get(uuid).updateValues(0, -fishPoints);
        }
    }

    public void setPlayerFishPoints(Player player, int points) {
        UUID uuid = player.getUniqueId();
        playerStatistics.put(uuid, new PlayerFishStats(1, points));
    }

    // -------------------------------------------- //
    // TOP SORT
    // -------------------------------------------- //

    public void updateTop() {
        main.getLogger().log(Level.INFO, "Updating fishing top...");

        this.playerLeaderBoard.clear();
        playerLeaderBoardUUID.clear();
        this.playerLeaderBoard.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("fishing-rods.top.chat-title", " ")));

        String positionLine = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("fishing-rods.top.chat-entry", " "));

        Map<UUID, Integer> unsortedPlayerStatistics = new HashMap<>();
        this.playerStatistics.forEach((key, value) -> unsortedPlayerStatistics.put(key, value.getAmount().get()));

        LinkedHashMap<UUID, Integer> sortedPlayerStatistics = new LinkedHashMap<>();
        unsortedPlayerStatistics.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(o -> sortedPlayerStatistics.put(o.getKey(), o.getValue()));

        int counter = 1;
        for (Map.Entry<UUID, Integer> entry : sortedPlayerStatistics.entrySet()) {
            if (counter == 10) break;

            playerLeaderBoardUUID.add(entry.getKey().toString());
            this.playerLeaderBoard.add(positionLine
                    .replace("%position%", counter + "")
                    .replace("%player%", Bukkit.getOfflinePlayer(entry.getKey()).getName())
                    .replace("%amount%", entry.getValue() + "")
            );

            counter++;
        }
    }

    // -------------------------------------------- //
    // DATABASE SAVE
    // -------------------------------------------- //

    @SneakyThrows
    public void save() {
        main.getLogger().log(Level.INFO, "Saving fishing database...");

        for (Map.Entry<UUID, PlayerFishStats> entry : this.playerStatistics.entrySet()) {
            PlayerFishStats playerFishStats = entry.getValue();
            saveFileConfig.set("players." + entry.getKey().toString() + ".amount", playerFishStats.getAmount().get());
            saveFileConfig.set("players." + entry.getKey().toString() + ".points", playerFishStats.getPoints().get());
            saveFileConfig.save(saveFile);
        }
    }

    // -------------------------------------------- //
    // AUTO FISHING
    // -------------------------------------------- //

    public boolean addPlayerAutoFishing(UUID uuid, Pair<Location, Location> pair) {
        return this.playersAutoFishing.put(uuid, pair) != null;
    }

    public boolean removePlayerAutoFishing(UUID uuid) {
        return this.playersAutoFishing.remove(uuid) != null;
    }

}
