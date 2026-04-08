package net.arna.jcraft.common.item;

import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
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
 * Herbal Tea — grants 1.5x nail regen speed for 3 minutes per sip.
 * Each item holds up to 16 sips (stored in NBT "Sips").
 */
public class HerbalTeaItem extends Item {
    public static final int MAX_SIPS = 16;
    private static final int USE_DURATION = 32;

    public HerbalTeaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        int sips = getSips(stack);
        if (sips <= 0) return InteractionResultHolder.fail(stack);
        return ItemUtils.startUsingInstantly(world, user, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!(user instanceof Player player)) return stack;

        int sips = getSips(stack);
        if (sips <= 0) return stack;

        if (!world.isClientSide) {
            CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(player);
            if (misc != null) {
                // 3 minutes = 3600 ticks; stacks additively up to a cap
                misc.setHerbalTeaTicks(Math.min(misc.getHerbalTeaTicks() + 3600, 7200));
                setSips(stack, sips - 1);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }

        user.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putInt("Sips", MAX_SIPS);
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return getSips(stack) > 0 ? UseAnim.DRINK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        int sips = getSips(stack);
        tooltip.add(Component.literal(sips + "/" + MAX_SIPS + " sips").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("jcraft.herbal_tea.tooltip").withStyle(ChatFormatting.GRAY));
    }

    public static int getSips(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) return 0;
        return nbt.getInt("Sips");
    }

    public static void setSips(ItemStack stack, int sips) {
        stack.getOrCreateTag().putInt("Sips", Math.max(0, Math.min(MAX_SIPS, sips)));
    }
}
