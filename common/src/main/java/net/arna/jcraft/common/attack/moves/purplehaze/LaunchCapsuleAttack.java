package net.arna.jcraft.common.attack.moves.purplehaze;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.PHCapsuleProjectile;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class LaunchCapsuleAttack<A extends AbstractPurpleHazeEntity<? extends A, ?>> extends AbstractMove<LaunchCapsuleAttack<A>, A> {
    public LaunchCapsuleAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    public static void launchCapsule(AbstractPurpleHazeEntity<?, ?> attacker, LivingEntity user, Direction gravity, float speed, float yaw) {
        final PHCapsuleProjectile capsule = new PHCapsuleProjectile(user, attacker.level(), attacker.getPoisonType());

        final Vec2 corrected = RotationUtil.rotPlayerToWorld(yaw, user.getXRot(), gravity);
        // Y,P to P,Y,R
        JUtils.shoot(capsule, user, corrected.y, corrected.x, 0.0F, speed, 0.1F);

        final Vec3 heightOffset = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow()).scale(0.5);
        capsule.setPos(attacker.getBaseEntity().position().add(heightOffset));

        attacker.level().addFreshEntity(capsule);
    }

    @Override
    public @NonNull MoveType<LaunchCapsuleAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        final LivingEntity shooter = (attacker.isRemote() && !attacker.remoteControllable()) ? attacker : user;
        LaunchCapsuleAttack.launchCapsule(attacker, shooter, GravityChangerAPI.getGravityDirection(shooter), 0.8F, shooter.getYRot());

        return Set.of();
    }

    @Override
    protected @NonNull LaunchCapsuleAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LaunchCapsuleAttack<A> copy() {
        return copyExtras(new LaunchCapsuleAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<LaunchCapsuleAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<LaunchCapsuleAttack<?>>, LaunchCapsuleAttack<?>> buildCodec(RecordCodecBuilder.Instance<LaunchCapsuleAttack<?>> instance) {
            return baseDefault(instance, LaunchCapsuleAttack::new);
        }
    }
}
