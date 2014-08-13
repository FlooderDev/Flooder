package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.utils.BossBarHandler;
import me.kitskub.gamelib.Logging;
import me.kitskub.gamelib.api.event.zone.UserLeftZoneEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakenEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingInterruptedEvent;
import me.kitskub.gamelib.api.event.zone.ZoneTakingTickEvent;
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
import org.bukkit.block.BlockState;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitTask;

public class FZone implements Zone {
    private final Cuboid cuboid;
    private final FArena arena;
    private BukkitTask takeTask;
    // Wool that might or might not be changed
    private List<Location> wool = null;
    private final List<Location> tempChangedWool = new ArrayList<>();
    private final List<Location> woolToChange = new ArrayList<>();
    private final ExpiringSetSimple<User> usersIn = new ExpiringSetSimple<>(2, TimeUnit.SECONDS);
    private final ExpiringSetSimple.ExpiredRemovalCallback<User> callback;
    private User taking = null;

    public FZone(final Cuboid cuboid, final FArena arena) {
        this.cuboid = cuboid;
        this.arena = arena;
        callback = new ExpiringSetSimple.ExpiredRemovalCallback<User>() {
            @Override
            public boolean onExpire(User user) {
                if (cuboid.contains(user.getPlayer().getLocation())) {
                    return false;
                }
                Bukkit.getPluginManager().callEvent(new UserLeftZoneEvent(arena.getActiveGame(), FZone.this, user));
                return true;
            }
        };
    }

    @Override
    public Cuboid getCuboid() {
        return cuboid;
    }

    @Override
    public FArena getArena() {
        return arena;
    }

    @Override
    public Set<User> getUsersIn() {
        return Collections.unmodifiableSet(usersIn);
    }

    @Override
    public void beginTaking(User user) {
        if (wool == null) {
            wool = new ArrayList<>();
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
                takeTask = Bukkit.getScheduler().runTaskTimer(arena.getOwningPlugin().getPlugin(), new TakeRunnable(4 * arena.getZoneCaptureTime()), 1, 5);
                Bukkit.getPluginManager().callEvent(new ZoneTakingTickEvent(arena.getActiveGame(), FZone.this, 0f));
                ChatUtils.broadcast(arena.getActiveGame(), Defaults.Lang.TAKING_ZONE.getMessage().replace("<player>", taking.getPlayerName()));            }
        } else {
            if (usersIn.size() != 1) {
                ChatUtils.broadcast(arena.getActiveGame(), Defaults.Lang.NO_TAKE.getMessage().replace("<player>", taking.getPlayerName()).replace("<entered>", user.getPlayerName()));
                Bukkit.getPluginManager().callEvent(new ZoneTakingInterruptedEvent(arena.getActiveGame(), this));
                cancelTakeTask();
            }
        }
    }

    @Override
    public void reset() {
        taking = null;
        if (takeTask != null) {
            takeTask.cancel();
            takeTask = null;
        }
        if (wool != null) {
            for (Location l : wool) {
                BlockState state = l.getBlock().getState();
                state.setData(new Wool(DyeColor.WHITE));
                state.update();
            }
            wool = null;
        }
        tempChangedWool.clear();
        woolToChange.clear();
    }

    private static final Wool GREEN = new Wool(DyeColor.GREEN);
    private static final Wool WHITE = new Wool(DyeColor.WHITE);

    private void cancelTakeTask() {
        takeTask.cancel();
        takeTask = null;
        for (Location l : tempChangedWool) {
            BlockState state = l.getBlock().getState();
            state.setData(WHITE);
            state.update();
        }
        tempChangedWool.clear();
        woolToChange.clear();
    }

    private class TakeRunnable implements Runnable {
        private int taken = 0;
        private final int needed;

        public TakeRunnable(int needed) {
            this.needed = needed;
            woolToChange.addAll(wool);
        }

        @Override
        public void run() {
            if (usersIn.isEmpty()) {
                Bukkit.getPluginManager().callEvent(new ZoneTakingInterruptedEvent(arena.getActiveGame(), FZone.this));
                cancelTakeTask();
                return;
            }
            taken++;
            float percentDone = ((float) taken) / needed;
            int shouldBeDone = (int) (percentDone * wool.size());
            int left = shouldBeDone - tempChangedWool.size();
            Random r = new Random();
            for (int i = 0; i < left; i++) {
                Location change = woolToChange.remove(r.nextInt(woolToChange.size()));
                BlockState state = change.getBlock().getState();
                state.setData(GREEN);
                state.update();
                tempChangedWool.add(change);
            }
            BossBarHandler.get().updatePercent(taking, percentDone);
            if (taken >= needed) {
                takeTask.cancel();
                takeTask = null;
                tempChangedWool.clear();
                if (!woolToChange.isEmpty()) {
                    Logging.severe("Not all the wool was changed in a zone!");
                }
                ChatUtils.broadcast(arena.getActiveGame(), Defaults.Lang.TAKEN.getMessage().replace("<player>", taking.getPlayerName()));
                taking.getPlayer().playSound(taking.getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                Bukkit.getPluginManager().callEvent(new ZoneTakenEvent(arena.getActiveGame(), FZone.this, taking, ZoneTakenEvent.TakenType.USER));
                taking = null;
            } else {
                Bukkit.getPluginManager().callEvent(new ZoneTakingTickEvent(arena.getActiveGame(), FZone.this, percentDone));
            }
        }
    }
}
