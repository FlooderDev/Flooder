package me.kitskub.flooder.core.infohandler;

import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.framework.Game;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.infohandler.GameInfoHandler;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class WaterHurtHandler extends GameInfoHandler {
    private HurtRunnable runnable;

    public WaterHurtHandler(User user) {
        super(user);
    }

    public void start() {
        if (runnable == null) {
            runnable = new HurtRunnable();
            runnable.runTaskTimer(Flooder.getInstance(), 0, 20);
        }
    }

    public static boolean inWater(User user) {
        Block lower = user.getPlayer().getLocation().getBlock();
        Block upper = user.getPlayer().getLocation().add(0, 1, 0).getBlock();
        if (lower.getType() == Material.WATER || lower.getType() == Material.STATIONARY_WATER || upper.getType() == Material.WATER || upper.getType() == Material.STATIONARY_WATER) return true;
        return false;
    }

    private class HurtRunnable extends BukkitRunnable {

        @Override
        public void run() {
            if (!inWater(user) || user.getGame() == null || user.getGame().getState() != Game.GameState.RUNNING) {
                cancel();
                runnable = null;
            }
            double health = user.getPlayer().getHealth();
            health -= 4;
            if (health <= 0) {
                health = 0;
                ChatUtils.broadcast(user.getGame(), Defaults.Lang.DIEDFROMWATER.getMessage().replace("<player>", user.getDisplayName()));
            }
            user.getPlayer().setHealth(health);
        }
         
    }

    public static final GameInfoHandlerCreator<WaterHurtHandler> CREATOR = new WaterHurtHandlerCreator();
    private static class WaterHurtHandlerCreator implements GameInfoHandler.GameInfoHandlerCreator<WaterHurtHandler> {
        @Override
        public Class<WaterHurtHandler> getHandlerClass() {
            return WaterHurtHandler.class;
        }

        @Override
        public WaterHurtHandler createNew(User user) {
            return new WaterHurtHandler(user);
        }
    }
}
