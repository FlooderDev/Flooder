package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.command.CommandSender;

public class StartCommand extends Command {

	public StartCommand() {
		super(Perms.ADMIN_START, Flooder.faCH(), "start");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		String name = args.length < 1 ? Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(cs, this);
			return;
		}
		FGame game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}

		int seconds;

		if (args.length == 2) {//TODO better
			try {
				seconds = Integer.parseInt(args[1]);
			} catch (Exception ex) {
				ChatUtils.error(cs, "'%s' is not an integer.", args[1]);
				return;
			}
		}

		else {
			seconds = Config.COUNTDOWN.getGlobalInt();
		}
		if (!game.startGame(cs, seconds)) {
			ChatUtils.error(cs, "Failed to start %s.", game.getName());
		}
	}

	@Override
	public String getInfo() {
		return "manually start a game";
	}

	@Override
	public String getLocalUsage() {
		return "start [game] [seconds]";
	}
    
}
