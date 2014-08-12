package me.kitskub.flooder.listeners;

import java.util.Random;
import java.util.logging.Level;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.Logging;
import me.kitskub.gamelib.framework.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class WaterRunner {
    private final FGame game;
    private Runner runner;

    public WaterRunner(FGame game) {
        this.game = game;
    }

    public void start() {
        runner = new Runner();
        // Run every 3 seconds after 10 seconds
        runner.runTaskTimer(Flooder.getInstance(), 10 * 20, 5 * 20);
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
                Location curr = p.getLocation().clone().add(0, 5, 0);
                if (curr.getBlock().getType() != Material.AIR) Logging.logger.log(Level.WARNING, "{0} ''s current location is not in air!", p.getName());
                float percentage = getPercentage(curr.getY(), lowest, highest);
                attempt:
                for (int i = 0; i < 5; i++) {
                    if (rand.nextFloat() > percentage) continue;
                    curr.setYaw((float) (curr.getYaw() + (.3 * rand.nextFloat()) + .1) * (rand.nextBoolean() ? -1 : 1));
                    curr.setPitch((float) (curr.getPitch() + (.3 * rand.nextFloat()) + .1) * (rand.nextBoolean() ? -1 : 1));
                    BlockIterator it = new BlockIterator(curr);
                    int distance = 0;
                    Block lastAir = null;
                    while (it.hasNext() && distance++ < 15) {
                        Block next = it.next();
                        // If the distance is less than 2, always continue
                        if (p.getLocation().distanceSquared(next.getLocation()) < 5 * 5 || game.getActiveArena().takeZone.getCuboid().contains(curr, 4)) continue;
                        // Keep searching until we encounter a block that isn't air
                        if (next.getType() == Material.AIR) {
                            lastAir = next;
                            continue;
                        }
                        // If there is no air outside of the 2 block range, try again
                        if (lastAir == null) break;

                        game.getResetter().add(lastAir.getLocation(), lastAir.getState());
                        lastAir.setType(Material.WATER);

                        Vector direction = p.getLocation().getDirection();
                        direction.setY(direction.getY() * -1);
                        BlockFace one = yawToFace(p.getLocation().getYaw(), false);
                        BlockFace two;
                        if ((two = yawToFace(p.getLocation().getYaw() + 45, false)) == one) {
                            two = yawToFace(p.getLocation().getYaw() - 45, false);
                        }

                        lastAir = lastAir.getRelative(one.getOppositeFace());
                        game.getResetter().add(lastAir.getLocation(), lastAir.getState());
                        lastAir.setType(Material.WATER);

                        lastAir = lastAir.getRelative(two.getOppositeFace());
                        game.getResetter().add(lastAir.getLocation(), lastAir.getState());
                        lastAir.setType(Material.WATER);

                        lastAir = lastAir.getRelative(one);
                        game.getResetter().add(lastAir.getLocation(), lastAir.getState());
                        lastAir.setType(Material.WATER);

                        break attempt;
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

    public static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static final BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }
}
