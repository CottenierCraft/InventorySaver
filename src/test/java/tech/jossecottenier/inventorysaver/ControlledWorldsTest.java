package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;

public class ControlledWorldsTest {
	private ServerMock server;
	private Main plugin;
	
	private File worldsFile;
	private FileConfiguration configuration;
	private WorldMock fileWorldMock = new WorldMock();
	
	@Before
	public void setUp() {
		server = MockBukkit.mock();
		plugin = (Main)MockBukkit.load(Main.class);
		
		worldsFile = new File(plugin.getDataFolder() + File.separator + "worlds.yml");
		
		try {
			if (worldsFile.createNewFile()) {
				plugin.getLogger().info("worlds.yml did not exist yet and has been created.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		server.addWorld(fileWorldMock);
		simulateFile();
	}
	
	@After
	public void tearDown() {
		ControlledWorlds.getWorlds().clear();
		MockBukkit.unmock();
	}
	
	@Test
	public void returnsList() {
		assertTrue(ControlledWorlds.getWorlds() instanceof List<?>);
	}
	
	@Test
	public void listElementsAreWorlds() {
		for (Object worldObject : ControlledWorlds.getWorlds()) {
			assertTrue(worldObject instanceof World);
		}
	}
	
	@Test
	public void worldGetsAddedViaMethod() {
		final WorldMock worldMock = new WorldMock();
		
		ControlledWorlds.addWorld(worldMock);
		assertTrue(ControlledWorlds.getWorlds().contains(worldMock));
	}
	
	@Test
	public void worldGetsAddedViaFile() {
		assertTrue(ControlledWorlds.getWorlds().contains(fileWorldMock));
	}
	
	private void simulateFile() {
		configuration = YamlConfiguration.loadConfiguration(worldsFile);
		
		configuration.set("worlds", new String[] { fileWorldMock.getName() });
		
		try {
			configuration.save(worldsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ControlledWorlds.loadFromFile(worldsFile, server);
	}
}
