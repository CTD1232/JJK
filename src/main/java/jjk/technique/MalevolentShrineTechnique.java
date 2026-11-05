package jjk.technique;

import jjk.DomainSpec;
import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class MalevolentShrineTechnique extends Technique {
    public MalevolentShrineTechnique(JJKPlugin plugin) { super(plugin, "malevolent_shrine", "Domain Expansion: Malevolent Shrine"); }

    @Override
    public boolean cast(Player p) {
        DomainSpec s = new DomainSpec();
        s.name = "Malevolent Shrine";
        s.radius = num("radius", 20);
        s.durationTicks = (int) (num("duration", 10) * 20);
        s.upkeepPerSecond = num("upkeep", 10);
        s.interval = 5;
        if (bool("barrier", false)) {
            Material m = Material.matchMaterial(str("shell", "RED_TERRACOTTA"));
            s.shell = m == null ? Material.RED_TERRACOTTA : m;
        }
        double dmg = num("damage", 4);
        if (VFX.enabled()) {
            // The shrine itself (resource pack model) stands in front of the caster.
            Vector fwd = p.getLocation().getDirection().setY(0);
            if (fwd.lengthSquared() < 0.0001) fwd = new Vector(0, 0, 1);
            fwd.normalize();
            Location at = p.getLocation().add(fwd.multiply(num("shrine_distance", 9)));
            at.setYaw(p.getLocation().getYaw() + 180f);
            at.setPitch(0);
            float scale = (float) num("shrine_scale", 6);
            s.visuals = d -> java.util.Collections.<Entity>singletonList(VFX.spawn(at, VFX.SHRINE, scale, true));
        }
        // Sure-hit: endless Dismantle and Cleave slashes. Ignores Infinity.
        s.effect = (d, e) -> {
            Player owner = plugin.getServer().getPlayer(d.owner);
            if (owner == null) return;
            plugin.damage(e, dmg, owner, true);
            e.getWorld().spawnParticle(Particle.SWEEP_ATTACK, e.getLocation().add(0, 1, 0), 2, 0.5, 0.5, 0.5, 0);
            e.getWorld().spawnParticle(Particle.CRIT, e.getLocation().add(0, 1, 0), 8, 0.4, 0.6, 0.4, 0.2);
        };
        s.ambient = d -> FX.inside(d.center, d.spec.radius - 1, Particle.DUST, 40, FX.dust(160, 10, 10, 1.6f));
        return plugin.domains().expand(p, s);
    }
}
