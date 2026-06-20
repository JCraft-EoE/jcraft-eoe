package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.BlockProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class LaunchAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<LaunchAttack<A>, A> {
    public LaunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                        final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NotNull MoveType<LaunchAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        final LivingEntity baseEntity = attacker.getBaseEntity();

        final BlockProjectile block = new BlockProjectile(baseEntity.level());
        final BlockState steppingState = baseEntity.getBlockStateOn();
        if (steppingState.isAir() || !steppingState.canOcclude()) {
            block.setBlockStack(Items.STONE.getDefaultInstance());
        } else {
            block.setBlockStack(steppingState.getBlock().asItem().getDefaultInstance());
        }

        final Vec3i hoverDir = GravityChangerAPI.getGravityDirection(user).getNormal().multiply(-1);

        block.setMaster(user);
        block.moveTo(baseEntity.getX() + hoverDir.getX() * 1.5, baseEntity.getY() + hoverDir.getY() * 1.5,
                baseEntity.getZ() + hoverDir.getZ() * 1.5, baseEntity.getYRot(), baseEntity.getXRot());
        block.setDeltaMovement(hoverDir.getX() * 0.4, hoverDir.getY() * 0.4, hoverDir.getZ() * 0.4);
        baseEntity.level().addFreshEntity(block);

        return targets;
    }

    @Override
    protected @NonNull LaunchAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LaunchAttack<A> copy() {
        return copyExtras(new LaunchAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<LaunchAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<LaunchAttack<?>>, LaunchAttack<?>> buildCodec(
                final RecordCodecBuilder.Instance<LaunchAttack<?>> instance) {
            return attackDefault(instance, LaunchAttack::new);
        }
    }
}
