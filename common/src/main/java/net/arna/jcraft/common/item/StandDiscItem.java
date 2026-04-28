package net.arna.jcraft.common.item;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StandDiscItem extends Item {
    private static final TextColor[] SKIN_LEVEL_COLORS = {
            TextColor.fromLegacyFormat(ChatFormatting.GRAY),
            TextColor.fromLegacyFormat(ChatFormatting.RED),
            TextColor.fromLegacyFormat(ChatFormatting.BLUE),
            TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE)
    };
    private static final Component DEFAULT_SKIN = Component.literal("Default");

    public StandDiscItem(Properties settings) {
        super(settings);
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(Level world, Player user, @NonNull InteractionHand hand) {
        final ItemStack itemStack = user.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (JCraft.wasRecentlyAttacked(user.getCombatTracker())) {
            user.displayClientMessage(Component.translatable("jcraft.disc.combat"), true);
            return InteractionResultHolder.fail(itemStack);
        }

        // Get NBT
        StandType itemStand;
        int itemSkin = 0;

        final CompoundTag data = itemStack.getOrCreateTag();
        itemStand = StandTypeUtil.readFromNBT(data, "StandID");
        if (data.contains("Skin", Tag.TAG_INT)) {
            itemSkin = data.getInt("Skin");
        }

        if (itemStand != null && JCraft.getExclusiveStandsData().isStandUsed(itemStand)) {
            user.displayClientMessage(Component.translatable("jcraft.disc.stand_in_use"), true);
            return InteractionResultHolder.fail(itemStack);
        }

        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(user);
        final StandType userStand = standData.getType();
        final int userSkin = standData.getSkin();

        if (itemStand == userStand && (itemStand == null || itemSkin == userSkin)) {
            if (itemStand != null) {
                user.displayClientMessage(Component.translatable("jcraft.disc.same_stand"), true);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        standData.setTypeAndSkin(itemStand, itemSkin, false);
        if (userStand == null) {
            data.remove("StandID");
            data.remove("Skin");
        } else {
            data.putString("StandID", userStand.getId().toString());
            data.putInt("Skin", userSkin);
        }

        final StandEntity<?, ?> stand = standData.getStand();

        if (stand != null) {
            stand.desummon(); // Cleanup

            if (!stand.isRemoved()) {
                stand.discard(); // Forced removal in case desummon() didn't go through
            }

            standData.setStand(null);
        }

        JCraft.summon(world, user);

        // 1s usage cooldown to prevent overuse
        user.getCooldowns().addCooldown(this, 20);

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context) {
        StandType type = getStandType(stack);
        if (type == null) {
            tooltip.add(Component.literal("Empty").withStyle(s -> s.applyFormat(ChatFormatting.GRAY)));
            return;
        }

        StandData data = type.getData();
        tooltip.add(data.getInfo().getName().copy().withStyle(s -> s.withColor(data.isEvolution() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY)));

        int skin = getSkin(stack);
        tooltip.add((skin == 0 || skin >= data.getInfo().getSkinCount() ? DEFAULT_SKIN : data.getInfo().getSkinNames().get(skin - 1)).copy()
                .withStyle(s -> s.withColor(SKIN_LEVEL_COLORS[skin])));
    }

    public static ItemStack createDiscStack(StandType type, int skin) {
        if (skin < 0 || skin >= type.getData().getInfo().getSkinCount()) {
            throw new IndexOutOfBoundsException("Skin out of bounds");
        }

        ItemStack stack = new ItemStack(JItemRegistry.STAND_DISC.get());
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putString("StandID", type.getId().toString());
        nbt.putInt("Skin", skin);

        return stack;
    }

    public static boolean isEmptyDisc(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().contains("StandID", Tag.TAG_INT);
    }

    public static StandType getStandType(ItemStack stack) {
        if (!stack.is(JItemRegistry.STAND_DISC.get())) {
            return null;
        }

        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("StandID")) {
            return null;
        }

        return StandTypeUtil.readFromNBT(nbt, "StandID");
    }

    public static void setSkin(ItemStack stack, int skin) {
        if (!stack.is(JItemRegistry.STAND_DISC.get())) {
            return;
        }

        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt("Skin", skin);
    }

    public static int getSkin(ItemStack stack) {
        if (!stack.is(JItemRegistry.STAND_DISC.get())) {
            return 0;
        }

        CompoundTag nbt = stack.getTag();
        return nbt == null || !nbt.contains("Skin", Tag.TAG_INT) ? 0 : nbt.getInt("Skin");
    }
}
