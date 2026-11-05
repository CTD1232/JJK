package jjk;

import jjk.technique.Technique;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    private final JJKPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, String> lastCast = new HashMap<>();
    private final Map<UUID, Long> sealed = new HashMap<>();

    public AbilityManager(JJKPlugin plugin) { this.plugin = plugin; }

    /** The last technique this player successfully cast (used by Yuta's Copy). */
    public String lastCast(UUID id) { return lastCast.get(id); }

    /** Seals cursed techniques for this long (Higuruma's Confiscation). Physical techniques still work. */
    public void seal(UUID id, long millis) {
        sealed.merge(id, System.currentTimeMillis() + millis, Math::max);
    }

    public boolean isSealed(UUID id) { return sealed.getOrDefault(id, 0L) > System.currentTimeMillis(); }

    public boolean use(Player p, String techId) {
        Technique t = plugin.techniques().get(techId);
        if (t == null) { plugin.msg(p, "&cUnknown technique: " + techId); return false; }
        if (!t.enabled()) { plugin.msg(p, "&cThat technique is disabled."); return false; }

        if (t.usesCursedEnergy() && plugin.heavenly().isActive(p)) {
            plugin.msg(p, "&cHeavenly Restriction: you have no cursed energy");
            return false;
        }
        if (t.usesCursedEnergy() && isSealed(p.getUniqueId())) {
            plugin.msg(p, "&cYour cursed technique is sealed");
            return false;
        }

        long now = System.currentTimeMillis();
        Map<String, Long> cd = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());
        long until = cd.getOrDefault(t.id(), 0L);
        if (until > now) {
            plugin.msg(p, "&c" + t.display() + " &7on cooldown: &f" + String.format("%.1f", (until - now) / 1000.0) + "s");
            return false;
        }
        double cost = t.cost(p);
        if (plugin.energy().get(p) < cost) { plugin.msg(p, "&cNot enough cursed energy (" + (int) cost + " needed)"); return false; }
        if (!t.cast(p)) return false;
        plugin.energy().consume(p, cost);
        cd.put(t.id(), now + t.cooldownMs());
        if (!t.id().equals("copy") && !t.id().equals("mimic")) lastCast.put(p.getUniqueId(), t.id());
        return true;
    }
}
