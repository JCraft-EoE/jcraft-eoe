package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * KQ's throw move. Throws the main hand item as an ItemEntity with KQ's main bomb planted on it.
 * The item does not damage on impact—the bomb only detonates when the player triggers detonation.
 */
public final class KQTossMove extends AbstractMove<KQTossMove, AbstractKillerQueenEntity<?, ?>> {
    private WeakReference<ItemEntity> thrownEntity = new WeakReference<>(null);

    public KQTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
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
        final Vec3 lookVec = lookAngle.scale(0.75f * (getChargeTime() / 40f));
        // Offset item forward in throw direction (from elbow to hand position)
        final Vec3 throwPos = new Vec3(
                user.getX() + lookAngle.x * 0.3,
                user.getY() + user.getBbHeight() * 0.5,
                user.getZ() + lookAngle.z * 0.3
        );
        final ItemEntity thrown = new ItemEntity(attacker.level(), throwPos.x, throwPos.y, throwPos.z,
                itemStack.copyWithCount(1), lookVec.x, lookVec.y, lookVec.z);
        thrown.setNeverPickUp();

        attacker.level().addFreshEntity(thrown);
        JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(thrown);
        attacker.playSound(JSoundRegistry.TOSS.get());
        itemStack.shrink(1);
        thrownEntity = new WeakReference<>(thrown);

        return Set.of();
    }

    @Override
    protected @NonNull KQTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull KQTossMove copy() {
        return copyExtras(new KQTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<KQTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQTossMove>, KQTossMove> buildCodec(final RecordCodecBuilder.Instance<KQTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance()).apply(instance, KQTossMove::new);
        }
    }
}
