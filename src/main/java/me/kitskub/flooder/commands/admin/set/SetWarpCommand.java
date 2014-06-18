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

public class SetWarpCommand extends PlayerCommand {

	public SetWarpCommand() {
		super(Perms.ADMIN_SET_WARP, Commands.ADMIN_SET_HELP.getCommand(), "warp");
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
        Location loc = player.getLocation();
        if ("spectator".equalsIgnoreCase(type)) {
            arena.specWarp = loc;
        } else {
            ChatUtils.error(player, "Invalid type!");
            return;
        }
        ChatUtils.send(player, "Warp set.");
	}

	@Override
	public String getInfo() {
		return "set a warp";
	}

	@Override
	public String getLocalUsage() {
		return "warp <arena> <spectator>";
	}
	
}
