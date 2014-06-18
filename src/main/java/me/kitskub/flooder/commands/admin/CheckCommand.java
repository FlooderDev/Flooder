package me.kitskub.flooder.commands.admin;

import java.util.Collection;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder; 
import me.kitskub.flooder.core.FArena;
import me.kitskub.gamelib.utils.GeneralUtils;
import org.bukkit.command.CommandSender;

public class CheckCommand extends Command {

	public CheckCommand() {
		super(Perms.ADMIN_CHECK, Flooder.faCH(), "check");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
		if (args.length < 1) {
			ChatUtils.helpCommand(cs, this);
			return;
		}
        String name = args[0];
		FArena arena = Flooder.gameMaster().getArena(name);
		if (arena == null) {
			ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", name));
			return;
		}
        Collection<String> errors = arena.verifyData();
        if (GeneralUtils.printErrors(cs, errors, "Problems: ")) {
            ChatUtils.send(cs, "Arena is ready to be used!");
        }
	}

	@Override
	public String getInfo() {
		return "check an arena's setup";
	}

	@Override
	public String getLocalUsage() {
		return "check [arena]";
	}
    
}
