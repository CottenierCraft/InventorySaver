package tech.jossecottenier.inventorysaver;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class InventorySaver implements Listener {
	
	public static List<String> serializeInventory(Player player) {
		final PlayerInventory inventory = player.getInventory();
		
		return null;
	}
	
	public static void saveInventory(Player player, JavaPlugin plugin) {
		
	}
	
	public static void saveInventory(Player player) {
		saveInventory(player, Main.instance);
	}
	
}
