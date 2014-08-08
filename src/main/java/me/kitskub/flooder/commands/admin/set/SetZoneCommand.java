package me.kitskub.flooder.commands.admin.set;

import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.listeners.FSessionCallbacks;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.listeners.general.SessionListener;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class SetZoneCommand extends PlayerCommand {

    public SetZoneCommand() {
	    super(Commands.ADMIN_SET_HELP.getCommand(), "zone", "<arena name>", "set the zone", Perms.ADMIN_SET_SPAWN);
    }

	@Override
	public void handlePlayer(Player player, String label, String[] args) {
	    if (args.length < 1) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    FArena a = Flooder.gameMaster().getArena(args[0]);
	    if (a == null) {
			ChatUtils.error(player, Defaults.Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
			return;
		}

        ChatUtils.send(player, "Click the first corner of the zone.");
        SessionListener.addSession(FSessionCallbacks.zoneSetter, player, a.getOwningPlugin(), a);
	}

	@Override
	public String getInfo() {
		return "set the zone";
	}

	@Override
	public String getLocalUsage() {
		return "zone <arena name>";
	}
}
