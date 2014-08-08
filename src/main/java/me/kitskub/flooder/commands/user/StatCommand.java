package me.kitskub.flooder.commands.user;

import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StatCommand extends Command {

	public StatCommand() {
		super(Flooder.fCH(), "stat", "[game name]", "list stats for a game", Perms.USER_STAT);
	}

	@Override
	public void handle(CommandSender cs, String cmd, String[] args) {		
		String name = args.length < 1 ? Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(cs, this);
			return;
		}

		FGame game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}
		ChatUtils.send(cs, ChatColor.GREEN, ChatUtils.getHeadLiner(Flooder.getInstance()));
		game.listStats(cs);
	}
}
