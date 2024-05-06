package dev.csmc.arena;

import dev.csmc.arena.instances.Arena;
import dev.csmc.arena.mobs.ArenaMob;
import dev.csmc.arena.mobs.ArenaSkeleton;
import dev.csmc.arena.mobs.ArenaZombie;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Setter
public class ArenaGame {
    public GameState gameState = GameState.BREAK;
    public List<ArenaPlayer> players = new ArrayList<>();
    public List<ArenaPlayer> deadPlayers = new ArrayList<>();
    public HashMap<UUID, Boolean> readyPlayers = new HashMap<>();
    public List<ArenaMob> mobs = new ArrayList<>();
    public Arena arena;
    public int teamCoins = 0;
    public int round = 0;

    public void startRound() {
        this.gameState = GameState.ROUND;
        this.round++;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            for (ArenaPlayer player : players) {
                // TODO: Use updateGameScoreboard()
                player.createGameScoreboard();
            }

            return TaskSchedule.seconds(1);
        });

        MinecraftServer.getSchedulerManager().submitTask(new Supplier<>() {
            final int repeat = 1;
            final int secondsToRun = 6;
            int elapsedTicks = 0;

            @Override
            public TaskSchedule get() {
                if (elapsedTicks >= secondsToRun) {
                    return TaskSchedule.stop();
                }

                Audiences.all().showTitle(Title.title(Component.text((5 - elapsedTicks) == 0 ? "START" : String.valueOf(5 - elapsedTicks), NamedTextColor.YELLOW, TextDecoration.BOLD), Component.text("")));
                Audiences.all().playSound(Sound.sound().type((5 - elapsedTicks) == 0 ? SoundEvent.ENTITY_ENDER_DRAGON_GROWL : SoundEvent.BLOCK_NOTE_BLOCK_COW_BELL).pitch(1 + (float) elapsedTicks / 2).build());

                elapsedTicks += repeat;
                return TaskSchedule.seconds(repeat);
            }
        });

        MinecraftServer.getGlobalEventHandler().addListener(EntityDeathEvent.class, (event) -> {
            if (!(event.getEntity() instanceof ArenaMob) && !(event.getInstance().equals(arena.INSTANCE)))
                return;
            if (mobs.isEmpty()) {
                gameState = GameState.BREAK;
                return;
            }
            ArenaMob mob = (ArenaMob) event.getEntity();
//            ArenaPlayer damager = this.players.stream().filter(plr -> plr.getUuid() == Collections.max(mob.damagedBy.entrySet(), Map.Entry.comparingByValue()).getKey()).toList().getFirst();
            UUID damagerUUID = Collections.max(mob.damagedBy.entrySet(), Map.Entry.comparingByKey()).getKey();
            ArenaPlayer damager = this.players.stream().filter(plr -> plr.getUuid().equals(damagerUUID)).findFirst().orElse(null);
            if(damager != null) damager.setCoins(damager.getCoins() + 1);
            setTeamCoins(getTeamCoins() + 2);

            mobs.remove(mob);

        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            this.spawnZombies();
            if (round >= 2) this.spawnSkeletons();
            return TaskSchedule.stop();
        }, TaskSchedule.seconds(5));
    }

    public void endRound() {
        this.gameState = GameState.BREAK;
    }

    public void stopGame() {
        MinecraftServer.getInstanceManager().unregisterInstance(arena.INSTANCE);
        arena.INSTANCE = null;
        this.gameState = null;
        this.readyPlayers = null;
        this.players = null;
    }

    public void spawnZombies() {
        for (int i = 0; i < Math.floor(1.5 * round) + 5; i++) {
            Random random = new Random((long) (System.currentTimeMillis() * 1.25 + System.currentTimeMillis()));
            Pos pos = new Pos(0, 2, 0);
            // -46 to 46
            pos = pos.withX(random.nextInt(46 + 46) - 46);
            pos = pos.withZ(random.nextInt(46 + 46) - 46);

            mobs.add(new ArenaZombie(arena.INSTANCE, pos));
        }
    }

    public void spawnSkeletons() {
        for (int i = 0; i < Math.floor(1.5 * round) + 1; i++) {
            Random random = new Random(System.currentTimeMillis());
            Pos pos = new Pos(0, 2, 0);
            // -46 to 46
            pos = pos.withX(random.nextInt(46 + 46) - 46);
            pos = pos.withZ(random.nextInt(46 + 46) - 46);

            mobs.add(new ArenaSkeleton(arena.INSTANCE, pos));
        }
    }

    private void init() {
        arena = new Arena();
        arena.createServer();

        for (ArenaPlayer player : players) {
            player.setInstance(arena.INSTANCE);
            readyPlayers.put(player.getUuid(), false);
            player.arenaGame = this;

            player.sendMessage(
                    Component.join(JoinConfiguration.builder().build(),
                            Component.text("Welcome to the Arena! Get ready for adrenaline-pumping battles against various mobs. To dive into the action, simply click on the NPC and then on the feather item to start your journey. Prepare to test your skills and emerge victorious in the ultimate combat arena!", NamedTextColor.GRAY)
                    )
            );
        }

        ArenaNPC arenaNPC = ArenaNPC.spawnNPC(arena.INSTANCE);
        arena.INSTANCE.eventNode().addListener(EntityAttackEvent.class, arenaNPC::handle)
                .addListener(PlayerEntityInteractEvent.class, arenaNPC::handle);
    }

    public void readyUp(ArenaPlayer player) {
        readyPlayers.put(player.getUuid(), true);
        if (readyPlayers.values().stream().filter(t -> t).count() == this.players.size()) {
            startRound();
            readyPlayers.replaceAll((p, v) -> false);
        }
    }

    public ArenaGame(List<ArenaPlayer> players) {
        this.players = players;
        init();
    }

    public enum GameState {
        ROUND, BREAK, ENDED
    }
}
