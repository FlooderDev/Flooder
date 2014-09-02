package me.kitskub.flooder.core.effectitems;

import java.util.Collections;
import java.util.Map;
import me.kitskub.flooder.core.FlooderEffectItem;
import me.kitskub.gamelib.framework.User;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SerializableAs(Knockback.NAME)
public class Knockback extends FlooderEffectItem {
    public static final String NAME = "Knockback";
    public static final Knockback INSTANCE = new Knockback();

    private Knockback() {
        super(NAME);
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(Material.STICK, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onHurtWith(User damager, User damagee, EntityDamageByEntityEvent event) {
        event.setCancelled(true);
        damagee.getPlayer().setVelocity(damager.getPlayer().getLocation().getDirection().normalize().multiply(10));
    }

    @Override
    public void onInteractWith(User interacter, User interactee, PlayerInteractEntityEvent event) {}

    @Override
    public void onInteractWith(User interacter, PlayerInteractEvent event) {}

    @Override
    public Map<String, Object> serialize() {
        return Collections.EMPTY_MAP;
    }

    public static Knockback deserialize(Map<String, Object> map) {
        return INSTANCE;

    }
}
