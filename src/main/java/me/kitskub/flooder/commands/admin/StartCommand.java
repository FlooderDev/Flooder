package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand extends Command {

	public StartCommand() {
		super(Flooder.faCH(), "start", "[game (or game in)] [seconds]", "manually start a game", Perms.ADMIN_START);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
        FGame game = null;
        if (cs instanceof Player && args.length < 2) {
            game = User.get((Player) cs).getGame(FGame.class);
        }
        String name;
        int timeArg = 0;
        try {
            Integer.parseInt(args[0]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Not a number and there is a value
            name = Config.DEFAULT_GAME.getGlobalString();
            game = Flooder.gameMaster().getGame(name);
            timeArg = 1;
            if (game == null) {
                ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", name));
                return;
            }
        }
        if (game == null) {
            ChatUtils.helpCommand(cs, this);
            return;
        }

		int seconds;

		if (args.length >= timeArg + 1) {
			try {
				seconds = Integer.parseInt(args[timeArg]);
			} catch (NumberFormatException ex) {
				ChatUtils.error(cs, "'%s' is not an integer.", args[timeArg]);
				return;
			}
		} else {
			seconds = Config.COUNTDOWN.getGlobalInt();
		}
		if (!game.startGame(cs, seconds)) {
			ChatUtils.error(cs, "Failed to start %s.", game.getName());
		}
	}
}
