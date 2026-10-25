package net.arna.jcraft.common.attack.moves.silverchariot;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.minecraft.world.entity.LivingEntity;

public final class RayDartAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<RayDartAttack<A>, A> {
    public RayDartAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                         final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
        mobilityType = MobilityType.DASH;
    }

    @Override
    public @NonNull MoveType<RayDartAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        final LivingEntity user = attacker.getUser();
        if (user != null && user.onGround()) {
            user.setDeltaMovement(user.getDeltaMovement().add(getRotVec(attacker).scale(1)));
            user.hurtMarked = true;
        }
    }

    @Override
    protected @NonNull RayDartAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull RayDartAttack<A> copy() {
        return copyExtras(new RayDartAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<RayDartAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RayDartAttack<?>>, RayDartAttack<?>> buildCodec(RecordCodecBuilder.Instance<RayDartAttack<?>> instance) {
            return attackDefault(instance, RayDartAttack::new);
        }
    }
}
