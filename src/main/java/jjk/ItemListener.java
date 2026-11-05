package jjk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {
    private final JJKPlugin plugin;

    public ItemListener(JJKPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a == Action.PHYSICAL) return;
        Player p = e.getPlayer();
        boolean left = a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK;
        String trigger = (p.isSneaking() ? "shift_" : "") + (left ? "left_click" : "right_click");
        String tech = plugin.items().ability(e.getItem(), trigger);
        if (tech == null) return;
        e.setCancelled(true);
        plugin.abilities().use(p, tech);
    }

    /** Left-clicking an entity does not fire PlayerInteractEvent, so catch the attack too. */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (plugin.isInternalDamage()) return;
        if (!(e.getDamager() instanceof Player p)) return;
        String tech = plugin.items().ability(p.getInventory().getItemInMainHand(),
                p.isSneaking() ? "shift_left_click" : "left_click");
        if (tech == null) return;
        e.setCancelled(true);
        plugin.abilities().use(p, tech);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        ItemStack held = e.getOffHandItem(); // the item that was in the main hand
        String tech = plugin.items().ability(held, p.isSneaking() ? "shift_swap" : "swap");
        if (tech == null) return;
        e.setCancelled(true);
        plugin.abilities().use(p, tech);
    }
}
