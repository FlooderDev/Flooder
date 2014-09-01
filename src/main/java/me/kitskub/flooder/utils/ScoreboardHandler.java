package me.kitskub.flooder.utils;

import me.kitskub.flooder.Defaults.Config;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FGame;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.utils.GeneralUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHandler extends BukkitRunnable {
    private final FGame game;
    private int secondsLeft;

    public ScoreboardHandler(FGame game) {
        this.game = game;
        this.secondsLeft = Config.GAME_DURATION.getGlobalInt() * 60;
        for (User p : game.getAllPlayers()) {
            p.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            Objective obj = p.getPlayer().getScoreboard().registerNewObjective("PLAYERKDR", "dummy");
            obj.setDisplayName(GeneralUtils.formatTimeScoreboard(secondsLeft));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.getScore("Deaths").setScore(-1);
            updatePlayer(p);
        }
        runTaskTimer(Flooder.getInstance(), 20, 20);
    }

    private void updatePlayer(User p) {
        Scoreboard board = p.getPlayer().getScoreboard();
        Objective obj = board.getObjective("PLAYERKDR");
        obj.setDisplayName(GeneralUtils.formatTimeScoreboard(secondsLeft));
        obj.getScore("Deaths").setScore(p.getStat().get(game).getDeaths().size());
    }

    public void run() {
        secondsLeft--;
        for (User p : game.getAllPlayers()) {
            updatePlayer(p);
        }
    }
}
