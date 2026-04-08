package net.arna.jcraft.common.item;

import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Herbal Tea — single-use item, stacks to 16.
 * Grants 1.5x nail regen speed for 3 minutes.
 */
public class HerbalTeaItem extends Item {
    public static final int MAX_SIPS = 1; // kept for JCreativeMenuTabRegistry compatibility
    private static final int USE_DURATION = 32;

    public HerbalTeaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!(user instanceof Player player)) return stack;

        if (!world.isClientSide) {
            // Add 3 min of Keratin Growth, stacking up to 6 min
            int existing = player.hasEffect(JStatusRegistry.KERATIN_GROWTH.get())
                    ? player.getEffect(JStatusRegistry.KERATIN_GROWTH.get()).getDuration() : 0;
            int newDuration = Math.min(existing + 3600, 7200);
            player.addEffect(new MobEffectInstance(JStatusRegistry.KERATIN_GROWTH.get(), newDuration, 0, false, true));
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        user.gameEvent(GameEvent.DRINK);
        stack.shrink(1);
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("jcraft.herbal_tea.tooltip"));
    }
}
