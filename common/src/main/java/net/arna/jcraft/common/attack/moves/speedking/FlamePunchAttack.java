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
    /** Seconds the target burns after being hit */
    private final int fireDuration;

    /** Full constructor. */
    public FlamePunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final float damage, final int stun, final float hitboxSize, final float knockback,
                            final float offset, final int fireDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.fireDuration = fireDuration;
    }

    /** Legacy constructor used by the codec — delegates with default fireDuration. */
    public FlamePunchAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final float damage, final int stun, final float hitboxSize, final float knockback,
                            final float offset) {
        this(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, 3);
    }

    @Override
    public @NonNull MoveType<FlamePunchAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void processTarget(SpeedKingEntity attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        target.setSecondsOnFire(fireDuration);
    }

    @Override
    protected @NonNull FlamePunchAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FlamePunchAttack copy() {
        return copyExtras(new FlamePunchAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), fireDuration));
    }

    public static class Type extends AbstractSimpleAttack.Type<FlamePunchAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlamePunchAttack>, FlamePunchAttack> buildCodec(RecordCodecBuilder.Instance<FlamePunchAttack> instance) {
            return attackDefault(instance, FlamePunchAttack::new);
        }
    }
}
