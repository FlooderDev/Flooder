package me.kitskub.flooder;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.kitskub.gamelib.Log;
import me.kitskub.gamelib.SingleLineFormatter;

public class Logging extends Log {
	private static final Logger logger = Logger.getLogger("Flooder");
    protected static final Logging INSTANCE = new Logging();

    private Logging() {
        super(Flooder.getInstance());
    }

    public static void init() {}

	public static void log(Level level, String record) {
		logger.log(level, record);		
	}

	public static void log(Level level, String record, String... strings) {
		logger.log(level, record, strings);		
	}

	static {
		try {
			Flooder instance = Flooder.getInstance();
			instance.getDataFolder().mkdirs();
			File file = Files.LOG.getFile();
			FileHandler handler = new FileHandler(file.getPath(), true);
			handler.setFormatter(new SingleLineFormatter());
			logger.addHandler(handler);
			logger.setLevel(Level.FINEST);
			Logger parent = Logger.getLogger("Minecraft");
			logger.setParent(parent);
		} catch (IOException ex) {
		}

	}

    @Override
    public Logger getLogger() {
        return logger;
    }

	@Override
    public String getLogPrefix() {
        return getLogPrefix0();
	}
    
    private static String getLogPrefix0() {
		return String.format("[%s] %s - ", Flooder.getInstance().getName(), Flooder.getInstance().getDescription().getVersion());
    }

	public static void info(String format, Object... args) {
		log(Level.INFO, getLogPrefix0() + String.format(format, args));
	}

	public static void info(String mess) {
		log(Level.INFO, getLogPrefix0() + mess);
	}

	public static void warning(String format, Object... args) {
		log(Level.WARNING, getLogPrefix0() + String.format(format, args));
	}

	public static void warning(String mess) {
		log(Level.WARNING, getLogPrefix0() + mess);
	}

	public static void severe(String format, Object... args) {
		log(Level.SEVERE, getLogPrefix0() + String.format(format, args));
	}

	public static void severe(String mess) {
		log(Level.SEVERE, getLogPrefix0() + mess);
	}

	public static void debug(String mess, Object... args) {
		log(Level.FINEST, getLogPrefix0() + String.format(mess, args));
	}

	public static void debug(String mess) {
		log(Level.FINEST, getLogPrefix0() + mess);
	}
}
