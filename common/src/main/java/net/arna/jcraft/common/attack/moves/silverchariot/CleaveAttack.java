package net.arna.jcraft.common.attack.moves.silverchariot;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.attack.moves.AbstractChargeAttack;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.world.entity.LivingEntity;

public final class CleaveAttack<A extends StandEntity<? extends A, ?>> extends AbstractSimpleAttack<CleaveAttack<A>, A> {
    public CleaveAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                        final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NonNull MoveType<CleaveAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        final LivingEntity user = attacker.getUserOrThrow();
        AbstractChargeAttack.prepDetachmentMove(attacker, user);
        attacker.setFreePos((user.position().add(attacker.getUserOrThrow().getLookAngle().scale(1.5)).toVector3f()));
        attacker.setFree(true);
    }

    @Override
    protected @NonNull CleaveAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CleaveAttack<A> copy() {
        return copyExtras(new CleaveAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<CleaveAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CleaveAttack<?>>, CleaveAttack<?>> buildCodec(RecordCodecBuilder.Instance<CleaveAttack<?>> instance) {
            return attackDefault(instance, CleaveAttack::new);
        }
    }
}
