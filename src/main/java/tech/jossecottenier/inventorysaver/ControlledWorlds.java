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
	
	public static List<World> getWorlds() {
		return worlds;
	}
	
	public static void addWorld(World world) {
		worlds.add(world);
	}
	
	public static void loadFromFile(File worldsFile, Server server) {
		final FileConfiguration configuration = YamlConfiguration.loadConfiguration(worldsFile);
		
		for (final String worldName : configuration.getStringList("worlds")) {
			worlds.add(server.getWorld(worldName));
		}
	}
	
	public static void loadFromFile() {
		final File worldsFile = new File(Main.instance.getDataFolder() + File.separator + "worlds.yml");
		loadFromFile(worldsFile, Main.instance.getServer());
	}
}
