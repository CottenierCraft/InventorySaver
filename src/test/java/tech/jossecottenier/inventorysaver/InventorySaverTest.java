package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
	
	private InventorySaver inventorySaver;
	private PlayerMock tester;
	
	private WorldMock controlledWorldMock;
	private WorldMock notControlledWorldMock;
	
	private File worldInventoryFile;
	private FileConfiguration configuration;
	
	@Before
	public void setUp() {
		server = MockBukkit.mock();
		plugin = (Main)MockBukkit.load(Main.class);
		
		inventorySaver = new InventorySaver();
		
		controlledWorldMock = new WorldMock(Material.OAK_PLANKS, 5);
		notControlledWorldMock = new WorldMock(Material.BIRCH_PLANKS, 5);
		worldInventoryFile = new File(plugin.getDataFolder() + File.separator + controlledWorldMock.getName() + ".yml");
		
		try {
			if (worldInventoryFile.createNewFile()) {
				Bukkit.getLogger().info(controlledWorldMock.getName() + ".yml did not exist yet and has been created.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		configuration = YamlConfiguration.loadConfiguration(worldInventoryFile);
		
		tester = createRandomPlayer();
		final PlayerMock[] players = new PlayerMock[] { createRandomPlayer(), createRandomPlayer() };
		
		ControlledWorlds.addWorld(controlledWorldMock);
		
		for (final PlayerMock player : players) {
			server.addPlayer(player);
			inventorySaver.saveInventory(player, plugin);
		}
	}
	
	@After
	public void tearDown() {
		MockBukkit.unmock();
	}
	
	private boolean entryCreated(PlayerMock playerMock) {
		return configuration.getStringList(playerMock.getName()).size() > 0;
	}
	
	private boolean serializedInventoryMatchesPlayers(PlayerMock player) {
		final String serializationInfile = configuration.getString(player.getName());
		final String serialization = inventorySaver.serializeInventory(player);
		
		return serializationInfile.equals(serialization);
	}
	
	private Map<String,ItemStack[]> getLoadedInventoryAndLoadedSerialization(PlayerMock player) {
		final Map<String,ItemStack[]> inventoryAndSerialization = new HashMap<>();
		
		final ItemStack[] playerInventoryContents = player.getInventory().getContents();
		final ItemStack[] loadedSerialization = inventorySaver.loadInventoryContents(player, plugin);
		
		inventoryAndSerialization.put("inventory", playerInventoryContents);
		inventoryAndSerialization.put("serialization", loadedSerialization);
		
		return inventoryAndSerialization;
	}
	
	@Test
	public void fileGetsCreated() {
		assertTrue(worldInventoryFile.exists());
	}
	
	@Test
	public void entryCreatedForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			assertTrue(entryCreated(playerMock));
		}
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
	public void inventorySavedWhenLeavingControlledWorld() {
		final PlayerTeleportEvent leaveEvent = new PlayerTeleportEvent(tester, controlledWorldMock.getSpawnLocation(), notControlledWorldMock.getSpawnLocation());
		inventorySaver.onTeleport(leaveEvent, plugin);
		
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	@Test
	public void inventorySavedWhenQuittingFromControlledWorld() {
		final PlayerQuitEvent quitEvent = new PlayerQuitEvent(tester, null);
		inventorySaver.onQuit(quitEvent, plugin);
		
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	@Test
	public void inventoryLoadedWhenJoiningControlledWorld() {
		final PlayerTeleportEvent joinEvent = new PlayerTeleportEvent(tester, notControlledWorldMock.getSpawnLocation(), controlledWorldMock.getSpawnLocation());
		inventorySaver.onTeleport(joinEvent, plugin);
		final Map<String,ItemStack[]> inventoryAndSerialization = getLoadedInventoryAndLoadedSerialization(tester);
		
		assertArrayEquals(inventoryAndSerialization.get("inventory"), inventoryAndSerialization.get("serialization"));
	}
	
	@Test
	public void serializationCanBeUpdatedOnCommand() {
		final ItemStack[] newInventoryContents = createRandomInventoryContents();
		tester.getInventory().setContents(newInventoryContents);
		tester.performCommand("inventory save");
		
		assertTrue(serializedInventoryMatchesPlayers(tester));
	}
	
	@Test
	public void serializationCanBeLoadedOnCommand() {
		tester.getInventory().clear();
		tester.performCommand("inventory load");
		
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
