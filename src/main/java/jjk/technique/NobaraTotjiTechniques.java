package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.Projectiles;
import jjk.util.VFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Nobara's Straw Doll Technique and Toji's cursed tools / Heavenly Restriction. */
final class NobaraTojiTechniques { private NobaraTojiTechniques() {} }

class StrawDoll {
    private StrawDoll() {}
    /** owner -> (marked entity -> expiry millis) */
    static final Map<UUID, Map<UUID, Long>> MARKS = new HashMap<>();

    static Map<UUID, Long> marks(Player p) { return MARKS.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>()); }
}

class NailTechnique extends Technique {
    NailTechnique(JJKPlugin plugin) { super(plugin, "nail", "Straw Doll: Nail"); }

    @Override
    public boolean cast(Player p) {
        double dmg = num("damage", 3), markSecs = num("mark_seconds", 20);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.6f);
        Projectiles.fly(plugin, p, p.getEyeLocation(), p.getEyeLocation().getDirection(), num("speed", 2.5), num("range", 35), 0.7,
                VFX.spawn(p.getEyeLocation(), VFX.NAIL, 0.7f, false),
                loc -> loc.getWorld().spawnParticle(Particle.CRIT, loc, 2, 0, 0, 0, 0),
                loc -> {
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.3, 1.3, 1.3)) {
                        if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && !e.equals(p)) {
                            plugin.damage(le, dmg, p, false);
                            StrawDoll.marks(p).put(le.getUniqueId(), System.currentTimeMillis() + (long) (markSecs * 1000));
                            FX.inside(le.getLocation().add(0, 1, 0), 0.8, Particle.DUST, 12, FX.dust(200, 60, 60, 1.2f));
                            plugin.msg(p, "&cResonance mark set");
                            return;
                        }
                    }
                });
        return true;
    }
}

class ResonanceTechnique extends Technique {
    ResonanceTechnique(JJKPlugin plugin) { super(plugin, "resonance", "Resonance"); }

    @Override
    public boolean cast(Player p) {
        double dmg = num("damage", 12);
        long now = System.currentTimeMillis();
        int hit = 0;
        for (Map.Entry<UUID, Long> en : StrawDoll.marks(p).entrySet()) {
            Entity e = Bukkit.getEntity(en.getKey());
            if (en.getValue() < now || !(e instanceof LivingEntity le) || !le.isValid()) continue;
            plugin.damage(le, dmg, p, false);
            FX.inside(le.getLocation().add(0, 1, 0), 0.9, Particle.DUST, 20, FX.dust(255, 40, 40, 1.4f));
            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1f, 1.8f);
            hit++;
        }
        StrawDoll.marks(p).clear();
        if (hit == 0) { plugin.msg(p, "&cNo marked targets"); return false; }
        plugin.msg(p, "&cResonance &7x" + hit);
        return true;
    }
}

class HairpinTechnique extends Technique {
    HairpinTechnique(JJKPlugin plugin) { super(plugin, "hairpin", "Hairpin"); }

    @Override
    public boolean cast(Player p) {
        float power = (float) num("power", 2.5);
        long now = System.currentTimeMillis();
        int hit = 0;
        for (Map.Entry<UUID, Long> en : StrawDoll.marks(p).entrySet()) {
            Entity e = Bukkit.getEntity(en.getKey());
            if (en.getValue() < now || !(e instanceof LivingEntity le) || !le.isValid()) continue;
            Location l = le.getLocation().add(0, 1, 0);
            l.getWorld().createExplosion(l, power, false, false, p);
            hit++;
        }
        StrawDoll.marks(p).clear();
        if (hit == 0) { plugin.msg(p, "&cNo marked targets"); return false; }
        return true;
    }
}

class HeavenlyRestrictionTechnique extends Technique {
    HeavenlyRestrictionTechnique(JJKPlugin plugin) { super(plugin, "heavenly_restriction", "Heavenly Restriction"); }

    @Override
    public boolean usesCursedEnergy() { return false; }

    @Override
    public boolean cast(Player p) {
        if (plugin.heavenly().isActive(p)) {
            plugin.heavenly().disable(p);
            plugin.msg(p, "&7Heavenly Restriction lifted");
        } else {
            plugin.infinity().set(p, false);
            plugin.heavenly().enable(p);
            plugin.msg(p, "&2Heavenly Restriction");
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 1.4f);
        }
        return true;
    }
}

/** Shared melee helper for Toji's tools. */
abstract class PhysicalTechnique extends Technique {
    PhysicalTechnique(JJKPlugin plugin, String id, String name) { super(plugin, id, name); }

    @Override
    public boolean usesCursedEnergy() { return false; }

    LivingEntity meleeTarget(Player p) {
        LivingEntity t = Aim.entity(p, num("reach", 4));
        if (t == null) plugin.msg(p, "&cNo target in reach");
        return t;
    }
}

class InvertedSpearTechnique extends PhysicalTechnique {
    InvertedSpearTechnique(JJKPlugin plugin) { super(plugin, "inverted_spear", "Inverted Spear of Heaven"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = meleeTarget(p);
        if (t == null) return false;
        plugin.damage(t, num("damage", 6), p, true);
        // Nullifies cursed technique effects on the target.
        if (t instanceof Player tp) {
            plugin.infinity().set(tp, false);
            plugin.domains().endOwned(tp);
            plugin.domains().clearSimpleDomain(tp.getUniqueId());
            plugin.msg(tp, "&cYour cursed technique was nullified");
        }
        plugin.shikigami().dispel(t);
        t.getWorld().spawnParticle(Particle.ENCHANTED_HIT, t.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.3);
        t.getWorld().playSound(t.getLocation(), Sound.ITEM_TRIDENT_HIT, 1.5f, 0.8f);
        return true;
    }
}

class SplitSoulTechnique extends PhysicalTechnique {
    SplitSoulTechnique(JJKPlugin plugin) { super(plugin, "split_soul", "Split Soul Katana"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = meleeTarget(p);
        if (t == null) return false;
        double dmg = num("damage", 14);
        plugin.damage(t, 0.01, p, true); // credit the hit and play the hurt animation
        // Cuts the soul directly: ignores armor and resistance.
        if (t.isValid() && !t.isDead()) t.setHealth(Math.max(0, t.getHealth() - dmg));
        t.getWorld().spawnParticle(Particle.SWEEP_ATTACK, t.getLocation().add(0, 1, 0), 3, 0.4, 0.5, 0.4, 0);
        t.getWorld().spawnParticle(Particle.SOUL, t.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
        t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);
        return true;
    }
}

class PlayfulCloudTechnique extends PhysicalTechnique {
    PlayfulCloudTechnique(JJKPlugin plugin) { super(plugin, "playful_cloud", "Playful Cloud"); }

    @Override
    public boolean cast(Player p) {
        double range = num("range", 3.5), dmg = num("damage", 10), knock = num("knockback", 1.2);
        Vector dir = p.getEyeLocation().getDirection().setY(0).normalize();
        boolean any = false;
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand) continue;
            Vector to = le.getLocation().toVector().subtract(p.getLocation().toVector()).setY(0);
            if (to.lengthSquared() < 0.01 || to.normalize().dot(dir) < 0.3) continue;
            plugin.damage(le, dmg, p, true);
            le.setVelocity(dir.clone().multiply(knock).setY(0.35));
            any = true;
        }
        Location c = p.getEyeLocation().add(dir.clone().multiply(1.8));
        c.getWorld().spawnParticle(Particle.SWEEP_ATTACK, c, 3, 0.8, 0.3, 0.8, 0);
        c.getWorld().playSound(c, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.5f, 0.6f);
        return any;
    }
}
