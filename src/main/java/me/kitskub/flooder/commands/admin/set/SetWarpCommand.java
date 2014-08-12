package me.kitskub.flooder.commands.admin.set;

import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetWarpCommand extends PlayerCommand {

	public SetWarpCommand() {
		super(Commands.ADMIN_SET_HELP.getCommand(), "warp", "<arena/game> <spectator|lobby|finished>", "set a warp", Perms.ADMIN_SET_WARP);
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
	    if(args.length < 2){
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    String type = args[1];
        Location loc = player.getLocation();
        if ("spectator".equalsIgnoreCase(type)) {
            FArena arena = Flooder.gameMaster().getArena(args[0]);
            if (arena == null) {
    		    ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
        	    return;
    	    }
            arena.specWarp = loc;
        } else if ("lobby".equalsIgnoreCase(type)) {
            FArena arena = Flooder.gameMaster().getArena(args[0]);
            if (arena == null) {
    		    ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
        	    return;
    	    }
            arena.lobbyWarp = loc;
        } else if ("finished".equalsIgnoreCase(type)) {
            FGame game = Flooder.gameMaster().getGame(args[0]);
            if (game == null) {
                ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
                return;
            }
            game.finishedWarp = loc;
        } else {
            ChatUtils.error(player, "Invalid type!");
            return;
        }
        ChatUtils.send(player, "Warp set.");
	}
}
