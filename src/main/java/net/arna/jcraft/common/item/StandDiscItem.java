package net.arna.jcraft.common.item;

import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.arna.jcraft.JCraft.standNames;

public class StandDiscItem extends Item {
    public StandDiscItem(Settings settings) {
        super(settings);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.pass(itemStack);
        }
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
        if (data == null) {
            return;
        }
        if (data.contains("StandID")) {
            int standID = data.getInt("StandID");
            if (standNames.containsKey(standID)) {
                tooltip.add(standNames.get(standID));
            }
        }
    }
}
