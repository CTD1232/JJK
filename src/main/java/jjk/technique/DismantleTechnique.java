package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DismantleTechnique extends Technique {
    private final Random rnd = new Random();

    public DismantleTechnique(JJKPlugin plugin) { super(plugin, "dismantle", "Dismantle"); }

    @Override
    public boolean cast(Player p) {
        double range = num("range", 25), width = num("width", 1.5), dmg = num("damage", 7);
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Location l = p.getEyeLocation();
        Set<UUID> hit = new HashSet<>();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.7f);
        for (double i = 0; i < range; i += 1) {
            l.add(dir);
            if (!l.getBlock().isPassable()) break;
            if ((int) i % 4 == 2) {
                Location s = l.clone().add(rnd.nextGaussian() * 0.3, rnd.nextGaussian() * 0.3, rnd.nextGaussian() * 0.3);
                s.setDirection(dir);
                ItemDisplay slash = VFX.spawn(s, VFX.SLASH, 3.2f, false);
                VFX.rotate(slash, (float) (rnd.nextDouble() * Math.PI), 0, 0, 1, 0);
                VFX.removeLater(slash, 5);
            } else if ((int) i % 3 == 0) {
                l.getWorld().spawnParticle(Particle.SWEEP_ATTACK, l, 1, 0, 0, 0, 0);
            }
            for (Entity e : l.getWorld().getNearbyEntities(l, width, width, width)) {
                if (e instanceof LivingEntity le && !(e instanceof ArmorStand) && !e.equals(p) && hit.add(e.getUniqueId())) {
                    plugin.damage(le, dmg, p, false);
                    le.getWorld().spawnParticle(Particle.CRIT, le.getLocation().add(0, 1, 0), 15, 0.4, 0.6, 0.4, 0.3);
                }
            }
        }
        return true;
    }
}
