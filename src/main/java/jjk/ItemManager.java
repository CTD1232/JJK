package jjk;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemManager {
    private static final Map<String, String> TRIGGER_NAMES = new LinkedHashMap<>();
    static {
        TRIGGER_NAMES.put("right_click", "Right-click");
        TRIGGER_NAMES.put("shift_right_click", "Sneak + Right-click");
        TRIGGER_NAMES.put("left_click", "Left-click");
        TRIGGER_NAMES.put("shift_left_click", "Sneak + Left-click");
        TRIGGER_NAMES.put("swap", "Swap hands");
        TRIGGER_NAMES.put("shift_swap", "Sneak + Swap hands");
    }

    private final JJKPlugin plugin;
    private final NamespacedKey idKey;

    public ItemManager(JJKPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "item");
    }

    public Set<String> ids() {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("items");
        return s == null ? Collections.emptySet() : s.getKeys(false);
    }

    private Component line(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy)
                .decoration(TextDecoration.ITALIC, false);
    }

    public ItemStack create(String id) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("items." + id);
        if (s == null) return null;
        Material mat = Material.matchMaterial(s.getString("material", "PAPER"));
        if (mat == null) mat = Material.PAPER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(line(s.getString("name", id)));
        List<Component> lore = new ArrayList<>();
        for (String l : s.getStringList("lore")) lore.add(line(l));
        ConfigurationSection ab = s.getConfigurationSection("abilities");
        if (ab != null) {
            lore.add(Component.empty());
            for (Map.Entry<String, String> e : TRIGGER_NAMES.entrySet()) {
                String tech = ab.getString(e.getKey());
                if (tech == null) continue;
                var t = plugin.techniques().get(tech);
                String shown = t == null ? tech : t.display();
                lore.add(line("&8" + e.getValue() + ": &7" + shown));
            }
        }
        meta.lore(lore);
        if (s.contains("model")) meta.setCustomModelData(s.getInt("model"));
        String itemModel = s.getString("item_model");
        if (itemModel != null) {
            NamespacedKey k = NamespacedKey.fromString("jjk:" + itemModel);
            if (k != null) meta.setItemModel(k);
        }
        if (s.getBoolean("glow", false)) meta.setEnchantmentGlintOverride(true);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public String itemId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public boolean isJJKItem(ItemStack stack) { return itemId(stack) != null; }

    /** Technique id bound to this trigger on this item, or null. */
    public String ability(ItemStack stack, String trigger) {
        String id = itemId(stack);
        if (id == null) return null;
        return plugin.getConfig().getString("items." + id + ".abilities." + trigger);
    }
}
