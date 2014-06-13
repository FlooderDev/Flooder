package me.kitskub.flooder.commands.admin.add;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.entity.Player;

public class AddHelp extends PlayerCommand {

	public AddHelp() {
		super(Perms.ADMIN_ADD_HELP, Flooder.faCH(), "add");
	}

	@Override
	public void handlePlayer(Player cs, String label, String[] args) {
        ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
		for (Command c : getSubCommands()) {
			ChatUtils.helpCommand(cs, c);
		}
	}

	@Override
	public String getInfo() {
		return "add items";
	}

	@Override
	public String getLocalUsage() {
		return "add";
	}
	
}
