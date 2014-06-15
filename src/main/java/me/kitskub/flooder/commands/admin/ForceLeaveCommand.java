package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command; 
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceLeaveCommand extends Command {

	public ForceLeaveCommand() {
		super(Perms.ADMIN_KILL, Flooder.faCH(), "forceleave");
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
		FGame game = user.getGameEntry().getGame(FGame.class);
		if (game == null) {
		    ChatUtils.error(cs, "%s is currently not in a game.", user.getPlayer().getName());
		    return;
		}
		ChatUtils.broadcast(game, "%s has been forced to leave by an admin.", user.getPlayer().getName());
		game.leave(user);
	}

	@Override
	public String getInfo() {
		return "forces a player to leave a game";
	}

	@Override
	public String getLocalUsage() {
		return "forceleave <player>";
	}
	
}
