package com.github.communitysourcedminecraft.lobby;

import com.github.communitysourcedminecraft.lobby.rpc.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nats.client.Nats;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.network.packet.client.play.ClientChatAckPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final Gson gson = new GsonBuilder()
		.disableHtmlEscaping()
		.create();

	private static final Pos SPAWN = new Pos(0, 10, 0, 180f, 0f);

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
		var proxySecret = System.getenv("PROXY_SECRET");
		if (proxySecret != null) {
			logger.info("Enabling Velocity proxy support...");
			VelocityProxy.enable(proxySecret);
		}

		var natsUrl = System.getenv("NATS_URL");
		var network = System.getenv("CSMC_NETWORK");
		var gameMode = System.getenv("CSMC_GAMEMODE");
		var podName = System.getenv("POD_NAME");
		var podNamespace = System.getenv("POD_NAMESPACE");

		var natsSubject = "csmc." + podNamespace + "." + network + ".gamemode." + gameMode + "." + podName;

		var nc = Nats.connectReconnectOnConnect(natsUrl);

		nc
			.createDispatcher((msg) -> {
				try {
					var rpc = gson.fromJson(new String(msg.getData()), RpcRequest.class);
					logger.info("Received RPC: {}", rpc);

					switch (rpc.type()) {
						case START_INSTALL -> {
							var req = gson.fromJson(rpc.data(), RpcStartInstall.Request.class);

							logger.info("Received START_INSTALL request: {}", req);

							var startInstallResponse = new RpcStartInstall.Response(Status.OK, "Hello from Java!");

							nc.publish(msg.getReplyTo(), gson
								.toJson(new RpcResponse(RpcType.START_INSTALL, startInstallResponse))
								.getBytes());
						}
						case null, default -> {
							logger.warn("Unknown RPC type: {}", rpc.type());
						}
					}
				} catch (Exception e) {
					logger.error("Error processing RPC", e);
				}
			})
			.subscribe(natsSubject);

		var minecraftServer = MinecraftServer.init();

		// WARN net.minestom.server.listener.manager.PacketListenerManager -- Packet class net.minestom.server.network.packet.client.play.ClientChatAckPacket does not have any default listener! (The issue comes from Minestom)
		MinecraftServer
			.getPacketListenerManager()
			.setPlayListener(ClientChatAckPacket.class, (player, packet) -> {});

		var instanceManager = MinecraftServer.getInstanceManager();
		var instanceContainer = instanceManager.createInstanceContainer();
		instanceContainer.setChunkSupplier(LightingChunk::new);

		var conn = (HttpsURLConnection) new URI("https://s3.devminer.xyz/csmc/lobby.polar")
			.toURL()
			.openConnection();
		instanceContainer.setChunkLoader(new PolarLoader(conn.getInputStream()));

		var globalEventHandler = MinecraftServer.getGlobalEventHandler();
		globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
			event.setSpawningInstance(instanceContainer);
			var player = event.getPlayer();

			player.setRespawnPoint(SPAWN);

			var uuid = player
				.getUuid()
				.toString();
			var ip = getPlayerIP(player);

			logger.info("Player {} ({}) connected from {}", player.getUsername(), uuid, ip);
		});
		globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
			var player = event.getPlayer();

			player.setGameMode(GameMode.ADVENTURE);
		});
		globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
			var player = event.getPlayer();

			var uuid = player
				.getUuid()
				.toString();
			var ip = getPlayerIP(player);

			logger.info("Player {} ({}) from {} disconnected", player.getUsername(), uuid, ip);
		});
		globalEventHandler.addListener(PlayerMoveEvent.class, event -> {
			if (event
				.getNewPosition()
				.blockY() > 0) return;

			event
				.getPlayer()
				.teleport(SPAWN);
		});

		minecraftServer.start("0.0.0.0", 25565);
	}

	private static String getPlayerIP(Player player) {
		return player
			.getPlayerConnection()
			.getRemoteAddress()
			.toString();
	}
}