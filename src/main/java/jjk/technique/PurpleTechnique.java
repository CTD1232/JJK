package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PurpleTechnique extends Technique {
    public PurpleTechnique(JJKPlugin plugin) { super(plugin, "gojo_purple", "Hollow Purple"); }

    @Override
    public boolean cast(Player p) {
        int charge = (int) (num("charge", 1.2) * 20);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, charge + 5, 4, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 2f, 0.5f);
        Location start = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(2));
        ItemDisplay blue = VFX.spawn(start, VFX.ORB_BLUE, 1.0f, false);
        ItemDisplay red = VFX.spawn(start, VFX.ORB_RED, 1.0f, false);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) { VFX.remove(blue); VFX.remove(red); cancel(); return; }
                Vector dir = p.getEyeLocation().getDirection();
                Location core = p.getEyeLocation().add(dir.clone().multiply(2));
                Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
                if (right.lengthSquared() < 0.0001) right = new Vector(1, 0, 0);
                right.normalize();
                double gap = 1.6 * (1 - (double) t / charge);
                if (blue != null) blue.teleport(core.clone().add(right.clone().multiply(gap)));
                if (red != null) red.teleport(core.clone().subtract(right.clone().multiply(gap)));
                FX.sphere(core, gap + 0.3, Particle.DUST, 10, FX.dust(150, 90, 255, 1.4f));
                if (++t >= charge) { VFX.remove(blue); VFX.remove(red); cancel(); fire(p); }
            }
        }.runTaskTimer(plugin, 0, 1);
        return true;
    }

    private void fire(Player p) {
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Location loc = p.getEyeLocation().add(dir.clone().multiply(2));
        double speed = num("speed", 2.5), range = num("range", 50), radius = num("radius", 3.5), dmg = num("damage", 40);
        boolean breakBlocks = bool("break_blocks", false);
        int carve = (int) num("carve_radius", 3);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
        ItemDisplay purple = VFX.spawn(loc, VFX.ORB_PURPLE, 3.6f, false);
        Set<UUID> hit = new HashSet<>();
        new BukkitRunnable() {
            double travelled = 0;
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                int steps = (int) Math.ceil(speed / 0.5);
                double step = speed / steps;
                for (int i = 0; i < steps; i++) {
                    loc.add(dir.clone().multiply(step));
                    travelled += step;
                    if (purple != null) purple.teleport(loc);
                    FX.inside(loc, radius * 0.7, Particle.DUST, 6, FX.dust(170, 60, 255, 2.5f));
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                        if (e instanceof LivingEntity le && !e.equals(p) && hit.add(e.getUniqueId())) {
                            plugin.damage(le, dmg, p, false);
                        }
                    }
                    if (breakBlocks) carve(loc, carve);
                    else if (!loc.getBlock().isPassable()) { end(loc, purple); cancel(); return; }
                    if (travelled >= range) { end(loc, purple); cancel(); return; }
                }
                if (purple != null) VFX.rotate(purple, (float) Math.toRadians(ticks * 25.0), 0.4f, 1f, 0.3f, 1);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void end(Location loc, ItemDisplay purple) {
        VFX.remove(purple);
        FX.inside(loc, 3, Particle.DUST, 60, FX.dust(170, 60, 255, 2.5f));
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
    }

    private void carve(Location c, int r) {
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z > r * r) continue;
                    Block b = c.clone().add(x, y, z).getBlock();
                    Material m = b.getType();
                    if (m.isAir() || m.getHardness() < 0 || b.isLiquid()) continue;
                    b.setType(Material.AIR, false);
                }
    }
}
