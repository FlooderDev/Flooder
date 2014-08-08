package me.kitskub.flooder.commands.user;

import java.util.Arrays;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class QuitCommand extends PlayerCommand {

	public QuitCommand() {
		super(Flooder.fCH(), "quit", "", "quit the current game indefinitely", Perms.USER_QUIT);
        setAliases(Arrays.asList("leave"));
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
        User user = User.get(player);
		FGame game = user.getGame(FGame.class);
		if (game == null) {
			ChatUtils.error(player, Lang.NOT_IN_GAME.getMessage());
			return;
		}

		game.leave(user);
	}
}
