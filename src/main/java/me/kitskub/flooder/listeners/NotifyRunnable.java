package me.kitskub.flooder.listeners;

import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class NotifyRunnable implements Listener {
    private final FGame game;
    private InnerNotifyRunnable runnable;

    public NotifyRunnable(FGame game) {
        this.game = game;
    }

    public void start() {
        runnable = new InnerNotifyRunnable();
    }

    public void stop() {
        if (runnable != null) {
            runnable.cancel();
        }
        runnable = null;
    }

    private class InnerNotifyRunnable extends BukkitRunnable {
        public InnerNotifyRunnable() {
            runTaskTimer(Flooder.getInstance(), Defaults.Config.NOTIFY_PERIOD.getGlobalInt() * 20, Defaults.Config.NOTIFY_PERIOD.getGlobalInt() * 20);
        }

        @Override
        public void run() {
            for (User u : game.getAllPlayers()) {
                ChatUtils.send(u.getPlayer(), Defaults.Lang.NOTIFY_MESSAGE.getMessage()
                        .replace("<players>", "" + game.getActivePlayers().size())
                        .replace("<max>", "" + game.getActiveArena().spawnpoints.size())
                        .replace("<left>", "" + (Defaults.Config.MIN_READY.getGlobalInt() - game.getActivePlayers().size()))
                );
            }
        }
    }
}
