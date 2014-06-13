package me.kitskub.flooder.commands.admin.set;

import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.framework.User;
import me.kitskub.gamelib.stats.Skills;
import me.kitskub.gamelib.stats.Skills.Type;
import me.kitskub.gamelib.utils.ChatUtils;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Defaults.Perms;
import me.kitskub.flooder.Flooder;
import org.bukkit.Bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSkillCommand extends Command {
    
    public SetSkillCommand() {
	    super(Perms.ADMIN_SET_SKILL, Defaults.Commands.ADMIN_SET_HELP.getCommand(), "skill");
    }

    @Override
    public void handle(CommandSender cs, String label, String[] args) {
        if (args.length < 3) {
			ChatUtils.helpCommand(cs, this);
			return;
		}
        Player player = Bukkit.getPlayer(args[0]);
		if (player == null) {
            ChatUtils.error(cs, "That user does not exist!");
		    return;
		}
        User user = User.get(player);
        Type t = Type.matchType(args[1]);
        if (t == null) {
            ChatUtils.error(cs, "Invalid skill type!");
            return;
        }
        Skills skills = Flooder.getInstance().getStatManager().get(user).getSkills();
        double previous = skills.getType(t);
        String valueArg = args[2];
        double value;
        try {
            value = Double.valueOf(valueArg.replace("+", "").replace("-", ""));
        } catch (NumberFormatException e) {
            ChatUtils.error(cs, "Invalid value! Must be a number!");
            return;
        }
        
        if (valueArg.startsWith("+")) {
            value = previous + value;
        } else if (valueArg.startsWith("-")) {
            value = previous - value;
            value = value < 0 ? 0 : value;
        }
        skills.set(t, value);
    }

	@Override
	public String getInfo() {
		return "sets a player's skill";
	}

	@Override
	public String getLocalUsage() {
		return "skill <player> <speed|damage|protection|clipSize|ammo|reloadTime> [+/-]<value>";
	}
}
