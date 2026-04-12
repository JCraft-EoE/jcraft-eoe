package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class FlamePunchAttack extends AbstractSimpleAttack<FlamePunchAttack, SpeedKingEntity> {

    public FlamePunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<FlamePunchAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void processTarget(SpeedKingEntity attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        target.setSecondsOnFire(3);
    }

    @Override
    protected @NonNull FlamePunchAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FlamePunchAttack copy() {
        return copyExtras(new FlamePunchAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<FlamePunchAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlamePunchAttack>, FlamePunchAttack> buildCodec(RecordCodecBuilder.Instance<FlamePunchAttack> instance) {
            return attackDefault(instance, FlamePunchAttack::new);
        }
    }
}