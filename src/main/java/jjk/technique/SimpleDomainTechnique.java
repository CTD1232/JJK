package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SimpleDomainTechnique extends Technique {
    public SimpleDomainTechnique(JJKPlugin plugin) { super(plugin, "simple_domain", "Simple Domain"); }

    @Override
    public boolean cast(Player p) {
        double secs = num("duration", 5);
        plugin.domains().grantSimpleDomain(p, secs);
        FX.sphere(p.getLocation().add(0, 1, 0), 2.5, Particle.END_ROD, 40, null);
        Location feet = p.getLocation().add(0, 0.03, 0);
        feet.setYaw(0);
        feet.setPitch(0);
        VFX.removeLater(VFX.spawn(feet, VFX.RING, 5f, true), (long) (secs * 20));
        plugin.msg(p, "&fSimple Domain &7raised: immune to sure-hit effects");
        return true;
    }
}
