package dev.csmc.hosting.rpc;

import java.util.UUID;

public class RPCTransferPlayer {
	public record Request(UUID uuid, String source, String destination) {
	}

	public record Response(Status status) {
	}
}
