package jjk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DomainManager {
    public static class Domain {
        public final UUID owner;
        public final Location center;
        public final DomainSpec spec;
        public int ticksLeft;
        final List<Location> shell = new ArrayList<>();
        final Set<UUID> viewers = new HashSet<>();
        final List<Entity> visuals = new ArrayList<>();
        BukkitTask task;

        Domain(UUID owner, Location center, DomainSpec spec) {
            this.owner = owner;
            this.center = center;
            this.spec = spec;
            this.ticksLeft = spec.durationTicks;
        }
    }

    private final JJKPlugin plugin;
    private final List<Domain> active = new ArrayList<>();
    private final Map<UUID, Long> simpleDomain = new HashMap<>();

    public DomainManager(JJKPlugin plugin) { this.plugin = plugin; }

    public boolean isProtected(LivingEntity e) {
        if (e instanceof Player hp && plugin.heavenly().isActive(hp)
                && plugin.getConfig().getBoolean("techniques.heavenly_restriction.domain_immune", true)) return true;
        Long until = simpleDomain.get(e.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public void grantSimpleDomain(Player p, double seconds) {
        simpleDomain.put(p.getUniqueId(), System.currentTimeMillis() + (long) (seconds * 1000));
    }

    public void clearSimpleDomain(UUID id) { simpleDomain.remove(id); }

    public Domain byOwner(Player p) {
        for (Domain d : active) if (d.owner.equals(p.getUniqueId())) return d;
        return null;
    }

    /** Returns false if nothing was spent (e.g. toggled an existing domain off). */
    public boolean expand(Player caster, DomainSpec spec) {
        Domain mine = byOwner(caster);
        if (mine != null) { end(mine, "dispelled"); return false; }

        Location center = caster.getLocation().clone();
        double myEnergy = plugin.energy().get(caster);

        // Domain clash: more remaining cursed energy wins.
        for (Domain d : new ArrayList<>(active)) {
            if (!d.center.getWorld().equals(center.getWorld())) continue;
            if (d.center.distance(center) >= d.spec.radius + spec.radius) continue;
            Player other = Bukkit.getPlayer(d.owner);
            double theirs = other == null ? 0 : plugin.energy().get(other);
            if (myEnergy > theirs) {
                end(d, "overpowered by a rival domain");
            } else {
                plugin.msg(caster, "&cYour domain clashed and was overpowered!");
                return true;
            }
        }

        Domain dom = new Domain(caster.getUniqueId(), center, spec);
        active.add(dom);
        if (spec.visuals != null) {
            for (Entity v : spec.visuals.apply(dom)) if (v != null) dom.visuals.add(v);
        }
        if (spec.shell != null) {
            dom.shell.addAll(shell(center, spec.radius));
            for (Player p : center.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(center) <= sq(spec.radius + 64)) {
                    dom.viewers.add(p.getUniqueId());
                    sendShell(p, dom);
                }
            }
        }
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 2f, 0.6f);
        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= sq(spec.radius + 40)) {
                plugin.chat(p, "&5&lDomain Expansion: &f&l" + spec.name);
            }
        }
        dom.task = new BukkitRunnable() {
            @Override
            public void run() {
                Player owner = Bukkit.getPlayer(dom.owner);
                dom.ticksLeft -= spec.interval;
                if (owner == null || owner.isDead() || dom.ticksLeft <= 0) { end(dom, "expired"); return; }
                if (spec.upkeepPerSecond > 0
                        && !plugin.energy().consume(owner, spec.upkeepPerSecond * spec.interval / 20.0)) {
                    end(dom, "collapsed from cursed energy burnout");
                    return;
                }
                if (spec.shell != null) {
                    for (Player p : center.getWorld().getPlayers()) {
                        if (!dom.viewers.contains(p.getUniqueId())
                                && p.getLocation().distanceSquared(center) <= sq(spec.radius + 64)) {
                            dom.viewers.add(p.getUniqueId());
                            sendShell(p, dom);
                        }
                    }
                }
                for (Entity e : center.getWorld().getNearbyEntities(center, spec.radius, spec.radius, spec.radius)) {
                    if (!(e instanceof LivingEntity le) || e instanceof ArmorStand || e.equals(owner)) continue;
                    if (e.getLocation().distanceSquared(center) > sq(spec.radius)) continue;
                    if (isProtected(le)) continue;
                    spec.effect.accept(dom, le);
                }
                spec.ambient.accept(dom);
            }
        }.runTaskTimer(plugin, 0, spec.interval);
        return true;
    }

    public void end(Domain d, String reason) {
        if (!active.remove(d)) return;
        if (d.task != null) d.task.cancel();
        for (Entity v : d.visuals) jjk.util.VFX.remove(v);
        for (UUID id : d.viewers) {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.getWorld().equals(d.center.getWorld())) continue;
            for (Location l : d.shell) p.sendBlockChange(l, l.getBlock().getBlockData());
        }
        Player owner = Bukkit.getPlayer(d.owner);
        if (owner != null) plugin.msg(owner, "&7" + d.spec.name + " " + reason);
        if (d.spec.onEnd != null) d.spec.onEnd.accept(d, reason);
    }

    public void endOwned(Player p) {
        Domain d = byOwner(p);
        if (d != null) end(d, "ended");
    }

    public void endAll() {
        for (Domain d : new ArrayList<>(active)) end(d, "ended");
    }

    private void sendShell(Player p, Domain d) {
        BlockData bd = d.spec.shell.createBlockData();
        for (Location l : d.shell) p.sendBlockChange(l, bd);
    }

    private List<Location> shell(Location c, double r) {
        List<Location> out = new ArrayList<>();
        int R = (int) Math.ceil(r) + 1;
        for (int x = -R; x <= R; x++)
            for (int y = -R; y <= R; y++)
                for (int z = -R; z <= R; z++) {
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    if (dist >= r - 0.5 && dist < r + 0.5) {
                        out.add(new Location(c.getWorld(), c.getBlockX() + x, c.getBlockY() + y, c.getBlockZ() + z));
                    }
                }
        return out;
    }

    private static double sq(double v) { return v * v; }
}
