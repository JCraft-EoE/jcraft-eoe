package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StandDiscItem extends Item {
    private static final TextColor[] SKIN_LEVEL_COLORS = {
            TextColor.fromFormatting(Formatting.GRAY),
            TextColor.fromFormatting(Formatting.RED),
            TextColor.fromFormatting(Formatting.BLUE),
            TextColor.fromFormatting(Formatting.LIGHT_PURPLE)
    };
    private static final Text DEFAULT_SKIN = Text.literal("Default");

    public StandDiscItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.pass(itemStack);

        if (user.getDamageTracker().wasRecentlyAttacked()) {
            user.sendMessage(Text.translatable("jcraft.disc.error"));
            return TypedActionResult.fail(itemStack);
        }

        // 1s usage cooldown to prevent overuse
        user.getItemCooldownManager().set(this, 20);

        // Get NBT and swap stands
        StandType itemStand = null;
        int itemSkin = 0;
        StandType userStand = null;
        int userSkin = 0;

        NbtCompound data = itemStack.getOrCreateNbt();
        StandComponent standData = JComponents.getStandData(user);

        userStand = standData.getType();
        userSkin = standData.getSkin();
        if (data.contains("StandID", NbtElement.INT_TYPE)) itemStand = StandType.fromId(data.getInt("StandID"));
        if (data.contains("Skin", NbtElement.INT_TYPE)) itemSkin = data.getInt("Skin");

        standData.setTypeAndSkin(itemStand, itemSkin);
        data.putInt("StandID", userStand == null ? 0 : userStand.getId());
        data.putInt("Skin", userSkin);

        StandEntity<?, ?> stand = standData.getStand();
        if (stand != null) stand.discard();
        JCraft.summon(world, user);

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        StandType type = getStandType(stack);
        if (type == null) {
            tooltip.add(Text.literal("Empty").styled(s -> s.withFormatting(Formatting.GRAY)));
            return;
        }

        tooltip.add(type.getNameText().copy().styled(s -> s.withColor(type.isEvolution() ? Formatting.LIGHT_PURPLE : Formatting.GRAY)));

        int skin = getSkin(stack);
        tooltip.add((skin == 0 || skin > type.getSkinCount() ? DEFAULT_SKIN : type.getSkinNames().get(skin - 1)).copy()
                .styled(s -> s.withColor(SKIN_LEVEL_COLORS[skin])));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        appendStacks(group, (List<ItemStack>) stacks);
    }

    public static void appendStacks(ItemGroup group, List<ItemStack> stacks) {
        boolean full = group == ItemGroup.SEARCH;
        if (!full && group != JCraft.JCRAFT_GROUP) return;

        stacks.add(new ItemStack(JObjectRegistry.STAND_DISC));

        for (StandType standType : StandType.values())
            for (int skin = 0; skin <= (full ? standType.getSkinCount() : 0); skin++)
                stacks.add(StandDiscItem.createDiscStack(standType, skin));
    }

    public static ItemStack createDiscStack(StandType type, int skin) {
        if (skin < 0 || skin > type.getSkinCount()) throw new IllegalArgumentException("Skin out of bounds");

        ItemStack stack = new ItemStack(JObjectRegistry.STAND_DISC);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("StandID", type.getId());
        nbt.putInt("Skin", skin);

        return stack;
    }

    public static boolean isEmptyDisc(ItemStack stack) {
        return stack.getNbt() == null || !stack.getNbt().contains("StandID", NbtElement.INT_TYPE);
    }

    public static StandType getStandType(ItemStack stack) {
        if (!stack.isOf(JObjectRegistry.STAND_DISC)) return null;

        NbtCompound nbt = stack.getNbt();
        return nbt == null || !nbt.contains("StandID", NbtElement.INT_TYPE) ? null : StandType.fromId(nbt.getInt("StandID"));
    }

    public static void setSkin(ItemStack stack, int skin) {
        if (!stack.isOf(JObjectRegistry.STAND_DISC)) return;

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("Skin", skin);
    }

    public static int getSkin(ItemStack stack) {
        if (!stack.isOf(JObjectRegistry.STAND_DISC)) return 0;

        NbtCompound nbt = stack.getNbt();
        return nbt == null || !nbt.contains("Skin", NbtElement.INT_TYPE) ? 0 : nbt.getInt("Skin");
    }
}
