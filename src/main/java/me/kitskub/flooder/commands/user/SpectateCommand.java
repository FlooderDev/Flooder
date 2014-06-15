package me.kitskub.flooder.commands.user;

import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder; 
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class SpectateCommand extends PlayerCommand {

	public SpectateCommand() {
		super(Perms.USER_SPECTATE, Flooder.fCH(), "spectate");
	}

	@Override
	public void handlePlayer(Player player, String label, String[] args) {
        User user = User.get(player);
        if (user.getGameEntry().getType() == User.GameEntry.Type.SPECTATING) {
            user.getGameEntry().getGame().leave(user);
            return;
        }
		String name = args.length < 1 ? Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(player, this);
			return;
		}
		FGame game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}
		game.spectate(user);
	}

	@Override
	public String getInfo() {
		return "sets player to flying to spectate a game or cancels a spectation";
	}

	@Override
	public String getLocalUsage() {
		return "spectate [game name]";
	}
    
}
