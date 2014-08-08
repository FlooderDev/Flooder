package me.kitskub.flooder.commands.admin.add;

import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FArena;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.entity.Player;

public class AddArenaCommand extends PlayerCommand {

	public AddArenaCommand() {
		super(Commands.ADMIN_ADD_HELP.getCommand(), "arena", "<arena name>", "add an arena", Perms.ADMIN_ADD_ARENA);
	}

	@Override
	public void handlePlayer(Player player, String label, String[] args) {
	    if (args.length < 1) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    FArena a = Flooder.gameMaster().getArena(args[0]);
	    if (a != null) {
		    ChatUtils.error(player, "%s already exists.", args[0]);
		    return;
	    }
        Flooder.gameMaster().createArena(args[0]);
        ChatUtils.send(player, "Arena created! Must now set the main arena cuboid, lobby cuboid, spectator cuboid, lobby signs, zones, and spawnpoints!");
	}
}
