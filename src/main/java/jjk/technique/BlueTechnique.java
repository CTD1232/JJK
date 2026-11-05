package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.FX;
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

public class BlueTechnique extends Technique {
    public BlueTechnique(JJKPlugin plugin) { super(plugin, "gojo_blue", "Cursed Technique Lapse: Blue"); }

    @Override
    public boolean cast(Player p) {
        Location target = Aim.point(p, num("range", 25));
        double radius = num("radius", 7), pull = num("pull", 0.7), dmg = num("damage", 2);
        int duration = (int) (num("duration", 3) * 20);
        p.getWorld().playSound(target, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 1.8f);
        ItemDisplay orb = VFX.spawn(target, VFX.ORB_BLUE, 1.6f, false);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (!p.isOnline() || t >= duration) { VFX.remove(orb); cancel(); return; }
                VFX.rotate(orb, (float) Math.toRadians(t * 18.0), 0.3f, 1f, 0.2f, 2);
                FX.sphere(target, 0.9, Particle.DUST, 8, FX.dust(60, 120, 255, 1.6f));
                FX.sphere(target, radius * 0.5, Particle.DUST, 6, FX.dust(150, 200, 255, 1f));
                for (Entity e : target.getWorld().getNearbyEntities(target, radius, radius, radius)) {
                    if (e.equals(p) || e.equals(orb)) continue;
                    Vector v = target.toVector().subtract(e.getLocation().toVector());
                    double dist = v.length();
                    if (dist > radius) continue;
                    if (dist > 0.6) e.setVelocity(v.normalize().multiply(pull));
                    if (dist < 1.8 && t % 10 == 0 && e instanceof LivingEntity le) plugin.damage(le, dmg, p, false);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }
}
