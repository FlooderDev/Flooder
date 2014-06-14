package me.kitskub.flooder.commands.user;

import java.util.Arrays;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Flooder;

import org.bukkit.entity.Player;

public class JoinCommand extends PlayerCommand {

	public JoinCommand() {
		super(Perms.USER_JOIN, Flooder.fCH(), "join");
        setAliases(Arrays.asList("j"));
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
		String name = args.length < 1 ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(player, this);
			return;
		}

		game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}

		game.join(User.get(player));
	}

	@Override
	public String getInfo() {
		return "join a game";
	}

	@Override
	public String getLocalUsage() {
		return "join [game name]";
	}
}
