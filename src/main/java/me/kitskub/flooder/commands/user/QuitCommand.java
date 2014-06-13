package me.kitskub.flooder.commands.user;

import java.util.Arrays;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import org.bukkit.entity.Player;

public class QuitCommand extends PlayerCommand {

	public QuitCommand() {
		super(Perms.USER_QUIT, Flooder.fCH(), "quit");
        setAliases(Arrays.asList("leave"));
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
        User user = User.get(player);
		game = user.getGameEntry().getGame();
		if (game == null) {
			ChatUtils.error(player, "You are currently not in a game.");
			return;
		}

		user.leaveGame();
	}

	@Override
	public String getInfo() {
		return "quit the current game indefinitely";
	}

	@Override
	public String getLocalUsage() {
		return "quit";
	}
    
}
