package net.arna.jcraft.common.attack.moves.kingcrimson;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.BloodProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import java.util.Set;

public final class BloodThrowAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<BloodThrowAttack<A>, A> {
    public BloodThrowAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<BloodThrowAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        attacker.getUserOrThrow().hurt(baseEntity.level().damageSources().magic(), 0.1f); // User throws their blood, dealing a bit of damage.
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final BloodProjectile bloodProjectile = new BloodProjectile(baseEntity.level(), user);
        bloodProjectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        bloodProjectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0, user.isShiftKeyDown() ? 1.33F : 0.66F, 0);
        bloodProjectile.setPos(baseEntity.getEyePosition());
        baseEntity.level().addFreshEntity(bloodProjectile);

        return Set.of();
    }

    @Override
    protected @NonNull BloodThrowAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BloodThrowAttack<A> copy() {
        return copyExtras(new BloodThrowAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<BloodThrowAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BloodThrowAttack<?>>, BloodThrowAttack<?>> buildCodec(RecordCodecBuilder.Instance<BloodThrowAttack<?>> instance) {
            return baseDefault(instance, BloodThrowAttack::new);
        }
    }
}
