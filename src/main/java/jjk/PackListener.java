package jjk;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Optionally sends the resource pack on join. Set resource_pack.url (and sha1) in config.yml. */
public class PackListener implements Listener {
    private final JJKPlugin plugin;

    public PackListener(JJKPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String url = plugin.getConfig().getString("resource_pack.url", "");
        if (url.isBlank()) return;
        String sha1 = plugin.getConfig().getString("resource_pack.sha1", "");
        if (sha1.length() == 40) {
            byte[] hash = new byte[20];
            for (int i = 0; i < 20; i++) hash[i] = (byte) Integer.parseInt(sha1.substring(i * 2, i * 2 + 2), 16);
            e.getPlayer().setResourcePack(url, hash);
        } else {
            e.getPlayer().setResourcePack(url);
        }
    }
}
