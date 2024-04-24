package com.github.communitysourcedminecraft.lobby.rpc;

public class RpcStartInstall {
	public record Request() {
	}

	public record Response(Status status, String message) {
	}
}
