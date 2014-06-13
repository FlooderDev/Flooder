package me.kitskub.flooder.listeners;

import me.kitskub.gamelib.listeners.ClickSession;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.core.FArena;
import me.kitskub.gamelib.utils.Cuboid;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FSessionCallbacks {

    public static final ClickSession.Callback<FArena> spawnAdder = new ClickSession.Callback<FArena>() {
        public boolean callback(PlayerInteractEvent event, ClickSession<FArena> session) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (session.getObject().addSpawnPoint(event.getClickedBlock().getLocation().add(.5, 1, .5))) {
                    session.clicked(event.getClickedBlock());
                    ChatUtils.send(event.getPlayer(), "Spawn point %s has been added to %s.", session.getBlocks().size(), session.getObject().getName());
                }
                else {
                    ChatUtils.error(event.getPlayer(), "%s already has this spawn point.", session.getObject().getName());
                }
                return false;
            }
            ChatUtils.send(event.getPlayer(), "You have added %d spawn points to the game %s.", session.getBlocks().size(), session.getObject().getName());
            return true;
        }
    };

    public static final ClickSession.Callback<FArena> spawnRemover = new ClickSession.Callback<FArena>() {
        public boolean callback(PlayerInteractEvent event, ClickSession<FArena> session) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (session.getObject().removeSpawnPoint(event.getClickedBlock().getLocation().add(.5, 1, .5))) {
                    session.clicked(event.getClickedBlock());
                    ChatUtils.send(event.getPlayer(), "Spawn point %s has been removed from %s.", session.getBlocks().size(), session.getObject().getName());
                }
                else {
                    ChatUtils.error(event.getPlayer(), "%s does not contain this spawn point.", session.getObject().getName());
                }
                return false;
            }
            ChatUtils.send(event.getPlayer(), "You have removed %d spawn points from the game %s.", session.getBlocks().size(), session.getObject().getName());
            return true;
        }
    };

    public static final ClickSession.Callback<FArena> zoneSetter = new ClickSession.Callback<FArena>() {
        public boolean callback(PlayerInteractEvent event, ClickSession<FArena> session) {
			if (session.getBlocks().size() < 1) {
				session.clicked(event.getClickedBlock());
				ChatUtils.send(event.getPlayer(), "First corner set.");
                return false;
			} else {
                Cuboid c = new Cuboid(session.getBlocks().get(0).getLocation(), event.getClickedBlock().getLocation());
                session.getObject().setZone(c);
				ChatUtils.send(event.getPlayer(), "Second corner and cuboid set.");
                return true;
			}
        }
    };
}
