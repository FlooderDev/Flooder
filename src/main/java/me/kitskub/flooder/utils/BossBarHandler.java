package me.kitskub.flooder.utils;

import me.confuser.barapi.BarAPI;
import me.kitskub.flooder.Logging;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;

public class BossBarHandler {
    private static BarHandler instance;
    public static BarHandler get() {
        if (instance != null) return instance;
        try {
            Class.forName("me.confuser.barapi.BarAPI");
            instance = new BarAPIHandler();
        } catch (ClassNotFoundException e) {
            Logging.warning("BarAPI not found. Cannot use Boss Bar.");
            instance = new NoBarAPIHandler();
        }
        return instance;
        
    } 

    public static interface BarHandler {
        void initForAll(FGame game);

        void remove(User user);

        void updatePercent(FGame game, float percent);
    }

    private static class BarAPIHandler implements BarHandler {
        public void initForAll(FGame game) {
            for (User u : game.getActivePlayers()) {
                BarAPI.setMessage(u.getPlayer(), "Time remaining", 100f);
            }
        }

        public void updatePercent(FGame game, float percent) {
            for (User u : game.getActivePlayers()) {
                BarAPI.setHealth(u.getPlayer(), percent);
            }
        }

        public void remove(User user) {
            BarAPI.removeBar(user.getPlayer());
        }
    }

    private static class NoBarAPIHandler implements BarHandler {
        public void initForAll(FGame game) {}

        public void remove(User user) {}

        public void updatePercent(FGame game, float percent) {}
    }
}
