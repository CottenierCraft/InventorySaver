package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class InventorySaverTest {
	private ServerMock server;
	private Main plugin;
	
	private PlayerMock tester;
	
	private File worldInventoryFile;
	private FileConfiguration configuration;
	
	@Before
	public void setUp() {
		server = MockBukkit.mock();
		plugin = (Main)MockBukkit.load(Main.class);
		
		final WorldMock worldMock = new WorldMock();
		worldInventoryFile = new File(plugin.getDataFolder() + File.separator + worldMock.getName() + ".yml");
		
		try {
			if (worldInventoryFile.createNewFile()) {
				Bukkit.getLogger().info(worldMock.getName() + ".yml did not exist yet and has been created.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		configuration = YamlConfiguration.loadConfiguration(worldInventoryFile);
		
		tester = createRandomPlayer();
		final PlayerMock[] players = new PlayerMock[] { createRandomPlayer(), createRandomPlayer() };
		
		ControlledWorlds.addWorld(worldMock);
		
		for (final PlayerMock player : players) {
			server.addPlayer(player);
			InventorySaver.saveInventory(player, plugin);
		}
	}
	
	@After
	public void tearDown() {
		MockBukkit.unmock();
	}
	
	@Test
	public void fileGetsCreated() {
		assertTrue(worldInventoryFile.exists());
	}
	
	private boolean entryCreated(PlayerMock playerMock) {
		return configuration.getStringList(playerMock.getName()).size() > 0;
	}
	
	@Test
	public void entryCreatedForFirstPlayer() {
		assertTrue(entryCreated(tester));
	}
	
	@Test
	public void entryCreatedForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			assertTrue(entryCreated(playerMock));
		}
	}
	
	private boolean serializedInventoryMatchesPlayers(PlayerMock player) {
		final List<String> serializationInfile = configuration.getStringList(player.getName());
		final List<String> serialization = InventorySaver.serializeInventory(player);
		
		return serializationInfile.equals(serialization);
	}
	
	@Test
	public void serializedInventoryIsCorrectForFirstPlayer() {
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	@Test
	public void serializedInventoryIsCorrectForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			assertTrue(serializedInventoryMatchesPlayers(playerMock));
		}
	}
	
	@Test
	public void serializationCanBeUpdatedOnCommand() {
		final ItemStack[] newInventoryContents = createRandomInventoryContents();
		tester.getInventory().setContents(newInventoryContents);
		tester.performCommand("inventorsaver save");
		
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	@Test
	public void serializationCanBeLoadedOnCommand() {
		tester.getInventory().clear();
		tester.performCommand("inventorsaver load");
		
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	private ItemStack[] createRandomInventoryContents() {
		final ItemStack[] inventoryContents = new ItemStack[41];
		final Random r = new Random();
		
		for (int i = 0; i < 41; i++) {
			if (r.nextBoolean()) {
				final Material[] materials = Material.values();
				inventoryContents[i] = new ItemStack(materials[r.nextInt(materials.length - 1)], r.nextInt(64));
			}
		}
		
		return inventoryContents;
	}
	
	private PlayerMock createRandomPlayer() {
		final PlayerMock player = new PlayerMock(server, "tester-" + server.getOnlinePlayers().size());
		
		player.getInventory().setContents(createRandomInventoryContents());
		return player;
	}
}
