package dev.csmc.arena;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Setter
@Getter
public class ArenaPlayer extends Player {
    public ArenaGroup arenaGroup = null;
    public ArenaGame arenaGame = null;

    public ArenaPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);
    }
}
