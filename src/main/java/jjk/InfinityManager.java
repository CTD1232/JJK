package jjk;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class InfinityManager {
    private final JJKPlugin plugin;
    private final Set<UUID> active = new HashSet<>();

    public InfinityManager(JJKPlugin plugin) { this.plugin = plugin; }

    public boolean isActive(Player p) { return active.contains(p.getUniqueId()); }

    public void set(Player p, boolean on) {
        if (on) active.add(p.getUniqueId()); else active.remove(p.getUniqueId());
    }

    public void start() {
        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {
                n++;
                double repel = plugin.getConfig().getDouble("techniques.gojo_infinity.repel_radius", 4);
                double upkeep = plugin.getConfig().getDouble("techniques.gojo_infinity.upkeep", 4);
                Iterator<UUID> it = active.iterator();
                while (it.hasNext()) {
                    Player p = plugin.getServer().getPlayer(it.next());
                    if (p == null || p.isDead()) { it.remove(); continue; }
                    if (n % 10 == 0 && !plugin.energy().consume(p, upkeep)) {
                        it.remove();
                        plugin.msg(p, "&bInfinity &cfaded: out of cursed energy");
                        continue;
                    }
                    for (Entity e : p.getNearbyEntities(repel, repel, repel)) {
                        if (e instanceof Projectile proj && !p.equals(proj.getShooter())) {
                            Vector away = proj.getLocation().toVector().subtract(p.getLocation().toVector());
                            if (away.lengthSquared() < 0.01) continue;
                            proj.setVelocity(away.normalize().multiply(0.4));
                            p.getWorld().spawnParticle(Particle.DUST, proj.getLocation(), 6, 0.2, 0.2, 0.2, 0,
                                    new Particle.DustOptions(Color.fromRGB(120, 200, 255), 1.2f));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 2, 2);
    }
}
