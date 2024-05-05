package dev.csmc.arena.mobs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
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

        setInstance(instance, position);
    }
}
