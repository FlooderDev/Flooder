package me.kitskub.flooder.commands.admin.set;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class SetHelp extends Command {

	public SetHelp() {
		super(Perms.ADMIN_SET_HELP, Flooder.faCH(), "set");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
        ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
		for (Command c : getSubCommands()) {
			ChatUtils.help(cs, c.getUsageAndInfo());
		}
	}

	@Override
	public String getInfo() {
		return "set items";
	}

	@Override
	public String getLocalUsage() {
		return "set";
	}
	
}
