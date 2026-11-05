package jjk;

import jjk.util.FX;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MeleeListener implements Listener {
    private final JJKPlugin plugin;

    public MeleeListener(JJKPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMelee(EntityDamageByEntityEvent e) {
        if (plugin.isInternalDamage()) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(e.getDamager() instanceof Player p) || !(e.getEntity() instanceof LivingEntity t)) return;

        if (plugin.melee().consumeBlackFlash(p)) {
            double mult = plugin.getConfig().getDouble("techniques.black_flash.multiplier", 2.5);
            e.setDamage(e.getDamage() * mult);
            var loc = t.getLocation().add(0, 1, 0);
            t.getWorld().spawnParticle(Particle.DUST, loc, 40, 0.5, 0.7, 0.5, 0, FX.dust(10, 10, 10, 2.4f));
            t.getWorld().spawnParticle(Particle.DUST, loc, 30, 0.5, 0.7, 0.5, 0, FX.dust(255, 30, 30, 1.8f));
            t.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 40, 0.6, 0.8, 0.6, 0.4);
            t.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.6f);
            int zone = (int) (plugin.getConfig().getDouble("techniques.black_flash.zone_seconds", 15) * 20);
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, zone, 1, false, true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, zone, 1, false, true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, zone, 0, false, true));
            plugin.energy().add(p, plugin.energy().max(p) * plugin.getConfig().getDouble("techniques.black_flash.ce_restore", 0.2));
            plugin.msg(p, "&0&lBLACK FLASH &7- you are in the Zone");
        }

        if (plugin.melee().consumeDivergent(p)) {
            double dealt = e.getDamage();
            double mult = plugin.getConfig().getDouble("techniques.divergent_fist.second_hit", 1.5);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (t.isDead()) return;
                plugin.damage(t, dealt * mult, p, false);
                t.getWorld().spawnParticle(Particle.CRIT, t.getLocation().add(0, 1, 0), 30, 0.4, 0.7, 0.4, 0.4);
                t.getWorld().playSound(t.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.5f, 0.5f);
            }, (long) (plugin.getConfig().getDouble("techniques.divergent_fist.delay", 1.0) * 20));
        }
    }
}
