package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.Logging;
import me.kitskub.gamelib.WorldNotFoundException;
import me.kitskub.gamelib.framework.Zone;
import me.kitskub.gamelib.framework.impl.GameMasterImpl;
import me.kitskub.gamelib.framework.impl.arena.BaseArena;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.Cuboid;
import me.kitskub.gamelib.utils.GeneralUtils;
import me.kitskub.gamelib.utils.config.ConfigSection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class FArena extends BaseArena<Flooder, FGame> {
	public final List<Location> spawnpoints;
    public Zone takeZone;

    public FArena(String name, ConfigSection coords) {
        super(Flooder.getInstance(), name, coords);
        this.spawnpoints = new ArrayList<Location>();
    }

    @Override
    public boolean doLoad() {
		spawnpoints.clear();
        if (coords.contains("spawn-points")) {
			ConfigurationSection spawnPointsSection = coords.getConfigurationSection("spawn-points");
			for (String key : spawnPointsSection.getKeys(false)) {
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
        return super.doLoad();
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
    public boolean doVerifyData() {
        return super.doVerifyData() && spawnpoints.size() > 2 && takeZone != null;
    }

    @Override
    public boolean doCheckData(CommandSender s) {
        boolean incomplete = false;
        if (spawnpoints.size() < 2) {
            ChatUtils.error(s, "There are less than two spawnpoints!");
            incomplete = true;
        }
        if (takeZone == null) {
            ChatUtils.error(s, "The take zone is not set!");
            incomplete = true;
        }
        return !incomplete && super.doCheckData(s);
    }

    public void setZone(Cuboid c) {
        takeZone = new FZone(c, this);
    }

    public int getZoneCaptureTime() {
        // TODO: config
        return 10;
    }

    public static GameMasterImpl.ArenaCreator<FArena> CREATOR = new FArenaCreator();
    private static class FArenaCreator implements GameMasterImpl.ArenaCreator<FArena> {
        public FArena createArena(String name, ConfigSection config) {
            return new FArena(name, config);
        }
    }
}
