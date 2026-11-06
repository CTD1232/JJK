package jjk.technique;

import jjk.JJKPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TechniqueRegistry {
    private final Map<String, Technique> map = new LinkedHashMap<>();

    public TechniqueRegistry(JJKPlugin plugin) {
        register(new BlueTechnique(plugin));
        register(new RedTechnique(plugin));
        register(new PurpleTechnique(plugin));
        register(new InfinityTechnique(plugin));
        register(new UnlimitedVoidTechnique(plugin));
        register(new DismantleTechnique(plugin));
        register(new CleaveTechnique(plugin));
        register(new FugaTechnique(plugin));
        register(new MalevolentShrineTechnique(plugin));
        register(new ReverseCursedTechnique(plugin));
        register(new SimpleDomainTechnique(plugin));
        // Milestone 2
        register(new DivineDogsTechnique(plugin));
        register(new NueTechnique(plugin));
        register(new ToadTechnique(plugin));
        register(new MaxElephantTechnique(plugin));
        register(new RabbitEscapeTechnique(plugin));
        register(new ChimeraShadowGardenTechnique(plugin));
        register(new MahoragaTechnique(plugin));
        register(new RatioStrikeTechnique(plugin));
        register(new OvertimeTechnique(plugin));
        register(new BlackFlashTechnique(plugin));
        register(new DivergentFistTechnique(plugin));
        register(new NailTechnique(plugin));
        register(new ResonanceTechnique(plugin));
        register(new HairpinTechnique(plugin));
        register(new HeavenlyRestrictionTechnique(plugin));
        register(new InvertedSpearTechnique(plugin));
        register(new SplitSoulTechnique(plugin));
        register(new PlayfulCloudTechnique(plugin));
        // Milestone 3
        register(new RikaTechnique(plugin));
        register(new CopyTechnique(plugin));
        register(new MimicTechnique(plugin));
        register(new AuthenticMutualLoveTechnique(plugin));
        register(new SpeechStopTechnique(plugin));
        register(new SpeechSleepTechnique(plugin));
        register(new SpeechBlastAwayTechnique(plugin));
        register(new SpeechCrushTechnique(plugin));
        register(new SpeechTwistTechnique(plugin));
        register(new DeadlySentencingTechnique(plugin));
        register(new ConfiscationTechnique(plugin));
        register(new ExecutionerSwordTechnique(plugin));
        register(new AbsorbSpiritTechnique(plugin));
        register(new ReleaseSpiritsTechnique(plugin));
        register(new UzumakiTechnique(plugin));
    }

    /** Add your own techniques here: register(new MyTechnique(plugin)) and give it a config section. */
    public void register(Technique t) { map.put(t.id(), t); }
    public Technique get(String id) { return map.get(id); }
    public Set<String> ids() { return map.keySet(); }
}
