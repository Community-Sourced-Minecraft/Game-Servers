package dev.csmc.arena;

import dev.csmc.arena.instances.Arena;
import dev.csmc.arena.mobs.ArenaSkeleton;
import dev.csmc.arena.mobs.ArenaZombie;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;

import java.util.*;

@Getter
@Setter
public class ArenaGame {
    public GameState gameState = GameState.BREAK;
    public List<ArenaPlayer> players = new ArrayList<>();
    public HashMap<UUID, Boolean> readyPlayers = new HashMap<>();
    public Arena arena;
    public int teamCoins = 0;
    public int round = 0;

    public void startRound() {
        this.gameState = GameState.ROUND;
        this.round++;
    }

    public void spawnZombies() {
        for (int i = 0; i < Math.floor(1.5 * round) + 5; i++) {
            Random random = new Random(System.currentTimeMillis());
            Pos pos = new Pos(0,2,0);
            // -46 to 46
            pos = pos.withX(random.nextInt(46 + 46) - 46);
            pos = pos.withZ(random.nextInt(46 + 46) - 46);

            new ArenaZombie(arena.INSTANCE, pos);
        }
    }

    public void spawnSkeletons() {
        for (int i = 0; i < Math.floor(1.5 * round) + 1; i++) {
            Random random = new Random(System.currentTimeMillis());
            Pos pos = new Pos(0,2,0);
            // -46 to 46
            pos = pos.withX(random.nextInt(46 + 46) - 46);
            pos = pos.withZ(random.nextInt(46 + 46) - 46);

            new ArenaSkeleton(arena.INSTANCE, pos);
        }
    }

    private void init() {
        arena = new Arena();
        arena.createServer();

        for (ArenaPlayer player : players) {
            player.setInstance(arena.INSTANCE);
            readyPlayers.put(player.getUuid(), false);
            player.arenaGame = this;
        }

        ArenaNPC arenaNPC = ArenaNPC.spawnNPC(arena.INSTANCE);
        arena.INSTANCE.eventNode().addListener(EntityAttackEvent.class, arenaNPC::handle)
                .addListener(PlayerEntityInteractEvent.class, arenaNPC::handle);
    }

    public void readyUp(ArenaPlayer player) {
        readyPlayers.replace(player.getUuid(), true);
    }

    public ArenaGame(List<ArenaPlayer> players) {
        this.players = players;
        init();
    }

    public enum GameState {
        ROUND, BREAK
    }
}
