package tech.jossecottenier.inventorysaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class Randomizer {
	private static final Random r = new Random();
	
	public static Material createRandomMaterial() {
		final Material[] materials = Material.values();
		final Material material = materials[r.nextInt(materials.length)];
		
		// Filter out legacy items as MockBukkit throws an exception on those and air
		if (material.name().contains("LEGACY") || material == Material.AIR) {
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
	
	public static ItemStack createRandomCustomItem() {
		final ItemStack customItem = new ItemStack(createRandomMaterial());
		final ItemMeta meta = customItem.getItemMeta();
		
		final ChatColor[] chatColors = ChatColor.values();
		final String displayName = chatColors[r.nextInt(chatColors.length)] + createRandomDisplayName();
		final int loreLength = r.nextInt(5) + 1;
		final List<String> lore = new ArrayList<>();
		
		for (int i = 0; i < loreLength; i++) {
			lore.add(chatColors[r.nextInt(chatColors.length)] + createRandomDisplayName());
		}
		
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		customItem.setItemMeta(meta);
		
		return customItem;
	}
	
	public static Inventory createRandomInventory() {
		final int size = (r.nextInt(6) + 1) * 9;
		final Inventory inventory = Bukkit.createInventory(null, size);
		inventory.setContents(createRandomInventoryContents(size));
		
		return inventory;
	}
	
	public static ItemStack[] createRandomInventoryContents(int size) {
		final ItemStack[] inventoryContents = new ItemStack[size];
		final Random r = new Random();
		
		for (int i = 0; i < size; i++) {
			if (r.nextBoolean()) {
				ItemStack item;
				if (r.nextBoolean()) {
					item = Randomizer.createRandomItemStack();
				} else {
					item = Randomizer.createRandomModifiedTool();
				}
				
				inventoryContents[i] = item;
			}
		}
		
		return inventoryContents;
	}
	
	public static ItemStack[] createRandomInventoryContents() {
		return createRandomInventoryContents(41);
	}
	
	public static ItemStack[] createRandomInventoryContentsIncluding(ItemStack item, int size) {
		final ItemStack[] inventoryContents = createRandomInventoryContents(size);
		inventoryContents[r.nextInt(inventoryContents.length)] = item;
		
		return inventoryContents;
	}
	
	public static ItemStack[] createRandomInventoryContentsIncluding(ItemStack item) {
		return createRandomInventoryContentsIncluding(item, 41);
	}
	
	public static PlayerMock createRandomPlayer(ServerMock server, int id) {
		final PlayerMock player = new PlayerMock(server, "tester-" + id);
		
		player.getInventory().setContents(createRandomInventoryContents());
		return player;
	}
}
