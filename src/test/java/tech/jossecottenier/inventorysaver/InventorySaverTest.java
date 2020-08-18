package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
	private PlayerMock firstPlayer;
	
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
		
		final PlayerMock[] players = new PlayerMock[] { Randomizer.createRandomPlayer(server, 0), Randomizer.createRandomPlayer(server, 1) };
		
		ControlledWorlds.addWorld(controlledWorldMock);
		
		boolean first = true;
		for (final PlayerMock player : players) {
			if (first) {
				firstPlayer = player;
				first = false;
			}
			
			server.addPlayer(player);
		}
	}
	
	@After
	public void tearDown() {
		MockBukkit.unmock();
	}
	
	private String createdEntry(PlayerMock player) {
		// Update configuration file
		configuration = YamlConfiguration.loadConfiguration(worldInventoryFile);
		
		return configuration.getString(player.getName());
	}
	
	private boolean serializedInventoryMatchesPlayers(PlayerMock player) {
		// Update configuration file
		configuration = YamlConfiguration.loadConfiguration(worldInventoryFile);
		
		final String serializationInFile = configuration.getString(player.getName());
		final String serialization = inventorySaver.serializeInventory(player);
		
		// "serialization" should go first as "serializationInFile" is nullable
		return serialization.equals(serializationInFile);
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
		inventorySaver.saveInventory(firstPlayer);
		assertTrue(worldInventoryFile.exists());
	}
	
	@Test
	public void entryCreatedForFirstPlayer() {
		inventorySaver.saveInventory(firstPlayer);
		assertNotNull(createdEntry(firstPlayer));
	}
	
	@Test
	public void entryCreatedForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			inventorySaver.saveInventory(playerMock);
			assertNotNull(createdEntry(playerMock));
		}
	}
	
	@Test
	public void serializedInventoryIsCorrectForFirstPlayer() {
		inventorySaver.saveInventory(firstPlayer);
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void serializedInventoryIsCorrectForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			inventorySaver.saveInventory(playerMock);
			assertTrue(serializedInventoryMatchesPlayers(playerMock));
		}
	}
	
	@Test
	public void inventorySavedWhenLeavingControlledWorld() {
		final PlayerTeleportEvent leaveEvent = new PlayerTeleportEvent(firstPlayer, controlledWorldMock.getSpawnLocation(), notControlledWorldMock.getSpawnLocation());
		inventorySaver.onTeleport(leaveEvent, plugin);
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void inventorySavedWhenQuittingFromControlledWorld() {
		// Simulating event of quitting from a controlled world
		firstPlayer.teleport(controlledWorldMock.getSpawnLocation());
		final PlayerQuitEvent quitEvent = new PlayerQuitEvent(firstPlayer, null);
		inventorySaver.onQuit(quitEvent, plugin);
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void inventoryLoadedWhenJoiningControlledWorld() {
		// Save inventory and clear
		inventorySaver.saveInventory(firstPlayer, plugin);
		firstPlayer.getInventory().clear();
		
		final PlayerTeleportEvent joinEvent = new PlayerTeleportEvent(firstPlayer, notControlledWorldMock.getSpawnLocation(), controlledWorldMock.getSpawnLocation());
		inventorySaver.onTeleport(joinEvent, plugin);
		final Map<String,ItemStack[]> inventoryAndSerialization = getLoadedInventoryAndLoadedSerialization(firstPlayer);
		
		assertArrayEquals(inventoryAndSerialization.get("inventory"), inventoryAndSerialization.get("serialization"));
	}
	
	@Test
	public void serializationCanBeUpdatedOnCommand() {
		final ItemStack[] newInventoryContents = Randomizer.createRandomInventoryContents();
		firstPlayer.getInventory().setContents(newInventoryContents);
		firstPlayer.performCommand("inventory save");
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void serializationCanBeLoadedOnCommand() {
		firstPlayer.getInventory().clear();
		firstPlayer.performCommand("inventory load");
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
}
