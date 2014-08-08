package me.kitskub.flooder.commands.admin;

import java.util.Collection;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.GeneralUtils;
import org.bukkit.command.CommandSender;

public class CheckCommand extends Command {

	public CheckCommand() {
		super(Flooder.faCH(), "check", "[arena]", "check an arena's setup", Perms.ADMIN_CHECK);
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
}
