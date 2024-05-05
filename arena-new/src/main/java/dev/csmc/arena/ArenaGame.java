package dev.csmc.arena;

import dev.csmc.arena.instances.Arena;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ArenaGame {
    public GameState gameState = GameState.BREAK;
    public List<ArenaPlayer> players = new ArrayList<>();
    public Arena arena;
    public int round = 0;

    public void startRound() {
        this.gameState = GameState.ROUND;
        this.round++;
    }

    private void init() {
        arena = new Arena();
        arena.createServer();
        for (ArenaPlayer player : players) {
            player.setInstance(arena.INSTANCE);
        }
    }

    public ArenaGame(List<ArenaPlayer> players) {
        this.players = players;
        init();
    }

    public enum GameState {
        ROUND, BREAK
    }
}
