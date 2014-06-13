package me.kitskub.flooder.commands.admin.set;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.impl.arena.BaseArena.RegionPoint;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends PlayerCommand {

    public SetSpawnCommand() {
	    super(Perms.ADMIN_SET_SPAWN, Commands.ADMIN_SET_HELP.getCommand(), "spawn");
    }

    @Override
    public void handlePlayer(Player player, String cmd, String[] args) {
	    if (args.length < 2) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
        FArena arena = Flooder.gameMaster().getArena(args[0]);
	    if (arena == null) {
		    ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
		    return;
	    }
	    
	    Location loc = player.getLocation();
        if ("lobby".equalsIgnoreCase(args[1])) {
            arena.set(RegionPoint.LOBBY_WARP, loc, arena.getLobbyArea());
        } else if ("spectator".equalsIgnoreCase(args[1])) {
            arena.set(RegionPoint.SPEC_WARP, loc, arena.getSpecArea());
        } else {
            ChatUtils.error(player, "Invalid type!");
            return;
        }
	    ChatUtils.send(player, "Spawn has been set for %s.", arena.getName());
    }

	@Override
	public String getInfo() {
		return "set the spawnpoint for a game";
	}

	@Override
	public String getLocalUsage() {
		return "spawn <arena name> [lobby|spectator]";
	}
    
}
