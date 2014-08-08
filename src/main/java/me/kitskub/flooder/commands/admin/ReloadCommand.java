package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.command.CommandSender;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super(Flooder.faCH(), "reload", "", "reload the plugin", Perms.ADMIN_RELOAD);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		Flooder.reload();
		ChatUtils.send(cs, "[Flooder] Reloaded %s", Flooder.getInstance().getDescription().getVersion());
	}
}
