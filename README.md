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

### Via the API

Controlled worlds can also be updated dynamically via the API:

```java
import tech.jossecottenier.inventorysaver.ControlledWorlds;

ControlledWorlds.add(Bukkit.getWorld("AWorld")); // World will be controlled until it gets removed
```

Furthermore, the `InventorySaver` methods can also be used from the outside, each of them contain documentation comments.

```java
import tech.jossecottenier.inventorysaver.InventorySaver;

final InventorySaver inventorySaver = new InventorySaver();
final Player player = Bukkit.getPlayer("APlayer");

final String serialization = inventorySaver.serializeInventory(player); // Will return serialized string of "APlayer"'s inventory
final ItemStack[] inventoryContents = inventorySaver.loadInventoryContents(serialization); // Will deserialize the string and return "APlayer"'s inventory contents
```

## Help and contributions

This project is pretty much written on a holiday and is not meant to become popular in any means, but this naturally does not include your possibility of helping out and helping it reach its fullest potential. Therefore, pull requests are welcome and even encouraged, and I will always be happy to be contacted.

- Josse