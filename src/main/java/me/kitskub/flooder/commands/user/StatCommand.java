package me.kitskub.flooder.commands.user;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Flooder;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StatCommand extends Command {

	public StatCommand() {
		super(Perms.USER_STAT, Flooder.fCH(), "stat");
	}

	@Override
	public void handle(CommandSender cs, String cmd, String[] args) {		
		String name = args.length < 1 ? Config.DEFAULT_GAME.getGlobalString() : args[0];
		if (name == null) {
			ChatUtils.helpCommand(cs, this);
			return;
		}

		game = Flooder.gameMaster().getGame(name);
		if (game == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}
		ChatUtils.send(cs, ChatColor.GREEN, ChatUtils.getHeadLiner(Flooder.getInstance()));
		game.listStats(cs);
	}

	@Override
	public String getInfo() {
		return "list stats for a game";
	}

	@Override
	public String getLocalUsage() {
		return "stat [game name]";
	}
    
}
