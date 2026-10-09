package net.arna.jcraft.common.attack.moves.kingcrimson;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class KCDonutAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<KCDonutAttack<A>, A> {
    public KCDonutAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                         final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Vec3 pos = baseEntity.position().add(baseEntity.getLookAngle().scale(1.5));
        target.teleportToWithTicket(pos.x, target.getY(), pos.z);
    }

    @Override
    public @NonNull MoveType<KCDonutAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull KCDonutAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull KCDonutAttack<A> copy() {
        return copyExtras(new KCDonutAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<KCDonutAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KCDonutAttack<?>>, KCDonutAttack<?>> buildCodec(RecordCodecBuilder.Instance<KCDonutAttack<?>> instance) {
            return attackDefault(instance, KCDonutAttack::new);
        }
    }
}
