package me.kitskub.flooder;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.core.FArena;
import me.kitskub.flooder.core.FClass;
import me.kitskub.flooder.core.FGame;
import me.kitskub.flooder.utils.FlooderItem;
import me.kitskub.gamelib.ClassPlugin;
import me.kitskub.gamelib.EffectItemPlugin;
import me.kitskub.gamelib.Games;
import me.kitskub.gamelib.Perm;
import me.kitskub.gamelib.commands.CommandHandler;
import me.kitskub.gamelib.framework.EffectItem;
import me.kitskub.gamelib.framework.GameClass;
import me.kitskub.gamelib.framework.GameMaster;
import me.kitskub.gamelib.framework.Manager;
import me.kitskub.gamelib.framework.impl.FlatFileStatManager;
import me.kitskub.gamelib.framework.impl.GameMasterImpl;
import me.kitskub.gamelib.listeners.general.ArenaProtectionListener;
import me.kitskub.gamelib.listeners.general.InfoSignListener;
import me.kitskub.gamelib.listeners.general.PlayerAutoJoinListener;
import me.kitskub.gamelib.register.GLPermission;
import me.kitskub.gamelib.stats.GlobalPlayerStat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Flooder extends JavaPlugin implements ClassPlugin<FClass, FGame, FArena>, EffectItemPlugin<FGame, FArena> {

    public static final String CMD_ADMIN = "fa", CMD_USER = "f";
    private static Flooder instance;
    private static GameMaster<Flooder, FGame, FArena> gameMaster;
    private static Manager<FClass> classManager;
    private static Manager<EffectItem> effectItemManager;
    private static FlatFileStatManager<GlobalPlayerStat> statManager;
    private static PlayerAutoJoinListener pajListener;
    private static ArenaProtectionListener apListener;
    private final CommandHandler fCH = new CommandHandler(CMD_USER, this);
    private final CommandHandler faCH = new CommandHandler(CMD_ADMIN, this);

    @Override
    public void onLoad() {
        instance = this;
        Logging.init();

        ConfigurationSerialization.registerClass(FClass.class, "FClass");
        ConfigurationSerialization.registerClass(FlooderItem.class, "FlooderItem");
    }

    @Override
    public void onEnable() {
        Files.INSTANCE.loadAll();
        gameMaster = new GameMasterImpl<>(FArena.CREATOR, FGame.CREATOR, Files.ARENAS, Files.GAMES, Perms.ADMIN_EDIT_ARENA);
        classManager = new Manager<>(FClass.class);
        statManager = new FlatFileStatManager<>(GlobalPlayerStat.CREATOR, Files.USERS.getConfig());
        effectItemManager = new Manager<>(EffectItem.class);
        loadManagers();

        registerCommands();
        Logging.info("%s game(s) loaded.", gameMaster.getGames().size());
        Logging.info("%s class(es) loaded: ", classManager.getAll().size());
        for (GameClass c : classManager.getAll()) {
            Logging.info("  " + c.getName());
        }
        Games.register(this);
        pajListener = new PlayerAutoJoinListener(gameMaster.getGames());
        apListener = new ArenaProtectionListener(this);
        Bukkit.getPluginManager().registerEvents(pajListener, this);
        Logging.info("Enabled.");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable() {
        for (FGame game : gameMaster.getGames()) {
            game.cancelGame();
        }

        saveAll();
        Logging.info("Disabled.");
    }

    public static void saveAll() {
        gameMaster.save();
        classManager.saveTo(Files.CLASSES.getConfig());
        statManager.save();
        effectItemManager.saveTo(Files.EFFECT_ITEMS.getConfig());

        Files.INSTANCE.saveAll();
    }

    public static void loadManagers() {
        effectItemManager.loadFrom(Files.EFFECT_ITEMS.getConfig());
        statManager.load();
        classManager.loadFrom(Files.CLASSES.getConfig());
        gameMaster.load();

    }

    public static void reload() {
        Files.INSTANCE.loadAll();
        loadManagers();
    }

    private static void registerCommands() {
        instance.getCommand(CMD_USER).setExecutor(instance.fCH);
        instance.getCommand(CMD_ADMIN).setExecutor(instance.faCH);
        for (Perm p : Perms.values()) {
            Permission permission = p.getPermission();
            if (p.getParent() != null) {
                permission.addParent(p.getParent().getPermission(), true);
            }
        }
        Commands.init();
    }

    public static boolean hasPermission(CommandSender cs, Perm perm) {
        return GLPermission.hasPermission(cs, perm);
    }

    public static Flooder getInstance() {
        return instance;
    }

    public static boolean checkPermission(CommandSender cs, Perm perm) {
        if (!Flooder.hasPermission(cs, perm)) {
            cs.sendMessage(ChatColor.RED + Lang.NO_PERM.getMessage());
            return false;
        }
        return true;
    }

    public static GameMaster<Flooder, FGame, FArena> gameMaster() {
        return gameMaster;
    }

    public static Manager<FClass> classManager() {
        return classManager;
    }

    public GameMaster<Flooder, FGame, FArena> getGameMaster() {
        return gameMaster;
    }

    public Manager<FClass> getClassManager() {
        return classManager;
    }

    public FlatFileStatManager getStatManager() {
        return statManager;
    }

    public InfoSignListener getInfoSignListener() {
        return null;
    }

    @Override
    public Manager<EffectItem> getEffectItemManager() {
        return effectItemManager;
    }

    public Plugin getPlugin() {
        return this;
    }

    public static CommandHandler fCH() {
        return getInstance().fCH;
    }

    public static CommandHandler faCH() {
        return getInstance().faCH;
    }

    public static void connectToServer(String serverName, Player p) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        p.sendPluginMessage(Flooder.getInstance(), "BungeeCord", out.toByteArray());
    }
}
