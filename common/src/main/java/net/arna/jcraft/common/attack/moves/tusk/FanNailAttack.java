package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.itfs.AttackRotationOffsetOverride;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Tusk Act 2 - Homing Nail Fan (Ultimate)
 * Fires 5 nails in a spread pattern. Each nail gently homes toward the nearest entity.
 * Nails fly through blocks and will not stop until they hit a living entity or exhaust their range.
 */
public final class FanNailAttack extends AbstractMove<FanNailAttack, TuskAct2Entity> implements AttackRotationOffsetOverride {

    public FanNailAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public float getAttackRotationOffset(StandEntity<?, ?> attacker) {
        return 0f;
    }

    @Override
    public @NotNull MoveType<FanNailAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct2Entity attacker, LivingEntity user) {
        final float spread = 10.0f;
        int index = 0;
        for (int i = 0; i < 5; i++) {
            NailProjectile nail = NailProjectile.fromTuskAct2Fan(attacker);
            if (nail == null) break;

            // Fan pattern: 0, +1, -1, +2, -2 offsets
            if (i % 2 == 0) index -= i;
            else index += i;

            final float yaw = user.getYRot() + index * spread;
            Vec3 rotVec = RotationUtil.vecPlayerToWorld(
                    RotationUtil.rotToVec(yaw, user.getXRot()),
                    GravityChangerAPI.getGravityDirection(user)
            );

            Vec3 spawnPos = user.position().add(0, user.getBbHeight() * 0.55, 0);
            nail.setPos(spawnPos);
            nail.setDeltaMovement(rotVec.scale(0.7)); // slow so homing is effective
            attacker.level().addFreshEntity(nail);
        }
        attacker.playSound(JSoundRegistry.TUSK_SHOT.get(), 1.0f, 0.8f);
        return Set.of();
    }

    @Override
    protected @NonNull FanNailAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FanNailAttack copy() {
        return copyExtras(new FanNailAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<FanNailAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<FanNailAttack>, FanNailAttack>
        buildCodec(RecordCodecBuilder.Instance<FanNailAttack> instance) {
            return baseDefault(instance, FanNailAttack::new);
        }
    }
}
