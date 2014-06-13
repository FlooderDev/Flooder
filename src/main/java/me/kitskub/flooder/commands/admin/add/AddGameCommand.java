package me.kitskub.flooder.commands.admin.add;

import me.kitskub.gamelib.api.event.GameCreateEvent;
import me.kitskub.gamelib.api.event.GameCreatedEvent;
import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddGameCommand extends PlayerCommand {

	public AddGameCommand() {
		super(Perms.ADMIN_ADD_GAME, Commands.ADMIN_ADD_HELP.getCommand(), "game");
	}

	@Override
	public void handlePlayer(Player player, String label, String[] args) {
	    if (args.length < 1) {
		    ChatUtils.helpCommand(player, this);
		    return;
	    }
	    game = Flooder.gameMaster().getGame(args[0]);
	    if (game != null) {
		    ChatUtils.error(player, "%s already exists.", args[0]);
		    return;
	    }
        GameCreateEvent first = new GameCreateEvent(args[0]);
        Bukkit.getPluginManager().callEvent(first);
	    if(first.isCancelled()) {
	    	ChatUtils.error(player, "Creation of game %s was cancelled.", args[0]);
            return;
	    }
        FGame game = Flooder.gameMaster().createGame(args[0]);
	    GameCreatedEvent event = new GameCreatedEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        ChatUtils.send(player, ChatColor.GREEN, "%s has been created. Now you must add an arena.", args[0]);
	}

	@Override
	public String getInfo() {
		return "add a game";
	}

	@Override
	public String getLocalUsage() {
		return "game <game name>";
	}
	
}
