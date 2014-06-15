package me.kitskub.flooder;

import static me.kitskub.flooder.utils.ConfigUtils.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

public class ItemConfig {
    public static final String CHANCE = "chance";

	public static Map<ItemStack, Double> getLoot(String itemset, Set<String> checked, String type, String value, double def) {
		Map<ItemStack, Double> chestLoot = new HashMap<ItemStack, Double>();
		if (checked.contains(itemset)) return chestLoot;
		for (Item i : getItemSection(Files.ITEMCONFIG.getConfig(), "itemsets." + itemset + "." + type)) {
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
	    List<Item> list = getItemSection(Files.ITEMCONFIG.getConfig(), itemSection);
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put(CHANCE, chance);
	    list.add(new Item(item, map));
	    Files.ITEMCONFIG.getConfig().set(itemSection, list);
	}

	public static Map<ItemStack, Double> getChestLoot() {
		Map<ItemStack, Double> chestLoot = new HashMap<ItemStack, Double>();
		for (Item i : getItemSection(Files.ITEMCONFIG.getConfig(), "chest-loot")) {
			Object get = i.getValues().get(CHANCE);
			chestLoot.put(i.getStack(), (get instanceof Double) ? (Double) get : .333);
		}
		return chestLoot;
	}
}
