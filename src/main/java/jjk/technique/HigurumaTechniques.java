package jjk.technique;

import jjk.DomainManager;
import jjk.DomainSpec;
import jjk.JJKPlugin;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Hiromi Higuruma: Judgeman, Confiscation and the Executioner's Sword. */
final class HigurumaTechniques { private HigurumaTechniques() {} }

class DeadlySentencingTechnique extends Technique {
    DeadlySentencingTechnique(JJKPlugin plugin) { super(plugin, "deadly_sentencing", "Domain Expansion: Deadly Sentencing"); }

    @Override
    public boolean cast(Player p) {
        DomainSpec s = new DomainSpec();
        s.name = "Deadly Sentencing";
        s.radius = num("radius", 16);
        s.durationTicks = (int) (num("duration", 15) * 20);
        s.upkeepPerSecond = num("upkeep", 12);
        s.interval = 10;
        s.shell = null;
        long start = System.currentTimeMillis();
        Set<UUID> defendants = new HashSet<>();
        float diameter = (float) (s.radius * 2);
        float gavelScale = (float) (s.radius * 0.7);
        s.visuals = d -> {
            Location top = d.center.clone().add(0, s.radius * 0.55, 0);
            Location floor = d.center.clone().add(0, 0.03, 0);
            floor.setYaw(0);
            floor.setPitch(0);
            return Arrays.<Entity>asList(VFX.spawn(top, VFX.GAVEL, gavelScale, false), VFX.spawn(floor, VFX.RING, diameter, true));
        };
        // Everyone inside is a defendant: their cursed techniques are sealed for the trial.
        s.effect = (d, e) -> {
            defendants.add(e.getUniqueId());
            plugin.abilities().seal(e.getUniqueId(), 2500);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, false, false));
        };
        s.ambient = d -> FX.inside(d.center, d.spec.radius - 1, Particle.ENCHANT, 40, null);
        // Verdict: anyone who hurt a player during the trial is guilty (Executioner's Sword), the rest are confiscated.
        double sword = num("sword_damage", 40), confiscate = num("confiscation_seconds", 60);
        s.onEnd = (d, reason) -> {
            if (!reason.equals("expired")) return;
            Player owner = Bukkit.getPlayer(d.owner);
            if (owner == null || owner.isDead()) return;
            for (UUID id : defendants) {
                if (!(Bukkit.getEntity(id) instanceof LivingEntity le) || !le.isValid() || le.isDead()) continue;
                Location above = le.getEyeLocation().add(0, 0.8, 0);
                if (plugin.lastAttack(id) >= start) {
                    VFX.text(above, "&4&lGUILTY", 40);
                    plugin.damage(le, 0.01, owner, true);
                    if (le.isValid() && !le.isDead()) le.setHealth(Math.max(0, le.getHealth() - sword)); // ignores armor
                    le.getWorld().playSound(le.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2f, 0.6f);
                } else {
                    VFX.text(above, "&a&lNOT GUILTY", 40);
                    plugin.abilities().seal(id, (long) (confiscate * 1000));
                }
            }
        };
        return plugin.domains().expand(p, s);
    }
}

class ConfiscationTechnique extends Technique {
    ConfiscationTechnique(JJKPlugin plugin) { super(plugin, "confiscation", "Confiscation"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("range", 20));
        if (t == null) { plugin.msg(p, "&cNo target in sight"); return false; }
        plugin.abilities().seal(t.getUniqueId(), (long) (num("seconds", 20) * 1000));
        VFX.text(t.getEyeLocation().add(0, 0.8, 0), "&6&lCONFISCATED", 40);
        t.getWorld().playSound(t.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.5f, 1.4f);
        t.getWorld().spawnParticle(Particle.ENCHANT, t.getLocation().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.5);
        if (t instanceof Player tp) plugin.msg(tp, "&cYour cursed technique was confiscated");
        return true;
    }
}

class ExecutionerSwordTechnique extends Technique {
    ExecutionerSwordTechnique(JJKPlugin plugin) { super(plugin, "executioner_sword", "Executioner's Sword"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("range", 8));
        if (t == null) { plugin.msg(p, "&cNo target in reach"); return false; }
        long window = (long) (num("guilt_window", 15) * 1000);
        if (System.currentTimeMillis() - plugin.lastAttack(t.getUniqueId()) > window) {
            VFX.text(t.getEyeLocation().add(0, 0.8, 0), "&a&lNOT GUILTY", 30);
            plugin.msg(p, "&7No recent crime: the sword will not answer");
            return false;
        }
        VFX.text(t.getEyeLocation().add(0, 0.8, 0), "&4&lGUILTY", 40);
        plugin.damage(t, 0.01, p, true);
        if (t.isValid() && !t.isDead()) t.setHealth(Math.max(0, t.getHealth() - num("damage", 30))); // ignores armor
        t.getWorld().playSound(t.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 2f, 0.6f);
        t.getWorld().spawnParticle(Particle.SWEEP_ATTACK, t.getLocation().add(0, 1, 0), 4, 0.5, 0.7, 0.5, 0);
        return true;
    }
}
