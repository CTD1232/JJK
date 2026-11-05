package jjk;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resonance marks (Nobara): nails mark a target, Hairpin detonates every mark the caster owns. */
public class MarkManager {
    private record Mark(UUID owner, long until) {}
    private final Map<UUID, Mark> marks = new HashMap<>();

    public void mark(LivingEntity e, Player owner, double seconds) {
        marks.put(e.getUniqueId(), new Mark(owner.getUniqueId(), System.currentTimeMillis() + (long) (seconds * 1000)));
    }

    public List<LivingEntity> marked(Player owner) {
        List<LivingEntity> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Mark>> it = marks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Mark> en = it.next();
            Entity e = Bukkit.getEntity(en.getKey());
            if (en.getValue().until() < now || !(e instanceof LivingEntity le) || le.isDead()) { it.remove(); continue; }
            if (en.getValue().owner().equals(owner.getUniqueId())) out.add(le);
        }
        return out;
    }

    public void clear(Player owner) {
        marks.values().removeIf(m -> m.owner().equals(owner.getUniqueId()));
    }
}
