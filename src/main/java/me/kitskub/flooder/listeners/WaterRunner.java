package me.kitskub.flooder.listeners;

import java.util.Random;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

public class WaterRunner {
    private final FGame game;
    private Runner runner;

    public WaterRunner(FGame game) {
        this.game = game;
    }

    public void start() {
        runner = new Runner();
        // Run every 3 seconds after 10 seconds
        runner.runTaskTimer(Flooder.getInstance(), 10 * 20, 3 * 20);
    }

    public void stop() {
        runner.cancel();
        runner = null;
    }

    private class Runner extends BukkitRunnable {
        private int timesRun;

        @Override
        public void run() {
            double highest = -1;
            double lowest = -1;
            for (User u : game.getActivePlayers()) {
                final double curr = u.getPlayer().getLocation().getY();
                if (highest == -1 || highest < curr) highest = curr;
                if (lowest == -1 || lowest > curr) lowest = curr;
            }
            Random rand = new Random();
            for (User u : game.getActivePlayers()) {
                Player p = u.getPlayer();
                Location curr = p.getLocation();
                float percentage = getPercentage(curr.getY(), lowest, highest);
                if (rand.nextFloat() <= percentage) {
                    attempt:
                    for (int i = 0; i < 5; i++) {
                        curr.setYaw((float) (curr.getYaw() + (.3 * rand.nextFloat()) + .1) * (rand.nextBoolean() ? -1 : 1));
                        curr.setPitch((float) (curr.getPitch() + (.3 * rand.nextFloat()) + .1) * (rand.nextBoolean() ? -1 : 1));
                        BlockIterator it = new BlockIterator(curr);
                        int distance = 0;
                        Block last;
                        while (it.hasNext() && distance++ < 10) {
                            Block next = it.next();
                            last = next;
                            if (distance < 3) continue;
                            if (distance >= 3) {
                                last = next;
                            }
                            if (distance == 3) continue;
                            if (next.getType() == Material.AIR) continue;
                            game.getResetter().add(last.getLocation(), last.getState());
                            last.setType(Material.WATER);
                            break attempt;
                        }
                    }
                }
            }
            timesRun++;
        }

        private float getPercentage(double curr, double lowest, double highest) {
            double loc = (curr - lowest) / (highest - lowest);
            double percentage;
            double base;
            if (timesRun < 10) base = .3; // Less than 30 seconds
            else if (timesRun >= 10 && timesRun < 100) base = .6; // Between 30 seconds and 5 minutes
            else return 1; // Between 30 seconds and 5 minutes
            percentage = base + (1.258 * loc) - (4.375 * loc * loc) + (2.92 * loc * loc * loc);
            if (percentage > 1) return 1;
            if (percentage < 0) return 0;
            return (float) percentage;
        }
    }
}
