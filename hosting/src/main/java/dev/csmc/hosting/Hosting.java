package dev.csmc.hosting;

import io.nats.client.JetStreamApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Hosting {
	private final ServerInfo info;
	private final NATSConnection nats;

	public static Hosting init(@NonNull String gamemode) throws JetStreamApiException, IOException, InterruptedException {
		var info = ServerInfo.parse(gamemode);
		var nats = NATSConnection.connectBlocking(info);

		return new Hosting(info, nats);
	}
}
