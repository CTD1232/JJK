package jjk;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Timing windows for melee techniques: Black Flash and Divergent Fist. */
public class MeleeManager {
    private record Window(long start, long end) {}
    private final Map<UUID, Window> blackFlash = new HashMap<>();
    private final Map<UUID, Long> divergent = new HashMap<>();

    public void openBlackFlash(Player p, long delayMs, long windowMs) {
        long now = System.currentTimeMillis();
        blackFlash.put(p.getUniqueId(), new Window(now + delayMs, now + delayMs + windowMs));
    }

    /** True once if the player lands a hit inside the perfect window. */
    public boolean consumeBlackFlash(Player p) {
        Window w = blackFlash.get(p.getUniqueId());
        if (w == null) return false;
        long now = System.currentTimeMillis();
        if (now > w.end()) { blackFlash.remove(p.getUniqueId()); return false; }
        if (now < w.start()) return false; // too early: hit lands as a normal hit, window stays
        blackFlash.remove(p.getUniqueId());
        return true;
    }

    public void armDivergent(Player p, double seconds) {
        divergent.put(p.getUniqueId(), System.currentTimeMillis() + (long) (seconds * 1000));
    }

    public boolean consumeDivergent(Player p) {
        Long until = divergent.remove(p.getUniqueId());
        return until != null && until >= System.currentTimeMillis();
    }
}
