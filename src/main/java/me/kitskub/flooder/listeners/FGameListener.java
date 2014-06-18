package me.kitskub.flooder.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.ItemConfig;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractMonitor(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!(event.getClickedBlock().getState() instanceof Chest)) return;

        Player player = event.getPlayer();
		User user = User.get(player);
        if (user.getGameEntry().getGame() != game) return;

		final Chest state = (Chest) event.getClickedBlock().getState();
        if (game.addChest(state)) {
            fillChest(state);
        }
	}

	public static void fillChest(Chest chest) {
		chest.getInventory().clear();

		//Contains the whole itemset.

		Map<ItemStack, Double> map = ItemConfig.getChestLoot();
        int initialSize = map.size();
		if (map.isEmpty()) {
			return;
		}
		ItemStack last = null;

		//Chest size
		final int size = chest.getInventory().getSize();

		List<Integer> slots = range(0, size - 1);//By adding this, we know that we won't pick an index that has been used before

        Random random = new Random();
        //This calculate the amount of items that will be in the chest.
        //We don't want a lot of duplicates, so if there are a lot of items in the config, we can have more
        final int amountCount = (int) ((random.nextDouble() + .5) * (map.size() / size));
		if (amountCount == 0) {
			return; // If there are no items, don't continue with method
		}
		final int minItems = (int) Math.floor(amountCount / 2);

		//Let's calculate what item we can have.
		Iterator<Map.Entry<ItemStack, Double>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<ItemStack, Double> entry = iterator.next();
			if (!iterator.hasNext()) {
				last = entry.getKey();
			}
			double rand = random.nextDouble();
			if (rand >= entry.getValue()) {
				iterator.remove();
			}

		}

		ArrayList<ItemStack> arrayItemStack = new ArrayList<ItemStack>(map.keySet());
		if (arrayItemStack.isEmpty()) {
			arrayItemStack.add(last);//Just in case
		}
        //We add the items in the chest.
		int toRandom = (amountCount - minItems) * (arrayItemStack.size() / initialSize);
		int amount = toRandom == 0 ? minItems : random.nextInt(toRandom) + minItems;
		for (int i = 0; i < amount; i++) {
			ItemStack stack = arrayItemStack.get(random.nextInt(arrayItemStack.size()));
			int slot = random.nextInt(slots.size());
			chest.getInventory().setItem(slots.get(slot), stack);
			slots.remove(slot);
		}
	}

	private static List<Integer> range(int min, int max) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = min; i <= max; i++) {
			list.add(i);
		}
		return list;
	}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = User.get(event.getPlayer());
        if (user.getGameEntry().getGame() != game) return;
        Block lower = event.getPlayer().getLocation().getBlock();
        Block upper = event.getPlayer().getLocation().add(0, 1, 0).getBlock();
        if (lower.getType() == Material.WATER || upper.getType() == Material.WATER) {
            user.getPlayer().setHealth(0);
            game.playerKilled(null, user);
        }
        if (game.getState() == Game.GameState.COUNTING && (
                event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        User user = User.get(event.getEntity());
        if (user.getGameEntry().getGame() != game) return;
        User killer = event.getEntity().getKiller() == null ? null : User.get(event.getEntity().getKiller());
        game.playerKilled(killer, user);
    }
}
