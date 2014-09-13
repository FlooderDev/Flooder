package me.kitskub.flooder.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AcidRainRunner {
    private final FGame game;
    private Runner runner;

    public AcidRainRunner(FGame game) {
        this.game = game;
    }

    public void start() {
        runner = new Runner();
        runner.runTaskTimer(Flooder.getInstance(), 10 * 20, 15 * 20);
    }

    public void stop() {
        runner.cancel();
        runner = null;
    }

    private class Runner extends BukkitRunnable {
        private List<User> remaining;

        @Override
        public void run() {
            final Random rand = new Random();
            if (remaining.isEmpty()) {
                remaining = new ArrayList<>(game.getActivePlayers());
            }
            final User next = remaining.remove(rand.nextInt(remaining.size()));
            if (rand.nextFloat() < .5) return;
            Player p = next.getPlayer();
            p.setPlayerWeather(WeatherType.DOWNFALL);
            Bukkit.getScheduler().runTaskTimer(Flooder.getInstance(), new BukkitRunnable() {
                private int timesLeft = 4;
                @Override
                public void run() {
                    double damage = 2 * (1 + rand.nextFloat());
                    double health = next.getPlayer().getHealth();
                    if (--timesLeft == 0 || next.getGame() != game || health - damage <= 0) {
                        cancel();
                        next.getPlayer().setPlayerWeather(WeatherType.CLEAR);
                        return;
                    }
                    Block block = next.getPlayer().getLocation().getBlock();
                    int height = 3;
                    boolean rain = true;
                    while (height-- > 0) {
                        block = block.getRelative(BlockFace.UP);
                        if (block.getType().isSolid()) {
                            rain = false;
                            break;
                        }
                    }
                    if (rain) {
                        next.getPlayer().setHealth(health - damage);
                    }
                }
            }, 20, 20);
        }
    }
}
