package jjk.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class FX {
    private FX() {}

    public static Particle.DustOptions dust(int r, int g, int b, float size) {
        return new Particle.DustOptions(Color.fromRGB(r, g, b), size);
    }

    private static Vector randomUnit() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Vector v;
        do {
            v = new Vector(rnd.nextDouble(-1, 1), rnd.nextDouble(-1, 1), rnd.nextDouble(-1, 1));
        } while (v.lengthSquared() > 1 || v.lengthSquared() < 0.0001);
        return v;
    }

    /** Particles on the surface of a sphere. */
    public static void sphere(Location c, double r, Particle p, int count, Object data) {
        World w = c.getWorld();
        for (int i = 0; i < count; i++) {
            w.spawnParticle(p, c.clone().add(randomUnit().normalize().multiply(r)), 1, 0, 0, 0, 0, data);
        }
    }

    /** Particles scattered through the inside of a sphere. */
    public static void inside(Location c, double r, Particle p, int count, Object data) {
        World w = c.getWorld();
        for (int i = 0; i < count; i++) {
            w.spawnParticle(p, c.clone().add(randomUnit().multiply(r)), 1, 0, 0, 0, 0, data);
        }
    }
}
