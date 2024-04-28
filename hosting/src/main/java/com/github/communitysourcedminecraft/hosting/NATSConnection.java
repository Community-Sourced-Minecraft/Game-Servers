package com.github.communitysourcedminecraft.hosting;

import com.github.communitysourcedminecraft.hosting.rpc.RPCRequest;
import com.github.communitysourcedminecraft.hosting.rpc.RPCResponse;
import com.github.communitysourcedminecraft.hosting.rpc.RPCType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nats.client.*;
import io.nats.client.api.KeyValueConfiguration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

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
					logger.warn("No handler for RPC type: {}", req.type());
					return;
				}

				var response = handler.handle(req.data());
				connection.publish(msg.getReplyTo(), gson
					.toJson(new RPCResponse(req.type(), response))
					.getBytes());
			} catch (Exception e) {
				logger.error("Error processing RPC", e);
			}
		});

		dispatcher.subscribe(info.rpcNetworkSubject(), info.podName());
		dispatcher.subscribe(info.rpcNetworkSubject(), info.gameMode());

		connection
			.keyValueManagement()
			.create(KeyValueConfiguration
				.builder()
				.name(info.kvInstancesKey())
				.build());
		this.instancesKV = connection.keyValue(info.kvInstancesKey());
	}

	public static NATSConnection connectBlocking(ServerInfo info) throws IOException, InterruptedException, JetStreamApiException {
		var natsUrl = System.getenv("NATS_URL");
		var nc = Nats.connectReconnectOnConnect(natsUrl);

		return new NATSConnection(info, nc);
	}

	public void registerHandler(RPCType type, RPCHandler handler) {
		handlers.put(type, handler);
	}

	public void registerThisInstance(int port) throws IOException, JetStreamApiException {
		instancesKV.put(info.podName(), gson.toJson(info.instanceInfo(port)));
	}


	@FunctionalInterface
	public interface RPCHandler {
		RPCResponse handle(String reqData);
	}
}
