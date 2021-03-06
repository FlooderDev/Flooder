package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.Logging;
import me.kitskub.gamelib.WorldNotFoundException;
import me.kitskub.gamelib.framework.Zone;
import me.kitskub.gamelib.framework.impl.GameMasterImpl;
import me.kitskub.gamelib.framework.impl.arena.AbstractArena;
import me.kitskub.gamelib.utils.Cuboid;
import me.kitskub.gamelib.utils.GeneralUtils;
import me.kitskub.gamelib.utils.config.ConfigSection;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class FArena extends AbstractArena<Flooder, FGame> {
	public final List<Location> spawnpoints;
    public Zone takeZone;
    public Location specWarp;
    public Location lobbyWarp;

    public FArena(String name, ConfigSection coords) {
        super(Flooder.getInstance(), name, coords);
        this.spawnpoints = new ArrayList<>();
    }

    @Override
    public boolean doLoad() {
		spawnpoints.clear();
        if (coords.contains("spawn-points")) {
			ConfigSection spawnPointsSection = coords.getConfigSection("spawn-points");
			for (String key : spawnPointsSection.getKeys()) {
				String str = spawnPointsSection.getString(key);
				try {
					spawnpoints.add(GeneralUtils.parseToLoc(str));
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
				} catch (NumberFormatException e) {
					Logging.debug(e.getMessage());
				}
			}
		}
        Cuboid takeCuboid = Cuboid.parseFromString(coords.getString("take-zone", ""));
        if (takeCuboid != null) {
            takeZone = new FZone(takeCuboid, this);
        }
        specWarp = GeneralUtils.parseToLoc(coords.getString("spec-warp", ""));
        lobbyWarp = GeneralUtils.parseToLoc(coords.getString("lobby-warp", ""));
        return super.doLoad();
    }

    @Override
    public boolean doSave() {
		ConfigurationSection spawnPointsSection = coords.createSection("spawn-points");
		int cntr;
		for (cntr = 0; cntr < spawnpoints.size(); cntr++) {
			Location loc = spawnpoints.get(cntr);
			if (loc == null) continue;
			String parsed = GeneralUtils.parseToString(loc);
			spawnPointsSection.set("" + (cntr + 1), parsed);
		}
        if (takeZone != null) {
            coords.set("take-zone", takeZone.getCuboid().parseToString());
        }
        if (specWarp != null) coords.set("spec-warp", GeneralUtils.parseToString(specWarp));
        if (lobbyWarp != null) coords.set("lobby-warp", GeneralUtils.parseToString(lobbyWarp));
        return super.doSave();
    }

	public boolean addSpawnPoint(Location loc) {
		if (loc == null) return false;
		if (spawnpoints.contains(loc)) return false;
		spawnpoints.add(loc);
		return true;
	}

	public boolean removeSpawnPoint(Location loc) {
		if (loc == null) return false;
		Iterator<Location> iterator = spawnpoints.iterator();
		while (iterator.hasNext()) {
			if (GeneralUtils.equals(loc, iterator.next())) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

    @Override
    public Collection<String> verifyData() {
        Set<String> errors = new HashSet<>(super.verifyData());
        if (spawnpoints.size() < 2) {
            errors.add("There are less than two spawnpoints!");
        }
        if (takeZone == null) {
            errors.add("The take zone is not set!");
        }
        if (specWarp == null) {
            errors.add("No spectator warp.");
        }
        if (lobbyWarp == null) {
            errors.add("No lobby warp.");
        }
        return errors;
    }

    public void setZone(Cuboid c) {
        takeZone = new FZone(c, this);
    }

    public int getZoneCaptureTime() {
        return Defaults.Config.ZONE_CAPTURE_TIME.getGlobalInt();
    }

    public static GameMasterImpl.ArenaCreator<FArena> CREATOR = new FArenaCreator();
    private static class FArenaCreator implements GameMasterImpl.ArenaCreator<FArena> {
        public FArena createArena(String name, ConfigSection config) {
            return new FArena(name, config);
        }
    }
}
