package dev.csmc.arena;

import dev.csmc.arena.mobs.ArenaMob;
import dev.csmc.arena.upgrades.IUpgrade;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

@Setter
@Getter
public class ArenaPlayer extends Player {
    public ArenaGroup arenaGroup = null;
    public ArenaGame arenaGame = null;
    public int coins = 0;
    public ArenaClass currentClass = ArenaClass.BERSERK;
    public HashMap<IUpgrade, Number> upgrades = new HashMap<>();

    public ArenaPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        super(uuid, username, playerConnection);

        eventNode().addListener(EntityAttackEvent.class, (event) -> {
            if(event.getTarget() instanceof ArenaMob arenaMob) {
                event.getTarget().takeKnockback(0.4f, this.getPosition().direction().x() < 0 ? 1 : -1, this.getPosition().direction().z() < 0 ? 1 : -1);
                arenaMob.damage(Damage.fromPlayer(this, 5f));
                arenaMob.damagedBy.put(event.getEntity().getUuid(), arenaMob.damagedBy.get(event.getEntity().getUuid()) != null ? arenaMob.damagedBy.get(event.getEntity().getUuid()) + 1 : 1);
            }
        });
    }

    // TODO: Create updateGameScoreboard()
    public void createGameScoreboard() {
        Sidebar sidebar = new Sidebar(Component.text("ᴀʀᴇɴᴀ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

        sidebar.createLine(createEmptyLine(5));
        sidebar.createLine(new Sidebar.ScoreboardLine(
                "round",
                Component.join(JoinConfiguration.builder().build(),
                        Component.text("⚔", NamedTextColor.YELLOW),
                        Component.text(" ʀᴏᴜɴᴅ: ", NamedTextColor.YELLOW, TextDecoration.BOLD),
                        Component.text(arenaGame.round, NamedTextColor.WHITE),
                        Component.text(" (" + arenaGame.mobs.size() + ")", NamedTextColor.DARK_GRAY)
                ),
                4, Sidebar.NumberFormat.blank()
        ));
        sidebar.createLine(createEmptyLine(4));
        sidebar.createLine(new Sidebar.ScoreboardLine(
                "coins",
                Component.join(JoinConfiguration.builder().build(),
                        Component.text(" ᴄᴏɪɴѕ: ", NamedTextColor.YELLOW, TextDecoration.BOLD),
                        Component.text(this.coins, NamedTextColor.WHITE)
                ),
                3, Sidebar.NumberFormat.blank()
        ));
        sidebar.createLine(new Sidebar.ScoreboardLine(
                "team_coins",
                Component.join(JoinConfiguration.builder().build(),
                        Component.text(" ᴛᴇᴀᴍ ᴄᴏɪɴѕ: ", NamedTextColor.YELLOW, TextDecoration.BOLD),
                        Component.text(arenaGame.teamCoins, NamedTextColor.WHITE)
                ),
                2, Sidebar.NumberFormat.blank()
        ));
        sidebar.createLine(createEmptyLine(1));
        sidebar.createLine(new Sidebar.ScoreboardLine(
                "ip",
                Component.text("ᴄѕᴍᴄ.ᴅᴇᴠ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                0, Sidebar.NumberFormat.blank()
        ));

        sidebar.addViewer(this);
    }

    private Sidebar.ScoreboardLine createEmptyLine(int line) {
        return new Sidebar.ScoreboardLine(
                "emptyLine-" + line,
                Component.text(""),
                line,
                Sidebar.NumberFormat.blank()
        );
    }

    public enum ArenaClass {
        BERSERK, TANK, ARCHER
    }
}
