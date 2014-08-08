package me.kitskub.flooder.commands.admin;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class SaveCommand extends Command {

	public SaveCommand() {
		super(Flooder.faCH(), "save", "", "save Flooder", Perms.ADMIN_SAVE);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		Flooder.saveAll();
		ChatUtils.send(cs, "Saved all data.");
	}
}
