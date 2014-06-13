package me.kitskub.flooder.commands.admin.remove;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.entity.Player;

public class RemoveHelp extends PlayerCommand {

	public RemoveHelp() {
		super(Perms.ADMIN_REMOVE_HELP, Flooder.faCH(), "remove");
	}

	@Override
	public void handlePlayer(Player cs, String label, String[] args) {
		for (Command c : getSubCommands()) {
			ChatUtils.helpCommand(cs, c);
		}
	}

	@Override
	public String getInfo() {
		return "remove items";
	}

	@Override
	public String getLocalUsage() {
		return "remove";
	}
	
}
