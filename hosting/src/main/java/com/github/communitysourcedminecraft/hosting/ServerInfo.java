package com.github.communitysourcedminecraft.hosting;

import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.slf4j.LoggerFactory.*;

public record ServerInfo(String network, String gameMode, String podName, String podNamespace,
                         int port) {
	private static final Logger logger = getLogger(ServerInfo.class);

	public static ServerInfo parse(String gamemode) {
		var network = System.getenv("CSMC_NETWORK");
		if (network == null) {
			logger.warn("CSMC_NETWORK not set, defaulting to 'default'");
			network = "default";
		}

		var podName = System.getenv("POD_NAME");
		if (podName == null) {
			logger.error("POD_NAME not set, defaulting to 'default'");
			podName = "default";
		}

		var podNamespace = System.getenv("POD_NAMESPACE");
		if (podNamespace == null) {
			logger.error("POD_NAMESPACE not set, defaulting to 'default'");
			podNamespace = "default";
		}

		var rawPort = System.getenv("PORT");
		if (rawPort == null) {
			logger.error("PORT not set, defaulting to '25565'");
			rawPort = "25565";
		}
		var port = Integer.parseInt(rawPort);

		return new ServerInfo(network, gamemode, podName, podNamespace, port);
	}

	public String rpcNetworkSubject() {
		return "csmc." + podNamespace + "." + network;
	}

	public String kvNetworkKey() {
		return "csmc_" + podNamespace + "_" + network;
	}

	public String kvGamemodeKey() {
		return kvNetworkKey() + "_gamemode_" + gameMode;
	}

	// KV: csmc_<namespace>_<network>_instances<Container hostname, InstanceInfo>
	public String kvInstancesKey() {
		return kvNetworkKey() + "_instances";
	}

	public InstanceInfo instanceInfo() throws UnknownHostException {
		return new InstanceInfo(gameMode, InetAddress
			.getLocalHost()
			.getHostAddress(), port);
	}

	public record InstanceInfo(String gamemode, String address, int port) {
	}
}
