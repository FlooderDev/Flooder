package me.kitskub.flooder.commands.user;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FClass;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.infohandler.GameClassHandler;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class ClassCommand extends PlayerCommand {

	public ClassCommand() {
		super(Flooder.fCH(), "class", "<class name>", "sets a class", Perms.USER_CLASS);
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
        if (args.length < 1) {
            ChatUtils.error(player, "Please specify a class!");
            return;
        }
        User user = User.get(player);
		FGame game = user.getGame(FGame.class);
		if (game == null) {
			ChatUtils.error(player, "You are currently not in a game.");
			return;
		}
        if (!game.getState().isPreGame()) {
            ChatUtils.error(player, "You cannot do that while playing!");
            return;
        }
        FClass selected = game.getOwningPlugin().getClassManager().get(args[0]);
        if (selected == null) {
            ChatUtils.error(player, "That class does not exist!");
            return;
        }
        user.getInfoHandler(GameClassHandler.CREATOR).setClass(selected);
	}
}
