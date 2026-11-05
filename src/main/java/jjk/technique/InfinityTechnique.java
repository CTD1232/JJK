package jjk.technique;

import jjk.JJKPlugin;
import org.bukkit.entity.Player;

public class InfinityTechnique extends Technique {
    public InfinityTechnique(JJKPlugin plugin) { super(plugin, "gojo_infinity", "Infinity"); }

    @Override
    public double cost(Player p) { return plugin.infinity().isActive(p) ? 0 : super.cost(p); }

    @Override
    public boolean cast(Player p) {
        boolean on = !plugin.infinity().isActive(p);
        plugin.infinity().set(p, on);
        plugin.msg(p, on ? "&bInfinity &aactive" : "&bInfinity &7off");
        return true;
    }
}
