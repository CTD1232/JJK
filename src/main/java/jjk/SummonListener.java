package jjk;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SummonListener implements Listener {
    private final JJKPlugin plugin;

    public SummonListener(JJKPlugin plugin) { this.plugin = plugin; }

    /** No friendly fire between a sorcerer and their shikigami. */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Projectile pr && pr.getShooter() instanceof Entity shooter) damager = shooter;
        SummonManager sm = plugin.summons();
        if (sm.sameTeam(damager, e.getEntity())) { e.setCancelled(true); return; }
        if (damager instanceof Player p && e.getEntity() instanceof LivingEntity victim) sm.setTarget(p, victim);
        if (e.getEntity() instanceof Player p && damager instanceof LivingEntity attacker) sm.setTarget(p, attacker);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (e.getTarget() != null && plugin.summons().isSummon(e.getEntity())
                && plugin.summons().sameTeam(e.getEntity(), e.getTarget())) e.setCancelled(true);
    }

    /** Mahoraga adapts: each new kind of damage is weakened more every time it lands. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAdapt(EntityDamageEvent e) {
        SummonManager.Summon s = plugin.summons().get(e.getEntity());
        if (s == null || !s.kind.equals("mahoraga") || !(e.getEntity() instanceof LivingEntity m)) return;
        String key = e.getCause().name();
        if (e instanceof EntityDamageByEntityEvent be) key += ":" + be.getDamager().getType() + (be.getDamager() instanceof Player p ? ":" + p.getName() : "");
        int level = s.adapt.getOrDefault(key, 0);
        double per = plugin.getConfig().getDouble("techniques.mahoraga.adapt_per_level", 0.18);
        e.setDamage(e.getDamage() * Math.max(0.05, 1 - per * level));
        int maxLevel = (int) plugin.getConfig().getDouble("techniques.mahoraga.max_adapt_level", 5);
        if (level < maxLevel) {
            s.adapt.put(key, level + 1);
            double heal = plugin.getConfig().getDouble("techniques.mahoraga.heal_on_adapt", 0.05) * plugin.summons().maxHealth(m);
            m.setHealth(Math.min(plugin.summons().maxHealth(m), m.getHealth() + heal));
            Player owner = plugin.getServer().getPlayer(s.owner);
            if (owner != null) plugin.msg(owner, "&8Mahoraga adapted &7(" + (level + 1) + "/" + maxLevel + ")");
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        SummonManager.Summon s = plugin.summons().get(e.getEntity());
        if (s == null) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
        plugin.summons().forget(e.getEntity());
        Player owner = plugin.getServer().getPlayer(s.owner);
        if (owner != null) plugin.msg(owner, "&c" + s.kind.replace('_', ' ') + " was destroyed");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.summons().dismiss(e.getPlayer(), null);
        plugin.marks().clear(e.getPlayer());
        plugin.heavenly().set(e.getPlayer(), false);
    }
}
