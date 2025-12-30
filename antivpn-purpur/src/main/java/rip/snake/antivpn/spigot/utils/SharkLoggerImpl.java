package rip.snake.antivpn.spigot.utils;

import io.antivpn.api.logger.VPNLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class SharkLoggerImpl implements VPNLogger {

    private final Logger logger;
    private final boolean debugEnabled;

    public SharkLoggerImpl() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AntiVPNPlus");
        this.logger = plugin != null
                ? plugin.getLogger()
                : Logger.getLogger("AntiVPNPlus");

        // Paper respects logger levels correctly
        this.debugEnabled = logger.isLoggable(Level.FINE);
    }

    @Override
    public void log(String message, Object... args) {
        logInternal(Level.INFO, message, args);
    }

    @Override
    public void fine(String message, Object... args) {
        logInternal(Level.FINE, message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        if (!debugEnabled) {
            return;
        }
        logInternal(Level.FINE, message, args);
    }

    @Override
    public void error(String message, Object... args) {
        logInternal(Level.SEVERE, message, args);
    }

    /* ---------------------------------------------------------------------- */
    /* Internal                                                               */
    /* ---------------------------------------------------------------------- */

    private void logInternal(Level level, String message, Object... args) {
        if (!logger.isLoggable(level)) {
            return;
        }

        String formatted = (args == null || args.length == 0)
                ? message
                : String.format(message, args);

        logger.log(level, formatted);
    }
}
