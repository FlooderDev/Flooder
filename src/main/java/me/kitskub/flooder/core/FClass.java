package me.kitskub.flooder.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.kitskub.gamelib.Perm;
import me.kitskub.gamelib.Perm.AbstractPerm;
import me.kitskub.gamelib.framework.Class;
import me.kitskub.gamelib.framework.EffectItem;
import me.kitskub.gamelib.framework.User;
import me.kitskub.flooder.Defaults;
import me.kitskub.flooder.Logging;
import me.kitskub.flooder.Flooder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;


@SerializableAs("TBClass")
public class FClass implements Class {
    private final String name;
    private final List<ItemStack> items;
    private final Map<String,Boolean> perms;
    private final Perm reguiredPerm;

    public FClass(String name) {
        this.name = name;
        this.perms = new HashMap<String, Boolean>();
        this.items = new ArrayList<ItemStack>();
        this.reguiredPerm = new AbstractPerm(new Permission(Defaults.Perms.USER_CLASS.getPermission().getName() + "." + name), Defaults.Perms.USER_CLASS);
        reguiredPerm.getPermission().addParent(Defaults.Perms.USER_CLASS.getPermission(), false);
    }
    
    
    public String getName() {
        return name;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        for (String s : perms.keySet()) {
            map.put("perms." + s, perms.get(s));
        }
        map.put("items", items);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static FClass deserialize(Map<String, Object> map) {
        FClass impl = new FClass((String) map.get("name"));
        Object perms = map.get("perms");
        if (perms instanceof ConfigurationSection) {
            for (String s : ((ConfigurationSection) perms).getKeys(false)) {
                impl.perms.put(s, ((ConfigurationSection) perms).getBoolean(s));
            }
        }
        Object itemList = map.get("items");
        if (itemList != null) impl.items.addAll((List<ItemStack>) itemList);
        return impl;

    }

    public PermissionAttachment grantPermissions(User user) {
        if (perms.isEmpty()) return null;
        
        PermissionAttachment pa = user.addAttachment();
        
        for (Map.Entry<String,Boolean> entry : perms.entrySet()) {
            try {
                pa.setPermission(entry.getKey(), entry.getValue());
            }
            catch (Exception e) {
                String perm   = entry.getKey() + ":" + entry.getValue();
                String player = user.getPlayer().getName();
                Logging.warning("[PERM00] Failed to attach permission '" + perm + "' to player '" + player + " with class " + name
                                + "'.\nPlease verify that your class permissions are well-formed.");
            }
        }
        return pa;
    }
    
    public void grantInitialItems(User p) {
        PlayerInventory inv = p.getPlayer().getInventory();

        // Fork over the items.
        for (ItemStack stack : items) {
            inv.addItem(stack);
        }
        
        p.getPlayer().updateInventory();
    }

    public Perm getRequiredPermission() {
        return reguiredPerm;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
