package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.Logging;
import me.kitskub.flooder.core.infohandler.SpawnTakenHandler;
import me.kitskub.flooder.listeners.FGameListener;
import me.kitskub.flooder.listeners.WaterRunner;
import me.kitskub.flooder.reset.GameResetter;
import me.kitskub.flooder.utils.BossBarHandler;
import me.kitskub.gamelib.GameCountdown;
import me.kitskub.gamelib.GameLib;
import me.kitskub.gamelib.api.event.GameEndEvent;
import me.kitskub.gamelib.api.event.GamePreStartEvent;
import me.kitskub.gamelib.api.event.GameStartedEvent;
import me.kitskub.gamelib.api.event.PlayerJoinGameEvent;
import me.kitskub.gamelib.api.event.PlayerKilledEvent;
import me.kitskub.gamelib.api.event.PlayerLeftGameEvent;
import me.kitskub.gamelib.framework.Game.GameState;
import me.kitskub.gamelib.framework.TimedGame;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.impl.AbstractGame;
import me.kitskub.gamelib.framework.impl.GameMasterImpl;
import me.kitskub.gamelib.framework.infohandler.GameClassHandler;
import me.kitskub.gamelib.games.DataSave;
import me.kitskub.gamelib.stats.PlayerStat;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.GeneralUtils;
import me.kitskub.gamelib.utils.config.ConfigSection;
import me.kitskub.paintball.core.infohandler.LastSpawnHandler;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class FGame extends AbstractGame<Flooder, FGame, FArena> implements TimedGame<Flooder, FGame, FArena> {
    public Location finishedWarp;
    // Players
    private final Set<User> playing;
    private final Set<User> postWaiting;
    private final Set<User> spectating;
    private final Set<Location> availableSpawns;
    //Listeners
    private final FGameListener listener;
    private final GameResetter resetter;
    private final WaterRunner waterRunner;
    //Temp
    private final Set<Chest> chests;

    public FGame(String name) {
        super(name);
        this.active = null;
        this.state = GameState.DISABLED;
        this.countdown = null;

        this.playing = new HashSet<>();
        this.postWaiting = new HashSet<>();
        this.spectating = new HashSet<>();
        this.availableSpawns = new HashSet<>();

        this.listener = new FGameListener(this);
        this.resetter = new GameResetter(this);
        this.waterRunner = new WaterRunner(this);

        this.chests = new HashSet<>();
    }

    @Override
    public synchronized void join(User player) {
        if (!preJoinValidation(player)) {
            return;
        }
	    if (isFull()) {
		    ChatUtils.error(player.getPlayer(), "%s is already full.", name);
	    }
        if (state == GameState.INACTIVE) {
            if (!playing.isEmpty()) {
                Logging.warning("Game was inactive with players!");
            }
            state = GameState.WAITING;
            pickArena();
        }
	    boolean canceled = false;
	    PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, player);
	    if (!canceled) Bukkit.getPluginManager().callEvent(event);
	    if (canceled || event.isCancelled()) {
		    if (playing.isEmpty()) {
                clearArena();
                state = GameState.INACTIVE;
            }
		    return;
	    }
        player.setGame(this, User.PlayingType.PLAYING);
        DataSave.saveDataWithInvClear(player);
	    Location loc = getNextOpenSpawnPoint();
        player.getInfoHandler(SpawnTakenHandler.CREATOR).setSpawnTaken(loc);
        player.getInfoHandler(GameClassHandler.CREATOR).setClass(FClass.blank, false);
        player.getPlayer().teleport(active.lobbyWarp);
        Bukkit.getPluginManager().callEvent(new PlayerJoinGameEvent(this, player));
    }

    @Override
    public synchronized void leave(User player) {
        if (!leavingGame(player)) return;
        if (playing.isEmpty()) {
            Logging.debug("Player left and there are no players in.");
            cancelGame();
            return;
        }
        if (state == GameState.RUNNING) {
            if (playing.size() == 1) {
                win(playing.iterator().next());
            }
        }
    }

    private synchronized boolean leavingGame(User player) {
        if (!playing.remove(player) & !postWaiting.remove(player)) return false;
        player.getInfoHandler(GameClassHandler.CREATOR).setClass(null);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.getPlayer().removePotionEffect(PotionEffectType.JUMP);
        BossBarHandler.get().remove(player);
        leavingArena(player);
        player.setGame(null, User.PlayingType.NONE);
        Bukkit.getPluginManager().callEvent(new PlayerLeftGameEvent(this, player));
        return true;
    }

    @Override
    public synchronized void spectate(User player) {
        if (state == GameState.INACTIVE || state == GameState.DISABLED) {
            ChatUtils.error(player.getPlayer(), "There is no one in that game!");
            return;
        }
        player.setGame(this, User.PlayingType.SPECTATING);
        spectating.add(player);
        joiningArena(player, active.specWarp);
    }

    @Override
    public void leaveSpectate(User player) {
        player.setGame(null, User.PlayingType.NONE);
        spectating.remove(player);
        leavingArena(player);
    }
    
    private void leavingArena(User player) {
        player.unsubscribe(this);
        DataSave.loadData(player);
        player.getPlayer().teleport(finishedWarp);
    }

    private void joiningArena(User player, Location toTeleport) {
        player.getPlayer().teleport(toTeleport);
        player.subscribe(this);
    }

    @Override
    public Set<User> getActivePlayers() {
        return new HashSet<>(playing);
    }

    @Override
    public Set<User> getSpectatingPlayers() {
        return new HashSet<>(spectating);
    }

    public Set<User> getPostWaiting() {
        return new HashSet<>(postWaiting);
    }

    @Override
    public Set<User> getAllPlayers() {
        HashSet<User> set = new HashSet<>(playing);
        set.addAll(postWaiting);
        set.addAll(spectating);
        return set;
    }

    @Override
    public String cancelGame() {
        if (state == GameState.INACTIVE) {
            return "Cannot stop a game that is inactive!";
        } else if (state == GameState.DISABLED) {
            return "Cannot stop a disabled game!";
        }
        if (countdown != null) {
            countdown.cancel();
            countdown = null;
        }
        List<User> list;
        boolean callEvent = false;
        if (state == GameState.RUNNING) {
            callEvent = true;
            waterRunner.stop();
            resetter.resetChanges();
        }
        list = new ArrayList<>(spectating);
        for (User p : list) {
            leaveSpectate(p);
        }
        list = new ArrayList<>(playing);
        for (User p : list) {
            leavingGame(p);
        }
        list = new ArrayList<>(postWaiting);
        for (User p : list) {
            leavingGame(p);
        }
        clearArena();
        state = GameState.INACTIVE;
        if (callEvent) Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
        return null;
    }

    @Override
    public void endByTime() {
        ChatUtils.broadcast(this, "The game has ended because time ran out.");
    }

    @Override
    public String outOfTimeMessage() {
        return "<game> is ending because it ran out of time!";
    }

    @Override
    public String timeLeftMessage() {
        return "<game> has <time> minute(s) left.";
    }

    public void win(final User winner) {
        ChatUtils.broadcast(this, winner.getPlayerName() + " has won the game!");
        for (User u : playing) {
            if (u == winner) {
                Bukkit.getScheduler().runTaskLater(GameLib.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        winner.getPlayer().playSound(winner.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                }
                }, 2);
                getOwningPlugin().getStatManager().get(winner).addWin();
            } else {
                final User loser = u;
                Bukkit.getScheduler().runTaskLater(GameLib.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        loser.getPlayer().playSound(loser.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                    }
                }, 2);
                getOwningPlugin().getStatManager().get(u).addLoss();
            }
        }
        cancelGame();
    }

    @Override
    public synchronized boolean startGame() {
        return startGame(Bukkit.getConsoleSender(), Config.COUNTDOWN.getGlobalInt());
    }

    @Override
    public synchronized boolean startGame(CommandSender cs, int seconds) {
		if (state == GameState.DISABLED) {
            return sendErrorAndReturnFalse(cs, Lang.NOT_ENABLED.getMessage().replace("<game>", name));
        } else if (state == GameState.RUNNING) {
            return sendErrorAndReturnFalse(cs, Lang.RUNNING.getMessage().replace("<game>", name));
        }
		if (playing.size() < 2 || playing.size() < Config.MIN_PLAYERS.getGlobalInt()) {
            return sendErrorAndReturnFalse(cs, String.format("There are not enough players in %s", name));
        }
		if (countdown != null) {
			if (seconds >= countdown.getTimeLeft()) {
                return sendErrorAndReturnFalse(cs, Lang.ALREADY_COUNTING_DOWN.getMessage().replace("<game>", name));
            }
            countdown.cancel();
            countdown = null;
		} else {
            for (User p : playing) {
                joiningArena(p, p.getInfoHandler(SpawnTakenHandler.CREATOR).getSpawnTaken());
            }
            for (User p : playing) {
                p.getPlayer().teleport(p.getPlayer());
            }
        }
        if (seconds > 0) {
            countdown = new GameCountdown(this, seconds, cs, GameCountdown.getDefaultStartingString());
            state = GameState.COUNTING;
            return true;
        }
        GamePreStartEvent event = new GamePreStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cancelGame();
            return sendErrorAndReturnFalse(cs, "Game cancelled by event");
        }
        availableSpawns.clear();
        resetter.init();
        state = GameState.RUNNING;
        for (User p : playing) {
            p.getInfoHandler(LastSpawnHandler.CREATOR).setLastSpawn(System.currentTimeMillis());
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            p.getInfoHandler(GameClassHandler.CREATOR).getActiveClass().grantInitialItems(p);
        }
        this.waterRunner.start();
        
        Bukkit.getPluginManager().callEvent(new GameStartedEvent(this));
        return true;
    }

    private static boolean sendErrorAndReturnFalse(CommandSender cs, String message) {
        ChatUtils.error(cs, message);
        return false;
    }

    @Override
    public boolean saveTo(ConfigSection section) {
		section.set("enabled", state != GameState.DISABLED);
        List<String> arenaNames = new ArrayList<String>();
        for (FArena a : arenas) {
            arenaNames.add(a.getName());
        }
		section.set("arenas", arenaNames);
        if (finishedWarp != null) section.set("finished-warp", GeneralUtils.parseToString(finishedWarp));
        return true;
    }

    @Override
    public boolean loadFrom(ConfigSection section) {
        boolean bad = false;
        finishedWarp = GeneralUtils.parseToLoc(section.getString("finished-warp"));
        arenas.clear();
        for (String s : section.getStringList("arenas")) {
            FArena arena = Flooder.gameMaster().getArena(s);
            if (arena == null) {
                bad = true;
                continue;
            }
            arenas.add(arena);
        }
        setEnabled(section.getBoolean("enabled", true));
        return !bad;
    }

    private boolean preJoinValidation(User player) {
        if (state == GameState.DISABLED) {
            ChatUtils.error(player.getPlayer(), Lang.NOT_ENABLED.getMessage().replace("<game>", name));
            return false;
        }
        if (state == GameState.RUNNING) {
            ChatUtils.error(player.getPlayer(), Lang.RUNNING.getMessage().replace("<game>", name));
            return false;
        }
        if (player.getPlayingType() != User.PlayingType.NONE) {
            ChatUtils.error(player.getPlayer(), Lang.IN_GAME.getMessage().replace("<game>", player.getGame().getName()));
            return false;
        }
        return true;
    }

    private void pickArena() {
        List<FArena> valid = getValidArenas();
        active = valid.get(new Random().nextInt(valid.size()));
        active.setActiveGame(this);
        availableSpawns.addAll(active.spawnpoints);

        Bukkit.getPluginManager().registerEvents(listener, Flooder.getInstance());
    }

    private void clearArena() {
        active.setActiveGame(null);
        active = null;

        HandlerList.unregisterAll(listener);

        for (Chest c : chests) {
            c.getBlockInventory().clear();
        }
        chests.clear();
    }

	public Location getNextOpenSpawnPoint() {
		Random rand = new Random();
		Location loc;
		do {
			loc = active.spawnpoints.get(rand.nextInt(active.spawnpoints.size()));
			if (loc == null) active.spawnpoints.remove(loc);
			
		} while (loc == null || !availableSpawns.remove(loc));
		return loc;
	}
 
    @Override
    protected String checkValid() {
        if (getValidArenas().isEmpty()) return "There are no enabled arenas in game: " + name + "!"; //Must contain at least one valid arena
        if (finishedWarp == null) return "The finished warp is not set!";
        return null;
    }

    // TODO: causes
    public synchronized void playerKilled(User killer, User killed) {
        Player killedPlayer = killed.getPlayer();
        killedPlayer.getWorld().playEffect(killed.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        if (killer != null) {
            killer.getPlayer().playSound(killed.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
        }
        Bukkit.getPluginManager().callEvent(new PlayerKilledEvent(this, killed, killer));
        playing.remove(killed);
        postWaiting.add(killed);
        if (playing.size() == 1) win(playing.iterator().next());
    }

    @Override
    public void setPlayerReady(User user) {
        //ChatUtils.send(user.getPlayer(), "You have been set as ready!");
        playing.add(user);
        Bukkit.getScheduler().runTask(Flooder.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (playing.size() >= Config.MIN_READY.getGlobalInt()) {
                    startGame();
                }
            }
        });
    }

    @Override
    public Flooder getOwningPlugin() {
        return Flooder.getInstance();
    }

    @Override
    public PlayerStat<FGame> newStat(User player) {
        return new PlayerStat<>(this, player);
    }

    @Override
    public boolean allowCommand() {
        return Defaults.Config.ALLOW_COMMAND.getGlobalBoolean();
    }

    @Override
    public int duration() {
        return Defaults.Config.GAME_DURATION.getGlobalInt();
    }

    @Override
    public boolean allowEditing(User user) {
        // TODO: only allow 20
        return state == GameState.RUNNING; 
    }

    public boolean addChest(Chest c) {
        return chests.add(c);
    }

    @Override
    public boolean isFull() {
        return availableSpawns.isEmpty();
    }

    public GameResetter getResetter() {
        return resetter;
    }

    public static GameMasterImpl.GameCreator<FGame> CREATOR = new FGameCreator();
    private static class FGameCreator implements GameMasterImpl.GameCreator<FGame> {
        @Override
        public FGame createGame(String name) {
            return new FGame(name);
        }
    }
}