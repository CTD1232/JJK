package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/** Toge Inumaki's Cursed Speech. Every word strains his throat (a little self-damage). */
final class TogeTechniques { private TogeTechniques() {} }

abstract class SpeechTechnique extends Technique {
    SpeechTechnique(JJKPlugin plugin, String id, String name) { super(plugin, id, name); }

    void say(Player p, String word) {
        double b = num("backlash", 2);
        if (b > 0) {
            p.setHealth(Math.max(1.0, p.getHealth() - b)); // never lethal
            p.playHurtAnimation(0f);
        }
        Location at = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(2.2));
        VFX.text(at, "&f&l" + word, 24);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2f, 0.5f);
    }

    LivingEntity aimed(Player p, double range) {
        LivingEntity t = Aim.entity(p, range);
        if (t == null) plugin.msg(p, "&cNo target in sight");
        return t;
    }

    List<LivingEntity> cone(Player p, double range, double minDot) {
        List<LivingEntity> out = new ArrayList<>();
        Vector dir = p.getEyeLocation().getDirection().normalize();
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof LivingEntity le) || e instanceof ArmorStand) continue;
            var info = plugin.shikigami().info(e);
            if (info != null && info.owner.equals(p.getUniqueId())) continue;
            Vector to = le.getLocation().add(0, 1, 0).toVector().subtract(p.getEyeLocation().toVector());
            if (to.length() <= range && to.normalize().dot(dir) >= minDot) out.add(le);
        }
        return out;
    }

    void freeze(LivingEntity e, int ticks) {
        e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 9, false, true));
        e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, ticks, 4, false, false));
        if (e instanceof Mob m) {
            m.setAware(false);
            new BukkitRunnable() {
                @Override
                public void run() { if (m.isValid()) m.setAware(true); }
            }.runTaskLater(plugin, ticks);
        }
    }
}

class SpeechStopTechnique extends SpeechTechnique {
    SpeechStopTechnique(JJKPlugin plugin) { super(plugin, "speech_stop", "Cursed Speech: Stop!"); }

    @Override
    public boolean cast(Player p) {
        List<LivingEntity> hit = cone(p, num("range", 12), 0.75);
        if (hit.isEmpty()) { plugin.msg(p, "&cNobody in front of you"); return false; }
        say(p, "Stop!");
        for (LivingEntity e : hit) freeze(e, (int) (num("seconds", 4) * 20));
        return true;
    }
}

class SpeechSleepTechnique extends SpeechTechnique {
    SpeechSleepTechnique(JJKPlugin plugin) { super(plugin, "speech_sleep", "Cursed Speech: Sleep!"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = aimed(p, num("range", 20));
        if (t == null) return false;
        say(p, "Sleep!");
        int ticks = (int) (num("seconds", 7) * 20);
        freeze(t, ticks);
        t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0, false, false));
        t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, 5, false, false));
        VFX.text(t.getEyeLocation().add(0, 0.6, 0), "&bZzz", ticks);
        return true;
    }
}

class SpeechBlastAwayTechnique extends SpeechTechnique {
    SpeechBlastAwayTechnique(JJKPlugin plugin) { super(plugin, "speech_blast_away", "Cursed Speech: Blast Away"); }

    @Override
    public boolean cast(Player p) {
        List<LivingEntity> hit = cone(p, num("range", 15), 0.5);
        if (hit.isEmpty()) { plugin.msg(p, "&cNobody in front of you"); return false; }
        say(p, "Blast Away!");
        Vector dir = p.getEyeLocation().getDirection().normalize();
        for (LivingEntity e : hit) {
            plugin.damage(e, num("damage", 4), p, false);
            e.setVelocity(dir.clone().multiply(num("knockback", 3.2)).setY(0.5));
        }
        return true;
    }
}

class SpeechCrushTechnique extends SpeechTechnique {
    SpeechCrushTechnique(JJKPlugin plugin) { super(plugin, "speech_crush", "Cursed Speech: Crush!"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = aimed(p, num("range", 20));
        if (t == null) return false;
        say(p, "Crush!");
        Location at = t.getLocation().add(0, 1, 0);
        at.getWorld().createExplosion(at, 0f, false, false); // visual only
        plugin.damage(t, num("damage", 18), p, false);
        t.setVelocity(new Vector(0, -1.2, 0));
        return true;
    }
}

class SpeechTwistTechnique extends SpeechTechnique {
    SpeechTwistTechnique(JJKPlugin plugin) { super(plugin, "speech_twist", "Cursed Speech: Twist!"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = aimed(p, num("range", 20));
        if (t == null) return false;
        say(p, "Twist!");
        plugin.damage(t, num("damage", 12), p, false);
        t.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0, false, false));
        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {
                if (n++ >= 14 || !t.isValid() || t.isDead()) { cancel(); return; }
                Location l = t.getLocation();
                l.setYaw(l.getYaw() + 75);
                t.teleport(l);
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }
}
