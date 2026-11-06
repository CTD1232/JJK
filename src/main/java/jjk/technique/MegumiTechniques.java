package jjk.technique;

import jjk.DomainSpec;
import jjk.JJKPlugin;
import jjk.ShikigamiManager;
import jjk.util.Aim;
import jjk.util.FX;
import jjk.util.VFX;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

/** Ten Shadows Technique: Megumi's shikigami. */
final class MegumiTechniques { private MegumiTechniques() {} }

class DivineDogsTechnique extends Technique {
    DivineDogsTechnique(JJKPlugin plugin) { super(plugin, "divine_dogs", "Divine Dogs"); }

    @Override
    public boolean cast(Player p) {
        plugin.shikigami().dismiss(p.getUniqueId(), "dogs");
        double secs = num("duration", 40), dmg = num("damage", 6), hp = num("health", 30);
        LivingEntity target = Aim.entity(p, 25);
        DyeColor[] colors = {DyeColor.WHITE, DyeColor.BLACK};
        String[] names = {"&f&lDivine Dog: White", "&8&lDivine Dog: Black"};
        for (int i = 0; i < 2; i++) {
            final DyeColor color = colors[i];
            final String name = names[i];
            Location l = p.getLocation().add(i == 0 ? 1.5 : -1.5, 0, 0);
            Wolf w = p.getWorld().spawn(l, Wolf.class, wolf -> {
                wolf.setOwner(p);
                wolf.setCollarColor(color);
                wolf.customName(plugin.legacy(name));
            });
            ShikigamiManager.attr(w, Attribute.MAX_HEALTH, hp);
            w.setHealth(hp);
            ShikigamiManager.attr(w, Attribute.ATTACK_DAMAGE, dmg);
            plugin.shikigami().track(w, p, "dogs", secs, true);
            if (target != null) w.setTarget(target);
            w.getWorld().spawnParticle(Particle.SMOKE, l.clone().add(0, 0.5, 0), 25, 0.3, 0.4, 0.3, 0.05);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.5f, 0.8f);
        return true;
    }
}

class NueTechnique extends Technique {
    NueTechnique(JJKPlugin plugin) { super(plugin, "nue", "Nue"); }

    @Override
    public boolean cast(Player p) {
        plugin.shikigami().dismiss(p.getUniqueId(), "nue");
        Location l = p.getLocation().add(0, 4, 0);
        Phantom ph = p.getWorld().spawn(l, Phantom.class, x -> {
            x.setShouldBurnInDay(false);
            x.setSize((int) num("size", 3));
            x.customName(plugin.legacy("&e&lNue"));
        });
        ShikigamiManager.attr(ph, Attribute.ATTACK_DAMAGE, num("damage", 6));
        plugin.shikigami().track(ph, p, "nue", num("duration", 15), true);
        LivingEntity t = Aim.entity(p, 30);
        if (t != null) {
            ph.setTarget(t);
            t.getWorld().strikeLightningEffect(t.getLocation());
            plugin.damage(t, num("shock_damage", 8), p, false);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 2f, 1.5f);
        return true;
    }
}

class ToadTechnique extends Technique {
    ToadTechnique(JJKPlugin plugin) { super(plugin, "toad", "Toad"); }

    @Override
    public boolean cast(Player p) {
        LivingEntity t = Aim.entity(p, num("range", 20));
        if (t == null) { plugin.msg(p, "&cNo target in sight"); return false; }
        Location from = p.getEyeLocation(), to = t.getLocation().add(0, 1, 0);
        Vector line = to.toVector().subtract(from.toVector());
        double len = line.length();
        Vector step = line.clone().normalize().multiply(0.5);
        Location c = from.clone();
        for (double d = 0; d < len; d += 0.5) {
            c.add(step);
            c.getWorld().spawnParticle(Particle.DUST, c, 1, 0, 0, 0, 0, FX.dust(255, 120, 160, 1.2f));
        }
        Vector pull = p.getLocation().toVector().subtract(t.getLocation().toVector());
        if (pull.lengthSquared() > 0.01) t.setVelocity(pull.normalize().multiply(num("pull", 1.3)).setY(0.3));
        t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int) (num("slow_seconds", 3) * 20), 2, false, true));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FROG_TONGUE, 2f, 1f);
        return true;
    }
}

class MaxElephantTechnique extends Technique {
    MaxElephantTechnique(JJKPlugin plugin) { super(plugin, "max_elephant", "Max: Elephant"); }

    @Override
    public boolean cast(Player p) {
        double range = num("range", 12), dmg = num("damage", 6), knock = num("knockback", 1.8);
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Random rnd = new Random();
        Vector flat = dir.clone().setY(0);
        if (flat.lengthSquared() > 0.0001) {
            flat.normalize();
            Location start = p.getLocation().add(flat.clone().multiply(2));
            start.setYaw(p.getLocation().getYaw());
            start.setPitch(0);
            ItemDisplay elephant = VFX.spawn(start, VFX.ELEPHANT, 3f, true);
            if (elephant != null) {
                new BukkitRunnable() {
                    int t = 0;
                    @Override
                    public void run() {
                        if (t++ >= 25 || !elephant.isValid()) { VFX.remove(elephant); cancel(); return; }
                        elephant.teleport(elephant.getLocation().add(flat.clone().multiply(0.5)));
                    }
                }.runTaskTimer(plugin, 0, 1);
            }
        }
        for (double i = 1; i < range; i += 0.5) {
            Location c = p.getEyeLocation().add(dir.clone().multiply(i));
            c.getWorld().spawnParticle(Particle.SPLASH, c, 8, i * 0.12, i * 0.12, i * 0.12, 0.1);
            if (rnd.nextInt(3) == 0) c.getWorld().spawnParticle(Particle.BUBBLE_POP, c, 3, i * 0.1, i * 0.1, i * 0.1, 0.05);
        }
        for (Entity e : p.getNearbyEntities(range, range, range)) {
            if (!(e instanceof LivingEntity le) || plugin.shikigami().info(e) != null && plugin.shikigami().info(e).owner.equals(p.getUniqueId())) continue;
            Vector to = le.getLocation().add(0, 1, 0).toVector().subtract(p.getEyeLocation().toVector());
            if (to.length() > range || to.normalize().dot(dir) < 0.6) continue;
            le.setFireTicks(0);
            le.setVelocity(dir.clone().multiply(knock).setY(0.4));
            plugin.damage(le, dmg, p, false);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 2f, 0.5f);
        return true;
    }
}

class RabbitEscapeTechnique extends Technique {
    RabbitEscapeTechnique(JJKPlugin plugin) { super(plugin, "rabbit_escape", "Rabbit Escape"); }

    @Override
    public boolean cast(Player p) {
        Random rnd = new Random();
        int count = (int) num("rabbits", 12);
        for (int i = 0; i < count; i++) {
            Location l = p.getLocation().add(rnd.nextDouble() * 4 - 2, 0, rnd.nextDouble() * 4 - 2);
            Rabbit r = p.getWorld().spawn(l, Rabbit.class);
            plugin.shikigami().track(r, p, "rabbit", num("duration", 6), false);
        }
        double radius = num("radius", 8);
        for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Mob m && p.equals(m.getTarget())) m.setTarget(null);
            if (e instanceof LivingEntity le && plugin.shikigami().info(e) == null) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false));
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false));
        p.getWorld().spawnParticle(Particle.POOF, p.getLocation().add(0, 1, 0), 40, 1, 0.5, 1, 0.05);
        return true;
    }
}

class ChimeraShadowGardenTechnique extends Technique {
    ChimeraShadowGardenTechnique(JJKPlugin plugin) { super(plugin, "chimera_shadow_garden", "Domain Expansion: Chimera Shadow Garden"); }

    @Override
    public boolean cast(Player p) {
        DomainSpec s = new DomainSpec();
        s.name = "Chimera Shadow Garden";
        s.radius = num("radius", 15);
        s.durationTicks = (int) (num("duration", 15) * 20);
        s.upkeepPerSecond = num("upkeep", 12);
        s.interval = 10;
        s.shell = null; // barrierless: the shadows pool across the ground
        double dmg = num("damage", 1);
        float diameter = (float) (s.radius * 2);
        s.visuals = d -> {
            Location floor = d.center.clone().add(0, 0.03, 0);
            floor.setYaw(0);
            floor.setPitch(0);
            return java.util.Collections.<Entity>singletonList(VFX.spawn(floor, VFX.SHADOW, diameter, true));
        };
        s.effect = (d, e) -> {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 3, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 30, 2, false, false));
            e.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false));
            Player owner = plugin.getServer().getPlayer(d.owner);
            if (owner != null && dmg > 0) plugin.damage(e, dmg, owner, true);
        };
        s.ambient = d -> {
            FX.inside(d.center, d.spec.radius - 1, Particle.SQUID_INK, 30, null);
            for (Entity sh : plugin.shikigami().ownedBy(d.owner)) {
                if (sh instanceof LivingEntity le && le.getLocation().distanceSquared(d.center) <= d.spec.radius * d.spec.radius) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 1, false, false));
                }
            }
        };
        return plugin.domains().expand(p, s);
    }
}

class MahoragaTechnique extends Technique {
    MahoragaTechnique(JJKPlugin plugin) { super(plugin, "mahoraga", "Divergent Sila Divine General Mahoraga"); }

    @Override
    public boolean cast(Player p) {
        if (plugin.shikigami().has(p.getUniqueId(), "mahoraga")) {
            plugin.shikigami().dismiss(p.getUniqueId(), "mahoraga");
            plugin.msg(p, "&6Mahoraga &7dismissed");
            return false;
        }
        Location l = p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(3));
        double hp = num("health", 200);
        IronGolem g = p.getWorld().spawn(l, IronGolem.class, x -> {
            x.setPlayerCreated(true);
            x.customName(plugin.legacy("&6Mahoraga"));
            x.setCustomNameVisible(true);
        });
        ShikigamiManager.attr(g, Attribute.MAX_HEALTH, hp);
        g.setHealth(hp);
        ShikigamiManager.attr(g, Attribute.ATTACK_DAMAGE, num("damage", 14));
        plugin.shikigami().track(g, p, "mahoraga", num("duration", 60), true);
        ItemDisplay wheel = VFX.spawn(g.getLocation(), VFX.WHEEL, 2.4f, false);
        if (wheel != null) {
            wheel.setBillboard(org.bukkit.entity.Display.Billboard.VERTICAL);
            VFX.offset(wheel, 0f, 1.6f, 0f);
            g.addPassenger(wheel);
            plugin.shikigami().info(g).visual = wheel;
        }
        LivingEntity t = Aim.entity(p, 30);
        if (t != null) g.setTarget(t);
        FX.sphere(g.getLocation().add(0, 3.5, 0), 1.2, Particle.END_ROD, 40, null);
        p.getWorld().playSound(l, Sound.ENTITY_WITHER_SPAWN, 2f, 0.5f);
        return true;
    }
}
