package me.viiral.runicfishing.command;

import me.viiral.runicfishing.RunicFishing;
import me.viiral.runicfishing.gui.RodsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RodsCommand implements CommandExecutor {

    private final RunicFishing main;

    public RodsCommand(RunicFishing main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        new RodsGUI(main).open(player);

        return true;
    }

}
