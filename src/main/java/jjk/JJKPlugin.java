package jjk;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import jjk.technique.TechniqueRegistry;

public class JJKPlugin extends JavaPlugin {
    private EnergyManager energy;
    private TechniqueRegistry techniques;
    private AbilityManager abilities;
    private ItemManager items;
    private DomainManager domains;
    private InfinityManager infinity;
    private ShikigamiManager shikigami;
    private HeavenlyRestrictionManager heavenly;
    private HeavenlyRestrictionManager heavenly;
    private MarkManager marks;
    private MeleeManager melee;
    private SummonManager summons;
    private boolean internalDamage;
    private boolean bypassInfinity;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        jjk.util.VFX.init(this);
        energy = new EnergyManager(this);
        items = new ItemManager(this);
        domains = new DomainManager(this);
        infinity = new InfinityManager(this);
        shikigami = new ShikigamiManager(this);
        heavenly = new HeavenlyRestrictionManager(this);
        heavenly = new HeavenlyRestrictionManager(this);
        marks = new MarkManager();
        melee = new MeleeManager();
        summons = new SummonManager(this);
        techniques = new TechniqueRegistry(this);
        abilities = new AbilityManager(this);

        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PackListener(this), this);
        getServer().getPluginManager().registerEvents(new MeleeListener(this), this);
        getServer().getPluginManager().registerEvents(new SummonListener(this), this);
        JJKCommand cmd = new JJKCommand(this);
        getCommand("jjk").setExecutor(cmd);
        getCommand("jjk").setTabCompleter(cmd);

        energy.start();
        infinity.start();
        shikigami.start();
        summons.start();
    }

    @Override
    public void onDisable() {
        if (domains != null) domains.endAll();
        if (shikigami != null) shikigami.endAll();
        jjk.util.VFX.clear();
        if (summons != null) for (var p : getServer().getOnlinePlayers()) summons.dismiss(p, null);
        if (heavenly != null) for (var p : getServer().getOnlinePlayers()) heavenly.set(p, false);
    }

    public EnergyManager energy() { return energy; }
    public TechniqueRegistry techniques() { return techniques; }
    public AbilityManager abilities() { return abilities; }
    public ItemManager items() { return items; }
    public DomainManager domains() { return domains; }
    public InfinityManager infinity() { return infinity; }
    public HeavenlyRestrictionManager heavenly() { return heavenly; }
    public MarkManager marks() { return marks; }
    public MeleeManager melee() { return melee; }
    public SummonManager summons() { return summons; }

    private final java.util.Map<java.util.UUID, Long> lastAttack = new java.util.HashMap<>();

    /** Records that this entity just hurt a player (Higuruma's trials judge on this). */
    public void recordAttack(java.util.UUID id) { lastAttack.put(id, System.currentTimeMillis()); }
    public long lastAttack(java.util.UUID id) { return lastAttack.getOrDefault(id, 0L); }

    public ShikigamiManager shikigami() { return shikigami; }
    public HeavenlyRestrictionManager heavenly() { return heavenly; }

    public net.kyori.adventure.text.Component legacy(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public boolean isInternalDamage() { return internalDamage; }
    public boolean isBypassInfinity() { return bypassInfinity; }

    /** All technique damage goes through here so it never retriggers item listeners. */
    public void damage(LivingEntity target, double amount, Player source, boolean ignoreInfinity) {
        boolean prevInternal = internalDamage, prevBypass = bypassInfinity;
        internalDamage = true;
        bypassInfinity = ignoreInfinity;
        try {
            target.setNoDamageTicks(0);
            target.damage(amount, source);
        } finally {
            internalDamage = prevInternal;
            bypassInfinity = prevBypass;
        }
    }

    public void msg(Player p, String legacy) {
        p.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(legacy));
    }

    public void chat(Player p, String legacy) {
        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(legacy));
    }
}
