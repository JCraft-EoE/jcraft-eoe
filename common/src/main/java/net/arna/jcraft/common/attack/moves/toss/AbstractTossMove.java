package net.arna.jcraft.common.attack.moves.toss;

import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractTossMove<T extends AbstractTossMove<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {

    public AbstractTossMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (attacker.getBaseEntity().level().isClientSide()) return Set.of();
        final ItemStack itemStack = attacker.getBaseEntity().getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) return Set.of();
        final Entity thrown = tossItem(attacker, itemStack, user);
        onTossed(attacker, user, thrown);
        return Set.of();
    }

    protected void onTossed(A attacker, LivingEntity user, @Nullable Entity thrown) {}

    private @NonNull Vec3 computeThrowPos(@NonNull LivingEntity user) {
        final Vec3 lookAngle = user.getLookAngle();
        return new Vec3(
                user.getX() + lookAngle.x * 0.3,
                user.getY() + user.getBbHeight() * 0.5,
                user.getZ() + lookAngle.z * 0.3
        );
    }

    protected @Nullable Entity tossItem(A attacker, @NonNull ItemStack itemStack, @NonNull LivingEntity user) {
        return JUtils.tossItem(attacker.getBaseEntity(), attacker.getBaseEntity().level(), itemStack, getChargeTime() / 40f, true, computeThrowPos(user));
    }
}
