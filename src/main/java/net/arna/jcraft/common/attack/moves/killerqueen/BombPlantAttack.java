package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.BombTrackerComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BombPlantAttack extends AbstractSimpleAttack<BombPlantAttack, AbstractKillerQueenEntity<?, ?>> {
    public BombPlantAttack(int cooldown, int windup, int duration, float attackDistance, int stun, float hitboxSize, float offset) {
        super(cooldown, windup, duration, attackDistance, 0f, stun, hitboxSize, 0f, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> attacker, LivingEntity user, MoveContext ctx) {
        BombTrackerComponent tracker = JComponents.getBombTracker(user);

        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        targets.stream()
                .findFirst()
                .<Entity>map(JUtils::getUserIfStand)
                .or(() -> {
                    // If none are found, re-do an optimized hitbox check for any entity type
                    Vec3d rotVec = getRotVec(attacker);
                    Vec3d boxCenter = attacker.getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                    Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                    List<Entity> hit = attacker.world.getEntitiesByClass(Entity.class,
                            new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));
                    return hit.isEmpty() ? Optional.empty() : Optional.of(hit.get(0));
                })
                .ifPresent(target -> tracker.getMainBomb().setBomb(target));

        return targets;
    }
    @Override
    protected @NonNull BombPlantAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BombPlantAttack copy() {
        return copyExtras(new BombPlantAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun(), getHitboxSize(),
                getOffset()));
    }
}
