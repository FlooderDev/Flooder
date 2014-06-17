package me.kitskub.flooder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.kitskub.gamelib.Perm;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.flooder.commands.admin.AddarenaCommand;
import me.kitskub.flooder.commands.admin.CheckCommand;
import me.kitskub.flooder.commands.admin.ForceLeaveCommand;
import me.kitskub.flooder.commands.admin.KickCommand;
import me.kitskub.flooder.commands.admin.ReloadCommand;
import me.kitskub.flooder.commands.admin.StartCommand;
import me.kitskub.flooder.commands.admin.StopCommand;
import me.kitskub.flooder.commands.admin.add.AddArenaCommand;
import me.kitskub.flooder.commands.admin.add.AddClassSignCommand;
import me.kitskub.flooder.commands.admin.add.AddGameCommand;
import me.kitskub.flooder.commands.admin.add.AddHelp;
import me.kitskub.flooder.commands.admin.add.AddSpawnPointCommand;
import me.kitskub.flooder.commands.admin.remove.RemoveHelp;
import me.kitskub.flooder.commands.admin.remove.RemoveSignCommand;
import me.kitskub.flooder.commands.admin.set.SetCuboidCommand;
import me.kitskub.flooder.commands.admin.set.SetEnabledCommand;
import me.kitskub.flooder.commands.admin.set.SetHelp;
import me.kitskub.flooder.commands.admin.set.SetWarpCommand;
import me.kitskub.flooder.commands.admin.set.SetZoneCommand;
import me.kitskub.flooder.commands.user.AboutCommand;
import me.kitskub.flooder.commands.user.ClassCommand;
import me.kitskub.flooder.commands.user.JoinCommand;
import me.kitskub.flooder.commands.user.ListCommand;
import me.kitskub.flooder.commands.user.QuitCommand;
import me.kitskub.flooder.commands.user.SpectateCommand;
import me.kitskub.flooder.commands.user.StatCommand;
import me.kitskub.flooder.commands.user.SubscribeCommand;
import org.bukkit.permissions.Permission;

public class Defaults {
	
    public enum Lang {
	
	JOIN("<player> has joined the game <game>."),
	REJOIN("<player> has rejoined the game <gam"),
	LEAVE("<player> has left the game <game>."),
	QUIT("<player> has quit the game <game>."),
	KILL("<killer> shot <killed> in game <game>."),
	DEATH("<player> died in <game>"),
	NO_PERM("You do not have permission."),
	NO_WINNER("You do not have permission."),
	WIN("You do not have permission."),
	ALREADY_COUNTING_DOWN("<game> is already counting down."),
	NOT_ENABLED("<game> is currently not enabled."),
	NOT_RUNNING("<game> is not running."),
	NOT_EXIST("<item> does not exist."),
	RUNNING("<game> is already running."),
	IN_GAME("You are in <game>."),
	NOT_IN_GAME("You are not in a game.");
	
	
	private String value;
	
	private Lang(String message) {
	    this.value = message;
	}
	
	public String getMessage(){
	    return value;
	}
    }

    public enum Config {

        DEFAULT_GAME("Flooder", "default-game"),
        MIN_READY(5, "min-ready"),
        MIN_PLAYERS(2, "min-players"),
        GAME_DURATION(10, "game-duration"),
        ALLOW_COMMAND(false, "allow-command"),
        COUNTDOWN(10, "countdown"),
        ALLOWED_COMMANDS(new ArrayList<String>(), "allowed-commands"),
        GRACE_PERIOD(5, "grace-period"),
        MONGODB_SERVER("127.0.0.1", "mongodb.server");
	
	
	private Object value;
	private String option;

	private Config(Object message, String option) {
	    this.value = message;
	    this.option = option;
	}

	public boolean getGlobalBoolean(){
		return Files.CONFIG.getConfig().getBoolean("global." + option, (Boolean) value);
	}

	public int getGlobalInt(){
		return Files.CONFIG.getConfig().getInt("global." + option, (Integer) value);
	}

	public double getGlobalDouble(){
		return Files.CONFIG.getConfig().getDouble("global." + option, (Double) value);
	}

	public String getGlobalString(){
		return Files.CONFIG.getConfig().getString("global." + option, (String) value);
	}

	@SuppressWarnings("unchecked")
	public List<String> getGlobalStringList() {
        return Files.CONFIG.getConfig().getStringList("global." + option, (List<String>) value);
	}
	/** 
	 * For safe recursiveness 
	 * return boolean if found, null if not
	 */
	private Boolean getBoolean(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().contains("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getBoolean("setups." + setup + "." + option);
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getStringList("setups." + setup + ".inherits")) {
			Boolean b = getBoolean(parent, checked);
			if (b != null) return b;
		}
		return null;
	}
	public boolean getBoolean(String setup) {
		Boolean b = getBoolean(setup, new HashSet<String>());
		return b == null ? getGlobalBoolean() : b;
	}
	
	/** 
	 * For safe recursiveness 
	 * return String if found, null if not
	 */	
	private String getString(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().contains("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getString("setups." + setup + "." + option);
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getStringList("setups." + setup + ".inherits")) {
			String s = getString(parent, checked);
			if (s != null) return s;
		}
		return null;
	}
	public String getString(String setup) {
		String s = getString(setup, new HashSet<String>());
		return s == null ? getGlobalString() : s;
	}
	
	/** 
	 * For safe recursiveness 
	 * return Integer if found, null if not
	 */
	private Integer getInt(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().contains("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getInt("setups." + setup + "." + option);
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getStringList("setups." + setup + ".inherits")) {
			Integer i = getInt(parent, checked);
			if (i != null) return i;
		}
		return null;
	}
	public int getInt(String setup) {
		Integer i = getInt(setup, new HashSet<String>());
		return i == null ? getGlobalInt() : i;
	}
	
	/** 
	 * For safe recursiveness 
	 * return Integer if found, null if not
	 */
	private Double getDouble(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().contains("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getDouble("setups." + setup + "." + option);
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getStringList("setups." + setup + ".inherits")) {
			Double d = getDouble(parent, checked);
			if (d != null) return d;
		}
		return null;
	}
	public double getDouble(String setup) {
		Double d = getDouble(setup, new HashSet<String>());
		return d == null ? getGlobalDouble() : d;
	}
	
	/** 
	 * For safe recursiveness 
	 * return List if found, null if not
	 */
	private List<String> getStringList(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		List<String> strings = new ArrayList<String>();
		if (Files.CONFIG.getConfig().contains("setups." + setup + "." + option)) {
			strings.addAll(Files.CONFIG.getConfig().getStringList("setups." + setup + "." + option));
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getStringList("setups." + setup + ".inherits")) {
			List<String> list = getStringList(parent, checked);
			if (list != null) strings.addAll(list);
		}
		return strings;
	}
	
	/* returns combination of all lists, including global */
	public List<String> getStringList(String setup) {
		List<String> list = getStringList(setup, new HashSet<String>());
		return list == null ? getGlobalStringList() : list;
	}
    }
    
    

    public enum Perms implements Perm {
        
		ALL(new Permission("flooder"), null, "gives the player all permissions"),
		ADMIN(new Permission("flooder.admin"), ALL, "gives the player all admin permissions"),
		ADMIN_ALLOW_FLIGHT(new Permission("flooder.admin.allowflight"), ADMIN, "allows the player to fly in game"),
		ADMIN_ADDARENA(new Permission("flooder.admin.addarena"), ADMIN),
		ADMIN_ADD_ARENA(new Permission("flooder.add.arena"), ADMIN),
		ADMIN_ADD_CLASS(new Permission("flooder.add.class"), ADMIN),
		ADMIN_ADD_GAME(new Permission("flooder.add.game"), ADMIN),
		ADMIN_ADD_GAME_SIGN(new Permission("flooder.add.gamesign"), ADMIN),
		ADMIN_ADD_HELP(new Permission("flooder.add.help"), ADMIN, "allows the player to view add help page"),
		ADMIN_ADD_INFO_WALL(new Permission("flooder.add.infowall"), ADMIN),
		ADMIN_ADD_JOIN_SIGN(new Permission("flooder.add.joinsign"), ADMIN),
		ADMIN_SET_READY_BLOCK(new Permission("flooder.add.readyblock"), ADMIN),
		ADMIN_ADD_CLASS_SIGN(new Permission("flooder.add.classsign"), ADMIN),
		ADMIN_ADD_SPONSOR_LOOT(new Permission("flooder.add.sponsorloot"), ADMIN),
		ADMIN_ADD_SPAWNPOINT(new Permission("flooder.add.spawnpoint"), ADMIN),
		ADMIN_ADD_ITEMSPAWN(new Permission("flooder.add.itemspawn"), ADMIN),
		ADMIN_CHECK(new Permission("flooder.admin.check"), ADMIN, "Allows an admin to chat to a game by typing \"hg\" in front of their message"),
		ADMIN_EDIT_ARENA(new Permission("flooder.admin.editarena"), ADMIN, "Allows an admin to edit an arena"),
		ADMIN_CHAT(new Permission("flooder.admin.chat"), ADMIN, "Allows an admin to chat to a game by typing \"hg\" in front of their message"),
		ADMIN_CREATE_SIGN(new Permission("flooder.create.sign"), ADMIN, "Allows player to create a sign listener"),
		ADMIN_CREATE_SIGN_GAMEEND(new Permission("flooder.create.sign.gameend"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_GAMEPAUSE(new Permission("flooder.create.sign.gamepause"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_GAMESTART(new Permission("flooder.create.sign.gamestart"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERJOIN(new Permission("flooder.create.sign.playerjoin"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERKICK(new Permission("flooder.create.sign.playerkick"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERKILL(new Permission("flooder.create.sign.playerkill"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERLEAVE(new Permission("flooder.create.sign.playerleave"), ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERQUIT(new Permission("flooder.create.sign.playerquit"), ADMIN_CREATE_SIGN),
		ADMIN_REMOVE_HELP(new Permission("flooder.remove.help"), ADMIN, "allows the player to view remove help page"),
		ADMIN_REMOVE_SPAWNPOINT(new Permission("flooder.remove.spawnpoint"), ADMIN),
		ADMIN_REMOVE_CHEST(new Permission("flooder.remove.chest"), ADMIN),
		ADMIN_REMOVE_GAME(new Permission("flooder.remove.game"), ADMIN),
		ADMIN_REMOVE_ITEMSET(new Permission("flooder.remove.itemset"), ADMIN),
		ADMIN_REMOVE_SIGN(new Permission("flooder.remove.sign"), ADMIN),
        ADMIN_SET_HELP(new Permission("flooder.set.help"), ADMIN, "allows the player to view set help page"),
        ADMIN_SET_CUBOID(new Permission("flooder.set.cuboid"), ADMIN),
		ADMIN_SET_ENABLED(new Permission("flooder.set.enabled"), ADMIN),
		ADMIN_SET_FIXED_CHEST(new Permission("flooder.set.fixedchest"), ADMIN),
		ADMIN_SET_SPAWN(new Permission("flooder.set.spawn"), ADMIN),
		ADMIN_SET_SKILL(new Permission("flooder.set.skill"), ADMIN),
		ADMIN_SET_TEAMSPAWN(new Permission("flooder.set.teamspawn"), ADMIN),
		ADMIN_SET_WARP(new Permission("flooder.set.warp"), ADMIN),
		ADMIN_SET_ZONE(new Permission("flooder.set.zone"), ADMIN),
		ADMIN_FORCE_CLEAR(new Permission("flooder.game.forceclear"), ADMIN),
		ADMIN_STOP(new Permission("flooder.game.stop"), ADMIN),
		ADMIN_START(new Permission("flooder.game.start"), ADMIN),
		ADMIN_PAUSE(new Permission("flooder.game.pause"), ADMIN),
		ADMIN_RESUME(new Permission("flooder.game.resume"), ADMIN),
		ADMIN_RELOAD(new Permission("flooder.admin.reload"), ADMIN),
		ADMIN_KICK(new Permission("flooder.admin.kick"), ADMIN),
		ADMIN_KILL(new Permission("flooder.admin.kill"), ADMIN),
		ADMIN_HELP(new Permission("flooder.admin.help"), ADMIN, "allows a player to view admin commands"),
		ADMIN_RESTOCK(new Permission("flooder.admin.restock"), ADMIN),
		USER(new Permission("flooder.user"), ALL),
		USER_AUTO_SUBSCIBE(new Permission("flooder.user.autosubscribe"), null, "whether a user autosubscribes to a game or not; is not inherited from *"),
		USER_AUTO_JOIN_ALLOWED(new Permission("flooder.user.autojoinallowed"), USER, "whether a user can autojoin games; can also have flooder.user.autojoinallowed.<game>"),
		USER_ABOUT(new Permission("flooder.user.about"), USER),
		USER_BACK(new Permission("flooder.user.back"), USER),
		USER_CLASS(new Permission("flooder.user.class"), USER),
		USER_JOIN(new Permission("flooder.user.join"), USER),
		USER_KIT(new Permission("flooder.user.kit"), null, "whether a user gets all kits on start; can also add specific kits with flooder.user.kit.<kit>"),
		USER_LEAVE(new Permission("flooder.user.leave"), USER),
		USER_LIST(new Permission("flooder.user.list"), USER),
		USER_REJOIN(new Permission("flooder.user.rejoin"), USER),
		USER_SEARCH(new Permission("flooder.user.search"), USER),
		USER_SPECTATE(new Permission("flooder.user.spectate"), USER),
		USER_SPONSOR(new Permission("flooder.user.sponsor"), USER),
		USER_SUBSCRIBE(new Permission("flooder.user.subscribe"), USER),
		USER_TEAM(new Permission("flooder.user.team"), USER),
		USER_VOTE(new Permission("flooder.user.vote"), USER),
		USER_STAT(new Permission("flooder.user.stat"), USER),
		USER_HELP(new Permission("flooder.user.help"), USER, "allows a player to view user commands"),
		USER_QUIT(new Permission("flooder.user.quit"), USER);

        private Permission value;
        private Perm parent;
        private String info;

        private Perms(Permission permission, Perm parent) {
            this.value = permission;
            this.parent = parent;
        }

        private Perms(Permission permission, Perm parent, String info) {
            this.value = permission;
            this.parent = parent;
            this.info = info;
        }

        public Permission getPermission() {
            return value;
        }

        public Perm getParent() {
            return parent;
        }

        public String getInfo() {
            return info;
        }
    }
    
    public enum Commands {	
	    
	ADMIN_ADD_HELP(new AddHelp()),
	ADMIN_ADD_ARENA(new AddArenaCommand()),
	ADMIN_ADD_CLASS_SIGN(new AddClassSignCommand()),
	ADMIN_ADD_GAME(new AddGameCommand()),
	ADMIN_ADD_SPAWNPOINT(new AddSpawnPointCommand()),
	ADMIN_REMOVE_HELP(new RemoveHelp()),
	ADMIN_REMOVE_SIGN(new RemoveSignCommand()),
	ADMIN_SET_HELP(new SetHelp()),
    ADMIN_SET_CUBOID(new SetCuboidCommand()),
	ADMIN_SET_ENABLED(new SetEnabledCommand()),
	ADMIN_SET_WARP(new SetWarpCommand()),
    ADMIN_SET_ZONE(new SetZoneCommand()),
	ADMIN_ADDARENA(new AddarenaCommand()),
	ADMIN_CHECK(new CheckCommand()),
	ADMIN_START(new StartCommand()),
	ADMIN_STOP(new StopCommand()),
	ADMIN_RELOAD(new ReloadCommand()),
	ADMIN_KICK(new KickCommand()),
	ADMIN_FORCE_LEAVE(new ForceLeaveCommand()),
	USER_ABOUT(new AboutCommand()),
	USER_CLASS(new ClassCommand()),
	USER_JOIN(new JoinCommand()),
	USER_LIST(new ListCommand()),
	USER_QUIT(new QuitCommand()),
	USER_SPECTATE(new SpectateCommand()),
	USER_STAT(new StatCommand()),
	USER_SUBSCRIBE(new SubscribeCommand());
	
	private Command command;
	
	private Commands(Command command) {
		this.command = command;
	}
	
	public 	Command getCommand() {
		return command;
	}
	
	public static void init() {} // Just so the class gets loaded
    }

}