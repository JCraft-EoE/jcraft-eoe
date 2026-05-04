package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class TossMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<TossMove<A>, A> {

    public TossMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<TossMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (attacker instanceof StandEntity<?,?> stand && !stand.level().isClientSide()) {
            final ItemStack projectile = stand.getItemInHand(InteractionHand.MAIN_HAND);
            if (!projectile.isEmpty()) {
                final Vec3 lookAngle = user.getLookAngle();
                final Vec3 lookVec = lookAngle.scale(0.75f * (getChargeTime() / 40f));
                final Vec3 throwPos = new Vec3(
                        user.getX() + lookAngle.x,
                        user.getY() + user.getBbHeight(),
                        user.getZ() + lookAngle.z
                ); //TODO: fix item position
                final ItemEntity thrown = new ItemEntity(stand.level(), throwPos.x, throwPos.y, throwPos.z,
                        projectile.copyWithCount(1), lookVec.x, lookVec.y, lookVec.z);
                thrown.setNeverPickUp();
                stand.level().addFreshEntity(thrown);
                projectile.shrink(1);
            }
        }
        return Set.of();
    }

    @Override
    protected @NonNull TossMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TossMove<A> copy() {
        return copyExtras(new TossMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<TossMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TossMove<?>>, TossMove<?>> buildCodec(RecordCodecBuilder.Instance<TossMove<?>> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance()).apply(instance, TossMove::new);
        }
    }
}
