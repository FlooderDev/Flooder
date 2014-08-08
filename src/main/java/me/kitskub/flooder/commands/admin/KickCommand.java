package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils; 
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand extends Command {

	public KickCommand() {
		super(Flooder.faCH(), "kick", "<player>", "kick a player from a game", Perms.ADMIN_KICK);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		if (args.length < 1) {
			ChatUtils.helpCommand(cs, this);
			return;
		}
        Player player = Bukkit.getPlayer(args[0]);
		if (player == null) {
            ChatUtils.error(cs, "That user does not exist!");
		    return;
		}
        User user = User.get(player);
		Game<?, ?, ?> game = user.getGame();
		if (game == null) {
		    ChatUtils.error(cs, "%s is currently not in a game.", user.getPlayer().getName());
		    return;
		}
        if (user.getPlayingType() == User.PlayingType.PLAYING) {
            ChatUtils.broadcast(game, "%s has been kicked from the game %s.", cs.getName(), game.getName());
        }
		game.leave(user);
	}
}
