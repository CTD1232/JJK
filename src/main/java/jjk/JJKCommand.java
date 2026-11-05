package jjk;

import jjk.technique.Technique;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JJKCommand implements CommandExecutor, TabCompleter {
    private final JJKPlugin plugin;

    public JJKCommand(JJKPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (!s.hasPermission("jjk.admin")) { s.sendMessage("No permission."); return true; }
        if (a.length == 0) {
            s.sendMessage("/jjk give <item> [player] | energy <get|set|max|refill> [player] [amount] | cast <technique> [player] | list | reload");
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                s.sendMessage("JujutsuCraft config reloaded.");
            }
            case "list" -> {
                s.sendMessage("Items: " + String.join(", ", plugin.items().ids()));
                s.sendMessage("Techniques: " + String.join(", ", plugin.techniques().ids()));
            }
            case "give" -> {
                if (a.length < 2) { s.sendMessage("/jjk give <item> [player]"); return true; }
                Player t = target(s, a, 2);
                if (t == null) return true;
                ItemStack it = plugin.items().create(a[1]);
                if (it == null) { s.sendMessage("Unknown item. Use /jjk list"); return true; }
                t.getInventory().addItem(it);
                s.sendMessage("Gave " + a[1] + " to " + t.getName());
            }
            case "energy" -> {
                if (a.length < 2) { s.sendMessage("/jjk energy <get|set|max|refill> [player] [amount]"); return true; }
                Player t = target(s, a, 2);
                if (t == null) return true;
                switch (a[1].toLowerCase()) {
                    case "get" -> s.sendMessage(t.getName() + ": " + (int) plugin.energy().get(t) + "/" + (int) plugin.energy().max(t));
                    case "refill" -> { plugin.energy().set(t, plugin.energy().max(t)); s.sendMessage("Refilled."); }
                    case "set", "max" -> {
                        if (a.length < 4) { s.sendMessage("Amount required."); return true; }
                        try {
                            double v = Double.parseDouble(a[3]);
                            if (a[1].equalsIgnoreCase("set")) plugin.energy().set(t, v); else plugin.energy().setMax(t, v);
                            s.sendMessage("Done.");
                        } catch (NumberFormatException ex) { s.sendMessage("Not a number."); }
                    }
                    default -> s.sendMessage("get, set, max or refill");
                }
            }
            case "cast" -> {
                if (a.length < 2) { s.sendMessage("/jjk cast <technique> [player]"); return true; }
                Player t = target(s, a, 2);
                if (t == null) return true;
                plugin.abilities().use(t, a[1]);
            }
            default -> s.sendMessage("Unknown subcommand.");
        }
        return true;
    }

    private Player target(CommandSender s, String[] a, int idx) {
        if (a.length > idx) {
            Player p = plugin.getServer().getPlayerExact(a[idx]);
            if (p == null) s.sendMessage("Player not found.");
            return p;
        }
        if (s instanceof Player p) return p;
        s.sendMessage("Specify a player.");
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) out.addAll(List.of("give", "energy", "cast", "list", "reload"));
        else if (a.length == 2 && a[0].equalsIgnoreCase("give")) out.addAll(plugin.items().ids());
        else if (a.length == 2 && a[0].equalsIgnoreCase("cast")) out.addAll(plugin.techniques().ids());
        else if (a.length == 2 && a[0].equalsIgnoreCase("energy")) out.addAll(List.of("get", "set", "max", "refill"));
        else if ((a.length == 3 && (a[0].equalsIgnoreCase("give") || a[0].equalsIgnoreCase("cast")))
                || (a.length == 3 && a[0].equalsIgnoreCase("energy")))
            plugin.getServer().getOnlinePlayers().forEach(p -> out.add(p.getName()));
        String prefix = a[a.length - 1].toLowerCase();
        out.removeIf(x -> !x.toLowerCase().startsWith(prefix));
        return out;
    }
}
