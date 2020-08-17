package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
	
	private boolean serializedInventoryMatchesPlayers(PlayerMock player) {
		final String serializationInFile = configuration.getString(player.getName());
		final String serialization = inventorySaver.serializeInventory(player);
		
		return serializationInFile.equals(serialization);
	}
	
	private Map<String,ItemStack[]> getLoadedInventoryAndLoadedSerialization(PlayerMock player) {
		final Map<String,ItemStack[]> inventoryAndSerialization = new HashMap<>();
		
		final ItemStack[] playerInventoryContents = player.getInventory().getContents();
		final ItemStack[] loadedSerialization = inventorySaver.loadInventoryContents(player, plugin);
		
		inventoryAndSerialization.put("inventory", playerInventoryContents);
		inventoryAndSerialization.put("serialization", loadedSerialization);
		
		return inventoryAndSerialization;
	}
	
	private void saveInventoryAndUpdateConfiguration(PlayerMock player) {
		inventorySaver.saveInventory(player);
		configuration = YamlConfiguration.loadConfiguration(worldInventoryFile);
	}
	
	@Test
	public void fileGetsCreated() {
		saveInventoryAndUpdateConfiguration(firstPlayer);
		assertTrue(worldInventoryFile.exists());
	}
	
	@Test
	public void entryCreatedForFirstPlayer() {
		saveInventoryAndUpdateConfiguration(firstPlayer);
		assertNotNull(configuration.getString(firstPlayer.getName()));
	}
	
	@Test
	public void entryCreatedForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			saveInventoryAndUpdateConfiguration(playerMock);
			assertNotNull(configuration.getString(playerMock.getName()));
		}
	}
	
	@Test
	public void serializedInventoryIsCorrectForFirstPlayer() {
		saveInventoryAndUpdateConfiguration(firstPlayer);
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void serializedInventoryIsCorrectForAllPlayers() {
		for (final PlayerMock playerMock : server.getOnlinePlayers()) {
			saveInventoryAndUpdateConfiguration(playerMock);
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
		final PlayerQuitEvent quitEvent = new PlayerQuitEvent(firstPlayer, null);
		inventorySaver.onQuit(quitEvent, plugin);
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void inventoryLoadedWhenJoiningControlledWorld() {
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
