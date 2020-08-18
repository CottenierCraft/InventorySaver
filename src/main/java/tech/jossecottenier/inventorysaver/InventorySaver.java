package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.io.IOException;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class InventorySaver implements Listener,CommandExecutor {
	
	/**
	 * Gets the inventory file in a specified plugin namespace
	 * from the players world, in which the serialized player 
	 * inventories of that world are stored.
	 * 
	 * @param player Player whose world's inventory file to get
	 * @param plugin Plugin in which namespace the file should be searched
	 * @return Player's world's inventory file
	 */
	private File getWorldInventoryFile(Player player, JavaPlugin plugin) {
		final File file = new File(plugin.getDataFolder() + File.separator + player.getWorld().getName() + ".yml");
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * Gets the inventory file from the players world, 
	 * in which the serialized player 
	 * inventories of that world are stored.
	 * 
	 * @param player Player whose world's inventory file to get
	 * @return Player's world's inventory file
	 */
	private File getWorldInventoryFile(Player player) {
		return getWorldInventoryFile(player, Main.instance);
	}
	
	/**
	 * Serializes a specified player's inventory
	 * to a efficiently storable string.
	 * 
	 * @param player Player whose inventory to serialize
	 * @return Serialized string of the player's inventory
	 */
	public String serializeInventory(Player player) {
		String serialization = "";
		final ItemStack[] inventoryContents = player.getInventory().getContents();
		
		for (ItemStack item : inventoryContents) {
			serialization += new SavedItem(item).serialize() + "-";
		}
		
		// Remove end minus sign
		serialization = serialization.substring(0, serialization.length() - 1);
		
		return serialization;
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to the player's world's inventory file in the specified
	 * plugin's namespace
	 * @param player Player whose inventory is to be saved
	 * @param plugin Plugin in which namespace the inventory file should go.
	 */
	protected void saveInventory(Player player, JavaPlugin plugin) {
		final File file = getWorldInventoryFile(player);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		
		configuration.set(player.getName(), serializeInventory(player));
		try {
			configuration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to the player's world's inventory file
	 * 
	 * @param player Player whose inventory is to be saved
	 */
	public void saveInventory(Player player) {
		saveInventory(player, Main.instance);
	}
	
	/**
	 * Loads the serialized inventory of the player's world's
	 * inventory file saved in a specified plugin's namespace
	 * to an ItemStack array.
	 * 
	 * @param player Player whose saved inventory is to be loaded.
	 * @param plugin Plugin in which namespace the inventory file is saved.
	 * @return ItemStack array with the loaded inventory contents.
	 */
	protected ItemStack[] loadInventoryContents(Player player, JavaPlugin plugin) {
		final File file = getWorldInventoryFile(player, plugin);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		final String serialization = configuration.getString(player.getName());
		final String[] serializedItems = serialization.split("-");
		final ItemStack[] inventoryContents = new ItemStack[serializedItems.length];
		
		for (int i = 0; i < serializedItems.length; i++) {
			inventoryContents[i] = SavedItem.deserialize(serializedItems[i]);
		}
		
		return inventoryContents;
	}
	
	/**
	 * Loads the serialized inventory of the player's world's
	 * inventory file saved in the InventorySaver plugin's namespace
	 * to an ItemStack array
	 * 
	 * @param player Player whose saved inventory is to be loaded
	 * @return
	 */
	public ItemStack[] loadInventoryContents(Player player) {
		return loadInventoryContents(player, Main.instance);
	}
	
	protected void onTeleport(PlayerTeleportEvent event, JavaPlugin plugin) {
		final Player player = event.getPlayer();
		final World from = event.getFrom().getWorld();
		final World to = event.getTo().getWorld();
		
		// From a controlled world to an uncontrolled world
		if (ControlledWorlds.getWorlds().contains(from) && !ControlledWorlds.getWorlds().contains(to)) {
			saveInventory(player, plugin);
			return;
		}
		
		// From an uncontrolled world to a controlled world
		if (!ControlledWorlds.getWorlds().contains(from) && ControlledWorlds.getWorlds().contains(to)) {
			final ItemStack[] contents = loadInventoryContents(player);
			player.getInventory().setContents(contents);
			return;
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		onTeleport(event, Main.instance);
	}
	
	protected void onQuit(PlayerQuitEvent event, JavaPlugin plugin) {
		if (ControlledWorlds.getWorlds().contains(event.getPlayer().getWorld())) {
			saveInventory(event.getPlayer(), plugin);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		onQuit(event, Main.instance);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equalsIgnoreCase("inventory")) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is to be executed by players.");
			return false;
		}
		
		if (args.length != 1) {
			return false;
		}
		
		final Player player = (Player)sender;
		final String instruction = args[0];
		
		switch (instruction) {
			case "save":
				saveInventory(player);
				break;
			case "load":
				player.getInventory().setContents(loadInventoryContents(player));
				break;
		}
		
		return true;
	}
}
