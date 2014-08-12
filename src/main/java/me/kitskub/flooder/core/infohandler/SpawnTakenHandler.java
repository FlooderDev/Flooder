package me.kitskub.flooder.core.infohandler;

import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.infohandler.GameInfoHandler;
import org.bukkit.Location;

public class SpawnTakenHandler extends GameInfoHandler {
    private Location spawnTaken;

    public SpawnTakenHandler(User user) {
        super(user);
    }

    public Location getSpawnTaken() {
        return spawnTaken;
    }

    public void setSpawnTaken(Location spawnTaken) {
        this.spawnTaken = spawnTaken;
    }


    public static final GameInfoHandlerCreator<SpawnTakenHandler> CREATOR = new SpawnTakenHandlerCreator();
    private static class SpawnTakenHandlerCreator implements GameInfoHandler.GameInfoHandlerCreator<SpawnTakenHandler> {
        @Override
        public Class<SpawnTakenHandler> getHandlerClass() {
            return SpawnTakenHandler.class;
        }

        @Override
        public SpawnTakenHandler createNew(User user) {
            return new SpawnTakenHandler(user);
        }
    }
}
