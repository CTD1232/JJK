package jjk;

import jjk.util.FX;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tracks Ten Shadows shikigami: owner, lifetime, targeting and Mahoraga's adaptation. */
public class SummonManager {
    public static class Summon {
        public final UUID owner;
        public final String kind;
        public final long expiry;
        public final Map<String, Integer> adapt = new HashMap<>();
        Summon(UUID owner, String kind, long expiry) { this.owner = owner; this.kind = kind; this.expiry = expiry; }
        public int totalAdaptation() { return adapt.values().stream().mapToInt(Integer::intValue).sum(); }
    }

    private final JJKPlugin plugin;
    private final Map<UUID, Summon> summons = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private double wheelAngle = 0;

    public SummonManager(JJKPlugin plugin) { this.plugin = plugin; }

    public void register(LivingEntity e, Player owner, String kind, double seconds) {
        e.setPersistent(false);
        if (e instanceof Mob m) m.setRemoveWhenFarAway(true);
        summons.put(e.getUniqueId(), new Summon(owner.getUniqueId(), kind, System.currentTimeMillis() + (long) (seconds * 1000)));
    }

    public Summon get(Entity e) { return summons.get(e.getUniqueId()); }
    public boolean isSummon(Entity e) { return summons.containsKey(e.getUniqueId()); }

    public boolean sameTeam(Entity a, Entity b) {
        UUID oa = ownerOf(a), ob = ownerOf(b);
        return oa != null && oa.equals(ob);
    }

    /** Owner of a summon, or the entity's own id if it is a player. */
    public UUID ownerOf(Entity e) {
        Summon s = summons.get(e.getUniqueId());
        if (s != null) return s.owner;
        return e instanceof Player ? e.getUniqueId() : null;
    }

    public List<LivingEntity> of(Player owner, String kind) {
        List<LivingEntity> out = new ArrayList<>();
        for (Map.Entry<UUID, Summon> en : summons.entrySet()) {
            Summon s = en.getValue();
            if (!s.owner.equals(owner.getUniqueId())) continue;
            if (kind != null && !s.kind.equals(kind)) continue;
            if (Bukkit.getEntity(en.getKey()) instanceof LivingEntity le) out.add(le);
        }
        return out;
    }

    public boolean has(Player owner, String kind) { return !of(owner, kind).isEmpty(); }

    public void dismiss(Player owner, String kind) {
        for (LivingEntity le : of(owner, kind)) remove(le);
    }

    public void remove(LivingEntity e) {
        summons.remove(e.getUniqueId());
        e.getWorld().spawnParticle(Particle.LARGE_SMOKE, e.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.02);
        e.remove();
    }

    public void forget(Entity e) { summons.remove(e.getUniqueId()); }

    public void setTarget(Player owner, LivingEntity t) { lastTarget.put(owner.getUniqueId(), t.getUniqueId()); }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                wheelAngle += 0.6;
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, Summon>> it = summons.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Summon> en = it.next();
                    Entity ent = Bukkit.getEntity(en.getKey());
                    Summon s = en.getValue();
                    if (!(ent instanceof LivingEntity le) || le.isDead()) { it.remove(); continue; }
                    Player owner = Bukkit.getPlayer(s.owner);
                    if (owner == null || now > s.expiry) {
                        it.remove();
                        le.getWorld().spawnParticle(Particle.LARGE_SMOKE, le.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.02);
                        le.remove();
                        continue;
                    }
                    if (s.kind.equals("mahoraga")) wheel(le, s);
                    if (le instanceof Mob mob && !(mob instanceof org.bukkit.entity.Wolf)) retarget(mob, owner);
                }
            }
        }.runTaskTimer(plugin, 10, 10);
    }

    private void retarget(Mob mob, Player owner) {
        LivingEntity cur = mob.getTarget();
        if (cur != null && !cur.isDead() && !sameTeam(cur, mob)) return;
        LivingEntity t = null;
        UUID lt = lastTarget.get(owner.getUniqueId());
        if (lt != null && Bukkit.getEntity(lt) instanceof LivingEntity le && !le.isDead()
                && le.getWorld().equals(mob.getWorld()) && le.getLocation().distanceSquared(mob.getLocation()) < 40 * 40
                && !sameTeam(le, mob)) t = le;
        if (t == null) {
            double best = Double.MAX_VALUE;
            for (Entity e : mob.getNearbyEntities(16, 8, 16)) {
                if (e instanceof Monster m && !isSummon(m)) {
                    double d = m.getLocation().distanceSquared(mob.getLocation());
                    if (d < best) { best = d; t = m; }
                }
            }
        }
        mob.setTarget(t);
    }

    /** Mahoraga's eight-handled wheel; spokes light up as it adapts. */
    private void wheel(LivingEntity e, Summon s) {
        Location c = e.getLocation().add(0, e.getHeight() + 0.8, 0);
        int lit = Math.min(8, s.totalAdaptation());
        for (int i = 0; i < 8; i++) {
            double a = wheelAngle + i * Math.PI / 4;
            Color col = i < lit ? Color.fromRGB(255, 215, 80) : Color.fromRGB(110, 110, 110);
            Location p = c.clone().add(Math.cos(a) * 0.9, 0, Math.sin(a) * 0.9);
            e.getWorld().spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0, new Particle.DustOptions(col, 1.3f));
        }
    }

    public double maxHealth(LivingEntity e) { return e.getAttribute(Attribute.MAX_HEALTH).getValue(); }
}
