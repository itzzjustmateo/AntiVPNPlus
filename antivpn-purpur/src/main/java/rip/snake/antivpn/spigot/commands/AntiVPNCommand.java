package rip.snake.antivpn.spigot.commands;

import io.antivpn.api.data.socket.request.impl.CheckRequest;
import io.antivpn.api.data.socket.response.impl.CheckResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.snake.antivpn.commons.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public final class AntiVPNCommand implements CommandExecutor {

    private static final int STRESS_TEST_REQUESTS = 1000;

    private final Service service;

    public AntiVPNCommand(Service service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /* ------------------------------------------------------------------ */
        /* Console only                                                        */
        /* ------------------------------------------------------------------ */

        if (sender instanceof Player) {
            sender.sendMessage("§cThis command can only be used from the console.");
            return true;
        }

        /* ------------------------------------------------------------------ */
        /* No arguments → info banner                                          */
        /* ------------------------------------------------------------------ */

        if (args.length == 0) {
            sender.sendMessage("§8----------------------------------------");
            sender.sendMessage("§bAntiVPNPlus §7— §dPurpurMC Edition");
            sender.sendMessage("§7Continued-Development by §bItzzMateo");
            sender.sendMessage("§8Experimental build · just for fun");
            sender.sendMessage("§8I think it wont work · So not recommended to use");
            sender.sendMessage("§8----------------------------------------");
            sender.sendMessage("§7Usage:");
            sender.sendMessage("§f/antivpn <token>");
            sender.sendMessage("§f/antivpn check §8(debug only)");
            return true;
        }

        String argument = args[0];

        /* ------------------------------------------------------------------ */
        /* Debug stress test                                                   */
        /* ------------------------------------------------------------------ */

        if (argument.equalsIgnoreCase("check")) {

            if (!service.getVpnConfig().isDebug()) {
                sender.sendMessage("§cDebug mode is disabled.");
                return true;
            }

            sender.sendMessage("§7Running AntiVPN socket stress test...");
            ThreadLocalRandom random = ThreadLocalRandom.current();
            CountDownLatch latch = new CountDownLatch(STRESS_TEST_REQUESTS);

            long start = System.nanoTime();

            for (int i = 0; i < STRESS_TEST_REQUESTS; i++) {
                String ip = random.nextInt(256) + "."
                        + random.nextInt(256) + "."
                        + random.nextInt(256) + "."
                        + random.nextInt(256);

                CompletableFuture<CheckResponse> future =
                        service.getAntiVPN()
                                .getSocketManager()
                                .getSocketDataHandler()
                                .verify(new CheckRequest(ip, ip.replace('.', '_')));

                if (future == null) {
                    latch.countDown();
                    continue;
                }

                future.whenComplete((res, err) -> latch.countDown());
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sender.sendMessage("§cStress test interrupted.");
                return true;
            }

            double duration = (System.nanoTime() - start) / 1_000_000_000.0;
            sender.sendMessage(String.format(
                    "§aHandled %d requests in %.2f seconds.",
                    STRESS_TEST_REQUESTS,
                    duration
            ));
            return true;
        }

        /* ------------------------------------------------------------------ */
        /* Token handling                                                      */
        /* ------------------------------------------------------------------ */

        boolean success = processToken(argument);

        sender.sendMessage(success
                ? "§aToken processed successfully."
                : "§cFailed to process token.");

        return true;
    }

    private boolean processToken(String token) {
        service.getVpnConfig().setSecret(token);
        service.getAntiVPN().getAntiVPNConfig().withApiKey(token);
        service.getAntiVPN().getSocketManager().reconnect();
        return service.saveConfig();
    }
}
