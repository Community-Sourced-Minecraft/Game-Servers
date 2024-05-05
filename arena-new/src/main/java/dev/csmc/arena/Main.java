package dev.csmc.arena;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.csmc.arena.commands.ArenaCommand;
import dev.csmc.arena.commands.GroupCommand;
import dev.csmc.arena.instances.Lobby;
import dev.csmc.hosting.Hosting;
import dev.csmc.hosting.rpc.RPCResponse;
import dev.csmc.hosting.rpc.RPCStartInstall;
import dev.csmc.hosting.rpc.RPCType;
import dev.csmc.hosting.rpc.Status;
import io.nats.client.JetStreamApiException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.monitoring.TickMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

final class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public static void main(final String[] args) throws IOException, InterruptedException, JetStreamApiException {
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setPlayerProvider(ArenaPlayer::new);

        var hosting = Hosting.init("arena");
        var info = hosting.getInfo();
        var nats = hosting.getNats();

        nats.registerHandler(RPCType.START_INSTALL, (reqData, _msg) -> {
            var req = gson.fromJson(reqData, RPCStartInstall.Request.class);
            LOGGER.info("Received START_INSTALL requires: {}", req);

            // TODO: Implement installation logic

            var startInstallResponse = new RPCStartInstall.Response(Status.OK, "Hello from Java!");

            return new RPCResponse(RPCType.START_INSTALL, startInstallResponse);
        });

        // Commands
        {
            CommandManager commandManager = MinecraftServer.getCommandManager();
            commandManager.register(new ArenaCommand());
            commandManager.register(new GroupCommand());
        }

        // Events
        {
            GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

            eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                event.setSpawningInstance(Lobby.INSTANCE);
                event.getPlayer().setRespawnPoint(new Pos(0.5, 16, 0.5));

                Audiences.all().sendMessage(
                        Component.join(
                                JoinConfiguration.builder().build(),
                                Component.text("+", NamedTextColor.GREEN),
                                Component.text(" " + event.getPlayer().getUsername(), NamedTextColor.GRAY)
                        )
                );
            });

            eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
                Audiences.all().sendMessage(
                        Component.join(
                                JoinConfiguration.builder().build(),
                                Component.text("-", NamedTextColor.RED),
                                Component.text(" " + event.getPlayer().getUsername(), NamedTextColor.GRAY)
                        )
                );
            });

            eventHandler.addListener(PlayerSpawnEvent.class, event -> {
                if(!event.isFirstSpawn()) return;
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
                event.getPlayer().setEnableRespawnScreen(false);
            });

            // TODO: Add metrics
        }


        var proxySecret = System.getenv("PROXY_SECRET");
        if(proxySecret != null) {
            LOGGER.info("Enabling Velocity proxy support...");
            VelocityProxy.enable(proxySecret);
        }

        nats.registerThisInstance();
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            try {
                nats.deregisterThisInstance();
            } catch(IOException | JetStreamApiException e) {
                throw new RuntimeException(e);
            }
        });

        minecraftServer.start("0.0.0.0", info.port());
    }
}