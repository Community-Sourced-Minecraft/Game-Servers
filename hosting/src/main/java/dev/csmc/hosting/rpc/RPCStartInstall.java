package dev.csmc.hosting.rpc;

public class RPCStartInstall {
	public record Request() {
	}

	public record Response(Status status, String message) {
	}
}
