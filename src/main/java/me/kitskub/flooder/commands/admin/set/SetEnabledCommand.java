package me.kitskub.flooder.commands.admin.set;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.gamelib.utils.Enableable;
import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Defaults.Lang;
import me.kitskub.flooder.Flooder;

import org.bukkit.command.CommandSender;

public class SetEnabledCommand extends Command {

    public SetEnabledCommand() {
	    super(Perms.ADMIN_SET_ENABLED, Commands.ADMIN_SET_HELP.getCommand(), "enabled");
    }

    @Override
    public void handle(CommandSender cs, String cmd, String[] args) {
	    if (args.length < 2) {
		    ChatUtils.helpCommand(cs, this);
		    return;
	    }
        Enableable e;
        if ("game".equalsIgnoreCase(args[0])) {
            e = Flooder.gameMaster().getGame(args[1]);
        } else if ("arena".equalsIgnoreCase(args[0])) {
            e = Flooder.gameMaster().getArena(args[1]);
        } else {
            ChatUtils.error(cs, "Invalid type:" + args[0]);
            ChatUtils.helpCommand(cs, this);
            return;
        }
        if (e == null) {
            ChatUtils.error(cs, Lang.NOT_EXIST.getMessage().replace("<item>", args[0]));
            return;
        }

	    boolean flag;
	    if (args.length < 3) {
		    flag = true;
	    } else {
		    flag = Boolean.valueOf(args[2]);
	    }
	    if (!e.setEnabled(flag, cs)) return;//Let it do it's thing
	    if (flag) {
		    ChatUtils.send(cs, "%s has been enabled.", args[1]);
	    } else {
		    ChatUtils.send(cs, "%s has been disabled and the active game was stopped if it was running.", args[1]);
	    }
    }

	@Override
	public String getInfo() {
		return "enable or disable a game or arena";
	}

	@Override
	public String getLocalUsage() {
		return "enabled <game|arena> <name> [true/false]";
	}
    
}
