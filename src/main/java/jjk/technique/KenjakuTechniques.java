package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.Projectiles;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Kenjaku's Cursed Spirit Manipulation: absorb hostile mobs, release them as summons, or fire them as Uzumaki. */
final class KenjakuTechniques { private KenjakuTechniques() {} }

final class SpiritPool {
    private SpiritPool() {}
    static final Map<UUID, Deque<EntityType>> POOL = new HashMap<>();
    static final Set<EntityType> BLOCKED = Set.of(EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.WARDEN, EntityType.ELDER_GUARDIAN);

    static Deque<EntityType> of(Player p) { return POOL.computeIfAbsent(p.getUniqueId(), k -> new ArrayDeque<>()); }
}

class AbsorbSpiritTechnique extends Technique {
    AbsorbSpiritTechnique(JJKPlugin plugin) { super(plugin, "absorb_spirit", "Cursed Spirit Manipulation"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("range", 15));
        if (!(t instanceof Monster) || SpiritPool.BLOCKED.contains(t.getType()) || plugin.shikigami().info(t) != null) {
            plugin.msg(p, "&cAim at a hostile cursed spirit");
            return false;
        }
        var pool = SpiritPool.of(p);
        if (pool.size() >= (int) num("max_spirits", 20)) { plugin.msg(p, "&cYou cannot hold any more spirits"); return false; }
        pool.add(t.getType());
        Location at = t.getLocation().add(0, 1, 0);
        at.getWorld().spawnParticle(Particle.PORTAL, at, 60, 0.4, 0.8, 0.4, 1.2);
        at.getWorld().playSound(at, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
        t.remove();
        plugin.msg(p, "&5Absorbed &f" + t.getType().name().toLowerCase() + " &7(" + pool.size() + " held)");
        return true;
    }
}

class ReleaseSpiritsTechnique extends Technique {
    ReleaseSpiritsTechnique(JJKPlugin plugin) { super(plugin, "release_spirits", "Release Spirits"); }

    @Override
    public boolean cast(Player p) {
        var pool = SpiritPool.of(p);
        if (pool.isEmpty()) { plugin.msg(p, "&cNo spirits held"); return false; }
        LivingEntity target = Aim.entity(p, 30);
        int n = Math.min(pool.size(), (int) num("max_release", 8));
        double secs = num("duration", 20);
        for (int i = 0; i < n; i++) {
            EntityType type = pool.poll();
            Location l = p.getLocation().add(Math.cos(i * 2.4) * 2.5, 0, Math.sin(i * 2.4) * 2.5);
            Entity e = p.getWorld().spawnEntity(l, type);
            if (e instanceof LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) (secs * 20) + 40, 0, false, false));
                le.customName(plugin.legacy("&5Cursed Spirit"));
            }
            if (e instanceof Mob m && target != null) m.setTarget(target);
            plugin.shikigami().track(e, p, "spirit", secs, true);
            p.getWorld().spawnParticle(Particle.SOUL, l.add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.02);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.6f);
        plugin.msg(p, "&5Released &f" + n + " &5spirits");
        return true;
    }
}

class UzumakiTechnique extends Technique {
    UzumakiTechnique(JJKPlugin plugin) { super(plugin, "uzumaki", "Maximum: Uzumaki"); }

    @Override
    public boolean cast(Player p) {
        var pool = SpiritPool.of(p);
        int n = pool.size();
        if (n == 0) { plugin.msg(p, "&cNo spirits to compress"); return false; }
        pool.clear();
        double dmg = num("base_damage", 10) + n * num("damage_per_spirit", 6);
        double radius = Math.min(num("max_radius", 10), 4 + n * 0.3);
        float scale = (float) (1.5 + n * 0.15);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2f, 0.6f);
        Projectiles.fly(plugin, p, p.getEyeLocation(), p.getEyeLocation().getDirection(), num("speed", 1.8), num("range", 45), 1.5,
                VFX.spawn(p.getEyeLocation(), VFX.UZUMAKI, scale, false),
                loc -> loc.getWorld().spawnParticle(Particle.DUST, loc, 5, 0.3, 0.3, 0.3, 0, FX.dust(90, 30, 150, 2f)),
                loc -> {
                    loc.getWorld().createExplosion(loc, 0f, false, false); // visual only
                    FX.inside(loc, radius, Particle.DUST, 80, FX.dust(70, 20, 120, 2.5f));
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                        if (!(e instanceof LivingEntity le) || e.equals(p)) continue;
                        var info = plugin.shikigami().info(e);
                        if (info != null && info.owner.equals(p.getUniqueId())) continue;
                        plugin.damage(le, dmg, p, false);
                    }
                });
        return true;
    }
}
