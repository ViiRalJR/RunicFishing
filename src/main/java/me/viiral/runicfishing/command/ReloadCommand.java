package me.viiral.runicfishing.command;

import me.viiral.runicfishing.RunicFishing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final RunicFishing main;

    public ReloadCommand(RunicFishing main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.main.reloadConfig();
        sender.sendMessage(this.main.getMessageManager().getReloadNotice());
        return true;
    }

}
