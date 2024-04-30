package com.github.communitysourcedminecraft.hosting;

import com.github.communitysourcedminecraft.hosting.rpc.RPCRequest;
import com.github.communitysourcedminecraft.hosting.rpc.RPCResponse;
import com.github.communitysourcedminecraft.hosting.rpc.RPCTransferPlayer;
import com.github.communitysourcedminecraft.hosting.rpc.RPCType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nats.client.*;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NATSConnection {
	private final ServerInfo info;
	@Getter
	private final Connection connection;

	private final KeyValue instancesKV;

	private static final Logger logger = LoggerFactory.getLogger(NATSConnection.class);
	private static final Gson gson = new GsonBuilder()
		.disableHtmlEscaping()
		.create();

	private final HashMap<RPCType, RPCHandler> handlers = new HashMap<>();

	private NATSConnection(
		ServerInfo info, Connection connection) throws IOException, JetStreamApiException {
		this.info = info;
		this.connection = connection;

		var dispatcher = connection.createDispatcher((msg) -> {
			try {
				var req = gson.fromJson(new String(msg.getData()), RPCRequest.class);
				logger.info("Received RPC: {}", req);

				var handler = handlers.get(req.type());
				if (handler == null) {
					logger.warn("No handler for RPC type, nak()ing: {}", req.type());
					msg.nak();
					return;
				}

				var response = handler.handle(req.data(), msg);
				connection.publish(msg.getReplyTo(), gson
					.toJson(new RPCResponse(req.type(), response))
					.getBytes());
			} catch (Exception e) {
				logger.error("Error processing RPC", e);
			}
		});

		dispatcher.subscribe(info.rpcNetworkSubject(), info.podName());
		dispatcher.subscribe(info.rpcNetworkSubject(), info.gameMode());

		try {
			connection
				.keyValueManagement()
				.create(KeyValueConfiguration
					.builder()
					.name(info.kvInstancesKey())
					.build());
		} catch (JetStreamApiException e) {
			if (e.getErrorCode() == 400) {
				logger.info("Key-Value store already exists");
			} else {
				throw e;
			}
		}
		this.instancesKV = connection.keyValue(info.kvInstancesKey());
	}

	public static NATSConnection connectBlocking(ServerInfo info) throws IOException, InterruptedException, JetStreamApiException {
		var natsUrl = System.getenv("NATS_URL");
		if (natsUrl == null) {
			logger.warn("NATS_URL not set, defaulting to localhost:4222");
			natsUrl = "nats://localhost:4222";
		}

		var nc = Nats.connectReconnectOnConnect(natsUrl);

		return new NATSConnection(info, nc);
	}

	public void registerHandler(RPCType type, RPCHandler handler) {
		handlers.put(type, handler);
	}

	public void registerThisInstance() throws IOException, JetStreamApiException {
		logger.info("Registering this instance: {}", info.instanceInfo());
		instancesKV.put(info.podName(), gson.toJson(info.instanceInfo()));
	}

	public void deregisterThisInstance() throws IOException, JetStreamApiException {
		instancesKV.delete(info.podName());
	}

	public List<String> getServersForGamemode(String gamemode) throws JetStreamApiException, IOException, InterruptedException {
		return instancesKV
			.keys()
			.parallelStream()
			.map(k -> {
				try {
					return instancesKV.get(k);
				} catch (IOException | JetStreamApiException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(v -> gson
				.fromJson(new String(v.getValue()), ServerInfo.InstanceInfo.class)
				.gamemode()
				.equals(gamemode))
			.map(KeyValueEntry::getKey)
			.toList();
	}

	public void transferPlayer(UUID uuid, String to) {
		var inner = gson.toJson(new RPCTransferPlayer.Request(uuid, info.podName(), to));
		var payload = gson.toJson(new RPCRequest(RPCType.TRANSFER_PLAYER, inner));

		connection
			.request(info.rpcNetworkSubject(), payload.getBytes())
			.handle((message, throwable) -> {
				if (throwable != null) {
					logger.error("Error transferring player", throwable);
					return null;
				}

				var res = gson.fromJson(new String(message.getData()), RPCTransferPlayer.Response.class);
				logger.info("Received transfer response: {}", res);

				return null;
			});
	}

	@FunctionalInterface
	public interface RPCHandler {
		RPCResponse handle(String reqData, Message msg);
	}
}
