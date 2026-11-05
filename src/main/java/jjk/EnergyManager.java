package jjk;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyManager {
    private final JJKPlugin plugin;
    private final NamespacedKey curKey;
    private final NamespacedKey maxKey;

    public EnergyManager(JJKPlugin plugin) {
        this.plugin = plugin;
        this.curKey = new NamespacedKey(plugin, "ce");
        this.maxKey = new NamespacedKey(plugin, "ce_max");
    }

    public double max(Player p) {
        return p.getPersistentDataContainer().getOrDefault(maxKey, PersistentDataType.DOUBLE,
                plugin.getConfig().getDouble("energy.max", 1000));
    }

    public double get(Player p) {
        return Math.min(max(p), p.getPersistentDataContainer().getOrDefault(curKey, PersistentDataType.DOUBLE, max(p)));
    }

    public void set(Player p, double v) {
        p.getPersistentDataContainer().set(curKey, PersistentDataType.DOUBLE, Math.max(0, Math.min(max(p), v)));
    }

    public void setMax(Player p, double v) {
        p.getPersistentDataContainer().set(maxKey, PersistentDataType.DOUBLE, Math.max(1, v));
        set(p, get(p));
    }

    public void add(Player p, double v) { set(p, get(p) + v); }

    public boolean consume(Player p, double v) {
        if (v <= 0) return true;
        double cur = get(p);
        if (cur < v) return false;
        set(p, cur - v);
        return true;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double regenPerTick = plugin.getConfig().getDouble("energy.regen", 6) / 2.0;
                boolean bar = plugin.getConfig().getBoolean("energy.actionbar", true);
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.heavenly().isActive(p)) continue;
                    if (plugin.heavenly().isActive(p)) { set(p, 0); continue; }
                    if (get(p) < max(p)) add(p, regenPerTick);
                    if (bar && plugin.items().isJJKItem(p.getInventory().getItemInMainHand())) {
                        plugin.msg(p, "&9CE &f" + (int) get(p) + "&7/&f" + (int) max(p));
                    }
                }
            }
        }.runTaskTimer(plugin, 10, 10);
    }
}
