package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.Projectiles;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FugaTechnique extends Technique {
    public FugaTechnique(JJKPlugin plugin) { super(plugin, "fuga", "Fuga (Open)"); }

    @Override
    public boolean cast(Player p) {
        int charge = (int) (num("charge", 1.0) * 20);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 2f, 0.5f);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) { cancel(); return; }
                Location hand = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(1.5));
                p.getWorld().spawnParticle(Particle.FLAME, hand, 6, 0.3, 0.3, 0.3, 0.02);
                if (++t >= charge) { cancel(); fire(p); }
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }

    private void fire(Player p) {
        float power = (float) num("power", 7.0);
        boolean fire = bool("fire", true), breakBlocks = bool("break_blocks", false);
        double dmg = num("damage", 20);
        Vector dir = p.getEyeLocation().getDirection();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
        ItemDisplay flame = VFX.spawn(p.getEyeLocation(), VFX.FLAME, 2.2f, false);
        Projectiles.fly(plugin, p, p.getEyeLocation(), dir, num("speed", 2.0), num("range", 60), 1.2, flame,
                loc -> {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 6, 0.25, 0.25, 0.25, 0.02);
                    loc.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.2, 0.2, 0.2, 0, FX.dust(255, 120, 0, 2f));
                },
                loc -> {
                    loc.getWorld().createExplosion(loc, power, fire, breakBlocks, p);
                    FX.inside(loc, power, Particle.FLAME, 80, null);
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, power, power, power)) {
                        if (e instanceof LivingEntity le && !e.equals(p)) {
                            le.setFireTicks(100);
                            plugin.damage(le, dmg, p, false);
                        }
                    }
                });
    }
}
