package jjk.util;

import jjk.JJKPlugin;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public final class Projectiles {
    private Projectiles() {}

    public static void fly(JJKPlugin plugin, Player caster, Location start, Vector dir, double speed, double range,
                           double hitRadius, Consumer<Location> trail, Consumer<Location> onHit) {
        fly(plugin, caster, start, dir, speed, range, hitRadius, null, trail, onHit);
    }

    /** Flies a point along dir; calls onHit once on a block, living entity or max range. */
    public static void fly(JJKPlugin plugin, Player caster, Location start, Vector dir, double speed, double range,
                           double hitRadius, org.bukkit.entity.Entity visual, Consumer<Location> trail, Consumer<Location> onHit) {
        Location loc = start.clone();
        Vector d = dir.clone().normalize();
        new BukkitRunnable() {
            double travelled = 0;
            @Override
            public void run() {
                int steps = (int) Math.ceil(speed / 0.5);
                double step = speed / steps;
                for (int i = 0; i < steps; i++) {
                    loc.add(d.clone().multiply(step));
                    travelled += step;
                    trail.accept(loc.clone());
                    if (visual != null) visual.teleport(loc);
                    boolean hit = !loc.getBlock().isPassable();
                    if (!hit) {
                        for (Entity e : loc.getWorld().getNearbyEntities(loc, hitRadius, hitRadius, hitRadius)) {
                            if (e instanceof LivingEntity && !(e instanceof ArmorStand) && !e.equals(caster)) { hit = true; break; }
                        }
                    }
                    if (hit || travelled >= range) {
                        onHit.accept(loc.clone());
                        jjk.util.VFX.remove(visual);
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
