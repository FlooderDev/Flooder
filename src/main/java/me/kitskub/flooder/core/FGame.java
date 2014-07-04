package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.listeners.FGameListener;
import me.kitskub.flooder.reset.GameResetter;
import me.kitskub.flooder.utils.BossBarHandler;
import me.kitskub.gamelib.GameCountdown;
import me.kitskub.gamelib.GameLib;
import me.kitskub.gamelib.GameRewardManager;
import me.kitskub.gamelib.api.event.GameEndEvent;
import me.kitskub.gamelib.api.event.GamePreStartEvent;
import me.kitskub.gamelib.api.event.GameStartedEvent;
import me.kitskub.gamelib.api.event.PlayerJoinGameEvent;
import me.kitskub.gamelib.api.event.PlayerKilledEvent;
import me.kitskub.gamelib.framework.Arena.ArenaState;
import me.kitskub.gamelib.framework.Game.GameState;
import me.kitskub.gamelib.framework.TimedGame;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.impl.GameMasterImpl;
import me.kitskub.gamelib.games.DataSave;
import me.kitskub.gamelib.stats.PlayerStat;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.GeneralUtils;
import me.kitskub.gamelib.utils.config.ConfigSection;
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


public class FGame implements TimedGame<Flooder, FGame, FArena> {
    private final String name;
    private final List<FArena> arenas;
    public Location finishedWarp;

    private FArena active;
    private GameState state;
    private GameCountdown countdown;
    // Players
    private final Set<User> players;
    private final Set<User> spectating;
	private final Map<String, Location> spawnsTaken;
    //Listeners
    private final FGameListener listener;
    private final GameResetter resetter;
    //Temp
    private final Set<Chest> chests;

    public FGame(String name) {
        this.name = name;
        this.arenas = new ArrayList<FArena>();
        this.active = null;
        this.state = GameState.DISABLED;
        this.countdown = null;

        this.players = new HashSet<User>();
        this.spectating = new HashSet<User>();
        this.spawnsTaken = new HashMap<String, Location>();

        this.listener = new FGameListener(this);
        this.resetter = new GameResetter(this);

        this.chests = new HashSet<Chest>();
    }

    public String getName() {
        return name;
    }

    public boolean setEnabled(boolean flag) {
        return setEnabled(flag, Bukkit.getConsoleSender());
    }

    public synchronized boolean setEnabled(boolean flag, CommandSender cs) {
        if (!flag) {
            if (state == GameState.RUNNING) cancelGame();
            state = GameState.DISABLED;
        }
        if (flag && state == GameState.DISABLED) {
            String valid = checkValid();
            if (valid == null) state = GameState.INACTIVE;
            else {
                ChatUtils.error(cs, valid + " Cannot enable!");
                return false;
            }
        }
        return true;
    }
    
    public void addArena(FArena a) {
        arenas.add(a);
        validate();
    }

    public Set<FArena> getArenas() {
        return new HashSet<FArena>(arenas);
    }

    public FArena getActiveArena() {
        return active;
    }

    public synchronized void join(User player) {
        if (!preJoinValidation(player)) {
            return;
        }
        player.setGame(this, User.GameEntry.Type.PLAYING);
        if (players.isEmpty()) {
            state = GameState.WAITING;
            pickArena();
        }
	    boolean canceled = false;
	    if (spawnsTaken.size() >= active.spawnpoints.size()) {
		    ChatUtils.error(player.getPlayer(), "%s is already full.", name);
		    canceled = true;
	    }
	    PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, player);
	    if (!canceled) Bukkit.getPluginManager().callEvent(event);
	    if (canceled || event.isCancelled()) {
		    if (players.isEmpty()) clearArena();
		    state = GameState.INACTIVE;
		    return;
	    }
	    Location loc = getNextOpenSpawnPoint();
	    spawnsTaken.put(player.getPlayerName(), loc);
        DataSave.saveDataWithInvClear(player);
        player.setClassRaw(FClass.blank);
        Bukkit.getPluginManager().callEvent(new PlayerJoinGameEvent(this, player));
    }

    public synchronized void leave(User player) {
        if (!leavingGame(player)) return;
        if (players.isEmpty()) {
            cancelGame();
            return;
        }
        if (state == GameState.RUNNING) {
            win(players.iterator().next());
        }
    }

    private synchronized boolean leavingGame(User player) {
        if (!players.remove(player)) throw new IllegalStateException("Player is not in game!");
        player.setGame(null, User.GameEntry.Type.NONE);
        player.setClass(null);
        if (player.getPlayer().isOnline()) player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.getPlayer().removePotionEffect(PotionEffectType.JUMP);
        BossBarHandler.get().remove(player);
        leavingArena(player);
        return true;
    }

    public synchronized void spectate(User player) {
        if (state == GameState.INACTIVE || state == GameState.DISABLED) {
            ChatUtils.error(player.getPlayer(), "There is no one in that game!");
            return;
        }
        player.setGame(this, User.GameEntry.Type.SPECTATING);
        spectating.add(player);
        joiningArena(player, active.specWarp);
    }

    public void leaveSpectate(User player) {
        player.leaveGame();
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

    public Set<User> getActivePlayers() {
        return new HashSet<User>(players);
    }

    public Set<User> getSpectatingPlayers() {
        return new HashSet<User>(spectating);
    }

    public Set<User> getAllPlayers() {
        HashSet<User> set = new HashSet<User>(players);
        set.addAll(spectating);
        return set;
    }

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
        if (state == GameState.RUNNING) {
            Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
            resetter.resetChanges();
        }
        list = new ArrayList<User>(spectating);
        for (User p : list) {
            leaveSpectate(p);
        }
        list = new ArrayList<User>(players);
        for (User p : list) {
            leavingGame(p);
        }
        state = GameState.INACTIVE;
        clearArena();
        return null;
    }

    public void endByTime() {
        ChatUtils.broadcast(this, "The game has ended because time ran out.");
    }

    public void win(final User winner) {
        ChatUtils.broadcast(this, winner.getPlayerName() + " has won the game!");
        for (User u : players) {
            if (u == winner) {
                Bukkit.getScheduler().runTaskLater(GameLib.getInstance(), new Runnable() {
                    public void run() {
                        winner.getPlayer().playSound(winner.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                        GameRewardManager.rewardWin(winner);
                }
                }, 2);
                getOwningPlugin().getStatManager().get(winner).addWin();
                getOwningPlugin().getStatManager().get(winner).addPoints(pointsFromWin());
            } else {
                final User loser = u;
                Bukkit.getScheduler().runTaskLater(GameLib.getInstance(), new Runnable() {
                    public void run() {
                        loser.getPlayer().playSound(loser.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                        GameRewardManager.rewardLoss(loser);
                    }
                }, 2);
                getOwningPlugin().getStatManager().get(u).addLoss();
            }
        }
        cancelGame();
    }

    public synchronized boolean startGame() {
        return startGame(Bukkit.getConsoleSender(), Config.COUNTDOWN.getGlobalInt());
    }

    public synchronized boolean startGame(CommandSender cs, int seconds) {
		if (state == GameState.DISABLED) {
            return sendErrorAndReturnFalse(cs, Lang.NOT_ENABLED.getMessage().replace("<game>", name));
        } else if (state == GameState.RUNNING) {
            return sendErrorAndReturnFalse(cs, Lang.RUNNING.getMessage().replace("<game>", name));
        }
		if (players.size() < 2 || players.size() < Config.MIN_PLAYERS.getGlobalInt()) {
            return sendErrorAndReturnFalse(cs, String.format("There are not enough players in %s", name));
        }
		if (countdown != null) {
			if (seconds >= countdown.getTimeLeft()) {
                return sendErrorAndReturnFalse(cs, Lang.ALREADY_COUNTING_DOWN.getMessage().replace("<game>", name));
            }
            countdown.cancel();
            countdown = null;
		} else {
            for (User p : players) {
                joiningArena(p, spawnsTaken.get(p.getPlayerName()));
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
            return sendErrorAndReturnFalse(cs, "Game cancelled by event");
        }
        resetter.init();
        state = GameState.RUNNING;
        for (User p : players) {
            p.setLastSpawn(System.currentTimeMillis());
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
        }
        BossBarHandler.get().initForAll(this);

        Bukkit.getPluginManager().callEvent(new GameStartedEvent(this));
        return true;
    }

    private static boolean sendErrorAndReturnFalse(CommandSender cs, String message) {
        ChatUtils.error(cs, message);
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FGame other = (FGame) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

	@Override
	public String getInfo() {
		return String.format("%s - Enabled: %b", name, state != GameState.DISABLED);
	}
    
    @Override
	public void listStats(CommandSender cs) {
        if (state != GameState.RUNNING) {
            ChatUtils.send(cs, "State: " + state.name());
            return;
        }
        // TODO better stats
	}

    public synchronized GameState getState() {
        return state;
    }

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
            ChatUtils.error(player.getPlayer(), Lang.RUNNING.getMessage().replace("<item>", name));
            return false;
        }
        if (player.getGameEntry().getType() != User.GameEntry.Type.NONE) {
            ChatUtils.error(player.getPlayer(), Lang.IN_GAME.getMessage().replace("<game>", player.getGameEntry().getGame().getName()));
            return false;
        }
        return true;
    }

    private void pickArena() {
        List<FArena> valid = getValidArenas();
        active = valid.get(new Random().nextInt(valid.size()));
        active.setActiveGame(this);

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
			
		} while (loc == null || spawnsTaken.containsValue(loc));
		return loc;
	}

    private List<FArena> getValidArenas() {
        List<FArena> validArenas = new ArrayList<FArena>();
        for (FArena a : arenas) {
            if (a.getState() != ArenaState.DISABLED) validArenas.add(a);
        }
        return validArenas;
    }

    public String validate() {
        String valid = checkValid();
        if (valid == null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        return valid;
    }
    
    private String checkValid() {
        if (getValidArenas().isEmpty()) return "There are no enabled arenas in game: " + name + "!"; //Must contain at least one valid arena
        if (finishedWarp == null) return "The finished warp is not set!";
        return null;
    }

    public synchronized void playerKilled(User killer, User killed) {
        Player killedPlayer = killed.getPlayer();
        killedPlayer.getWorld().playEffect(killed.getPlayer().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        if (killer != null) {
            killer.getPlayer().playSound(killed.getPlayer().getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
        }
        leave(killed);
        Bukkit.getPluginManager().callEvent(new PlayerKilledEvent(this, killed, killer));
    }

    public void setPlayerReady(User user) {
        ChatUtils.send(user.getPlayer(), "You have been set as ready!");
        players.add(user);
        if (players.size() >= Config.MIN_READY.getGlobalInt()) {
            startGame();
        }
    }

    public Flooder getOwningPlugin() {
        return Flooder.getInstance();
    }

    public PlayerStat<FGame> newStat(User player) {
        return new PlayerStat<FGame>(this, player);
    }

    public boolean allowCommand() {
        return Defaults.Config.ALLOW_COMMAND.getGlobalBoolean();
    }

    public int pointsToWin() {
        return -1;
    }

    public int pointsFromWin() {
        return 0;
    }

    public int duration() {
        return Defaults.Config.GAME_DURATION.getGlobalInt();
    }

    public int pointPollInterval() {
        return 0;
    }

    @Override
    public boolean allowEditing(User user) {
        // TODO: only allow 20
        return state == GameState.RUNNING; 
    }

    public boolean addChest(Chest c) {
        return chests.add(c);
    }

    public boolean isFull() {
        return active != null && spawnsTaken.size() >= active.spawnpoints.size();
    }

    public Location getSpawnsTaken(User user) {
        return spawnsTaken.get(user.getPlayerName());
    }

    public static GameMasterImpl.GameCreator<FGame> CREATOR = new PBGameCreator();
    private static class PBGameCreator implements GameMasterImpl.GameCreator<FGame> {
        public FGame createGame(String name) {
            return new FGame(name);
        }
    }
}