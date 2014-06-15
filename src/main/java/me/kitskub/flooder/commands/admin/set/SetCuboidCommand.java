package me.kitskub.flooder.commands.admin.set;

import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.listeners.FSessionCallbacks;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.listeners.general.SessionListener;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class SetCuboidCommand extends PlayerCommand {

	public SetCuboidCommand() {
		super(Perms.ADMIN_SET_CUBOID, Commands.ADMIN_SET_HELP.getCommand(), "cuboid");
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
	    if(args.length < 2){
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    FArena arena = Flooder.gameMaster().getArena(args[0]);
	    
	    if (arena == null) {
		    ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
		    return;
	    }
	    String type = args[1];
        if ("main".equalsIgnoreCase(type)) {
             SessionListener.addSession(FSessionCallbacks.mainCuboidAdder, player, arena.getOwningPlugin(), arena);
        } else if ("lobby".equalsIgnoreCase(type)) {
            SessionListener.addSession(FSessionCallbacks.lobbyCuboidAdder, player, arena.getOwningPlugin(), arena);
        } else {
            ChatUtils.error(player, "Invalid type!");
            return;
        }
        ChatUtils.send(player, "Click the two corners to set a cuboid.");
	}

	@Override
	public String getInfo() {
		return "set a cuboid";
	}

	@Override
	public String getLocalUsage() {
		return "cuboid <arena name> [main|lobby]";
	}
	
}
