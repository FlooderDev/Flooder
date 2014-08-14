package me.kitskub.flooder.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.ItemConfig;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FGame;
import me.kitskub.flooder.core.infohandler.SpawnTakenHandler;
import me.kitskub.flooder.core.infohandler.WaterHurtHandler;
import me.kitskub.flooder.utils.BossBarHandler;
import me.kitskub.gamelib.api.event.UserClassChosenEvent;
import me.kitskub.gamelib.api.event.zone.UserLeftZoneEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakenEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingInterruptedEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingTickEvent;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
        if(user.getGame() != game) return;
		if(message.startsWith("/" + Flooder.CMD_ADMIN) || message.startsWith("/" + Flooder.CMD_USER)) return;
        for (String s : Defaults.Config.ALLOWED_COMMANDS.getGlobalStringList()) {
            if(message.startsWith("/" + s)) return;
        }
        if (user.getPlayingType() == User.PlayingType.PLAYING) ChatUtils.error(event.getPlayer(), "Cannot use that command while in a game!");
        else if (user.getPlayingType() == User.PlayingType.SPECTATING) ChatUtils.error(event.getPlayer(), "Cannot use that command while spectating a game!");
        event.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
	public void entityTargetEntityHighest(EntityTargetLivingEntityEvent event) {
		if (!(event.getTarget() instanceof Player)) return;
		Player player = (Player) event.getTarget();
        User user = User.get(player);
        if (user.getGame() != game) return;
        event.setCancelled(true);
	}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerChatHighest(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = User.get(player);
        Game userGame = user.getGame();
        for (Iterator<Player> it = event.getRecipients().iterator(); it.hasNext();) {
            Player p = it.next();
            Game otherGame = User.get(p).getGame();
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
        if (user.getGame() != game) return;

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

		ArrayList<ItemStack> arrayItemStack = new ArrayList<>(map.keySet());
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
		List<Integer> list = new ArrayList<>();
		for (int i = min; i <= max; i++) {
			list.add(i);
		}
		return list;
	}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = User.get(event.getPlayer());
        if (user.getGame() != game || user.getPlayingType() != User.PlayingType.PLAYING) return;
        if (game.getState() == Game.GameState.RUNNING && WaterHurtHandler.inWater(user)) {
            user.getInfoHandler(WaterHurtHandler.CREATOR).start();
        }
        if (game.getState() == Game.GameState.COUNTING && (
                event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()
                )) {
            Location frozenLoc = user.getInfoHandler(SpawnTakenHandler.CREATOR).getSpawnTaken();
            Location loc = frozenLoc.clone();
			loc.setPitch(event.getPlayer().getLocation().getPitch());
			loc.setYaw(event.getPlayer().getLocation().getYaw());
            event.getPlayer().teleport(loc);
        }
        FArena a = game.getActiveArena();
        if (game.getState() == Game.GameState.RUNNING && a != null) {
            if (a.takeZone.getCuboid().contains(user.getPlayer().getLocation())) {
                a.takeZone.beginTaking(user);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHurt(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        User user = User.get((Player) e.getEntity());
        if (user.getGame() == game && (game.getState().isPreGame() || !game.getActivePlayers().contains(user))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        User user = User.get(event.getEntity());
        if (user.getGame() != game || game.getState() != Game.GameState.RUNNING) return;
        User killer = event.getEntity().getKiller() == null ? null : User.get(event.getEntity().getKiller());
        game.playerKilled(killer, user);
        event.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        User user = User.get(event.getPlayer());
        if (game.getPostWaiting().contains(user) || game.getState().isPreGame()) {
            event.getPlayer().teleport(game.getActiveArena().lobbyWarp);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClassChosen(UserClassChosenEvent e) {
        if (e.getUser().getGame() == game && game.getState().isPreGame()) {
            game.setPlayerReady(e.getUser());
        }
    }

    @EventHandler
    public void onZoneTakingTick(ZoneTakingTickEvent e) {
        if (e.getGame() != game) return;
        Set<User> usersIn = e.getZone().getUsersIn();
        for (User u : usersIn) {
            BossBarHandler.get().updatePercent(u, 100 * (1 - e.getPercentDone()));
        }
    }

    @EventHandler
    public void onZoneTakingInterrupted(ZoneTakingInterruptedEvent e) {
        if (e.getGame() != game) return;
        Set<User> usersIn = e.getZone().getUsersIn();
        for (User u : usersIn) {
            BossBarHandler.get().remove(u);
        }
    }

    @EventHandler
    public void onUserLeaveZone(UserLeftZoneEvent e) {
        if (e.getGame() != game) return;
        BossBarHandler.get().remove(e.getUser());
    }

    @EventHandler
    public void onZoneTakenEvent(ZoneTakenEvent e) {
        if (e.getGame() != game) return;
        Set<User> usersIn = e.getZone().getUsersIn();
        for (User u : usersIn) {
            BossBarHandler.get().remove(u);
        }
        game.win((User) e.getTaken());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void foodChangeHighest(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        User user = User.get(player);
        if (user.getGame() != game) return;
        event.setCancelled(true);
    }
}
