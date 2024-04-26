package com.github.communitysourcedminecraft.hosting;

public record ServerInfo(String network, String gameMode, String podName, String podNamespace) {
	public static ServerInfo parse() {
		var network = System.getenv("CSMC_NETWORK");
		var gameMode = System.getenv("CSMC_GAMEMODE");
		var podName = System.getenv("POD_NAME");
		var podNamespace = System.getenv("POD_NAMESPACE");

		return new ServerInfo(network, gameMode, podName, podNamespace);
	}

	public String podRPCSubject() {
		return "csmc." + podNamespace + "." + network + ".gamemode." + gameMode + "." + podName;
	}

	public String kvNetworkKey() {
		return "csmc_" + podNamespace + "_network_" + network;
	}

	public String kvGamemodeKey() {
		return kvNetworkKey() + "_gamemode_" + gameMode;
	}
}
