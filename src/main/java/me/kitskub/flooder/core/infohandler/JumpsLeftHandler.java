package me.kitskub.flooder.core.infohandler;

import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.framework.infohandler.GameInfoHandler;

public class JumpsLeftHandler extends GameInfoHandler {
    private int jumpsLeft;

    public JumpsLeftHandler(User user) {
        super(user);
    }

    public int getJumpsLeft() {
        return jumpsLeft;
    }

    public void addJumps(int jumps) {
        jumpsLeft += jumps;
    }

    public boolean useJump() {
        if (jumpsLeft <= 0) return false;
        jumpsLeft--;
        return true;
    }

    public static final GameInfoHandler.GameInfoHandlerCreator<JumpsLeftHandler> CREATOR = new JumpsLeftHandlerCreator();
    private static class JumpsLeftHandlerCreator implements GameInfoHandler.GameInfoHandlerCreator<JumpsLeftHandler> {
        @Override
        public Class<JumpsLeftHandler> getHandlerClass() {
            return JumpsLeftHandler.class;
        }

        @Override
        public JumpsLeftHandler createNew(User user) {
            return new JumpsLeftHandler(user);
        }
    }
}
