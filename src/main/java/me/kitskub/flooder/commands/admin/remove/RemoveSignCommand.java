package me.kitskub.flooder.commands.admin.remove;

import me.kitskub.gamelib.commands.PlayerCommand;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults.Commands; 
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.gamelib.listeners.general.SessionCallbacks;
import me.kitskub.gamelib.listeners.general.SessionListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveSignCommand extends PlayerCommand {

	public RemoveSignCommand() {
		super(Perms.ADMIN_REMOVE_SIGN, Commands.ADMIN_REMOVE_HELP.getCommand(), "sign");
	}

	@Override
	public void handlePlayer(Player player, String cmd, String[] args) {
		SessionListener.addSession(SessionCallbacks.signRemover, player, null, null);
		ChatUtils.send(player, ChatColor.GREEN, "Hit a sign to remove it. If you do not hit a sign, nothing will happen.");
	}

	@Override
	public String getInfo() {
		return "remove a sign or an info wall that contains the sign";
	}

	@Override
	public String getLocalUsage() {
		return "sign";
	}
	
}
