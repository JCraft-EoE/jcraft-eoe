package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonGunslingerComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class RangerHolsterMove extends AbstractMove<RangerHolsterMove, RangerSpec> {
    public RangerHolsterMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    private static boolean canHolster(final ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxStackSize() == 1;
    }

    @Override
    public boolean conditionsMet(final RangerSpec attacker) {
        final LivingEntity user = attacker.getUser();
        return super.conditionsMet(attacker) &&
                (JComponentPlatformUtils.getGunslinger(user).hasHolsteredItem() || canHolster(user.getMainHandItem()));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final RangerSpec attacker, final LivingEntity user) {
        final CommonGunslingerComponent gunslinger = JComponentPlatformUtils.getGunslinger(user);
        final ItemStack holstered = gunslinger.getHolsteredItem();
        final ItemStack held = user.getMainHandItem();

        if (holstered.isEmpty()) {
            gunslinger.setHolsteredItem(held.copy());
            user.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            attacker.playAttackerSound(JSoundRegistry.RANGER_REHOLSTER.get(), 1.0f, 1.0f, false, true);
        } else if (canHolster(held)) {
            gunslinger.setHolsteredItem(held.copy());
            user.setItemInHand(InteractionHand.MAIN_HAND, holstered);
            attacker.playAttackerSound(JSoundRegistry.RANGER_UNHOLSTER.get(), 1.0f, 1.0f, false, true);
        } else {
            if (!held.isEmpty()) {
                if (user.getOffhandItem().isEmpty()) {
                    user.setItemInHand(InteractionHand.OFF_HAND, held);
                } else if (user instanceof Player player) {
                    player.drop(held, false);
                } else {
                    user.spawnAtLocation(held);
                }
            }
            gunslinger.setHolsteredItem(ItemStack.EMPTY);
            user.setItemInHand(InteractionHand.MAIN_HAND, holstered);
            attacker.playAttackerSound(JSoundRegistry.RANGER_UNHOLSTER.get(), 1.0f, 1.0f, false, true);
        }

        return Set.of();
    }

    @Override
    public @NonNull MoveType<RangerHolsterMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RangerHolsterMove getThis() {
        return this;
    }

    @Override
    public @NonNull RangerHolsterMove copy() {
        return copyExtras(new RangerHolsterMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<RangerHolsterMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RangerHolsterMove>, RangerHolsterMove> buildCodec(RecordCodecBuilder.Instance<RangerHolsterMove> instance) {
            return baseDefault(instance, RangerHolsterMove::new);
        }
    }
}
