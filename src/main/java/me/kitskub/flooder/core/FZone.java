package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import me.kitskub.flooder.utils.BossBarHandler;

import me.kitskub.gamelib.Logging;
import me.kitskub.gamelib.api.event.ZoneTakenEvent;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.Zone;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.Cuboid;
import me.kitskub.gamelib.utils.ExpiringSetSimple;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitTask;

public class FZone implements Zone {
    private final Cuboid cuboid;
    private final FArena arena;
    private BukkitTask takeTask;
    // Wool that might or might not be changed
    private List<Location> wool = null;
    private final List<Location> tempChangedWool = new ArrayList<Location>();
    private final List<Location> woolToChange = new ArrayList<Location>();

    public FZone(Cuboid cuboid, FArena arena) {
        this.cuboid = cuboid;
        this.arena = arena;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public FArena getArena() {
        return arena;
    }

    private final ExpiringSetSimple<User> usersIn = new ExpiringSetSimple<User>(2, TimeUnit.SECONDS);
    private User taking = null;

    public void beginTaking(User user) {
        if (wool == null) {
            wool = new ArrayList<Location>();
            for (int x = cuboid.getLower().getBlockX(); x <= cuboid.getUpper().getBlockX(); x++) {
                for (int y = cuboid.getLower().getBlockY(); y <= cuboid.getUpper().getBlockY(); y++) {
                    for (int z = cuboid.getLower().getBlockZ(); z <= cuboid.getUpper().getBlockZ(); z++) {
                        if (cuboid.getWorld().getBlockAt(x, y, z).getType() == Material.WOOL) {
                            wool.add(new Location(cuboid.getWorld(), x, y, z));
                        }
                    }
                }
            }
        }
        usersIn.add(user);

        if (takeTask == null) {
            if (usersIn.size() == 1) {
                taking = user;
                takeTask = Bukkit.getScheduler().runTaskTimer(arena.getOwningPlugin().getPlugin(), new TakeRunnable(2 * arena.getZoneCaptureTime()), 1, 10);
                ChatUtils.broadcast(arena.getActiveGame(), taking.getPlayerName() + " has begun taking the mountain!");
            }
        } else {
            if (usersIn.size() != 1) {
                ChatUtils.broadcast(arena.getActiveGame(), taking.getPlayerName() + " can't take the mountain because " + user.getPlayerName() + " has enter it.");
                cancelTakeTask();
            }
        }
    }
    
    public void reset() {
        taking = null;
        if (takeTask != null) {
            takeTask.cancel();
            takeTask = null;
        }
        if (wool != null) {
            byte data = new Wool(DyeColor.WHITE).getData();
            for (Location l : wool) {
                l.getBlock().setTypeIdAndData(Material.WOOL.getId(), data, false);
            }
            wool = null;
        }
        tempChangedWool.clear();
        woolToChange.clear();
        
    }
    
    private void cancelTakeTask() {
        takeTask.cancel();
        takeTask = null;
        byte data = new Wool(DyeColor.WHITE).getData();
        for (Location l : tempChangedWool) {
            l.getBlock().setTypeIdAndData(Material.WOOL.getId(), data, false);
        }
        tempChangedWool.clear();
        woolToChange.clear();
        BossBarHandler.get().updatePercent(arena.getActiveGame(), 100f);
    }

    private class TakeRunnable implements Runnable {
        private int taken = 0;
        private final int needed;
        private final byte newData;

        public TakeRunnable(int needed) {
            this.needed = needed;
            woolToChange.addAll(wool);
            newData = new Wool(DyeColor.GREEN).getData();
        }

        public void run() {
            if (!cuboid.contains(taking.getPlayer().getLocation())) {
                cancelTakeTask();
                return;
            }
            taken++;
            float percent = 1 - ((float)taken / needed) ;
            int shouldBeDone = (int) (percent * wool.size());
            int left = shouldBeDone - tempChangedWool.size();
            Random r = new Random();
            for (int i = 0; i < left; i++) {
                Location change = woolToChange.remove(r.nextInt(woolToChange.size()));
                change.getBlock().setTypeIdAndData(Material.WOOL.getId(), newData, false);
                tempChangedWool.add(change);
            }
            BossBarHandler.get().updatePercent(arena.getActiveGame(), percent);
            if (taken >= needed) {
                takeTask.cancel();
                takeTask = null;
                tempChangedWool.clear();
                if (!woolToChange.isEmpty()) Logging.severe("Not all the wool was changed in a zone!");
                ChatUtils.broadcast(arena.getActiveGame(), taking.getPlayerName() + " has taken the mountain!");
                taking.getPlayer().playSound(taking.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                Bukkit.getPluginManager().callEvent(new ZoneTakenEvent(arena.getActiveGame(), taking, ZoneTakenEvent.TakenType.USER));
                arena.getActiveGame().win(taking);
                taking = null;
            }
        }
    }
}

