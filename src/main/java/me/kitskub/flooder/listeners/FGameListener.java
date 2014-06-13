package me.kitskub.flooder.listeners;

import java.util.Iterator;
import java.util.Map;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FGameListener implements Listener {
    private final FGame game;
    public FGameListener(FGame game) {
        this.game = game;
    }

	@EventHandler(ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();
        if (game.allowCommand()) return;
        User user = User.get(player);
        if(user.getGameEntry().getGame() != game) return;
		if(message.startsWith("/" + Flooder.CMD_ADMIN) || message.startsWith("/" + Flooder.CMD_USER)) return;
        for (String s : Defaults.Config.ALLOWED_COMMANDS.getGlobalStringList()) {
            if(message.startsWith("/" + s)) return;
        }
        if (user.getGameEntry().getType() == User.GameEntry.Type.PLAYING) ChatUtils.error(event.getPlayer(), "Cannot use that command while in a game!");
        else if (user.getGameEntry().getType() == User.GameEntry.Type.SPECTATING) ChatUtils.error(event.getPlayer(), "Cannot use that command while spectating a game!");
        event.setCancelled(true);
    }

    @EventHandler()
	public void onPlayerInteract(PlayerInteractEvent event) {
        User user = User.get(event.getPlayer());
        if (user.getGameEntry().getGame() != game) return;
        boolean cancel = false;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && game.getState().isPreGame()) {
            Map<Location, me.kitskub.gamelib.framework.Class> signs = game.getActiveArena().getLobby().getSigns();
            if (signs.containsKey(event.getClickedBlock().getLocation())) {
                me.kitskub.gamelib.framework.Class clickedClass = signs.get(event.getClickedBlock().getLocation());
                user.setClass(clickedClass);
                cancel = true;
            }
        }
        if (cancel) {
            event.setUseItemInHand(Event.Result.DENY);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void entityTargetEntityHighest(EntityTargetLivingEntityEvent event) {
		if (!(event.getTarget() instanceof Player)) return;
		Player player = (Player) event.getTarget();
        User user = User.get(player);
        if (user.getGameEntry().getGame() != game) return;
        event.setCancelled(true);
	}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerChatHighest(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = User.get(player);
        Game userGame = user.getGameEntry().getGame();
        for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext();) {
            Player p = it.next();
            Game otherGame = User.get(p).getGameEntry().getGame();
            if ((userGame == game && !userGame.equals(otherGame)) || (otherGame == game && !otherGame.equals(userGame))) {
                it.remove();
            }
        }
    }
}
