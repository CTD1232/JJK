package jjk.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public final class Aim {
    private Aim() {}

    private static RayTraceResult trace(Player p, double range) {
        Location eye = p.getEyeLocation();
        return p.getWorld().rayTrace(eye, eye.getDirection(), range, FluidCollisionMode.NEVER, true, 0.3,
                e -> !e.equals(p) && !(e instanceof Item));
    }

    /** The point the player is looking at (block, entity, or max range). */
    public static Location point(Player p, double range) {
        RayTraceResult r = trace(p, range);
        if (r != null && r.getHitPosition() != null) return r.getHitPosition().toLocation(p.getWorld());
        Location eye = p.getEyeLocation();
        return eye.add(eye.getDirection().multiply(range));
    }

    public static LivingEntity entity(Player p, double range) {
        RayTraceResult r = trace(p, range);
        return r != null && r.getHitEntity() instanceof LivingEntity le ? le : null;
    }
}
