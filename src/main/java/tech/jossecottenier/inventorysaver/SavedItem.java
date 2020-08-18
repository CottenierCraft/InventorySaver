package tech.jossecottenier.inventorysaver;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SavedItem {
	private final ItemStack item;
	
	public SavedItem(ItemStack item) {
		this.item = item;
	}
	
	/**
	 * Constructs an ItemStack out of a serialized
	 * item string.
	 * 
	 * @param string Serialized item string
	 * @return Deserialized item
	 */
	public static ItemStack deserialize(String string) {
		if (string.equals("")) {
			return null;
		}
		
		final Map<String, String> arguments = new HashMap<>();
		
		final String[] argumentStrings = string.split(",");
		for (String argumentString : argumentStrings) {
			final String[] keyValuePair = argumentString.split("=");
			final String key = keyValuePair[0];
			String value;
			
			if (keyValuePair.length > 1) {
				value = keyValuePair[1];
			} else {
				value = null;
			}
			
			arguments.put(key, value);
		}
		
		final Material material = Material.valueOf(arguments.get("m"));
		final int amount = Integer.valueOf(arguments.get("a"));
		final String displayName = arguments.get("n");
		final int damage = Integer.valueOf(arguments.get("d"));
		final Map<Enchantment,Integer> enchantments = new HashMap<>();
		
		final String enchantmentsString = arguments.get("e");
		String[] enchantmentKeyIdPairs = new String[0];
		
		if (enchantmentsString != null) {
			enchantmentKeyIdPairs = enchantmentsString.split(":");
		}
		
		for (final String keyIdPair : enchantmentKeyIdPairs) {
			final String[] segments = keyIdPair.split(":");
			final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(segments[0]));
			final int level = Integer.valueOf(segments[1]);
			
			enchantments.put(enchantment, level);
		}
		
		final ItemStack constructedItemStack = new ItemStack(material, amount);
		final ItemMeta meta = constructedItemStack.getItemMeta();
		
		if (displayName != null && !displayName.equals("null")) {
			meta.setDisplayName(displayName);
		}
		((Damageable)meta).setDamage(damage);
		constructedItemStack.setItemMeta(meta);
		constructedItemStack.addEnchantments(enchantments);
		
		return constructedItemStack;
	}
	
	/**
	 * Serializes the ItemStack passed in its
	 * constructor to an item string which can
	 * be saved.
	 * 
	 * @return The serialized item string.
	 */
	public String serialize() {
		if (item == null) {
			return "";
		}
		
		final ItemMeta meta = item.getItemMeta();
		
		final Material material = item.getType();
		final int amount = item.getAmount();
		final String displayName = meta.getDisplayName();
		final int damage = ((Damageable)meta).getDamage();
		final Map<Enchantment,Integer> enchantments = item.getEnchantments();
		
		String enchantmentsString = "";
		
		for (final Enchantment enchantment : enchantments.keySet()) {
			enchantmentsString += String.format("%s:%d,", enchantment.getKey().getKey(), item.getEnchantmentLevel(enchantment));
		}
		
		// Remove last index (ending comma)
		if (enchantmentsString.length() > 2) {
			enchantmentsString = enchantmentsString.substring(0, enchantmentsString.length() - 2);
		}
		
		return String.format("m=%s,a=%d,n=%s,d=%d,e=%s", material.name(), amount, displayName, damage, enchantmentsString);
	}
	
	@Override
	public String toString() {
		return item.toString();
	}
}
