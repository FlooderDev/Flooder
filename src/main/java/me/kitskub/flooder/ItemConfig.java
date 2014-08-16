package me.kitskub.flooder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.kitskub.gamelib.utils.ConfigUtils;

import me.kitskub.gamelib.utils.config.Item;
import org.bukkit.inventory.ItemStack;

public class ItemConfig {
    public static final String CHANCE = "chance";

	public static Map<ItemStack, Double> getLoot(String itemset, Set<String> checked, String type, String value, double def) {
		Map<ItemStack, Double> chestLoot = new HashMap<>();
		if (checked.contains(itemset)) return chestLoot;
		for (Item i : ConfigUtils.getItemSection(Files.ITEMCONFIG.getConfig(), "itemsets." + itemset + "." + type)) {
			Object get = i.getValues().get(value);
			chestLoot.put(i.getStack(), (get instanceof Double) ? (Double) get : def);
		}
		checked.add(itemset);
		for (String parent : Files.ITEMCONFIG.getConfig().getStringList("itemsets." + itemset + ".inherits")) {
			checked.add(parent);
			chestLoot.putAll(getLoot(parent, checked, type, value, def));
		}
		return chestLoot;
	}

	/**
	 * Adds itemstack to chestLoot of the itemset provided, or global if itemset is empty or null
	 * @param item
	 * @param chance
	 */
	public static void addChestLoot(ItemStack item, double chance){
	    String itemSection = "chest-loot";
	    List<Item> list = ConfigUtils.getItemSection(Files.ITEMCONFIG.getConfig(), itemSection);
	    Map<String, Object> map = new HashMap<>();
	    map.put(CHANCE, chance);
	    list.add(new Item(item, map));
	    Files.ITEMCONFIG.getConfig().set(itemSection, list);
	}

	public static Map<ItemStack, Double> getChestLoot() {
		Map<ItemStack, Double> chestLoot = new HashMap<>();
		for (Item i : ConfigUtils.getItemSection(Files.ITEMCONFIG.getConfig(), "chest-loot")) {
			Object get = i.getValues().get(CHANCE);
			chestLoot.put(i.getStack(), (get instanceof Double) ? (Double) get : .333);
		}
		return chestLoot;
	}
}
