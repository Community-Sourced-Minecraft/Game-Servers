package dev.csmc.arena.mobs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;

import java.util.List;

public class ArenaMob extends EntityCreature {
    private static final int BLOCK_LENGTH = 6;
    private static final List<String> CHARACTERS = List.of(
            "", "▏", "▎", "▍", "▌", "▋", "▊", "▉"
    );
    private static final String FULL_BLOCK_CHAR = "█";

    public ArenaMob(EntityType type) {
        super(type);
        setCustomName(generateHealthBar(getMaxHealth(), getHealth()));
        setCustomNameVisible(true);
        eventNode().addListener(EntityDamageEvent.class, event ->
                        setCustomName(generateHealthBar(getMaxHealth(), getHealth())))
                .addListener(EntityDeathEvent.class, event ->
                        setCustomName(generateHealthBar(getMaxHealth(), 0)));
    }

    private static Component generateHealthBar(float maxHealth, float minHealth) {
        final double charHealth = (minHealth / maxHealth) * BLOCK_LENGTH;
        return Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(
                        FULL_BLOCK_CHAR.repeat((int) Math.floor(charHealth)),
                        NamedTextColor.RED
                )).append(Component.text(CHARACTERS.get((int) Math.round(
                        (charHealth - Math.floor(charHealth)) // number from 0-1
                                * (CHARACTERS.size() - 1) // indexes start at 0
                )), NamedTextColor.YELLOW))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .build();
    }
}
