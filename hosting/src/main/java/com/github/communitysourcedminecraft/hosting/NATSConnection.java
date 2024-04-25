package com.github.communitysourcedminecraft.hosting;

import com.github.communitysourcedminecraft.hosting.rpc.RPCRequest;
import com.github.communitysourcedminecraft.hosting.rpc.RPCResponse;
import com.github.communitysourcedminecraft.hosting.rpc.RPCType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class NATSConnection {
	private final ServerInfo info;
	@Getter
	private final Connection connection;
	private static final Logger logger = LoggerFactory.getLogger(NATSConnection.class);
	private static final Gson gson = new GsonBuilder()
		.disableHtmlEscaping()
		.create();

	private final HashMap<RPCType, RPCHandler> handlers = new HashMap<>();

	private NATSConnection(ServerInfo info, Connection connection) {
		this.info = info;
		this.connection = connection;

		connection
			.createDispatcher((msg) -> {
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
			})
			.subscribe(info.podRPCSubject());
	}

	public static NATSConnection connectBlocking(ServerInfo info) throws IOException, InterruptedException {
		var natsUrl = System.getenv("NATS_URL");
		var nc = Nats.connectReconnectOnConnect(natsUrl);

		return new NATSConnection(info, nc);
	}

	public void registerHandler(RPCType type, RPCHandler handler) {
		handlers.put(type, handler);
	}

	@FunctionalInterface
	public interface RPCHandler {
		RPCResponse handle(String reqData);
	}
}
