package jjk.technique;

import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CleaveTechnique extends Technique {
    public CleaveTechnique(JJKPlugin plugin) { super(plugin, "cleave", "Cleave"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("range", 20));
        if (t == null) { plugin.msg(p, "&cNo target in sight"); return false; }
        // Cleave adapts to its target: stronger the healthier (tougher) they are.
        double dmg = num("base_damage", 6) + t.getHealth() * num("health_scale", 0.35);
        plugin.damage(t, dmg, p, false);
        Location at = t.getLocation().add(0, t.getHeight() / 2, 0);
        at.setDirection(p.getLocation().getDirection().multiply(-1)); // face the attacker
        for (int sign : new int[]{1, -1}) {
            ItemDisplay slash = VFX.spawn(at, VFX.SLASH, 3.6f, false);
            VFX.rotate(slash, (float) Math.toRadians(45 * sign), 0, 0, 1, 0);
            VFX.removeLater(slash, 6);
        }
        t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.6f);
        t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 30, 0.5, 0.7, 0.5, 0.4);
        return true;
    }
}
