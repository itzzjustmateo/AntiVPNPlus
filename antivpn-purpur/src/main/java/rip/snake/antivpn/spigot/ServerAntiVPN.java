package rip.snake.antivpn.spigot;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.spigot.commands.AntiVPNCommand;
import rip.snake.antivpn.spigot.listeners.PlayerListener;
import rip.snake.antivpn.spigot.utils.SharkLoggerImpl;
import rip.snake.antivpn.spigot.version.VersionHelper;
import rip.snake.antivpn.spigot.version.impl.BukkitHelper;
import rip.snake.antivpn.spigot.version.impl.ViaVersion;

@Getter
public final class ServerAntiVPN extends JavaPlugin {

    private Service service;
    private VersionHelper versionHelper;

    /* ---------------------------------------------------------------------- */
    /* Plugin Lifecycle                                                       */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onLoad() {
        // Keep onLoad lightweight: filesystem + config prep only
        getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        printBanner();

        this.service = new Service(
                new SharkLoggerImpl(),
                getDataFolder().toPath(),
                getDescription().getVersion()
        );

        service.onLoad();

        setupVersionHelper();
        registerCommands();
        registerListeners();

        getLogger().info("AntiVPNPlus enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (service != null) {
            service.onDisable();
        }

        getLogger().info("AntiVPNPlus disabled.");
    }

    /* ---------------------------------------------------------------------- */
    /* Setup                                                                  */
    /* ---------------------------------------------------------------------- */

    private void registerCommands() {
        PluginCommand command = getCommand("antivpn");
        if (command == null) {
            getLogger().severe("Command 'antivpn' is missing from plugin.yml!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        AntiVPNCommand executor = new AntiVPNCommand(service);
        command.setExecutor(executor);
        command.setTabCompleter(executor); // future-proofing
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(
                new PlayerListener(this),
                this
        );
    }

    private void setupVersionHelper() {
        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            versionHelper = new ViaVersion();
            getLogger().info("ViaVersion detected — enhanced protocol handling enabled.");
        } else {
            versionHelper = new BukkitHelper();
            getLogger().warning("ViaVersion not found — using Bukkit protocol fallback.");
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Banner                                                                 */
    /* ---------------------------------------------------------------------- */

    private void printBanner() {
        getLogger().info("");
        getLogger().info(" █████╗ ███╗   ██╗████████╗██╗██╗   ██╗██████╗ ███╗   ██╗");
        getLogger().info("██╔══██╗████╗  ██║╚══██╔══╝██║██║   ██║██╔══██╗████╗  ██║");
        getLogger().info("███████║██╔██╗ ██║   ██║   ██║██║   ██║██████╔╝██╔██╗ ██║");
        getLogger().info("██╔══██║██║╚██╗██║   ██║   ██║╚██╗ ██╔╝██╔═══╝ ██║╚██╗██║");
        getLogger().info("██║  ██║██║ ╚████║   ██║   ██║ ╚████╔╝ ██║     ██║ ╚████║");
        getLogger().info("╚═╝  ╚═╝╚═╝  ╚═══╝   ╚═╝   ╚═╝  ╚═══╝  ╚═╝     ╚═╝  ╚═══╝");
        getLogger().info("");
        getLogger().info("AntiVPNPlus v" + getDescription().getVersion());
        getLogger().info("Continued-Development by ItzzMateo")
        getLogger().info("Running on " + Bukkit.getName());
        getLogger().info("");
    }
}
