package me.kitskub.paintball.core.infohandler;

import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.infohandler.GameInfoHandler;
import me.kitskub.gamelib.framework.infohandler.GameInfoHandler.GameInfoHandlerCreator;

public class LastSpawnHandler extends GameInfoHandler {
    private long lastSpawn;

    public LastSpawnHandler(User user) {
        super(user);
    }

    public long getLastSpawn() {
        return lastSpawn;
    }

    public void setLastSpawn(long lastSpawn) {
        this.lastSpawn = lastSpawn;
    }

    public static final GameInfoHandlerCreator<LastSpawnHandler> CREATOR = new LastSpawnHandlerCreator();

    private static class LastSpawnHandlerCreator implements GameInfoHandlerCreator<LastSpawnHandler> {
        @Override
        public Class<LastSpawnHandler> getHandlerClass() {
            return LastSpawnHandler.class;
        }

        @Override
        public LastSpawnHandler createNew(User user) {
            return new LastSpawnHandler(user);
        }
    }
}
