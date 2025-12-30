package rip.snake.antivpn.spigot.version;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface VersionHelper {

    /**
     * Returned when the protocol version cannot be determined.
     */
    int UNKNOWN_PROTOCOL = -1;

    /**
     * Returns the Minecraft protocol version for a player.
     *
     * ViaVersion implementations may return the exact client protocol.
     * Bukkit-based implementations may return the server protocol as a fallback.
     *
     * @param player the player instance
     * @return protocol version, or {@link #UNKNOWN_PROTOCOL} if unavailable
     */
    int getProtocolVersion(Player player);

    /**
     * Returns the Minecraft protocol version for a player by UUID.
     *
     * This method is safe to call in async contexts (e.g. AsyncPlayerPreLoginEvent)
     * for implementations that support it.
     *
     * Default implementation falls back to {@link #getProtocolVersion(Player)}.
     *
     * @param uuid the player's UUID
     * @return protocol version, or {@link #UNKNOWN_PROTOCOL} if unavailable
     */
    default int getProtocolVersion(UUID uuid) {
        return UNKNOWN_PROTOCOL;
    }
}
