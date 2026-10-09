package net.arna.jcraft.common.attack.moves.horus;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.LargeIcicleProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class IcicleFireAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<IcicleFireAttack<A>, A> {
    public static int MAX_ICICLE_CHARGE_TIME = 30;

    public IcicleFireAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<IcicleFireAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public boolean shouldPerform(final A attacker, final int moveStun) {
        return super.shouldPerform(attacker, moveStun);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final LargeIcicleProjectile instantIcicle = new LargeIcicleProjectile(baseEntity.level(), user);
        float scale = Mth.clamp(getChargeTime() / (MAX_ICICLE_CHARGE_TIME - 2.0f), 0.1f, 1.0f);
        instantIcicle.setScale(scale);
        instantIcicle.setInstant(true);

        final Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);

        final Vec3 velocity = user.getLookAngle().scale(0.01);
        final double e = velocity.x, f = velocity.y, g = velocity.z;
        final double l = velocity.horizontalDistance();

        instantIcicle.moveTo(baseEntity.getX() + heightOffset.x, baseEntity.getY() + heightOffset.y, baseEntity.getZ() + heightOffset.z,
                (float) (Mth.atan2(-e, -g) * 57.2957763671875),
                (float) (Mth.atan2(f, l) * 57.2957763671875)
        );
        instantIcicle.setDeltaMovement(velocity);
        instantIcicle.lock();

        baseEntity.level().addFreshEntity(instantIcicle);

        return Set.of();
    }

    @Override
    protected @NonNull IcicleFireAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull IcicleFireAttack<A> copy() {
        return copyExtras(new IcicleFireAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<IcicleFireAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<IcicleFireAttack<?>>, IcicleFireAttack<?>> buildCodec(RecordCodecBuilder.Instance<IcicleFireAttack<?>> instance) {
            return baseDefault(instance, IcicleFireAttack::new);
        }
    }
}
