package me.kitskub.flooder.commands.admin.add;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.entity.Player;

public class AddHelp extends PlayerCommand {

	public AddHelp() {
		super(Flooder.faCH(), "add", "", "add items", Perms.ADMIN_ADD_HELP);
	}

	@Override
	public void handlePlayer(Player cs, String label, String[] args) {
        ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
		for (Command c : getSubCommands()) {
			ChatUtils.helpCommand(cs, c);
		}
	}
}
