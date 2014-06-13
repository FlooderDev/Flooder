package me.kitskub.flooder.commands.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.stats.WebStatHandler;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.SQLStat;

import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Logging;
import me.kitskub.flooder.Flooder;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class SearchCommand extends Command {
	private final List<FutureTask<SQLStat>> searches = new ArrayList<FutureTask<SQLStat>>();

	public SearchCommand() {
		super(Perms.USER_SEARCH, Flooder.fCH(), "search");
	}

	@Override
	public void handle(final CommandSender cs, String cmd, final String[] args) {
		if (args.length < 1) {
			ChatUtils.error(cs, "Must have a player to search for!");
			return;
		}
		FutureTask<SQLStat> f = new FutureTask<SQLStat>(new Callable<SQLStat>() {
			public SQLStat call() throws Exception {
				return WebStatHandler.getStat(args[0]);
			}
		});
		synchronized(searches) {
			searches.add(f);
			Bukkit.getScheduler().runTaskAsynchronously(Flooder.getInstance(), f);
			Bukkit.getScheduler().runTaskTimer(Flooder.getInstance(), new BukkitRunnable() {
				public void run() {
					synchronized(searches) {
						if (searches.isEmpty()) {
							cancel();
							return;
						}
						Iterator<FutureTask<SQLStat>> iterator = searches.iterator();
						while (iterator.hasNext()) {
							FutureTask<SQLStat> next = iterator.next();
							if (next.isCancelled()) {
								cancel();
								return;
							}
							if (!next.isDone()) continue;
							iterator.remove();
							SQLStat stat;
							try {
								stat = next.get();
							} catch (Exception ex) {
								Logging.debug("Exception in search runnable");
								cancel();
								return;
							}
							if (stat == null) {
								ChatUtils.send(cs, "Could not find stat for: %s", args[0]);
							}
                            ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
							ChatUtils.send(cs, "Player stat for: %s", args[0]);
							ChatUtils.send(cs, "%s has a global rank of %s",args[0], stat.rank);
							ChatUtils.send(cs, "%s has played %s games for a total of %s", args[0], stat.totalGames, stat.totalTime);
							ChatUtils.send(cs, "%s has had %s wins, %s deaths, and %s kills", args[0], stat.wins, stat.deaths, stat.kills);
						}
					}
				}
			}, 0, 5);
			
		}
	}

	@Override
	public String getInfo() {
		return "searches for a player's stat and prints out the info";
	}

	@Override
	public String getLocalUsage() {
		return "search <player>";
	}

}
