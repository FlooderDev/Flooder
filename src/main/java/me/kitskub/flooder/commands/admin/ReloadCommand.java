package me.kitskub.flooder.commands.admin;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super(Perms.ADMIN_RELOAD, Flooder.faCH(), "reload");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		Flooder.reload();
		ChatUtils.send(cs, "Reloaded %s", Flooder.getInstance().getDescription().getVersion());
	}

	@Override
	public String getInfo() {
		return "reload Paintball";
	}

	@Override
	public String getLocalUsage() {
		return "reload";
	}
    
}
