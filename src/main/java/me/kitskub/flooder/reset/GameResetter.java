package me.kitskub.flooder.reset;

import java.util.Map; 
import java.util.concurrent.ConcurrentHashMap; 
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GameResetter implements Listener {
    private final FGame game;
	private final Map<Location, BlockState> changedBlocks = new ConcurrentHashMap<>();
	private final Map<Location, ItemStack[]> changedInvs = new ConcurrentHashMap<>();

    public GameResetter(FGame game) {
        this.game = game;
    }

	public void init() {
		Bukkit.getPluginManager().registerEvents(this, Flooder.getInstance());
	}

	public boolean resetChanges() {
        HandlerList.unregisterAll(this);
		Bukkit.getScheduler().runTask(Flooder.getInstance(), new Runnable() {
            @Override
			public void run() {
				for(Location l : changedBlocks.keySet()) {
					BlockState state = changedBlocks.get(l);
					state.update(true);
					if (!(state instanceof InventoryHolder)) continue;
					if (!changedInvs.containsKey(l)) continue;
					((InventoryHolder) state).getInventory().setContents(changedInvs.get(l));

				}
                changedBlocks.clear();
                changedInvs.clear();
			}
		});
		return true;
	}

	private synchronized void check(Location loc, BlockState state) {
		if (changedBlocks.containsKey(loc)) return; // Don't want to erase the original block
		changedBlocks.put(loc, state);
	}

	private synchronized void check(Location loc, ItemStack[] inv) {
		if (changedInvs.containsKey(loc)) return; // Don't want to erase the original block
		changedInvs.put(loc, inv);
	}

    private boolean inGame(Player player) {
        return User.get(player).getGameEntry().getGame() == game;
    }

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (inGame(event.getPlayer()) && event.getClickedBlock() != null) {
			check(event.getClickedBlock().getLocation(), event.getClickedBlock().getState());
            if (event.getClickedBlock().getState() instanceof InventoryHolder) {
                check(event.getClickedBlock().getLocation(), ((InventoryHolder) event.getClickedBlock().getState()).getInventory().getContents());
            }
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
        if (inGame(event.getPlayer())) {
            check(event.getBlock().getLocation(), event.getBlockReplacedState());
        }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
        if (inGame(event.getPlayer())) {
            check(event.getBlock().getLocation(), event.getBlock().getState());
        }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
        if (changedBlocks.containsKey(event.getBlock().getLocation())) {
            check(event.getToBlock().getLocation(), event.getToBlock().getState());
        }
	}
}
