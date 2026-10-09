package net.arna.jcraft.common.attack.moves.horus;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.IcicleProjectile;
import net.arna.jcraft.common.entity.stand.HorusEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ScatterAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<ScatterAttack<A>, A> {
    public ScatterAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<ScatterAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        for (int batch = 0; batch < 2; batch++) {
            final float offset = batch == 0 ? 10.0F : -10.0F;
            for (int i = 1; i < 4; i++) {
                final IcicleProjectile icicle = new IcicleProjectile(attacker.getBaseEntity().level(), user);
                final float pitch = user.getXRot();
                final float yaw = user.getYRot() + i * offset;
                Vec3 rotVec = RotationUtil.vecPlayerToWorld(RotationUtil.rotToVec(yaw, pitch), GravityChangerAPI.getGravityDirection(user));
                icicle.shoot(rotVec.x, rotVec.y, rotVec.z, 1.75F, 0.1F);

                Vec3 upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
                Vec3 heightOffset = upVec.scale(0.75);
                icicle.setPos(attacker.getBaseEntity().position().add(heightOffset));
                icicle.withReflect();

                attacker.getBaseEntity().level().addFreshEntity(icicle);
            }
        }

        return Set.of();
    }

    @Override
    protected @NonNull ScatterAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ScatterAttack<A> copy() {
        return copyExtras(new ScatterAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<ScatterAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ScatterAttack<?>>, ScatterAttack<?>> buildCodec(RecordCodecBuilder.Instance<ScatterAttack<?>> instance) {
            return baseDefault(instance, ScatterAttack::new);
        }
    }
}
