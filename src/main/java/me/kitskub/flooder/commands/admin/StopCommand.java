package me.kitskub.flooder.commands.admin;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class StopCommand extends Command {

	public StopCommand() {
		super(Perms.ADMIN_STOP, Flooder.faCH(), "stop");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {		
		String name = args.length < 1 ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(cs, this);
			return;
		}
		
		game = Flooder.gameMaster().getGame(name);
		if (game == null) {
		    ChatUtils.error(cs, "%s does not exist.", name);
		    return;
		}
		game.cancelGame(cs);
	}

	@Override
	public String getInfo() {
		return "manually stop a game";
	}

	@Override
	public String getLocalUsage() {
		return "stop [game name]";
	}
    
}
