package me.kitskub.flooder;

import me.kitskub.gamelib.PluginFileManager;

public class Files extends PluginFileManager {
    public static final Files INSTANCE = new Files();
    public static final PluginFile ITEMCONFIG = INSTANCE.createAndRegister("itemconfig.yml", FileType.YML, true);
    public static final PluginFile CONFIG = INSTANCE.createAndRegister("config.yml", FileType.YML, true);
    public static final PluginFile LANG = INSTANCE.createAndRegister("lang.yml", FileType.YML, true);
    public static final PluginFile USERS = INSTANCE.createAndRegister("users.yml", FileType.YML, false);
    public static final PluginFile CLASSES = INSTANCE.createAndRegister("classes.yml", FileType.YML, false);
    public static final PluginFile EFFECT_ITEMS = INSTANCE.createAndRegister("effectitems.yml", FileType.YML, false);
    public static final PluginFile ARENAS = INSTANCE.createAndRegister("arenas.yml", FileType.YML, false);
    public static final PluginFile GAMES = INSTANCE.createAndRegister("games.yml", FileType.YML, false);
    public static final PluginFile SIGNS = INSTANCE.createAndRegister("signs.yml", FileType.YML, false);

    public Files() {
        super(Flooder.getInstance());
    }

}
