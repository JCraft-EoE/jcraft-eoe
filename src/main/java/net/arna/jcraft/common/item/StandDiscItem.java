package net.arna.jcraft.common.item;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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
        int itemStandID = 0;
        int userStandID = 0;
        NbtCompound data = itemStack.getOrCreateNbt();
        NbtCompound userData = ((IEntityDataSaver) user).getPersistentData();

        if (userData.contains("StandID"))
            userStandID = userData.getInt("StandID");
        if (data.contains("StandID"))
            itemStandID = data.getInt("StandID");

        userData.putInt("StandID", itemStandID);
        data.putInt("StandID", userStandID);

        return TypedActionResult.success(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound data = stack.getNbt();
        if (data == null || !data.contains("StandID")) return;

        StandType type = StandType.fromId(data.getInt("StandID"));
        if (type == null) return;
        tooltip.add(type.getNameText().copy().styled(s -> s.withColor(type.isEvolution() ? Formatting.LIGHT_PURPLE : Formatting.GRAY)));
        
        // TODO add skin name to tooltip rather than just a number.
        int skin = getSkin(stack);
        tooltip.add(Text.literal("Skin " + skin).styled(s -> s.withColor(SKIN_LEVEL_COLORS[skin])));
    }
    
    public static boolean isEmptyDisc(ItemStack stack) {
        return stack.getNbt() == null || !stack.getNbt().contains("StandID", NbtElement.INT_TYPE);
    }
    
    public static void setSkin(ItemStack stack, int skin) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("Skin", skin);
    }
    
    public static int getSkin(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null || !nbt.contains("Skin", NbtElement.INT_TYPE) ? 0 : nbt.getInt("Skin");
    }
}
