package jjk;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Toji's Heavenly Restriction: no cursed energy, huge physical boost, melee ignores Infinity. */
public class HeavenlyRestrictionManager {
    private final JJKPlugin plugin;
    private final Set<UUID> active = new HashSet<>();

    public HeavenlyRestrictionManager(JJKPlugin plugin) { this.plugin = plugin; }

    public boolean isActive(Player p) { return active.contains(p.getUniqueId()); }

    private double num(String k, double d) { return plugin.getConfig().getDouble("techniques.heavenly_restriction." + k, d); }

    public void enable(Player p) {
        active.add(p.getUniqueId());
        plugin.energy().set(p, 0);
        mod(p, Attribute.MOVEMENT_SPEED, "hr_speed", num("speed", 0.5), AttributeModifier.Operation.ADD_SCALAR);
        mod(p, Attribute.ATTACK_DAMAGE, "hr_attack", num("attack_damage", 8), AttributeModifier.Operation.ADD_NUMBER);
        mod(p, Attribute.MAX_HEALTH, "hr_health", num("max_health", 20), AttributeModifier.Operation.ADD_NUMBER);
        mod(p, Attribute.JUMP_STRENGTH, "hr_jump", num("jump", 0.25), AttributeModifier.Operation.ADD_NUMBER);
        mod(p, Attribute.SAFE_FALL_DISTANCE, "hr_fall", num("safe_fall", 12), AttributeModifier.Operation.ADD_NUMBER);
        AttributeInstance hp = p.getAttribute(Attribute.MAX_HEALTH);
        if (hp != null) p.setHealth(hp.getValue());
    }

    public void disable(Player p) {
        active.remove(p.getUniqueId());
        unmod(p, Attribute.MOVEMENT_SPEED, "hr_speed");
        unmod(p, Attribute.ATTACK_DAMAGE, "hr_attack");
        unmod(p, Attribute.MAX_HEALTH, "hr_health");
        unmod(p, Attribute.JUMP_STRENGTH, "hr_jump");
        unmod(p, Attribute.SAFE_FALL_DISTANCE, "hr_fall");
        AttributeInstance hp = p.getAttribute(Attribute.MAX_HEALTH);
        if (hp != null && p.getHealth() > hp.getValue()) p.setHealth(hp.getValue());
    }

    private void mod(Player p, Attribute a, String key, double amount, AttributeModifier.Operation op) {
        AttributeInstance ai = p.getAttribute(a);
        if (ai == null) return;
        NamespacedKey k = new NamespacedKey(plugin, key);
        ai.removeModifier(k);
        ai.addTransientModifier(new AttributeModifier(k, amount, op));
    }

    private void unmod(Player p, Attribute a, String key) {
        AttributeInstance ai = p.getAttribute(a);
        if (ai != null) ai.removeModifier(new NamespacedKey(plugin, key));
    }
}
