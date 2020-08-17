package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
	
	private boolean fromSerializationEqualsItem(ItemStack item) {
		final String serializedItem = new SavedItem(item).toString();
		
		return SavedItem.fromString(serializedItem).equals(item);
	}
	
	@Test
	public void reconstructsStandardItem() {
		final ItemStack item = new ItemStack(createRandomMaterial());
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsStandardItemStack() {
		final ItemStack item = createRandomItemStack();
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsRenamedTool() {
		final ItemStack item = createRandomItemStack();
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(createRandomDisplayName());
		item.setItemMeta(meta);
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	@Test
	public void reconstructsDamagedTool() {
		final ItemStack tool = new ItemStack(createRandomTool());
		final Damageable damageable = (Damageable)tool.getItemMeta();
		damageable.setDamage(r.nextInt(tool.getType().getMaxDurability()));
		tool.setItemMeta((ItemMeta)damageable);
		
		assertTrue(fromSerializationEqualsItem(tool));
	}
	
	// Skipping tests involving enchantments as MockBukkit currently doesn't work with those
	@Ignore
	@Test
	public void reconstructsEnchantedTool() {
		final ItemStack tool = new ItemStack(createRandomTool());
		tool.addEnchantments(createRandomEnchantments());
		
		assertTrue(fromSerializationEqualsItem(tool));
	}
	
	// Skipping test (see method above)
	@Ignore
	@Test
	public void reconstructsCompletelyRandomizedItem() {
		final ItemStack item = new ItemStack(createRandomTool());
		final ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(createRandomDisplayName());
		((Damageable)meta).setDamage(r.nextInt(item.getType().getMaxDurability()));
		
		item.setItemMeta(meta);
		item.addEnchantments(createRandomEnchantments());
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	public void reconstructsCompletelyRandomizedItemWithoutEnchantments() {
		final ItemStack item = new ItemStack(createRandomTool());
		final ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(createRandomDisplayName());
		((Damageable)meta).setDamage(r.nextInt(item.getType().getMaxDurability()));
		
		item.setItemMeta(meta);
		
		assertTrue(fromSerializationEqualsItem(item));
	}
	
	private Material createRandomMaterial() {
		final Material[] materials = Material.values();
		final Material material = materials[r.nextInt(materials.length)];
		
		// Filter out legacy items as MockBukkit throws an exception on those
		if (material.name().contains("LEGACY")) {
			return createRandomMaterial();
		}
		
		return material;
	}
	
	private Material createRandomTool() {
		final Material possibleTool = createRandomMaterial();
		
		if (possibleTool.getMaxDurability() > 0) {
			return possibleTool;
		}
		
		return createRandomTool();
	}
	
	private ItemStack createRandomItemStack() {
		final Material material = createRandomMaterial();
		return new ItemStack(material, r.nextInt(material.getMaxStackSize()) + 1);
	}
	
	private String createRandomDisplayName() {
		String string = "";
		final int length = r.nextInt(12);
		
		for (int i = 0; i < length; i++) {
			string += (char)r.nextInt(126);
		}
		
		return string;
	}
	
	private Map<Enchantment, Integer> createRandomEnchantments() {
		final Map<Enchantment, Integer> enchantmentMap = new HashMap<>();
		final int amount = r.nextInt(5);
		
		// Temporarily hard-coding an array as Enchantment.values() currently doesn't work with MockBukkit
		final Enchantment[] enchantments = new Enchantment[] {Enchantment.SILK_TOUCH, Enchantment.DAMAGE_ALL, Enchantment.ARROW_DAMAGE, Enchantment.ARROW_INFINITE, Enchantment.KNOCKBACK, Enchantment.DEPTH_STRIDER};
		for (int i = 0; i < amount; i++) {
			final Enchantment enchantment = enchantments[r.nextInt(enchantments.length)];
			Bukkit.getLogger().info(enchantment == null ? "NULL" : "NOT NULL");
			System.out.println(enchantment);
			enchantmentMap.put(enchantment, r.nextInt(enchantment.getMaxLevel()));
		}
		
		return enchantmentMap;
	}
}
