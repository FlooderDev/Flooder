package me.kitskub.flooder.commands.admin.remove;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.listeners.SessionListener;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.listeners.FSessionCallbacks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveSpawnPointCommand extends PlayerCommand {

	public RemoveSpawnPointCommand() {
		super(Perms.ADMIN_REMOVE_SPAWNPOINT, Commands.ADMIN_REMOVE_HELP.getCommand(), "spawnpoint");
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {	    
	    if (args.length < 1) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    
	    FArena arena = Flooder.gameMaster().getArena(args[0]);
	    if(arena == null) {
		    ChatUtils.error(player, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
		    return;
	    }
	    
	    SessionListener.addSession(FSessionCallbacks.spawnRemover, player, Flooder.getInstance(), arena);
	    ChatUtils.send(player, ChatColor.GREEN, "Hit a spawn point to remove it from %s.", arena.getName());
	}

	@Override
	public String getInfo() {
		return "remove a spawnpoint";
	}

	@Override
	public String getLocalUsage() {
		return "spawnpoint <arena name>";
	}
	
}
