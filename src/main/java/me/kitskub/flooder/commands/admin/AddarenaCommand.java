package me.kitskub.flooder.commands.admin;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.framework.Arena;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class AddarenaCommand extends Command {

	public AddarenaCommand() {
		super(Perms.ADMIN_ADDARENA, Flooder.faCH(), "addarena");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		if (args.length < 2) {
			ChatUtils.send(cs, getUsage(), Flooder.CMD_ADMIN);
			return;
		}
		game = Flooder.gameMaster().getGame(args[0]);
		if (game == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
			return;
		}
        Arena arena = Flooder.gameMaster().getArena(args[1]);
		if (arena == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", args[1]));
			return;
		}        
        game.addArena(arena);
        ChatUtils.send(cs, "Arena added to game");
	}

	@Override
	public String getInfo() {
		return "add arena to game";
	}

	@Override
	public String getLocalUsage() {
		return "addarena [game] [arena]";
	}
    
}
