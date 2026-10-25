package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

public final class BubbleAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<BubbleAttack<A>, A> {
    private WeakReference<BubbleProjectile> bubbleProjectile;

    public BubbleAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<BubbleAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void tick(final A attacker) {
        if (attacker.hasUser())
            tickBubble(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final BubbleProjectile bubbleProjectile = new BubbleProjectile(attacker.getBaseEntity().level(), user);
        bubbleProjectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        bubbleProjectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0, 0.5f, 0f);
        bubbleProjectile.setPos(attacker.getBaseEntity().position().add(0, 1.25, 0));
        attacker.getBaseEntity().level().addFreshEntity(bubbleProjectile);
        this.bubbleProjectile = new WeakReference<>(bubbleProjectile);

        JComponentPlatformUtils.getBombTracker(user).getMainBomb().setBomb(bubbleProjectile);

        return Set.of();
    }

    public void tickBubble(final A stand) {
        final BubbleProjectile bubbleProjectile = this.bubbleProjectile == null ? null : this.bubbleProjectile.get();
        if (bubbleProjectile != null && !bubbleProjectile.isInGround() && stand.hasUser()) {
            bubbleProjectile.setDeltaMovement(stand.getUserOrThrow().getLookAngle().scale(0.5));
            bubbleProjectile.hurtMarked = true;
        }
    }

    @Override
    protected @NonNull BubbleAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BubbleAttack<A> copy() {
        return copyExtras(new BubbleAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<BubbleAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BubbleAttack<?>>, BubbleAttack<?>> buildCodec(RecordCodecBuilder.Instance<BubbleAttack<?>> instance) {
            return baseDefault(instance, BubbleAttack::new);
        }
    }
}
