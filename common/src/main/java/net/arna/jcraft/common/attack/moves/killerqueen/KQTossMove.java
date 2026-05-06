package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * KQ's throw move. Throws the main hand item as a projectile with KQ's main bomb planted on it.
 * The bomb only detonates when the player manually triggers detonation.
 */
public final class KQTossMove extends AbstractMove<KQTossMove, AbstractKillerQueenEntity<?, ?>> {

    @Getter
    private final float velocityMultiplier;

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
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

        final Vec3 lookAngle = user.getLookAngle();
        final Vec3 throwPos = new Vec3(
                user.getX() + lookAngle.x * 0.3,
                user.getY() + user.getBbHeight() * 0.5,
                user.getZ() + lookAngle.z * 0.3
        );
        final Entity thrown = JUtils.tossItem(attacker, attacker.level(), itemStack, getChargeTime() / 40f, true, throwPos);
        if (thrown != null) {
            JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(thrown);
        }
        attacker.playSound(JSoundRegistry.TOSS.get());

        return Set.of();
    }

    @Override
    protected @NonNull KQTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull KQTossMove copy() {
        return copyExtras(new KQTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier()));
    }

    public static class Type extends AbstractMove.Type<KQTossMove> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<KQTossMove, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(KQTossMove::getVelocityMultiplier);
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQTossMove>, KQTossMove> buildCodec(final RecordCodecBuilder.Instance<KQTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier()).apply(instance, KQTossMove::new);
        }
    }
}
