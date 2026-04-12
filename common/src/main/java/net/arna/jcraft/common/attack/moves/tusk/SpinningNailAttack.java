package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.itfs.AttackRotationOffsetOverride;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class SpinningNailAttack<A extends IAttacker<A, ?>> extends AbstractMove<SpinningNailAttack<A>, A> implements AttackRotationOffsetOverride {
    @Getter
    private final float baseSpeed;

    public SpinningNailAttack(int cooldown, int windup, int duration, float moveDistance, float baseSpeed) {
        super(cooldown, windup, duration, moveDistance);
        this.baseSpeed = baseSpeed;
        ranged = true;
    }

    @Override
    public float getAttackRotationOffset(StandEntity<?, ?> attacker) {
        return 0f;
    }

    @Override
    public @NotNull MoveType<SpinningNailAttack<A>> getMoveType() {
        return (MoveType<SpinningNailAttack<A>>) (MoveType<?>) Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        NailProjectile nail = null;

        if (attacker instanceof TuskAct2Entity tusk2) {
            // Act 2: Golden Rectangle Nail with creeping
            nail = NailProjectile.fromTuskAct2(tusk2, 15.0f, 8.0f);
        } else if (attacker instanceof TuskAct3Entity tusk3) {
            // Act 3: Regular nail
            nail = NailProjectile.fromTuskAct3(tusk3, 15.0f, 0f);
        }

        if (nail == null) return Set.of();

        // Position at stand height
        nail.setPos(attacker.getBaseEntity().position().add(0, attacker.getBaseEntity().getBbHeight() * 0.75, 0));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, baseSpeed, 1.0F);

        attacker.getBaseEntity().level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull SpinningNailAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SpinningNailAttack<A> copy() {
        return copyExtras(new SpinningNailAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), baseSpeed));
    }

    public static class Type extends AbstractMove.Type<SpinningNailAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<SpinningNailAttack<?>>, SpinningNailAttack<?>>
        buildCodec(RecordCodecBuilder.Instance<SpinningNailAttack<?>> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(SpinningNailAttack::getBaseSpeed)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, baseSpeed) ->
                    new SpinningNailAttack<>(cooldown, windup, duration, moveDistance, baseSpeed)));
        }
    }
}