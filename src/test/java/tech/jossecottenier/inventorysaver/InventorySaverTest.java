package tech.jossecottenier.inventorysaver;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import static org.junit.Assert.*;

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
		final String serialization = inventorySaver.serializeInventory(player.getInventory());
		
		// "serialization" should go first as "serializationInFile" is nullable
		return serialization.equals(serializationInFile);
	}
	
	// Append "null" at the end of an ItemStack array to match lengths
	// (This will in practice never be necessary but is needed here to compare the arrays)
	private ItemStack[] appendNullToMatchLength(ItemStack[] original, int length) {
		final ItemStack[] originalWithCorrectLength = new ItemStack[length];
		for (int i = 0; i < original.length; i++) {
			originalWithCorrectLength[i] = original[i];
		}
		
		return originalWithCorrectLength;
	}

	private ItemStack[] appendNullToMatchLength(ItemStack[] original) {
		return appendNullToMatchLength(original, 41);
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
	public void correctlyReconstructsGeneralInventories() {
		final Inventory inventory = Randomizer.createRandomInventory();
		final String serialization = inventorySaver.serializeInventory(inventory);
		
		final ItemStack[] deserialization = inventorySaver.deserializeInventory(serialization);
		final ItemStack[] inventoryContents = inventory.getContents();
		
		assertArrayEquals(inventoryContents, appendNullToMatchLength(deserialization, inventoryContents.length));
	}
	
	@Test
	public void correctlyLoadsCustomItem() {
		final ItemStack customItem = Randomizer.createRandomCustomItem();
		inventorySaver.addCustomItem(customItem);
		
		final Inventory inventory = Randomizer.createRandomInventory();
		final ItemStack[] playerInventoryContents = Randomizer.createRandomInventoryContentsIncluding(customItem, inventory.getSize());
		inventory.setContents(playerInventoryContents);
		
		final ItemStack[] reconstructedContents = inventorySaver.deserializeInventory(inventorySaver.serializeInventory(inventory));
		
		assertTrue(ArrayUtils.contains(reconstructedContents, customItem));
	}
	
	@Test
	public void inventorySaverGetsUpdatedViaMethod() {
		final InventorySaver newInventorySaver = new InventorySaver();
		newInventorySaver.addCustomItem(Randomizer.createRandomCustomItem());
		InventorySaver.setDefaultInventorySaver(newInventorySaver, plugin);
		
		assertEquals(plugin.getDefaultInventorySaver(), newInventorySaver);
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
		
		final ItemStack[] inventoryContents = inventoryAndSerialization.get("inventory");
		final ItemStack[] deserializedContents = inventoryAndSerialization.get("serialization");
		
		assertArrayEquals(inventoryContents, appendNullToMatchLength(deserializedContents, inventoryContents.length));
	}
	
	@Test
	public void customItemsGetLoadedWhenJoiningControlledWorldAfterUpdatingInventorySaver() {
		final ItemStack customItem = Randomizer.createRandomCustomItem();
		final ItemStack[] inventoryContents = Randomizer.createRandomInventoryContentsIncluding(customItem);
		firstPlayer.getInventory().setContents(inventoryContents);
		inventorySaver.saveInventory(firstPlayer, plugin);
		firstPlayer.getInventory().clear();
		
		final InventorySaver newInventorySaver = new InventorySaver();
		newInventorySaver.addCustomItem(customItem);
		InventorySaver.setDefaultInventorySaver(newInventorySaver);
		
		final PlayerTeleportEvent joinEvent = new PlayerTeleportEvent(firstPlayer, notControlledWorldMock.getSpawnLocation(), controlledWorldMock.getSpawnLocation());
		inventorySaver.onTeleport(joinEvent, plugin);
		final ItemStack[] loadedInventoryContents = firstPlayer.getInventory().getContents();
		
		assertArrayEquals(loadedInventoryContents, appendNullToMatchLength(inventoryContents, loadedInventoryContents.length));
	}

	@Test
	public void inventoryDoesNotLoadWhenTeleportedOnJoin() {
		// Teleporting to controlled world and quitting
		firstPlayer.teleport(controlledWorldMock.getSpawnLocation());
		server.getOnlinePlayers().remove(firstPlayer);
		inventorySaver.onQuit(new PlayerQuitEvent(firstPlayer, null), plugin);

		firstPlayer.getInventory().clear();

		server.addPlayer(firstPlayer);
		// Immediately teleporting (simulating teleport on join)
		firstPlayer.teleport(notControlledWorldMock.getSpawnLocation());
		final ItemStack[] newInventoryContents = firstPlayer.getInventory().getContents();

		assertArrayEquals(new ItemStack[41], newInventoryContents);
	}
	
	@Test
	public void serializationCanBeUpdatedOnCommand() {
		// Randomize inventory and simulate command
		final ItemStack[] newInventoryContents = Randomizer.createRandomInventoryContents();
		firstPlayer.getInventory().setContents(newInventoryContents);
		firstPlayer.performCommand("inventory save");
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
	
	@Test
	public void serializationCanBeLoadedOnCommand() {
		inventorySaver.saveInventory(firstPlayer, plugin);
		firstPlayer.getInventory().clear();
		firstPlayer.performCommand("inventory load");
		
		assertTrue(serializedInventoryMatchesPlayers(firstPlayer));
	}
}
