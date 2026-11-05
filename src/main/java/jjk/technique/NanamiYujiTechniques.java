package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Nanami's Ratio Technique, Yuji's Black Flash and Divergent Fist. */
final class NanamiYujiTechniques { private NanamiYujiTechniques() {} }

class RatioStrikeTechnique extends Technique {
    RatioStrikeTechnique(JJKPlugin plugin) { super(plugin, "ratio_strike", "Ratio Technique: 7:3"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("reach", 4));
        if (t == null) { plugin.msg(p, "&cNo target in reach"); return false; }
        // The weak point sits at 7:3 along the target's height.
        double h = t.getHeight();
        Location base = t.getLocation();
        for (double y = 0; y <= h; y += 0.15) {
            base.getWorld().spawnParticle(Particle.DUST, base.clone().add(0, y, 0), 1, 0, 0, 0, 0, FX.dust(255, 220, 60, 1f));
        }
        Location wp = base.clone().add(0, h * 0.7, 0);
        base.getWorld().spawnParticle(Particle.DUST, wp, 12, 0.25, 0.05, 0.25, 0, FX.dust(255, 255, 255, 1.6f));
        double dmg = num("base_damage", 9) * num("crit_multiplier", 1.7);
        if (OvertimeTechnique.active(p)) dmg *= num("overtime_multiplier", 1.6);
        plugin.damage(t, dmg, p, false);
        t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.7f);
        return true;
    }
}

class OvertimeTechnique extends Technique {
    private static final Map<UUID, Long> UNTIL = new HashMap<>();

    OvertimeTechnique(JJKPlugin plugin) { super(plugin, "overtime", "Overtime"); }

    static boolean active(Player p) { return UNTIL.getOrDefault(p.getUniqueId(), 0L) > System.currentTimeMillis(); }

    @Override
    public boolean cast(Player p) {
        if (active(p)) { plugin.msg(p, "&cAlready working overtime"); return false; }
        double secs = num("duration", 20), fatigue = num("fatigue_seconds", 10);
        UNTIL.put(p.getUniqueId(), System.currentTimeMillis() + (long) (secs * 1000));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, (int) (secs * 20), 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (secs * 20), 1, false, false));
        plugin.msg(p, "&eOvertime");
        new BukkitRunnable() {
            @Override
            public void run() {
                UNTIL.remove(p.getUniqueId());
                if (!p.isOnline()) return;
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (fatigue * 20), 1, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int) (fatigue * 20), 1, false, true));
                plugin.msg(p, "&7Overtime over: exhausted");
            }
        }.runTaskLater(plugin, (long) (secs * 20));
        return true;
    }
}

class BlackFlashTechnique extends Technique {
    private static final Map<UUID, Integer> STREAK = new HashMap<>();
    private static final Map<UUID, Long> LAST = new HashMap<>();
    private final Random rnd = new Random();

    BlackFlashTechnique(JJKPlugin plugin) { super(plugin, "black_flash", "Black Flash"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("reach", 4));
        if (t == null) { plugin.msg(p, "&cNo target in reach"); return false; }
        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - LAST.getOrDefault(id, 0L) > num("streak_window", 10) * 1000) STREAK.put(id, 0);
        LAST.put(id, now);
        int streak = STREAK.getOrDefault(id, 0);
        double chance = Math.min(0.9, num("chance", 0.15) + streak * num("streak_bonus", 0.1));
        double base = num("base_damage", 8);
        Location at = t.getLocation().add(0, 1, 0);
        if (rnd.nextDouble() < chance) {
            plugin.damage(t, base * num("multiplier", 2.5), p, false);
            STREAK.put(id, streak + 1);
            at.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, at, 60, 0.5, 0.7, 0.5, 0.6);
            for (int k = 0; k < 2; k++) {
                ItemDisplay bolt = VFX.spawn(at, VFX.BOLT, 3.2f, false);
                VFX.rotate(bolt, (float) (rnd.nextDouble() * Math.PI * 2), 0, 1, 0, 0);
                VFX.removeLater(bolt, 6);
            }
            FX.inside(at, 1.5, Particle.DUST, 40, FX.dust(10, 10, 10, 2.2f));
            FX.inside(at, 1.5, Particle.DUST, 40, FX.dust(220, 20, 20, 1.6f));
            at.getWorld().playSound(at, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 2f);
            int zone = (int) (num("zone_seconds", 15) * 20);
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, zone, 1, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, zone, 0, false, false));
            plugin.msg(p, "&c&lBLACK FLASH &7x" + (streak + 1));
        } else {
            plugin.damage(t, base, p, false);
            STREAK.put(id, 0);
            at.getWorld().spawnParticle(Particle.CRIT, at, 12, 0.4, 0.5, 0.4, 0.2);
        }
        return true;
    }
}

class DivergentFistTechnique extends Technique {
    DivergentFistTechnique(JJKPlugin plugin) { super(plugin, "divergent_fist", "Divergent Fist"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("reach", 4));
        if (t == null) { plugin.msg(p, "&cNo target in reach"); return false; }
        plugin.damage(t, num("first_damage", 6), p, false);
        t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 15, 0.4, 0.5, 0.4, 0.2);
        double second = num("second_damage", 10);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!t.isValid() || t.isDead() || !p.isOnline()) return;
                plugin.damage(t, second, p, false);
                t.getWorld().spawnParticle(Particle.EXPLOSION, t.getLocation().add(0, 1, 0), 1);
                t.getWorld().playSound(t.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
            }
        }.runTaskLater(plugin, (long) num("delay_ticks", 8));
        return true;
    }
}
