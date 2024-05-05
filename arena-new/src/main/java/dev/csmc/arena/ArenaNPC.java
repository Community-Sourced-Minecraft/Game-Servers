package dev.csmc.arena;

import dev.csmc.arena.guis.UpgradesGUI;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

import java.util.function.Consumer;

final class ArenaNPC extends EntityCreature {
    private final String name;
    private final Consumer<Player> onClick;

    ArenaNPC(String name, Instance instance, Point spawn, Consumer<Player> onClick) {
        super(EntityType.VILLAGER);

        this.name = name;
        this.onClick = onClick;

        setCustomName(Component.text(name));
        setCustomNameVisible(true);
        setInstance(instance, spawn);
    }

    public void handle(EntityAttackEvent event) {
        if (event.getTarget() != this) return;
        if (!(event.getEntity() instanceof Player player)) return;

        player.playSound(Sound.sound()
                .type(SoundEvent.BLOCK_BELL_USE)
                .pitch(2)
                .build(), event.getTarget());
        onClick.accept(player);
    }

    public void handle(PlayerEntityInteractEvent event) {
        if (event.getTarget() != this) return;
        if (event.getHand() != Player.Hand.MAIN) return;

        event.getEntity().playSound(Sound.sound()
                .type(SoundEvent.BLOCK_BELL_USE)
                .pitch(2)
                .build(), event.getTarget());
        onClick.accept(event.getEntity());
    }

    public static ArenaNPC spawnNPC(Instance instance) {
        return new ArenaNPC("Upgrades", instance, new Pos(0.5, 1, 0.5, 5.0f, 5.0f), player -> {
            player.openInventory(new UpgradesGUI((ArenaPlayer) player));
        });
    }
}
