package net.arna.jcraft.common.attack.moves.cream;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class ConsumeAttack extends AbstractSimpleAttack<ConsumeAttack, CreamEntity> {
    public ConsumeAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                         final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<ConsumeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final CreamEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        attacker.setVoidTime(120);
        attacker.setCharging(false);
        attacker.setCurrentMove(null);

        return targets;
    }

    @Override
    protected @NonNull ConsumeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ConsumeAttack copy() {
        return copyExtras(new ConsumeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<ConsumeAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<ConsumeAttack>, ConsumeAttack> buildCodec(RecordCodecBuilder.Instance<ConsumeAttack> instance) {
            return attackDefault(instance, ConsumeAttack::new);
        }
    }
}
