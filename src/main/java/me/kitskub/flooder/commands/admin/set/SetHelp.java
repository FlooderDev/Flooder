package me.kitskub.flooder.commands.admin.set;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.command.CommandSender;

public class SetHelp extends Command {

	public SetHelp() {
		super(Flooder.faCH(), "set", "", "set items", Perms.ADMIN_SET_HELP);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
        ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
		for (Command c : getSubCommands()) {
            ChatUtils.help(cs, c.getUsageAndInfo());
		}
	}
}
