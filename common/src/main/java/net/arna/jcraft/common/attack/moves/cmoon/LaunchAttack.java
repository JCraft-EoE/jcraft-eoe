package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function10;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.data.AttackMoveExtras;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Set;

public final class LaunchAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<LaunchAttack<A>, A> {
    @Getter
    int numProjectiles;

    public LaunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                        final float hitboxSize, final float knockback, final float offset, final int projectiles) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        numProjectiles = projectiles;

        ranged = true;
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    public @NotNull LaunchAttack<A> withProjectiles(int projectiles) {
        numProjectiles = projectiles;
        return getThis();
    }

    @Override
    public @NotNull MoveType<LaunchAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final BlockState steppingState = baseEntity.getBlockStateOn();
        final var gravity = GravityChangerAPI.getGravityDirection(user);
        final var upDir = gravity.step().negate();
        final var lookAngle = user.getLookAngle();

        for (int i = 0; i < numProjectiles; i++) {
            final var block = new BlockProjectile(user);
            final var launchDir = new Vector3f(upDir);

            if (numProjectiles > 1) {
                block.timeToLaunch += 10 * (numProjectiles - i);

                launchDir.add(
                        (float)lookAngle.x * i,
                        (float)lookAngle.y * i,
                        (float)lookAngle.z * i
                );
            }

            if (steppingState.isAir() || !steppingState.canOcclude()) {
                block.setBlockStack(Items.STONE.getDefaultInstance());
            } else {
                block.setBlockStack(steppingState.getBlock().asItem().getDefaultInstance());
            }

            block.moveTo(
                    baseEntity.getX() + launchDir.x * 1.5,
                    baseEntity.getY() + launchDir.y * 1.5,
                    baseEntity.getZ() + launchDir.z * 1.5,
                    baseEntity.getYRot(),
                    baseEntity.getXRot()
            );

            block.setDeltaMovement(launchDir.x * 0.4, launchDir.y * 0.4, launchDir.z * 0.4);

            baseEntity.level().addFreshEntity(block);
        }

        return targets;
    }

    @Override
    protected @NonNull LaunchAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LaunchAttack<A> copy() {
        return copyExtras(new LaunchAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), numProjectiles));
    }

    public static class Type extends AbstractSimpleAttack.Type<LaunchAttack<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<LaunchAttack<?>, Integer> numProjectiles() {
            return Codec.INT.fieldOf("numProjectiles").forGetter(LaunchAttack::getNumProjectiles);
        }

        protected Products.P12<
                RecordCodecBuilder.Mu<LaunchAttack<?>>,
                BaseMoveExtras, AttackMoveExtras, Integer, Integer, Integer, Float, Float, Integer, Float, Float, Float, Integer>
        launchDefault(RecordCodecBuilder.Instance<LaunchAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(), numProjectiles());
        }

        protected App<RecordCodecBuilder.Mu<LaunchAttack<?>>, LaunchAttack<?>>
        launchDefault(RecordCodecBuilder.Instance<LaunchAttack<?>> instance, Function10<Integer,
                Integer, Integer, Float, Float, Integer, Float, Float, Float, Integer, LaunchAttack<?>> function) {
            return launchDefault(instance).apply(instance, applyAttackExtras(function));
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<LaunchAttack<?>>, LaunchAttack<?>> buildCodec(
                final RecordCodecBuilder.Instance<LaunchAttack<?>> instance) {
            return launchDefault(instance, LaunchAttack::new);
        }
    }
}
