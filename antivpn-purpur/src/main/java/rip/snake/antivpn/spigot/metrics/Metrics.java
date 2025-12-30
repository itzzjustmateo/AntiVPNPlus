package rip.snake.antivpn.spigot;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import rip.snake.antivpn.commons.Service;
import rip.snake.antivpn.spigot.commands.AntiVPNCommand;
import rip.snake.antivpn.spigot.listeners.PlayerListener;
import rip.snake.antivpn.spigot.metrics.Metrics;
import rip.snake.antivpn.spigot.metrics.Metrics.SimplePie;
import rip.snake.antivpn.spigot.utils.Color;
import rip.snake.antivpn.spigot.utils.SharkLoggerImpl;
import rip.snake.antivpn.spigot.version.VersionHelper;
import rip.snake.antivpn.spigot.version.impl.BukkitHelper;
import rip.snake.antivpn.spigot.version.impl.ViaVersion;

@Getter
public final class ServerAntiVPN extends JavaPlugin {

  private Service service;
  private VersionHelper versionHelper;

  @Override
  public void onEnable() {
    printBanner();

    /* ------------------------------------------------------------------ */
    /* Core Service                                                        */
    /* ------------------------------------------------------------------ */

    this.service = new Service(
            new SharkLoggerImpl(),
            getDataFolder().toPath(),
            getDescription().getVersion()
    );
    this.service.onLoad();

    /* ------------------------------------------------------------------ */
    /* Version Detection                                                   */
    /* ------------------------------------------------------------------ */

    this.versionHelper = Bukkit.getPluginManager().isPluginEnabled("ViaVersion")
            ? new ViaVersion()
            : new BukkitHelper();

    /* ------------------------------------------------------------------ */
    /* Commands & Events                                                   */
    /* ------------------------------------------------------------------ */

    getCommand("antivpn").setExecutor(new AntiVPNCommand(service));
    getServer().getPluginManager().registerEvents(
            new PlayerListener(this),
            this
    );

    /* ------------------------------------------------------------------ */
    /* bStats Metrics                                                      */
    /* ------------------------------------------------------------------ */

    int pluginId = 28611;
    Metrics metrics = new Metrics(this, pluginId);

    metrics.addCustomChart(new SimplePie(
            "viaversion",
            () -> Bukkit.getPluginManager().isPluginEnabled("ViaVersion") ? "enabled" : "disabled"
    ));

    metrics.addCustomChart(new SimplePie(
            "server_software",
            () -> Bukkit.getName()
    ));
  }

  @Override
  public void onDisable() {
    if (service != null) {
      service.onDisable();
    }
  }

  /* ---------------------------------------------------------------------- */
  /* Banner                                                                 */
  /* ---------------------------------------------------------------------- */

  private void printBanner() {
    Bukkit.getConsoleSender().sendMessage(Color.colorize(
            "&b&lAntiVPNPlus &7v" + getDescription().getVersion()
    ));
    Bukkit.getConsoleSender().sendMessage(Color.colorize(
            "&7Running on &f" + Bukkit.getName()
    ));
    Bukkit.getConsoleSender().sendMessage(Color.colorize(
            "&7Java &f" + System.getProperty("java.version")
    ));
    Bukkit.getConsoleSender().sendMessage(Color.colorize(
            "&8----------------------------------------"
    ));
  }
}
