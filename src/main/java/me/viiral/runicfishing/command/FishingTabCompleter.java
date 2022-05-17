package me.viiral.runicfishing.command;

import me.viiral.runicfishing.RunicFishing;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class FishingTabCompleter implements TabCompleter {

    private final List<String> subCommands = new ArrayList<>();
    private final List<String> tiers = new ArrayList<>();

    public FishingTabCompleter(RunicFishing core){
        subCommands.add("fish");
        subCommands.add("help");
        subCommands.add("points");
        subCommands.add("rod");
        subCommands.add("shop");
        subCommands.add("top");
        tiers.addAll(core.getConfig().getConfigurationSection("fishing-rods.fish-tiers").getKeys(false));
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length){
            case 1:
                return subCommands;
            case 2:
                switch (args[1]){
                    case "rod":
                        return tiers;
                    case "points":
                    case "fish":
                        List<String> names = new ArrayList<>();
                        Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
                        return names;
                }
            case 3: {
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> names.add(player.getName()));
                return names;
            }
            default:
                return new ArrayList<>();
        }
    }

}
