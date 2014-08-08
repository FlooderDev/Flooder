package me.kitskub.flooder.commands.user;

import java.util.Set;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ListCommand extends Command {

	public ListCommand() {
		super(Flooder.fCH(), "list", "", "list games", Perms.USER_LIST);
	}

	@Override
	public void handle(CommandSender cs, String cmd, String[] args) {
		ChatUtils.send(cs, ChatColor.GREEN, ChatUtils.getHeadLiner(Flooder.getInstance()));
		Set<FGame> games = Flooder.gameMaster().getGames();
		if (games.isEmpty()) {
			ChatUtils.error(cs, "No games have been created yet.");
			return;
		}

		for (FGame g : games) {
			ChatUtils.send(cs, ChatColor.GOLD, "- " + g.getInfo());
		}
	} 
}