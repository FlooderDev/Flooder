package me.kitskub.flooder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    private static final Logger logger = Flooder.getInstance().getLogger();

    public static void init() {}

	public static void log(Level level, String record) {
		logger.log(level, record);
	}

	public static void log(Level level, String record, String... strings) {
		logger.log(level, record, strings);
	}

    private static String getLogPrefix0() {
		return String.format("[%s] ", Flooder.getInstance().getName());
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
