package me.kitskub.flooder.commands.user;

import java.util.Arrays;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class JoinCommand extends PlayerCommand {

	public JoinCommand() {
 		super(Flooder.fCH(), "join", "[game name]", "join a game", Perms.USER_JOIN);
        setAliases(Arrays.asList("j"));
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
		String name = args.length < 1 ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(player, this);
			return;
		}

		FGame game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}

		game.join(User.get(player));
	}
}
