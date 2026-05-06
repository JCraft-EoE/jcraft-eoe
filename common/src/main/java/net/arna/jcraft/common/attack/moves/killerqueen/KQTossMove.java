package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * KQ's throw move. Throws the main hand item as a projectile with KQ's main bomb planted on it.
 * The bomb only detonates when the player manually triggers detonation.
 */
public final class KQTossMove extends AbstractMove<KQTossMove, AbstractKillerQueenEntity<?, ?>> {

    @Getter
    private final float velocityMultiplier;

    @Getter
    private final float spreadMultiplier;

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        this(cooldown, windup, duration, moveDistance, velocityMultiplier, 1f);
    }

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
        this.spreadMultiplier = spreadMultiplier;
    }

    @Override
    public @NonNull MoveType<KQTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AbstractKillerQueenEntity<?, ?> attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        final ItemStack itemStack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) return Set.of();

        final Entity thrown = JUtils.tossItem(attacker, attacker.level(), itemStack, getChargeTime() * velocityMultiplier, spreadMultiplier, true);
        if (thrown != null) {
            JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(thrown);
        }
        return Set.of();
    }

    @Override
    protected @NonNull KQTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull KQTossMove copy() {
        return copyExtras(new KQTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractMove.Type<KQTossMove> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<KQTossMove, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(KQTossMove::getVelocityMultiplier);
        }

        protected RecordCodecBuilder<KQTossMove, Float> spreadMultiplier() {
            return Codec.FLOAT.fieldOf("spreadMultiplier").forGetter(KQTossMove::getSpreadMultiplier);
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQTossMove>, KQTossMove> buildCodec(final RecordCodecBuilder.Instance<KQTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, KQTossMove::new);
        }
    }
}
