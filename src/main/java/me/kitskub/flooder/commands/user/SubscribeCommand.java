package me.kitskub.flooder.commands.user;

import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class SubscribeCommand extends PlayerCommand {

	public SubscribeCommand() {
		super(Flooder.fCH(), "subscribe", "[game]", "subscribe to game messages", Perms.USER_SUBSCRIBE);
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
        if (args.length < 1) {
            ChatUtils.helpCommand(player, this);
            return;
        }
        FGame game = Flooder.gameMaster().getGame(args[0]);
        if (game == null) {
            ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
            return;
        }
        User user = User.get(player);
		if (user.isSubscribed(game)) {
			user.unsubscribe(game);
			ChatUtils.send(player, "You have been unsubscribed from those Flooder messages.");
		}
		else {
			user.unsubscribe(game);
			ChatUtils.send(player, "You have been subscribed to those Flooder messages.");
		}
	}
}
