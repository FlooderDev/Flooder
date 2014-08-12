package me.kitskub.flooder.utils;

import me.confuser.barapi.BarAPI;
import me.kitskub.flooder.Logging;
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
        void remove(User user);

        void updatePercent(User u , float percent);
    }

    private static class BarAPIHandler implements BarHandler {
        public void updatePercent(User u, float percent) {
            BarAPI.setMessage(u.getPlayer(), "Time Remaining", percent);
        }

        public void remove(User user) {
            BarAPI.removeBar(user.getPlayer());
        }
    }

    private static class NoBarAPIHandler implements BarHandler {
        public void remove(User user) {}

        public void updatePercent(User u, float percent) {}
    }
}
