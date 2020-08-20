package tech.jossecottenier.inventorysaver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;

public class MainTest {
	private Main plugin;
	private File configFile;
	
	@Before
	public void setUp() {
		MockBukkit.mock();
		plugin = (Main)MockBukkit.load(Main.class);
		
		configFile = Main.getConfigFile();
	}
	
	@After
	public void tearDown() {
		MockBukkit.unmock();
	}
	
	@Test
	public void loadsCorrectly() {
		assertNotNull(plugin);
	}
	
	@Test
	public void createsConfigFile() {
		assertNotNull(configFile);
	}
	
	@Test
	public void correctConfigFilePath() {
		assertEquals(configFile.getPath(), plugin.getDataFolder() + File.separator + "config.yml");
	}
	
	@Test
	public void replacesDefaultInventorySaver() {
		final InventorySaver newInventorySaver = new InventorySaver();
		newInventorySaver.addCustomItem(Randomizer.createRandomCustomItem());
		
		plugin.setDefaultInventorySaver(newInventorySaver);
		assertEquals(plugin.getDefaultInventorySaver(), newInventorySaver);
	}
	
}
