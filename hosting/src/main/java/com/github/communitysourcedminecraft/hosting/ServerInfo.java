package com.github.communitysourcedminecraft.hosting;

import java.net.InetAddress;
import java.net.UnknownHostException;

public record ServerInfo(String network, String gameMode, String podName, String podNamespace) {
	public static ServerInfo parse() {
		var network = System.getenv("CSMC_NETWORK");
		var gameMode = System.getenv("CSMC_GAMEMODE");
		var podName = System.getenv("POD_NAME");
		var podNamespace = System.getenv("POD_NAMESPACE");

		return new ServerInfo(network, gameMode, podName, podNamespace);
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

	public InstanceInfo instanceInfo(int port) throws UnknownHostException {
		return new InstanceInfo(gameMode, InetAddress
			.getLocalHost()
			.getHostAddress(), port);
	}

	public record InstanceInfo(String gamemode, String address, int port) {
	}
}
