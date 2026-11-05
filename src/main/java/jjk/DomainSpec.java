package jjk;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Describes a domain. Techniques build one of these and hand it to DomainManager. */
public class DomainSpec {
    public String name = "Domain";
    public double radius = 12;
    public int durationTicks = 200;
    public int interval = 10;              // ticks between sure-hit applications
    public double upkeepPerSecond = 0;     // CE drained per second while the domain is up
    public Material shell = null;          // null = barrierless domain
    public BiConsumer<DomainManager.Domain, LivingEntity> effect = (d, e) -> {};
    public Consumer<DomainManager.Domain> ambient = d -> {};
    /** Called when the domain ends; the reason is "expired" for a natural ending. */
    public java.util.function.BiConsumer<DomainManager.Domain, String> onEnd = null;
    /** Spawns this domain's custom-model visuals; they are removed automatically when the domain ends. */
    public java.util.function.Function<DomainManager.Domain, java.util.List<org.bukkit.entity.Entity>> visuals = null;
}
