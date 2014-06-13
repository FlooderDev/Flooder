package me.kitskub.flooder.commands.user;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FClass;
import me.kitskub.flooder.core.FGame;
import org.bukkit.entity.Player;

public class ClassCommand extends PlayerCommand {

	public ClassCommand() {
		super(Defaults.Perms.USER_CLASS, Flooder.fCH(), "class");
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
        if (args.length < 1) {
            ChatUtils.error(player, "Please specify a class!");
            return;
        }
        User user = User.get(player);
		game = user.getGameEntry().getGame(FGame.class);
		if (game == null) {
			ChatUtils.error(player, "You are currently not in a game.");
			return;
		}
        if (!game.getState().isPreGame()) {
            ChatUtils.error(player, "You cannot do that while playing!");
            return;
        }
        FClass selected = ((FGame) game).getOwningPlugin().getClassManager().get(args[0]);
        if (selected == null) {
            ChatUtils.error(player, "That class does not exist!");
            return;
        }
        user.setClass(selected);
	}

	@Override
	public String getInfo() {
		return "sets a class";
	}

	@Override
	public String getLocalUsage() {
		return "class <class name>";
	}
}
