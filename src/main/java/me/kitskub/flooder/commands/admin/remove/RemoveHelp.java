package me.kitskub.flooder.commands.admin.remove;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.entity.Player;

public class RemoveHelp extends PlayerCommand {

	public RemoveHelp() {
		super(Flooder.faCH(), "remove", "", "remove items", Perms.ADMIN_REMOVE_HELP);
	}

	@Override
	public void handlePlayer(Player cs, String label, String[] args) {
		for (Command c : getSubCommands()) {
            ChatUtils.help(cs, c.getUsageAndInfo());
		}
	}
}
