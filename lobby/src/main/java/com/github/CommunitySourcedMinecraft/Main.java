package com.github.CommunitySourcedMinecraft;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		var proxySecret = System.getenv("PROXY_SECRET");
		if (proxySecret != null) {
			logger.info("Enabling Velocity proxy support...");
			VelocityProxy.enable(proxySecret);
		}

		MinecraftServer minecraftServer = MinecraftServer.init();

		InstanceManager instanceManager = MinecraftServer.getInstanceManager();
		InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
		instanceContainer.setChunkSupplier(LightingChunk::new);
		instanceContainer.setGenerator(unit ->
			unit
				.modifier()
				.fillHeight(0, 40, Block.STONE));

		GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
		globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
			event.setSpawningInstance(instanceContainer);
			event
				.getPlayer()
				.setRespawnPoint(new Pos(0, 42, 0));
		});

		minecraftServer.start("0.0.0.0", 25565);
	}
}