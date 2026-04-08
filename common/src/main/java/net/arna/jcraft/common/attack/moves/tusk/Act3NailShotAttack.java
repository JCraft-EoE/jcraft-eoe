package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Tusk Act 3 - M1 (same stats as Act 2 Golden Rectangle Nail).
 * Spinning nail, 15-block range, creeps 8 blocks after hit.
 */
public final class Act3NailShotAttack extends AbstractMove<Act3NailShotAttack, TuskAct3Entity> {
    @Getter
    private final float baseSpeed;
    @Getter
    private final float maxRange;
    @Getter
    private final float creepDistance;

    public Act3NailShotAttack(int cooldown, int windup, int duration, float moveDistance,
                              float baseSpeed, float maxRange, float creepDistance) {
        super(cooldown, windup, duration, moveDistance);
        this.baseSpeed = baseSpeed;
        this.maxRange = maxRange;
        this.creepDistance = creepDistance;
        ranged = true;
    }

    @Override
    public @NotNull MoveType<Act3NailShotAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile nail = NailProjectile.fromTuskAct3(attacker, maxRange, creepDistance);
        if (nail == null) return Set.of();

        attacker.playSound(JSoundRegistry.TUSK_SHOT.get(), 1.0f, 1.0f);
        nail.setPos(attacker.position().add(0, attacker.getBbHeight() * 0.75, 0));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, baseSpeed, 1.0F);

        attacker.level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull Act3NailShotAttack getThis() {
        return this;
    }

    @Override
    public @NonNull Act3NailShotAttack copy() {
        return copyExtras(new Act3NailShotAttack(getCooldown(), getWindup(), getDuration(),
                getMoveDistance(), baseSpeed, maxRange, creepDistance));
    }

    public static class Type extends AbstractMove.Type<Act3NailShotAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<Act3NailShotAttack>, Act3NailShotAttack>
        buildCodec(RecordCodecBuilder.Instance<Act3NailShotAttack> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(Act3NailShotAttack::getBaseSpeed),
                    Codec.FLOAT.fieldOf("max_range").forGetter(Act3NailShotAttack::getMaxRange),
                    Codec.FLOAT.fieldOf("creep_distance").forGetter(Act3NailShotAttack::getCreepDistance)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, baseSpeed, maxRange, creepDistance) ->
                    new Act3NailShotAttack(cooldown, windup, duration, moveDistance, baseSpeed, maxRange, creepDistance)));
        }
    }
}
