# InventorySaver

InventorySaver is a lightweight plugin which can be used to save player inventories in specific worlds which can be handy on multi-world servers, and which can using the API be used to save, (de)serialize and load inventories in general.

## Usage

### Via config.yml

In the `config.yml`, a section "worlds" can be created with the name of worlds saved on the server on which the plugin is run. When listed there, the world will be considered a "controlled world". This means that a player's inventory will be serialized and saved when they leave this world, and that this save will be deserialized and loaded into their inventory when they join that world again.

```yml
# Inside the config.yml

worlds:
  - AWorld
  - AnotherWorld
```

### Via in-game commands

In-game, an inventory can also be saved using the `/inventory save` command to save it and later on the `/inventory load` command to load it. The InventorySaver instance used for saving and loading items via these commands can be specified using the API (see underneath).

### Via the API

Controlled worlds can also be updated dynamically via the API:

```java
import tech.jossecottenier.inventorysaver.ControlledWorlds;

ControlledWorlds.addWorld(Bukkit.getWorld("AWorld")); // World will be controlled until it gets removed
```

Furthermore, the `InventorySaver` methods can also be used from the outside, each of them contain documentation comments.

```java
import tech.jossecottenier.inventorysaver.InventorySaver;

final InventorySaver inventorySaver = new InventorySaver();
final Player player = Bukkit.getPlayer("APlayer");

final String serialization = inventorySaver.serializeInventory(player); // Will return serialized string of "APlayer"'s inventory
final ItemStack[] inventoryContents = inventorySaver.loadInventoryContents(serialization); // Will deserialize the string and return "APlayer"'s inventory contents
```

#### Support for custom items

This plugin also offers support for custom items. An ItemStack can be registered as a custom item to an `InventorySaver` instance, and from then on a serialized item which name matches a registered custom item will be loaded as said custom item. A custom item can be registered as following:

```java
import tech.jossecottenier.inventorysaver.InventorySaver;

final ItemStack customItem = <custom item with custom displayname, lore, ...>;
final InventorySaver inventorySaver = new InventorySaver();
inventorySaver.addCustomItem(customItem); // From now on the custom item will be correctly saved and loaded
```

If you want this InventorySaver instance to be used when loading inventories through the commands or through events (when joining a controlled world), you will need to update the default InventorySaver as following:

```java
InventorySaver.setDefaultInventorySaver(<new InventorySaver instance>);
```

## Help and contributions

This project is pretty much written on a holiday and is not meant to become popular by any means, but this naturally does not exclude your possibility of helping out and helping it reach its fullest potential. Therefore, pull requests are welcome and even encouraged, and I will always be happy to be contacted.

- Josse