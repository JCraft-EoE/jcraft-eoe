package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.Set;

public class SpinBarrageAttack extends AbstractBarrageAttack<SpinBarrageAttack, SilverChariotEntity> {
    public SpinBarrageAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public void onInitiate(SilverChariotEntity attacker) {
        LivingEntity user = attacker.getUser();
        if (user != null) {
            ItemStack mainHand = user.getMainHandStack(), offHand = user.getOffHandStack();
            if (mainHand.isOf(JObjectRegistry.ANUBIS)) {
                attacker.setStackInHand(Hand.OFF_HAND, mainHand.copy());
                mainHand.decrement(1);
                return;
            }
            if (offHand.isOf(JObjectRegistry.ANUBIS)) {
                attacker.setStackInHand(Hand.OFF_HAND, offHand.copy());
                offHand.decrement(1);
                return;
            }
        }
        super.onInitiate(attacker);
    }

    @Override
    public void onCancel(SilverChariotEntity attacker) {
        giveBack(attacker);
        super.onCancel(attacker);
    }

    @Override
    public void tick(SilverChariotEntity attacker) {
        if (attacker.getMoveStun() == 1)
            giveBack(attacker);
        super.tick(attacker);
    }

    private void giveBack(SilverChariotEntity attacker) {
        LivingEntity user = attacker.getUser();
        ItemStack itemStack = attacker.getOffHandStack();
        if (user != null && itemStack != null && !itemStack.isEmpty()) {
            if (user instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.giveItemStack(itemStack);
            else
                user.setStackInHand(Hand.MAIN_HAND, itemStack);
            itemStack.decrement(1);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        return super.perform(attacker, user, ctx);
    }

    @Override
    protected @NonNull SpinBarrageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SpinBarrageAttack copy() {
        return copyExtras(new SpinBarrageAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
