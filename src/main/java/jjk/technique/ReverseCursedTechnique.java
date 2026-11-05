package jjk.technique;

import jjk.JJKPlugin;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReverseCursedTechnique extends Technique {
    private final Map<UUID, BukkitTask> channels = new HashMap<>();

    public ReverseCursedTechnique(JJKPlugin plugin) { super(plugin, "reverse_cursed_technique", "Reverse Cursed Technique"); }

    @Override
    public double cost(Player p) { return channels.containsKey(p.getUniqueId()) ? 0 : super.cost(p); }

    @Override
    public boolean cast(Player p) {
        BukkitTask existing = channels.remove(p.getUniqueId());
        if (existing != null) {
            existing.cancel();
            plugin.msg(p, "&aReverse Cursed Technique &7stopped");
            return true;
        }
        double pulseCost = num("pulse_cost", 6), heal = num("heal_per_pulse", 1.0);
        int pulse = Math.max(1, (int) num("pulse_ticks", 5));
        plugin.msg(p, "&aReverse Cursed Technique &7channeling");
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) { stop(p); return; }
                double max = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                if (p.getHealth() >= max) { plugin.msg(p, "&aFully healed"); stop(p); return; }
                if (!plugin.energy().consume(p, pulseCost)) { plugin.msg(p, "&cOut of cursed energy"); stop(p); return; }
                p.setHealth(Math.min(max, p.getHealth() + heal));
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 12, 0.4, 0.7, 0.4, 0,
                        new Particle.DustOptions(Color.fromRGB(80, 255, 120), 1.3f));
            }
        }.runTaskTimer(plugin, pulse, pulse);
        channels.put(p.getUniqueId(), task);
        return true;
    }

    private void stop(Player p) {
        BukkitTask t = channels.remove(p.getUniqueId());
        if (t != null) t.cancel();
    }
}
