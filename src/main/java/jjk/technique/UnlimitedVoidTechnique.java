package jjk.technique;

import jjk.DomainSpec;
import jjk.JJKPlugin;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class UnlimitedVoidTechnique extends Technique {
    public UnlimitedVoidTechnique(JJKPlugin plugin) { super(plugin, "unlimited_void", "Domain Expansion: Unlimited Void"); }

    @Override
    public boolean cast(Player p) {
        DomainSpec s = new DomainSpec();
        s.name = "Unlimited Void";
        s.radius = num("radius", 14);
        s.durationTicks = (int) (num("duration", 12) * 20);
        s.upkeepPerSecond = num("upkeep", 10);
        s.interval = 10;
        if (bool("dome", true) && VFX.enabled()) {
            // Starfield room (resource pack model) instead of a block shell.
            s.shell = null;
            float side = (float) (s.radius * 2);
            s.visuals = d -> {
                Location c = d.center.clone().add(0, s.radius - 1, 0);
                return java.util.Collections.<Entity>singletonList(VFX.spawn(c, VFX.STARS, -side, -side, -side, false));
            };
        } else {
            Material m = Material.matchMaterial(str("shell", "BLACK_CONCRETE"));
            s.shell = m == null ? Material.BLACK_CONCRETE : m;
        }
        // Sure-hit: infinite information overload, so the victim cannot act. Potion-based, so Infinity does not stop it.
        s.effect = (d, e) -> {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 9, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 30, 4, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 4, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, false));
        };
        s.ambient = d -> FX.inside(d.center, d.spec.radius - 1, Particle.END_ROD, 30, null);
        return plugin.domains().expand(p, s);
    }
}
