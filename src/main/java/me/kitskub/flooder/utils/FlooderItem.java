package me.kitskub.flooder.utils;

import java.util.Map;
import me.kitskub.flooder.Flooder;
import me.kitskub.flooder.core.FlooderEffectItem;
import me.kitskub.gamelib.framework.EffectItem;
import me.kitskub.gamelib.utils.config.Item;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

@SerializableAs("FlooderItem")
public class FlooderItem extends Item {
    private final String effectItem;
    public FlooderItem(FlooderEffectItem effectItem, Map<String, Object> values) {
        super(null, values);
        this.effectItem = effectItem.getName();
    }

    public FlooderItem(Map<String, Object> map) {
        super(map);
        this.effectItem = (String) map.get("effectItem");
    }

    @Override
    public ItemStack getStack() {
        EffectItem get = Flooder.getInstance().getEffectItemManager().get(effectItem);
        return get == null ? null : get.toItemStack();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = super.serialize();
        serialized.put("effectItem", effectItem);
        return serialized;
    }

}
