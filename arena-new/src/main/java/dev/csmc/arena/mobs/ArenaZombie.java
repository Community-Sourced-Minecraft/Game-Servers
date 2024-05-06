package dev.csmc.arena.mobs;

import dev.csmc.arena.ArenaPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;

public class ArenaZombie extends ArenaMob {
    public ArenaZombie(Instance instance, Pos position) {
        super(EntityType.ZOMBIE);
        addAIGroup(
                List.of(new MeleeAttackGoal(this, 1.2, 20, TimeUnit.SERVER_TICK)),
                List.of(new ClosestEntityTarget(this, 32, entity -> entity instanceof Player))
        );

        setCanPickupItem(false);
        this.eventNode().addListener(EntityAttackEvent.class, (event) -> {
            ArenaPlayer player = (ArenaPlayer) event.getTarget();
            ArenaZombie zombie = (ArenaZombie) event.getEntity();

            player.takeKnockback(0.4f, zombie.getPosition().direction().x() < 0 ? 1 : -1, zombie.getPosition().direction().z() < 0 ? 1 : -1);
            player.damage(Damage.fromEntity(zombie, 0.5f * 1 + (float) player.getArenaGame().round / 10));
        });

        setInstance(instance, position);
    }
}
