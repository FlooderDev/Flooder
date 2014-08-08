package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopCommand extends Command {

	public StopCommand() {
		super(Flooder.faCH(), "stop", "[game name (or game in)]", "manually stop a game", Perms.ADMIN_STOP);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
        FGame game = null;
        if (args.length >= 1) {
            // Typed a name
            String name = Defaults.Config.DEFAULT_GAME.getGlobalString();
            game = Flooder.gameMaster().getGame(name);
            if (game == null) {
                ChatUtils.error(cs, Defaults.Lang.NOT_EXIST.getMessage().replace("<item>", name));
                return;
            }
        }
        if (cs instanceof Player) {
            game = User.get((Player) cs).getGame(FGame.class);
        }
        if (game == null) {
            ChatUtils.helpCommand(cs, this);
            return;
        }
		String error = game.cancelGame();
        if (error != null) {
            ChatUtils.error(cs, error);
        }
	}
}
