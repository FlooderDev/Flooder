package me.kitskub.flooder.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.ItemConfig;
import me.kitskub.flooder.Logging;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FGame;
import me.kitskub.flooder.core.infohandler.JumpsLeftHandler;
import me.kitskub.flooder.core.infohandler.SpawnTakenHandler;
import me.kitskub.flooder.core.infohandler.WaterHurtHandler;
import me.kitskub.flooder.utils.BossBarHandler;
import me.kitskub.gamelib.api.event.CountdownTickEvent;
import me.kitskub.gamelib.api.event.UserClassChosenEvent;
import me.kitskub.gamelib.api.event.zone.UserLeftZoneEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakenEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingInterruptedEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingTickEvent;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
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
		if (map.isEmpty()) {
			return;
		}
        final int size = chest.getInventory().getSize();
        final Random random = new Random();
        List<Integer> slots = range(0, size - 1);//By adding this, we know that we won't pick an index that has been used before
        List<ItemStack> keys = new ArrayList<>(map.keySet());
        Collections.shuffle(keys);
        for (Iterator<ItemStack> it = keys.iterator(); it.hasNext() && !slots.isEmpty();) {
            ItemStack key = it.next();
            int loc = random.nextInt(slots.size());
            int slot = slots.get(loc);
			slots.remove(loc);
            if (random.nextFloat() > map.get(key)) continue;
			chest.getInventory().setItem(slot, key);
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
        if (game.getState() == Game.GameState.COUNTING && game.frozenPlayers() && (
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
        if (user.getGame() == game) {
            if (game.getState().isPreGame()) {
                e.setCancelled(true);
            } else {
                if (user.getPlayer().getHealth() - e.getDamage() <= 0) {
                    e.setCancelled(true);
                    game.playerKilled(null, user);
                }
            }
        } else {

        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        User user = User.get(event.getEntity());
        if (user.getGame() != game || game.getState() != Game.GameState.RUNNING) return;
        User killer = event.getEntity().getKiller() == null ? null : User.get(event.getEntity().getKiller());
        game.playerKilled(killer, user);
        Logging.warning("Player died and shouldn't have.");
        event.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        User user = User.get(event.getPlayer());
        if (user.getGame() == game && game.getState().isPreGame()) {
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

    @EventHandler
    public void onCountdownTick(CountdownTickEvent e) {
        if (e.getGame() == game && e.getTick() == 10) {
            game.onFinalCountdown();
        }
        e.setMessage("Beginning in " + e.getMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpongePlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.SPONGE) {
            final Location spongeLoc = e.getBlockPlaced().getLocation();
            for (int y = Math.max(0, spongeLoc.getBlockY() - 2); y <= Math.min(255, spongeLoc.getBlockY() + 2); y++) {
                for (int x = spongeLoc.getBlockX() - 2; x <= spongeLoc.getBlockX(); x++) {
                    for (int z = spongeLoc.getBlockX() - 2; z <= spongeLoc.getBlockX(); z++) {
                        Location local = new Location(spongeLoc.getWorld(), x, y, z);
                        Block b = spongeLoc.getWorld().getBlockAt(local);
                        if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
                            game.getResetter().add(local, e.getBlockReplacedState());
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
            Bukkit.getScheduler().runTask(Flooder.getInstance(), new Runnable() {
                @Override
                public void run() {
                    spongeLoc.getBlock().setType(Material.AIR);
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTNTPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.TNT) {
            e.getBlock().setType(Material.AIR);
            game.getResetter().add(e.getBlock().getLocation(), e.getBlockReplacedState());
            TNTPrimed tnt = (TNTPrimed) e.getPlayer().getWorld().spawnEntity(e.getBlock().getLocation(), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(0);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFeatherClick(PlayerInteractEvent e) {
        User user = User.get(e.getPlayer());
        if (user.getGame() != game || game.getState() != Game.GameState.RUNNING) return;
        if (e.hasItem() && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem().getType() == Material.FEATHER) {
            user.getInfoHandler(JumpsLeftHandler.CREATOR).addJumps(Defaults.Config.JUMPS_PER_FEATHER.getGlobalInt());
            ItemStack item = e.getItem();
            int amount = item.getAmount();
            amount--;
            if (amount == 0) {
                e.getPlayer().setItemInHand(null);
            } else {
                item.setAmount(amount);
                e.getPlayer().setItemInHand(item);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        User user = User.get(player);
        if (user.getGame() != game) return;
        boolean canJump = user.getInfoHandler(JumpsLeftHandler.CREATOR).useJump();
        event.setCancelled(true);
        if (!canJump || player.getGameMode() != GameMode.SURVIVAL) return;

        player.setAllowFlight(false);
        player.setFlying(false);
        player.setVelocity(player.getVelocity().setY(1.6));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMoveOnGround(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.get(player);
        if (user.getGame() != game) return;
        boolean canJump = user.getInfoHandler(JumpsLeftHandler.CREATOR).getJumpsLeft() > 0;
        if (!canJump
                || player.getGameMode() != GameMode.SURVIVAL
                || player.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.AIR
                ) return;
        player.setAllowFlight(true);
    }
}
