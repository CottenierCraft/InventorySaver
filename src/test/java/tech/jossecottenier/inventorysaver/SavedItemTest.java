package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;

public class SavedItemTest {
	private final Random r = new Random();
	
	@Before
	public void setUp() {
		MockBukkit.mock();
	}
	
	@After
	public void tearDown() {
		MockBukkit.unmock();
	}
	
	private boolean fromSerializationEqualsItem(ItemStack item, InventorySaver inventorySaver) {
		final String serializedItem = new SavedItem(item).serialize();
		
		return SavedItem.deserialize(serializedItem, inventorySaver).equals(item);
	}
	
	private boolean fromSerializationEqualsItem(ItemStack item) {
		return fromSerializationEqualsItem(item, null);
	}
	
	@Test
	public void reconstructsStandardItem() {
		final ItemStack item = new ItemStack(Randomizer.createRandomMaterial());
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsStandardItemStack() {
		final ItemStack item = Randomizer.createRandomItemStack();
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsRenamedTool() {
		final ItemStack item = Randomizer.createRandomItemStack();
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Randomizer.createRandomDisplayName());
		item.setItemMeta(meta);
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsDamagedTool() {
		final ItemStack tool = new ItemStack(Randomizer.createRandomTool());
		final Damageable damageable = (Damageable)tool.getItemMeta();
		damageable.setDamage(r.nextInt(tool.getType().getMaxDurability()));
		tool.setItemMeta((ItemMeta)damageable);
		
		assertTrue(fromSerializationEqualsItem(tool));
	}
	
	// Skipping tests involving enchantments as MockBukkit currently doesn't work with those
	@Ignore
	@Test
	public void reconstructsEnchantedTool() {
		final ItemStack tool = new ItemStack(Randomizer.createRandomTool());
		tool.addEnchantments(Randomizer.createRandomEnchantments());
		
		assertTrue(fromSerializationEqualsItem(tool));
	}
	
	// Skipping test (see method above)
	@Ignore
	@Test
	public void reconstructsCompletelyRandomizedItem() {
		final ItemStack item = new ItemStack(Randomizer.createRandomTool());
		final ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(Randomizer.createRandomDisplayName());
		((Damageable)meta).setDamage(r.nextInt(item.getType().getMaxDurability()));
		
		item.setItemMeta(meta);
		item.addEnchantments(Randomizer.createRandomEnchantments());
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsCompletelyRandomizedItemWithoutEnchantments() {
		final ItemStack item = new ItemStack(Randomizer.createRandomTool());
		final ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(Randomizer.createRandomDisplayName());
		((Damageable)meta).setDamage(r.nextInt(item.getType().getMaxDurability()));
		
		item.setItemMeta(meta);
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsCustomItem() {
		final ItemStack customItem = Randomizer.createRandomCustomItem();
		final InventorySaver inventorySaver = new InventorySaver();
		inventorySaver.addCustomItem(customItem);
		
		assertTrue(fromSerializationEqualsItem(customItem, inventorySaver));
	}
}
