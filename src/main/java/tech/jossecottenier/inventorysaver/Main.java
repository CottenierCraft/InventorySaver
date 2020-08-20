package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class Main extends JavaPlugin {
	public static Main instance;
	private static File configFile;
	
	private InventorySaver inventorySaver;
	
	public Main() {
		super();
	}
	
	protected Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}
	
	@Override
	public void onEnable() {
		instance = this;
		configFile = new File(this.getDataFolder() + "/config.yml");
		
		try {
			if (configFile.createNewFile()) {
				getLogger().info("config.yml did not exist yet and has been created.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		inventorySaver = new InventorySaver();
		final PluginCommand inventoryCommand = this.getCommand("inventory");
		this.getServer().getPluginManager().registerEvents(inventorySaver, this);
		
		if (inventoryCommand != null) {
			inventoryCommand.setExecutor(inventorySaver);
			inventoryCommand.setTabCompleter(new TabComplete());
		}
		
		ControlledWorlds.loadFromFile();
	}
	
	public static File getConfigFile() {
		return configFile;
	}
	
	/**
	 * Sets the default inventory saver
	 * which loads/saves inventories
	 * when players join/leave worlds
	 * listed in the config.yml
	 */
	protected InventorySaver getDefaultInventorySaver() {
		return inventorySaver;
	}
	
	/**
	 * Sets the default inventory saver
	 * which loads/saves inventories
	 * when players join/leave worlds
	 * listed in the config.yml
	 * 
	 * @param inventorySaver New inventory saver which will load/save from now on
	 */
	protected void setDefaultInventorySaver(InventorySaver inventorySaver) {
		this.inventorySaver = inventorySaver;
	}
}
