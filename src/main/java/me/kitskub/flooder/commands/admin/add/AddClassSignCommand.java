package me.kitskub.flooder.commands.admin.add;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.listeners.SessionCallbacks;
import me.kitskub.gamelib.listeners.SessionListener;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang; 
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddClassSignCommand extends PlayerCommand {

	public AddClassSignCommand() {
		super(Perms.ADMIN_ADD_CLASS_SIGN, Commands.ADMIN_ADD_HELP.getCommand(), "classsign");
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
		if (args.length < 2) {
			ChatUtils.send(player, getUsage(), Flooder.CMD_ADMIN);
			return;
		}
		FArena arena = Flooder.gameMaster().getArena(args[0]);

		if (arena == null) {
			ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
			return;
		}
        FClass c = Flooder.classManager().get(args[1]);
        if (c == null) {
            ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[1]));
            return;
        }
        SessionListener.addSession(SessionCallbacks.classSignAdder, player, arena.getOwningPlugin(), arena, "class", c);
		ChatUtils.send(player, ChatColor.GREEN, "Left-click the sign to add it as a class sign.");
	}

	@Override
	public String getInfo() {
		return "add a class sign";
	}

	@Override
	public String getLocalUsage() {
		return "classsign <arena name> <class name>";
	}

}
