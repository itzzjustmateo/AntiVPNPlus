package rip.snake.antivpn.spigot.listeners;

import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import io.antivpn.api.utils.Event;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.commons.utils.StringUtils;
import rip.snake.antivpn.spigot.ServerAntiVPN;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static rip.snake.antivpn.spigot.utils.Color.colorize;

public final class PlayerListener implements Listener {

    private static final String HOSTNAME_META = "avpn-hostname";

    private final ServerAntiVPN plugin;
    private final Service service;

    public PlayerListener(ServerAntiVPN plugin) {
        this.plugin = plugin;
        this.service = plugin.getService();
    }

    /* ---------------------------------------------------------------------- */
    /* Pre Login (ASYNC)                                                      */
    /* ---------------------------------------------------------------------- */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        InetAddress address = event.getAddress();
        if (address == null) {
            return;
        }

        String ip = StringUtils.cleanAddress(address.getHostAddress());
        String name = event.getName();

        try {
            var future = service.getAntiVPN()
                    .getSocketManager()
                    .getSocketDataHandler()
                    .verify(new CheckRequest(ip, name));

            if (future == null) {
                service.getLogger().error(
                        "AntiVPN backend not connected (player=%s, ip=%s)",
                        name, ip
                );
                return;
            }

            CheckResponse response = future.get(3, TimeUnit.SECONDS);
            if (response == null) {
                return;
            }

            if (response.isAttack()) {
                kick(event, service.getAntiVPN()
                        .getSocketManager()
                        .getResponseKick());
                return;
            }

            if (!response.isValid()) {
                kick(event, service.getAntiVPN()
                        .getSocketManager()
                        .getShieldKick());
            }

        } catch (Exception ex) {
            service.getLogger().error(
                    "AntiVPN verification failed (player=%s, ip=%s): %s",
                    name, ip, ex.getMessage()
            );
        }
    }

    private void kick(AsyncPlayerPreLoginEvent event, String message) {
        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                colorize(message)
        );
    }

    /* ---------------------------------------------------------------------- */
    /* Login / Join                                                           */
    /* ---------------------------------------------------------------------- */

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        event.getPlayer().setMetadata(
                HOSTNAME_META,
                new FixedMetadataValue(plugin, event.getHostname())
        );
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        handlePlayer(event.getPlayer(), Event.PLAYER_JOIN);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        handlePlayer(event.getPlayer(), Event.PLAYER_QUIT);
    }

    /* ---------------------------------------------------------------------- */
    /* Backend Sync                                                           */
    /* ---------------------------------------------------------------------- */

    private void handlePlayer(Player player, Event type) {
        if (player.getAddress() == null) {
            return;
        }

        boolean onlineMode = Bukkit.getOnlineMode();
        String name = player.getName();
        String uuid = player.getUniqueId().toString();
        String ip = player.getAddress().getAddress().getHostAddress();
        String protocol = String.valueOf(
                plugin.getVersionHelper().getProtocolVersion(player)
        );

        String hostname = getMetadata(player, HOSTNAME_META);

        service.getAntiVPN()
                .getSocketManager()
                .getSocketDataHandler()
                .sendUserData(
                        name,
                        uuid,
                        protocol,
                        ip,
                        null,
                        hostname,
                        type,
                        onlineMode
                );
    }

    private String getMetadata(Player player, String key) {
        List<MetadataValue> values = player.getMetadata(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0).asString();
    }
}
