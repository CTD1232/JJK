package jjk;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tracks temporary summoned creatures (shikigami): owner, lifespan, targeting, Mahoraga adaptation. */
public class ShikigamiManager {
    public static class Info {
        public final UUID owner;
        public final String kind;
        public final long expire;
        public final boolean attacks;
        public final Map<String, Integer> adapt = new HashMap<>();
        public Entity visual;
        /** true = the visual is a skin that follows the (invisible) entity every tick. */
        public boolean syncVisual;

        Info(UUID owner, String kind, long expire, boolean attacks) {
            this.owner = owner;
            this.kind = kind;
            this.expire = expire;
            this.attacks = attacks;
        }
    }

    private final JJKPlugin plugin;
    private final Map<UUID, Info> tracked = new HashMap<>();
    private final Map<UUID, UUID> lastTarget = new HashMap<>();
    private final Map<UUID, Long> lastTargetTime = new HashMap<>();

    public ShikigamiManager(JJKPlugin plugin) { this.plugin = plugin; }

    public static void attr(LivingEntity e, Attribute a, double v) {
        AttributeInstance ai = e.getAttribute(a);
        if (ai != null) ai.setBaseValue(v);
    }

    public void track(Entity e, Player owner, String kind, double seconds, boolean attacks) {
        e.setPersistent(false);
        tracked.put(e.getUniqueId(), new Info(owner.getUniqueId(), kind,
                System.currentTimeMillis() + (long) (seconds * 1000), attacks));
    }

    public Info info(Entity e) { return tracked.get(e.getUniqueId()); }

    public void untrack(Entity e) {
        Info gone = tracked.remove(e.getUniqueId());
        if (gone != null && gone.visual != null) jjk.util.VFX.remove(gone.visual);
    }

    public boolean has(UUID owner, String kind) {
        for (Info i : tracked.values()) if (i.owner.equals(owner) && i.kind.equals(kind)) return true;
        return false;
    }

    public List<Entity> ownedBy(UUID owner) {
        List<Entity> out = new ArrayList<>();
        for (Map.Entry<UUID, Info> en : tracked.entrySet()) {
            if (!en.getValue().owner.equals(owner)) continue;
            Entity e = Bukkit.getEntity(en.getKey());
            if (e != null && e.isValid()) out.add(e);
        }
        return out;
    }

    /** Removes the owner's shikigami of this kind (or all kinds if kind is null). */
    public void dismiss(UUID owner, String kind) {
        for (Map.Entry<UUID, Info> en : new ArrayList<>(tracked.entrySet())) {
            Info i = en.getValue();
            if (!i.owner.equals(owner) || (kind != null && !kind.equals(i.kind))) continue;
            Entity e = Bukkit.getEntity(en.getKey());
            if (e != null) e.remove();
            if (i.visual != null) jjk.util.VFX.remove(i.visual);
            tracked.remove(en.getKey());
        }
    }

    /** Dispels one summon (used by the Inverted Spear of Heaven). */
    public void dispel(Entity e) {
        Info gone = tracked.remove(e.getUniqueId());
        if (gone != null) {
            if (gone.visual != null) jjk.util.VFX.remove(gone.visual);
            e.getWorld().spawnParticle(Particle.POOF, e.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0.05);
            e.remove();
        }
    }

    public void noteTarget(UUID owner, LivingEntity target) {
        if (target.getUniqueId().equals(owner)) return;
        Info i = tracked.get(target.getUniqueId());
        if (i != null && i.owner.equals(owner)) return;
        lastTarget.put(owner, target.getUniqueId());
        lastTargetTime.put(owner, System.currentTimeMillis());
    }

    private LivingEntity currentTarget(UUID owner) {
        Long t = lastTargetTime.get(owner);
        if (t == null || System.currentTimeMillis() - t > 10_000) return null;
        Entity e = Bukkit.getEntity(lastTarget.get(owner));
        return e instanceof LivingEntity le && le.isValid() && !le.isDead() ? le : null;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, Info>> it = tracked.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Info> en = it.next();
                    Info in = en.getValue();
                    Entity e = Bukkit.getEntity(en.getKey());
                    Player owner = Bukkit.getPlayer(in.owner);
                    if (e == null || !e.isValid() || owner == null || now >= in.expire) {
                        if (in.visual != null) jjk.util.VFX.remove(in.visual);
                        if (e != null && e.isValid()) {
                            e.getWorld().spawnParticle(Particle.POOF, e.getLocation().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0.05);
                            e.remove();
                        }
                        it.remove();
                        continue;
                    }
                    if (in.attacks && e instanceof Mob m) {
                        LivingEntity cur = m.getTarget();
                        LivingEntity want = currentTarget(in.owner);
                        if (want != null) {
                            if (!want.equals(cur)) m.setTarget(want);
                        } else if (cur instanceof Player) {
                            m.setTarget(null); // summons never pick on random players by themselves
                        }
                    }
                    if (e.getWorld().equals(owner.getWorld()) && e.getLocation().distanceSquared(owner.getLocation()) > 40 * 40) {
                        e.teleport(owner.getLocation());
                    }
                }
            }
        }.runTaskTimer(plugin, 10, 10);

        // Skins (e.g. Rika) follow their invisible body every tick.
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, Info> en : tracked.entrySet()) {
                    Info in = en.getValue();
                    if (!in.syncVisual || in.visual == null || !in.visual.isValid()) continue;
                    Entity e = Bukkit.getEntity(en.getKey());
                    if (e == null || !e.isValid()) continue;
                    org.bukkit.Location l = e.getLocation();
                    l.setPitch(0);
                    in.visual.teleport(l);
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    public void endAll() {
        for (UUID id : new ArrayList<>(tracked.keySet())) {
            Entity e = Bukkit.getEntity(id);
            Info i = tracked.get(id);
            if (i != null && i.visual != null) jjk.util.VFX.remove(i.visual);
            if (e != null) e.remove();
        }
        tracked.clear();
    }

    public Component label(String legacy) { return plugin.legacy(legacy); }
}
