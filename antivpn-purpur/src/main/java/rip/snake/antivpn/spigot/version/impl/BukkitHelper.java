package rip.snake.antivpn.spigot.version.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.snake.antivpn.spigot.utils.ProtocolVersion;
import rip.snake.antivpn.spigot.version.VersionHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitHelper implements VersionHelper {

    /**
     * Example Bukkit.getVersion():
     * "git-Purpur-2125 (MC: 1.21)"
     */
    private static final Pattern MC_VERSION_PATTERN =
            Pattern.compile("\\(MC: ([0-9]+(?:\\.[0-9]+)*)\\)");

    private static final int UNKNOWN_PROTOCOL = -1;

    private final int serverProtocol;

    public BukkitHelper() {
        this.serverProtocol = resolveServerProtocol();
    }

    @Override
    public int getProtocolVersion(Player player) {
        // Bukkit fallback cannot detect per-player versions
        // We return the server protocol as a best-effort guess
        return serverProtocol;
    }

    /* ---------------------------------------------------------------------- */
    /* Internal Logic                                                         */
    /* ---------------------------------------------------------------------- */

    private int resolveServerProtocol() {
        String bukkitVersion = Bukkit.getVersion();
        if (bukkitVersion == null) {
            return UNKNOWN_PROTOCOL;
        }

        Matcher matcher = MC_VERSION_PATTERN.matcher(bukkitVersion);
        if (!matcher.find()) {
            return UNKNOWN_PROTOCOL;
        }

        String mcVersion = matcher.group(1);
        ProtocolVersion protocolVersion = ProtocolVersion.getProtocolVersion(mcVersion);

        if (protocolVersion == null || protocolVersion.isUnknown() || protocolVersion.isLegacy()) {
            return UNKNOWN_PROTOCOL;
        }

        return protocolVersion.getProtocol();
    }
}
