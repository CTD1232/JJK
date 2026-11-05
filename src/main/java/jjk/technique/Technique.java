package jjk.technique;

import jjk.JJKPlugin;
import org.bukkit.entity.Player;

/** Base class. All numbers come from config.yml under techniques.<id>.* so admins can retune live. */
public abstract class Technique {
    protected final JJKPlugin plugin;
    private final String id;
    private final String defaultName;

    protected Technique(JJKPlugin plugin, String id, String defaultName) {
        this.plugin = plugin;
        this.id = id;
        this.defaultName = defaultName;
    }

    public String id() { return id; }
    public String display() { return str("name", defaultName); }

    protected double num(String key, double def) { return plugin.getConfig().getDouble("techniques." + id + "." + key, def); }
    protected boolean bool(String key, boolean def) { return plugin.getConfig().getBoolean("techniques." + id + "." + key, def); }
    protected String str(String key, String def) { return plugin.getConfig().getString("techniques." + id + "." + key, def); }

    /** Physical techniques that still work under Heavenly Restriction (no cursed energy needed). */
    public boolean usableUnderHR() { return false; }

    /** Physical techniques (Toji's tools) return false so they work under Heavenly Restriction. */
    public boolean usesCursedEnergy() { return true; }

    public boolean enabled() { return bool("enabled", true); }
    public double cost(Player p) { return num("cost", 0); }
    public long cooldownMs() { return (long) (num("cooldown", 0) * 1000); }

    /** Returns true if the technique actually fired (cost + cooldown are only charged then). */
    public abstract boolean cast(Player p);
}
