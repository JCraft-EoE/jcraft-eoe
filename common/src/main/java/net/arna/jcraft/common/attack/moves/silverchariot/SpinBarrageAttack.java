package net.arna.jcraft.common.attack.moves.silverchariot;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.Set;

public final class SpinBarrageAttack extends AbstractBarrageAttack<SpinBarrageAttack, SilverChariotEntity> {
    public SpinBarrageAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                             final float hitboxSize, final float knockback, final float offset, final int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NonNull MoveType<SpinBarrageAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(final SilverChariotEntity attacker) {
        final LivingEntity user = attacker.getUser();
        if (user != null) {
            final ItemStack mainHand = user.getMainHandItem(), offHand = user.getOffhandItem();
            if (mainHand.is(JItemRegistry.ANUBIS.get())) {
                attacker.setItemInHand(InteractionHand.OFF_HAND, mainHand.copy());
                mainHand.shrink(1);
                return;
            }
            if (offHand.is(JItemRegistry.ANUBIS.get())) {
                attacker.setItemInHand(InteractionHand.OFF_HAND, offHand.copy());
                offHand.shrink(1);
                return;
            }
        }
        super.onInitiate(attacker);
    }

    @Override
    public void onCancel(final SilverChariotEntity attacker) {
        super.onCancel(attacker);

        giveBack(attacker);
    }

    @Override
    public void activeTick(final SilverChariotEntity attacker, final int moveStun) {
        if (moveStun == 1) {
            giveBack(attacker);
        }
        super.activeTick(attacker, moveStun);
    }

    private void giveBack(final SilverChariotEntity attacker) {
        final LivingEntity user = attacker.getUser();
        final ItemStack itemStack = attacker.getOffhandItem();
        if (user != null && !itemStack.isEmpty()) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.addItem(itemStack);
            } else {
                user.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
            }
            itemStack.shrink(1);
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SilverChariotEntity attacker, final LivingEntity user) {
        return super.perform(attacker, user);
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

    public static class Type extends AbstractBarrageAttack.Type<SpinBarrageAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SpinBarrageAttack>, SpinBarrageAttack> buildCodec(RecordCodecBuilder.Instance<SpinBarrageAttack> instance) {
            return barrageDefault(instance, SpinBarrageAttack::new);
        }
    }
}
