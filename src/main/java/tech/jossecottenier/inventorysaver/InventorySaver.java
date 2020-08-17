package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventorySaver implements Listener {
	
	private File getWorldInventoryFile(Player player, JavaPlugin plugin) {
		final File file = new File(plugin.getDataFolder() + File.separator + player.getWorld().getName() + ".yml");
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
	
	public String serializeInventory(Player player) {
		String serialization = "";
		final ItemStack[] inventoryContents = player.getInventory().getContents();
		
		for (ItemStack item : inventoryContents) {
			serialization += new SavedItem(item).toString() + "-";
		}
		
		// Remove end minus sign
		serialization = serialization.substring(0, serialization.length() - 2);
		
		return serialization;
	}
	
	protected void saveInventory(Player player, JavaPlugin plugin) {
		final File file = getWorldInventoryFile(player, plugin);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		
		configuration.set(player.getName(), serializeInventory(player));
		try {
			configuration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveInventory(Player player) {
		saveInventory(player, Main.instance);
	}
	
	protected ItemStack[] loadInventoryContents(Player player, JavaPlugin plugin) {
		final File file = getWorldInventoryFile(player, plugin);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		final String serialization = configuration.getString(player.getName());
		final String[] serializedItems = serialization.split("-");
		final ItemStack[] inventoryContents = new ItemStack[serializedItems.length];
		
		for (int i = 0; i < serializedItems.length; i++) {
			inventoryContents[i] = SavedItem.fromString(serializedItems[i]);
		}
		
		return inventoryContents;
	}
	
	public ItemStack[] loadInventoryContents(Player player) {
		return loadInventoryContents(player, Main.instance);
	}
	
	protected void onTeleport(PlayerTeleportEvent event, JavaPlugin plugin) {
		
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		onTeleport(event, Main.instance);
	}
	
	protected void onQuit(PlayerQuitEvent event, JavaPlugin plugin) {
		
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		onQuit(event, Main.instance);
	}
}
