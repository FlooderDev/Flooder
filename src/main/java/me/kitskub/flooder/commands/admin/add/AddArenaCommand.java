package me.kitskub.flooder.commands.admin.add;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.framework.Arena;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;
import org.bukkit.entity.Player;

public class AddArenaCommand extends PlayerCommand {

	public AddArenaCommand() {
		super(Perms.ADMIN_ADD_ARENA, Commands.ADMIN_ADD_HELP.getCommand(), "arena");
	}

	@Override
	public void handlePlayer(Player player, String label, String[] args) {
	    if (args.length < 1) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    Arena a = Flooder.gameMaster().getArena(args[0]);
	    if (a != null) {
		    ChatUtils.error(player, "%s already exists.", args[0]);
		    return;
	    }
        Flooder.gameMaster().createArena(args[0]);
        ChatUtils.send(player, "Arena created! Must now set the main arena cuboid, lobby cuboid, lobby signs, and spawnpoints!");
	}

	@Override
	public String getInfo() {
		return "add an arena";
	}

	@Override
	public String getLocalUsage() {
		return "arena <arena name>";
	}
	
}
