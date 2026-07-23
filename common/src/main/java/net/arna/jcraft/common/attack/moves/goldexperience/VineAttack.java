package net.arna.jcraft.common.attack.moves.goldexperience;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.GEVinesEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VineAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<VineAttack<A>, A> {

    public VineAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                      final float damage, final int stun, final float hitboxSize, final float knockback,
                      final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NotNull MoveType<VineAttack<A>> getMoveType() {
        return VineAttack.Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final LivingEntity baseEntity = attacker.getBaseEntity();

        if (user.onGround() || !baseEntity.getFeetBlockState().isAir()) {
            final Vec3 direction = user.getLookAngle();

            final var vines = new GEVinesEntity(baseEntity.level(), user, direction.scale(0.67));

            final Direction gravity = GravityChangerAPI.getGravityDirection(baseEntity);
            GravityChangerAPI.setDefaultGravityDirection(vines, gravity);

            final Vec3 midPos = RotationUtil.vecPlayerToWorld(0.0, baseEntity.getBbHeight() * 0.45, 0.0, gravity);
            final double e = direction.x, f = direction.y, g = direction.z;
            final double l = direction.horizontalDistance();
            vines.moveTo(baseEntity.getX() + midPos.x, baseEntity.getY() + midPos.y, baseEntity.getZ() + midPos.z,
                    (float) (Mth.atan2(e, g) * 57.2957763671875),
                    (float) (Mth.atan2(f, l) * 57.2957763671875)
            );
            vines.setDeltaMovement(direction.scale(0.33));

            baseEntity.level().addFreshEntity(vines);

        }

        return targets;
    }

    @Override
    protected @NonNull VineAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull VineAttack<A> copy() {
        return copyExtras(new VineAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<VineAttack<?>> {
        public static final VineAttack.Type INSTANCE = new VineAttack.Type();
        
        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<VineAttack<?>>, VineAttack<?>>
        buildCodec(RecordCodecBuilder.Instance<VineAttack<?>> instance) {
            return attackDefault(instance, VineAttack::new);
        }
    }
}
