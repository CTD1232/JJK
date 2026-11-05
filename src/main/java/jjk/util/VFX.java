package jjk.util;

import jjk.JJKPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * Custom-model visuals: item_display entities using the resource pack's models (paper + custom_model_data 8001+).
 * Every method is null-safe, so with visuals.enabled=false the techniques simply skip them.
 */
public final class VFX {
    private VFX() {}

    public static final int ORB_BLUE = 8001, ORB_RED = 8002, ORB_PURPLE = 8003, FLAME = 8004, SLASH = 8005,
            WHEEL = 8006, ELEPHANT = 8007, NAIL = 8008, BOLT = 8009, STARS = 8010, SHRINE = 8011, SHADOW = 8012, RING = 8013,
            RIKA = 8014, GAVEL = 8015, UZUMAKI = 8016;

    private static final Set<Entity> LIVE = new HashSet<>();
    private static JJKPlugin plugin;

    public static void init(JJKPlugin p) { plugin = p; }

    public static boolean enabled() { return plugin != null && plugin.getConfig().getBoolean("visuals.enabled", true); }

    public static ItemDisplay spawn(Location loc, int model, float scale, boolean grounded) {
        return spawn(loc, model, scale, scale, scale, grounded);
    }

    /**
     * @param grounded true for models whose base sits at model y=0 (shrine, elephant, floor discs): lifts them so the base is at loc.
     * Negative scales flip a model inside out (used for the starfield room).
     */
    public static ItemDisplay spawn(Location loc, int model, float sx, float sy, float sz, boolean grounded) {
        if (!enabled()) return null;
        ItemStack st = new ItemStack(Material.PAPER);
        ItemMeta meta = st.getItemMeta();
        meta.setCustomModelData(model);
        st.setItemMeta(meta);
        ItemDisplay d = loc.getWorld().spawn(loc, ItemDisplay.class, x -> {
            x.setItemStack(st);
            x.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            x.setBrightness(new Display.Brightness(15, 15));
            x.setPersistent(false);
            x.setViewRange(2.0f);
            x.setTeleportDuration(2);
            x.setTransformation(new Transformation(new Vector3f(0, grounded ? Math.abs(sy) / 2f : 0, 0),
                    new AxisAngle4f(), new Vector3f(sx, sy, sz), new AxisAngle4f()));
        });
        LIVE.add(d);
        return d;
    }

    /** Floating text (cursed speech, verdicts). Works without the resource pack. */
    public static void text(Location loc, String legacy, long ticks) {
        Component c = LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
        TextDisplay t = loc.getWorld().spawn(loc, TextDisplay.class, x -> {
            x.text(c);
            x.setBillboard(Display.Billboard.CENTER);
            x.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            x.setShadowed(true);
            x.setBrightness(new Display.Brightness(15, 15));
            x.setPersistent(false);
            x.setViewRange(1.5f);
            x.setTeleportDuration(2);
            x.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(2.2f, 2.2f, 2.2f), new AxisAngle4f()));
        });
        LIVE.add(t);
        new BukkitRunnable() {
            long n = 0;
            @Override
            public void run() {
                if (n++ >= ticks || !t.isValid()) { remove(t); cancel(); return; }
                t.teleport(t.getLocation().add(0, 0.04, 0));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public static void remove(Entity e) {
        if (e == null) return;
        LIVE.remove(e);
        e.remove();
    }

    public static void removeLater(Entity e, long ticks) {
        if (e == null) return;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> remove(e), ticks);
    }

    public static void clear() {
        for (Entity e : new HashSet<>(LIVE)) e.remove();
        LIVE.clear();
    }

    /** Rotates a display around an axis (smoothly over `ticks`, 0 = instant). */
    public static void rotate(ItemDisplay d, float radians, float ax, float ay, float az, int ticks) {
        if (d == null) return;
        Transformation t = d.getTransformation();
        d.setInterpolationDelay(0);
        d.setInterpolationDuration(ticks);
        d.setTransformation(new Transformation(t.getTranslation(), new Quaternionf(new AxisAngle4f(radians, ax, ay, az)),
                t.getScale(), t.getRightRotation()));
    }

    /** Shifts a display's rendering offset (in blocks, relative to the entity). */
    public static void offset(ItemDisplay d, float x, float y, float z) {
        if (d == null) return;
        Transformation t = d.getTransformation();
        d.setTransformation(new Transformation(new Vector3f(x, y, z), t.getLeftRotation(), t.getScale(), t.getRightRotation()));
    }
}
