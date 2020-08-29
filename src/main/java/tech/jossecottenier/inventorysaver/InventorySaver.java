package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class InventorySaver implements Listener,CommandExecutor {
	private final List<ItemStack> customItems;
	private final Map<UUID,Long> joinTimes;
	
	public InventorySaver() {
		this.customItems = new ArrayList<>();
		this.joinTimes = new HashMap<>();
	}
	
	/**
	 * Adds an ItemStack to the list of custom items
	 * which will be replaced by their ItemStack
	 * if the ItemStack name and their saved name
	 * match
	 * 
	 * @param customItem Custom item to add
	 */
	public void addCustomItem(ItemStack customItem) {
		customItems.add(customItem);
	}
	
	/**
	 * Removes an ItemStack from the list of custom items
	 * which will be replaced by their ItemStack
	 * if the ItemStack name and their saved name match
	 * 
	 * @param customItem The custom item to be removed.
	 * @return Whether or not the custom item was in the list.
	 */
	public boolean removeCustomItem(ItemStack customItem) {
		return customItems.remove(customItem);
	}
	
	/**
	 * Returns the custom item which display
	 * name matches with the specified name
	 * or null if there is no such custom item
	 * registered.
	 * 
	 * @param name Name with which the custom item should match
	 * @return Custom item's ItemStack if there is a match, null if not.
	 */
	public ItemStack getCustomItem(String name) {
		final List<ItemStack> customItemMatches = customItems
				.stream()
				.filter(itemStack->itemStack.getItemMeta().getDisplayName().equals(name))
				.collect(Collectors.toList());
		return customItemMatches.size() > 0 ? customItemMatches.get(0) : null;
	}
	
	/**
	 * Gets the inventory file in a specified plugin namespace
	 * from a specified world, in which the serialized player
	 * inventories are stored.
	 * 
	 * @param world Player whose world's inventory file to get
	 * @param plugin Plugin in which namespace the file should be searched
	 * @return Player's world's inventory file
	 */
	private File getWorldInventoryFile(World world, JavaPlugin plugin) {
		final File file = new File(plugin.getDataFolder() + File.separator + world.getName() + ".yml");
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * Serializes a specified inventory
	 * to a efficiently storable string.
	 * 
	 * @param inventory Inventory which should be serialized
	 * @return Serialized string of the player's inventory
	 */
	public String serializeInventory(Inventory inventory) {
		StringBuilder serialization = new StringBuilder();
		final ItemStack[] inventoryContents = inventory.getContents();
		
		for (ItemStack item : inventoryContents) {
			serialization.append(new SavedItem(item).serialize()).append("-");
		}
		
		// Remove end minus sign
		serialization = new StringBuilder(serialization.substring(0, serialization.length() - 1));
		
		return serialization.toString();
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to a specified world's inventory file in the specified
	 * plugin's namespace
	 * @param player Player whose inventory is to be saved
	 * @param world The world to which inventory file the inventory should be saved.
	 * @param plugin Plugin in which namespace the inventory file should go.
	 */
	protected void saveInventory(Player player, World world, JavaPlugin plugin) {
		if (world == null) {
			world = player.getWorld();
		}
		final File file = getWorldInventoryFile(world, plugin);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		
		configuration.set(player.getName(), serializeInventory(player.getInventory()));
		try {
			configuration.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to the player's world's inventory file in the specified
	 * plugin's namespace
	 * @param player Player whose inventory should be saved.
	 * @param plugin Plugin in which namespace the inventory should be saved.
	 */
	protected void saveInventory(Player player, JavaPlugin plugin) {
		saveInventory(player, player.getWorld(), plugin);
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to a specified world's inventory file
	 * 
	 * @param player Player whose inventory is to be saved
	 * @param world World to which inventory file the player's inventory should be saved.
	 */
	public void saveInventory(Player player, World world) {
		saveInventory(player, world, Main.instance);
	}
	
	/**
	 * Serializes a player's inventory to a string and saves
	 * it to the player's world's inventory file
	 * 
	 * @param player Player whose inventory to save.
	 */
	public void saveInventory(Player player) {
		saveInventory(player, player.getWorld());
	}
	
	/**
	 * Deserializes a serialization string
	 * to an ItemStack array containing the
	 * inventory contents.
	 * 
	 * @param serialization The serialization string
	 * @return ItemStack array with inventory contents
	 */
	public ItemStack[] deserializeInventory(String serialization) {
		final String[] serializedItems = serialization.split("-");
		final ItemStack[] inventoryContents = new ItemStack[serializedItems.length];
		
		for (int i = 0; i < serializedItems.length; i++) {
			inventoryContents[i] = SavedItem.deserialize(serializedItems[i], this);
		}
		
		return inventoryContents;
	}
	
	/**
	 * Loads the serialized inventory of a specified world's
	 * inventory file saved in a specified plugin's namespace
	 * to an ItemStack array.
	 * 
	 * @param player Player whose saved inventory is to be loaded.
	 * @param world The world from which inventory file the inventory should be loaded.
	 * @param plugin Plugin in which namespace the inventory file is saved.
	 * @return ItemStack array with the loaded inventory contents.
	 */
	protected ItemStack[] loadInventoryContents(Player player, World world, JavaPlugin plugin) {
		final File file = getWorldInventoryFile(world, plugin);
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
		
		return deserializeInventory(configuration.getString(player.getName()));
	}
	
	/**
	 * Loads the serialized inventory of a specified world's
	 * inventory file saved in the InventorySaver plugin's namespace
	 * to an ItemStack array
	 * 
	 * @param player Player whose saved inventory is to be loaded
	 * @param world The world from which inventory file the inventory should be loaded.
	 * @return The loaded inventory contents.
	 */
	public ItemStack[] loadInventoryContents(Player player, World world) {
		return loadInventoryContents(player, world, Main.instance);
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
		return loadInventoryContents(player, player.getWorld(), plugin);
	}
	
	/**
	 * Loads the serialized inventory of the player's world's
	 * inventory file saved in the InventorySaver plugin's namespace
	 * to an ItemStack array
	 * 
	 * @param player Player whose saved inventory is to be loaded
	 * @return The loaded inventory contents.
	 */
	public ItemStack[] loadInventoryContents(Player player) {
		return loadInventoryContents(player, player.getWorld());
	}
	
	/**
	 * Sets the default inventory saver
	 * which loads/saves inventories
	 * when players join/leave worlds
	 * listed in the config.yml
	 * from a specified plugin
	 * 
	 * @param inventorySaver InventorySaver to which the default InventorySaver will be set
	 * @param plugin Plugin which default InventorySaver will be overwritten
	 */
	protected static void setDefaultInventorySaver(InventorySaver inventorySaver, Main plugin) {
		plugin.setDefaultInventorySaver(inventorySaver);
	}
	
	/**
	 * Sets the default inventory saver
	 * which loads/saves inventories
	 * when players join/leave worlds
	 * listed in the config.yml
	 * 
	 * @param inventorySaver The inventorySaver instance to which the default will be set.
	 */
	public static void setDefaultInventorySaver(InventorySaver inventorySaver) {
		setDefaultInventorySaver(inventorySaver, Main.instance);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}
	
	protected void onTeleport(PlayerTeleportEvent event, Main plugin) {
		final Player player = event.getPlayer();
		// Default joinTime to 0 (if something goes wrong in the join event, just load)
		final long joinTime = joinTimes.getOrDefault(player.getUniqueId(), 0l);
		final World from = event.getFrom().getWorld();
		final World to = event.getTo().getWorld();

		System.out.println(System.currentTimeMillis() - joinTime);
		// Don't load or save inventories if players get teleported within 50ms from join
		if ((System.currentTimeMillis() - joinTime) < 50) {
			return;
		}
		
		// From a controlled world to an uncontrolled world
		if (ControlledWorlds.getWorlds().contains(from) && !ControlledWorlds.getWorlds().contains(to)) {
			saveInventory(player, plugin);
			return;
		}
		
		// From an uncontrolled world to a controlled world
		if (!ControlledWorlds.getWorlds().contains(from) && ControlledWorlds.getWorlds().contains(to)) {
			final ItemStack[] contents = plugin.getDefaultInventorySaver().loadInventoryContents(player, to);
			player.getInventory().setContents(contents);
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		onTeleport(event, Main.instance);
	}
	
	protected void onQuit(PlayerQuitEvent event, JavaPlugin plugin) {
		if (ControlledWorlds.getWorlds().contains(event.getPlayer().getWorld())) {
			Main.instance.getDefaultInventorySaver().saveInventory(event.getPlayer(), plugin);
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
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof InventorySaver)) {
			return false;
		}
		
		final InventorySaver otherInventorySaver = (InventorySaver)other;
		
		return this.customItems.equals(otherInventorySaver.customItems);
	}
	
	@Override
	public String toString() {
		return String.format("InventorySaver{customItems=%s}", customItems.toString());
	}
}
