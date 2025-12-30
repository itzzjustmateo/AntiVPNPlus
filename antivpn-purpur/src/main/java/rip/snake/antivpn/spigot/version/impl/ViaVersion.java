package rip.snake.antivpn.spigot.version.impl;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.entity.Player;
import rip.snake.antivpn.spigot.version.VersionHelper;

import java.util.UUID;

public final class ViaVersion implements VersionHelper {

    private final ViaAPI<?> viaApi;

    public ViaVersion() {
        this.viaApi = Via.getAPI();
    }

    @Override
    public int getProtocolVersion(Player player) {
        if (player == null) {
            return UNKNOWN_PROTOCOL;
        }

        return getProtocolVersion(player.getUniqueId());
    }

    @Override
    public int getProtocolVersion(UUID uuid) {
        if (uuid == null || viaApi == null) {
            return UNKNOWN_PROTOCOL;
        }

        try {
            return viaApi.getPlayerVersion(uuid);
        } catch (Exception ex) {
            // ViaVersion *should* never throw here, but plugins lie sometimes
            return UNKNOWN_PROTOCOL;
        }
    }

    /**
     * Represents an unknown or unavailable protocol version.
     * VersionHelper implementations should agree on this value.
     */
    private static final int UNKNOWN_PROTOCOL = -1;
}
