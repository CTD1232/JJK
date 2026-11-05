package jjk.technique;

import jjk.DomainSpec;
import jjk.JJKPlugin;
import jjk.ShikigamiManager;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Yuta Okkotsu: Rika, Copy / Mimic, Authentic Mutual Love. */
final class YutaTechniques { private YutaTechniques() {} }

class RikaTechnique extends Technique {
    RikaTechnique(JJKPlugin plugin) { super(plugin, "rika", "Rika"); }

    @Override
    public boolean cast(Player p) {
        if (plugin.shikigami().has(p.getUniqueId(), "rika")) {
            plugin.shikigami().dismiss(p.getUniqueId(), "rika");
            plugin.msg(p, "&5Rika &7dismissed");
            return false;
        }
        Vector dir = p.getLocation().getDirection().setY(0);
        if (dir.lengthSquared() < 0.001) dir = new Vector(0, 0, 1);
        Location l = p.getLocation().add(dir.normalize().multiply(-2));
        double hp = num("health", 150);
        Ravager body = p.getWorld().spawn(l, Ravager.class, x -> x.setCustomNameVisible(false));
        ShikigamiManager.attr(body, Attribute.MAX_HEALTH, hp);
        body.setHealth(hp);
        ShikigamiManager.attr(body, Attribute.ATTACK_DAMAGE, num("damage", 12));
        plugin.shikigami().track(body, p, "rika", num("duration", 40), true);
        // The Ravager is only the body; the Rika model follows it. Without visuals the Ravager stays visible.
        ItemDisplay skin = VFX.spawn(l, VFX.RIKA, (float) num("scale", 1.9), true);
        if (skin != null) {
            body.setInvisible(true);
            ShikigamiManager.Info info = plugin.shikigami().info(body);
            info.visual = skin;
            info.syncVisual = true;
        }
        LivingEntity t = Aim.entity(p, 30);
        if (t != null) body.setTarget(t);
        p.getWorld().playSound(l, Sound.ENTITY_RAVAGER_ROAR, 2f, 0.5f);
        p.getWorld().spawnParticle(Particle.SOUL, l.clone().add(0, 1.5, 0), 40, 0.8, 1.2, 0.8, 0.03);
        return true;
    }
}

class CopyTechnique extends Technique {
    record Copied(String id, long until) {}
    static final Map<UUID, Copied> COPIED = new HashMap<>();

    CopyTechnique(JJKPlugin plugin) { super(plugin, "copy", "Copy"); }

    @Override
    public boolean cast(Player p) {
        if (!(Aim.entity(p, num("range", 20)) instanceof Player target)) {
            plugin.msg(p, "&cAim at another sorcerer");
            return false;
        }
        String id = plugin.abilities().lastCast(target.getUniqueId());
        Technique tech = id == null ? null : plugin.techniques().get(id);
        if (tech == null || !tech.usesCursedEnergy() || id.equals("copy") || id.equals("mimic")) {
            plugin.msg(p, "&cNothing to copy");
            return false;
        }
        COPIED.put(p.getUniqueId(), new Copied(id, System.currentTimeMillis() + (long) (num("duration", 300) * 1000)));
        plugin.msg(p, "&dCopied: &f" + tech.display());
        p.getWorld().spawnParticle(Particle.ENCHANT, target.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.5);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5f, 1.2f);
        return true;
    }
}

class MimicTechnique extends Technique {
    MimicTechnique(JJKPlugin plugin) { super(plugin, "mimic", "Mimic"); }

    @Override
    public boolean cast(Player p) {
        CopyTechnique.Copied c = CopyTechnique.COPIED.get(p.getUniqueId());
        if (c == null || c.until() < System.currentTimeMillis()) {
            plugin.msg(p, "&cNo copied technique");
            return false;
        }
        return plugin.abilities().use(p, c.id()); // the copied technique charges its own cost and cooldown
    }
}

class AuthenticMutualLoveTechnique extends Technique {
    AuthenticMutualLoveTechnique(JJKPlugin plugin) { super(plugin, "authentic_mutual_love", "Domain Expansion: Authentic Mutual Love"); }

    @Override
    public boolean cast(Player p) {
        DomainSpec s = new DomainSpec();
        s.name = "Authentic Mutual Love";
        s.radius = num("radius", 15);
        s.durationTicks = (int) (num("duration", 12) * 20);
        s.upkeepPerSecond = num("upkeep", 12);
        s.interval = 10;
        s.shell = null; // barrierless: swords rain down inside the domain
        double dmg = num("damage", 2);
        s.effect = (d, e) -> {
            Player owner = Bukkit.getPlayer(d.owner);
            if (owner != null) plugin.damage(e, dmg, owner, true);
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 30, 1, false, false));
            e.getWorld().spawnParticle(Particle.ENCHANTED_HIT, e.getLocation().add(0, 1, 0), 10, 0.4, 0.6, 0.4, 0.3);
        };
        s.ambient = d -> {
            FX.inside(d.center.clone().add(0, d.spec.radius * 0.6, 0), d.spec.radius * 0.8, Particle.ENCHANTED_HIT, 25, null);
            Player owner = Bukkit.getPlayer(d.owner);
            if (owner != null) owner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 2, false, false));
            for (Entity sh : plugin.shikigami().ownedBy(d.owner)) {
                if (sh instanceof LivingEntity le) le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 2, false, false));
            }
        };
        return plugin.domains().expand(p, s);
    }
}
