package dev.csmc.arena.instances;

import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public final class Lobby {
    public static final Instance INSTANCE;

    static {
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) new URI("https://s3.devminer.xyz/csmc/arena-lobby.polar")
                    .toURL()
                    .openConnection();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final Instance instance;
        try {
            instance = MinecraftServer.getInstanceManager().createInstanceContainer(new PolarLoader(conn.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        INSTANCE = instance;
    }
}
