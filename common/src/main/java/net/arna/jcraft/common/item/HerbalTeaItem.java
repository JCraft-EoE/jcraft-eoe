package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.stand.TuskAct1Entity;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
 * Herbal Tea — restores 1 nail per sip for Tusk users.
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
            // Add 1 nail to the active Tusk stand if the player has one
            var stand = JUtils.getStand(player);
            boolean addedNail = false;
            if (stand instanceof TuskAct1Entity tusk1) {
                tusk1.addNails(1.0f);
                addedNail = true;
            } else if (stand instanceof TuskAct2Entity tusk2) {
                tusk2.addNails(1.0f);
                addedNail = true;
            } else if (stand instanceof TuskAct3Entity tusk3) {
                tusk3.addNails(1.0f);
                addedNail = true;
            }

            if (addedNail) {
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
