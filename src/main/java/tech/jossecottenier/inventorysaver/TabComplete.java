package tech.jossecottenier.inventorysaver;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TabComplete implements TabCompleter {
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("inventory")) {
			if (args.length == 1) {
				return List.of("save", "load");
			}
		}
		
		return null;
	}
}
