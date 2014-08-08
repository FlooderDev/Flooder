package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.kitskub.gamelib.Perm;
import me.kitskub.gamelib.Perm.AbstractPerm;
import me.kitskub.gamelib.framework.GameClass;
import me.kitskub.gamelib.framework.User;
import me.kitskub.flooder.Defaults;
import me.kitskub.gamelib.GameLib;
import me.kitskub.gamelib.utils.ChatUtils;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;


@SerializableAs("TBClass")
public class FClass implements GameClass {
    public static final FClass blank = new FClass("Blank");
    private final String name;
    private final List<ItemStack> items;
    private final Perm reguiredPerm;

    public FClass(String name) {
        this.name = name;
        this.items = new ArrayList<ItemStack>();
        this.reguiredPerm = new AbstractPerm(new Permission(Defaults.Perms.USER_CLASS.getPermission().getName() + "." + name), Defaults.Perms.USER_CLASS);
        reguiredPerm.getPermission().addParent(Defaults.Perms.USER_CLASS.getPermission(), false);
    }
    
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("items", items);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static FClass deserialize(Map<String, Object> map) {
        FClass impl = new FClass((String) map.get("name"));
        Object itemList = map.get("items");
        if (itemList != null) impl.items.addAll((List<ItemStack>) itemList);
        return impl;

    }

    @Override
    public void grantInitialItems(User p) {
        PlayerInventory inv = p.getPlayer().getInventory();

        // Fork over the items.
        for (ItemStack stack : items) {
            inv.addItem(stack);
        }
        
        p.getPlayer().updateInventory();
    }

    @Override
    public Perm getRequiredPermission() {
        return reguiredPerm;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public boolean checkPermission(User user) {
        if (!GameLib.hasPermission(user.getPlayer(), reguiredPerm)) {
            ChatUtils.error(user.getPlayer(), DEFAULT_NOPERM);
            return false;
        }
        return true;
    }

    @Override
    public String getChosenMessage() {
        return DEFAULT_CHOSEN;
    }

    @Override
    public List<String> getDescription() {
        return Collections.EMPTY_LIST;
    }
}
