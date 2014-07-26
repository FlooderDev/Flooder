package me.kitskub.flooder.commands.admin;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class SaveCommand extends Command {

	public SaveCommand() {
		super(Perms.ADMIN_SAVE, Flooder.faCH(), "save");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		Flooder.saveAll();
		ChatUtils.send(cs, "Saved all data.");
	}

	@Override
	public String getInfo() {
		return "save Flooder";
	}

	@Override
	public String getLocalUsage() {
		return "save";
	}
    
}
