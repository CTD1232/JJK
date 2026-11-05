package jjk;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {
    private final JJKPlugin plugin;

    public CombatListener(JJKPlugin plugin) { this.plugin = plugin; }

    // ---- Infinity ----
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!plugin.infinity().isActive(p) || plugin.isBypassInfinity()) return;
        switch (e.getCause()) {
            case VOID, SUICIDE, STARVATION, CUSTOM, KILL -> { return; }
            default -> { }
        }
        // Heavenly Restriction users pass straight through Infinity in melee.
        if (e instanceof EntityDamageByEntityEvent be && be.getDamager() instanceof Player atk
                && plugin.heavenly().isActive(atk)) return;
        double hitCost = plugin.getConfig().getDouble("techniques.gojo_infinity.hit_cost", 8);
        if (!plugin.energy().consume(p, hitCost)) {
            plugin.infinity().set(p, false);
            plugin.msg(p, "&bInfinity &cbroke: out of cursed energy");
            return;
        }
        e.setCancelled(true);
        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 25, 0.6, 0.8, 0.6, 0,
                new Particle.DustOptions(Color.fromRGB(120, 200, 255), 1.5f));
        if (e instanceof EntityDamageByEntityEvent be && be.getDamager() instanceof LivingEntity d) {
            Vector away = d.getLocation().toVector().subtract(p.getLocation().toVector());
            if (away.lengthSquared() > 0.01) d.setVelocity(away.normalize().multiply(0.6).setY(0.2));
        }
    }

    // ---- Shikigami: friendly fire, Mahoraga adaptation, targeting ----
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShikigamiAttack(EntityDamageByEntityEvent e) {
        ShikigamiManager.Info atk = plugin.shikigami().info(e.getDamager());
        if (atk == null) return;
        if (e.getEntity().getUniqueId().equals(atk.owner)) { e.setCancelled(true); return; }
        ShikigamiManager.Info vic = plugin.shikigami().info(e.getEntity());
        if (vic != null && vic.owner.equals(atk.owner)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShikigamiDamaged(EntityDamageEvent e) {
        ShikigamiManager.Info info = plugin.shikigami().info(e.getEntity());
        if (info == null) return;
        if (e instanceof EntityDamageByEntityEvent be && be.getDamager().getUniqueId().equals(info.owner)) {
            e.setCancelled(true);
            return;
        }
        if (!"mahoraga".equals(info.kind)) return;
        String key = e.getCause().name();
        if (e instanceof EntityDamageByEntityEvent be) key += ":" + be.getDamager().getType().name();
        int n = info.adapt.getOrDefault(key, 0);
        double step = plugin.getConfig().getDouble("techniques.mahoraga.adapt_step", 0.25);
        double floor = plugin.getConfig().getDouble("techniques.mahoraga.adapt_floor", 0.05);
        e.setDamage(e.getDamage() * Math.max(floor, 1.0 - step * n));
        info.adapt.put(key, n + 1);
        Entity m = e.getEntity();
        m.customName(Component.text("Mahoraga ").append(plugin.legacy("&7(adapted x" + info.adapt.values().stream().mapToInt(Integer::intValue).sum() + ")")));
        m.getWorld().spawnParticle(Particle.END_ROD, m.getLocation().add(0, 3, 0), 12, 0.5, 0.2, 0.5, 0.02);
        if (info.visual instanceof org.bukkit.entity.ItemDisplay wheel) {
            int total = info.adapt.values().stream().mapToInt(Integer::intValue).sum();
            jjk.util.VFX.rotate(wheel, (float) Math.toRadians(45.0 * total), 0, 0, 1, 6);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNoteTargets(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof LivingEntity atk && !atk.equals(e.getEntity())) {
            plugin.recordAttack(atk.getUniqueId());
        }
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity victim) {
            plugin.shikigami().noteTarget(p.getUniqueId(), victim);
        }
        if (e.getEntity() instanceof Player p && e.getDamager() instanceof LivingEntity attacker) {
            plugin.shikigami().noteTarget(p.getUniqueId(), attacker);
        }
    }

    @EventHandler
    public void onSummonDeath(EntityDeathEvent e) {
        if (plugin.shikigami().info(e.getEntity()) == null) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
        plugin.shikigami().untrack(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { cleanup(e.getPlayer()); }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) { cleanup(e.getEntity()); }

    private void cleanup(Player p) {
        plugin.infinity().set(p, false);
        plugin.domains().endOwned(p);
        plugin.shikigami().dismiss(p.getUniqueId(), null);
        if (plugin.heavenly().isActive(p)) plugin.heavenly().disable(p);
    }
}
