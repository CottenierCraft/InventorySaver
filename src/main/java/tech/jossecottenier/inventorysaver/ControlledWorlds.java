package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ControlledWorlds {
	private static final List<World> worlds = new ArrayList<>();
	
	/**
	 * Getter for the worlds on which the players inventory
	 * is saved/loaded when they join/quit respectively.
	 * 
	 * @return The list of worlds which are currently getting controlled
	 */
	public static List<World> getWorlds() {
		return worlds;
	}
	
	/**
	 * Adds a world to the list of worlds where their
	 * inventory is saved/loaded when they join/quit
	 * respectively.
	 * 
	 * @param world
	 */
	public static void addWorld(World world) {
		worlds.add(world);
	}
	
	/**
	 * Tries to remove a world from the controlled worlds
	 * and returns whether it was in the list or not
	 * 
	 * @param world World to be removed
	 * @return Whether the world was in the list or not
	 */
	public static boolean removeWorld(World world) {
		return worlds.remove(world);
	}
	
	/**
	 * Adds worlds saved on a specified server listed in a 
	 * specified YML file to the controlled worlds list.
	 * 
	 * @param worldsFile Specified YML config file
	 * @param server Server containing the worlds
	 */
	protected static void loadFromFile(File worldsFile, Server server) {
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(worldsFile);
		
		for (final String worldName : configuration.getStringList("worlds")) {
			worlds.add(server.getWorld(worldName));
		}
	}
	
	/*
	 * Adds worlds from an optional config.yml file
	 * to the controlled worlds list.
	 */
	public static void loadFromFile() {
		loadFromFile(Main.getConfigFile(), Main.instance.getServer());
	}
}
