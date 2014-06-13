package me.kitskub.flooder.commands.user;

import me.kitskub.flooder.Defaults.Commands;
import me.kitskub.flooder.Defaults.Perms; 
import me.kitskub.flooder.Flooder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.kitskub.gamelib.Perm;
import me.kitskub.gamelib.commands.Command;
import me.kitskub.gamelib.utils.ChatUtils;

import org.bukkit.command.CommandSender;

public class AboutCommand extends Command {

	public AboutCommand() {
		super(Perms.USER_ABOUT, Flooder.fCH(), "about");
	}

	@Override
	public void handle(CommandSender cs, String label, String[] args) {
        ChatUtils.send(cs, ChatUtils.getHeadLiner(Flooder.getInstance()));
		ChatUtils.send(cs, "Developer - kitskub");
		if (cs.getName().equals("kitskub") && args.length == 1) {
			if (!args[0].equals("true")) return;
			File commandperms = new File(Flooder.getInstance().getDataFolder(), "commandperms.txt");
			try {
				commandperms.createNewFile();
				FileWriter writer;
				writer = new FileWriter(commandperms);
				writer.write("== <<color red>>Commands<</color>> ==\n");
				Map<Perm, Command> map = new HashMap<Perm, Command>();
				for (Commands c : Commands.values()) {
					Command command = c.getCommand();
					map.put(command.getPerm(), command);
					StringBuilder builder = new StringBuilder();
					builder.append("* **");
					builder.append(command.getUsage());
					builder.append("** - ");
					builder.append(command.getInfo());
					builder.append("\n");
					writer.write(builder.toString());
				}
				writer.write("== <<color red>>Permissions<</color>> ==\n");
				for (Perm permission : Perms.values()) {
					StringBuilder builder = new StringBuilder();
					builder.append("* **");
					builder.append(permission.getPermission().getName());
					builder.append("**");
					String info = "";
					if (map.containsKey(permission)) {
						info = " - Allows " + map.get(permission).getUsage();
					} else if (permission.getInfo() != null) {
						info = " - " + permission.getInfo();
					}
					builder.append(info);
					builder.append("\n");
					writer.write(builder.toString());
				}
				writer.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex.getMessage());
			}
				
		}
	}

	@Override
	public String getInfo() {
		return "gives basic info about Paintball";
	}

	@Override
	public String getLocalUsage() {
		return "about";
	}

}
