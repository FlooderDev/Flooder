package me.kitskub.flooder.commands.admin;

import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.command.CommandSender;

public class AddarenaCommand extends Command {

	public AddarenaCommand() {
		super(Flooder.faCH(), "addarena", "[game] [arena]", "add arena to game", Perms.ADMIN_ADDARENA);
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		if (args.length < 2) {
			ChatUtils.send(cs, getUsage(), Flooder.CMD_ADMIN);
			return;
		}
		FGame game = Flooder.gameMaster().getGame(args[0]);
		if (game == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
			return;
		}
        FArena arena = Flooder.gameMaster().getArena(args[1]);
		if (arena == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", args[1]));
			return;
		}        
        game.addArena(arena);
        ChatUtils.send(cs, "Arena added to game");
	}
}
