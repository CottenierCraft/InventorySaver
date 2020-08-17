package tech.jossecottenier.inventorysaver;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class InventorySaver implements Listener {
	
	public String serializeInventory(Player player) {
		final PlayerInventory inventory = player.getInventory();
		
		return null;
	}
	
	protected void saveInventory(Player player, JavaPlugin plugin) {
		
	}
	
	public void saveInventory(Player player) {
		saveInventory(player, Main.instance);
	}
	
	protected ItemStack[] loadInventoryContents(Player player, JavaPlugin plugin) {
		return null;
	}
	
	public ItemStack[] loadInventoryContents(Player player) {
		return loadInventoryContents(player, Main.instance);
	}
	
	protected void onTeleport(PlayerTeleportEvent event, JavaPlugin plugin) {
		
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		onTeleport(event, Main.instance);
	}
	
	protected void onQuit(PlayerQuitEvent event, JavaPlugin plugin) {
		
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		onQuit(event, Main.instance);
	}
}
