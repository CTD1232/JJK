package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.Projectiles;
import jjk.util.VFX;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RedTechnique extends Technique {
    public RedTechnique(JJKPlugin plugin) { super(plugin, "gojo_red", "Cursed Technique Reversal: Red"); }

    @Override
    public boolean cast(Player p) {
        float power = (float) num("power", 4.0);
        boolean breakBlocks = bool("break_blocks", false);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.6f);
        Projectiles.fly(plugin, p, p.getEyeLocation(), p.getEyeLocation().getDirection(),
                num("speed", 1.6), num("range", 45), 1.0,
                VFX.spawn(p.getEyeLocation(), VFX.ORB_RED, 1.1f, false),
                loc -> loc.getWorld().spawnParticle(Particle.DUST, loc, 4, 0.15, 0.15, 0.15, 0, FX.dust(255, 40, 40, 1.8f)),
                loc -> {
                    loc.getWorld().createExplosion(loc, power, false, breakBlocks, p);
                    FX.inside(loc, power * 0.8, Particle.DUST, 40, FX.dust(255, 60, 60, 2f));
                });
        return true;
    }
}
