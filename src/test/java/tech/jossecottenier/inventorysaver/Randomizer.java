package tech.jossecottenier.inventorysaver;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class Randomizer {
	private static final Random r = new Random();
	
	public static Material createRandomMaterial() {
		final Material[] materials = Material.values();
		final Material material = materials[r.nextInt(materials.length)];
		
		// Filter out legacy items as MockBukkit throws an exception on those
		if (material.name().contains("LEGACY")) {
			return createRandomMaterial();
		}
		
		return material;
	}
	
	public static Material createRandomTool() {
		final Material possibleTool = createRandomMaterial();
		
		if (possibleTool.getMaxDurability() > 0) {
			return possibleTool;
		}
		
		return createRandomTool();
	}
	
	public static ItemStack createRandomItemStack() {
		final Material material = createRandomMaterial();
		return new ItemStack(material, r.nextInt(material.getMaxStackSize()) + 1);
	}
	
	public static String createRandomDisplayName() {
		String string = "";
		final int length = r.nextInt(12) + 1;
		
		for (int i = 0; i < length; i++) {
			if (r.nextInt(4) == 3) {
				string += r.nextInt(10);
			} else {
				char character = (char)(r.nextInt(26) + 97);
				if (r.nextBoolean()) character -= 32;
				
				string += character;
			}
		}
		
		return string;
	}
	
	public static Map<Enchantment, Integer> createRandomEnchantments() {
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
	
	public static ItemStack createRandomModifiedTool() {
		final ItemStack tool = new ItemStack(createRandomTool());
		final ItemMeta meta = tool.getItemMeta();
		
		meta.setDisplayName(createRandomDisplayName());
		((Damageable)meta).setDamage(r.nextInt(tool.getType().getMaxDurability()));
		
		tool.setItemMeta(meta);
		
		return tool;
	}
}
