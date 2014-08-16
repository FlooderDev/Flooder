package me.kitskub.flooder.core;

import me.kitskub.flooder.Flooder;
import me.kitskub.gamelib.EffectItemPlugin;
import me.kitskub.gamelib.framework.EffectItem;

public abstract class FlooderEffectItem implements EffectItem {
    protected final String name;

    public FlooderEffectItem(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public EffectItemPlugin<?, ?> getOwningPlugin() {
        return Flooder.getInstance();
    }
}
