package net.arna.jcraft.common.item;

import net.arna.jcraft.api.attack.moves.BlockMarkerMove;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.common.marker.BlockMarkerMoves;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class RewindMockItem extends MockItem {

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand usedHand) {
        final ItemStack mockItem = player.getItemInHand(usedHand);
        if (!(mockItem.getItem() instanceof RewindMockItem)) { // should never happen
            return InteractionResultHolder.fail(mockItem);
        }
        final ItemStack resolvedItem = resolveMockStack(mockItem);
        if (resolvedItem == mockItem) {
            return InteractionResultHolder.fail(mockItem);
        }
        player.setItemInHand(usedHand, resolvedItem);
        return InteractionResultHolder.success(resolvedItem);
    }

    public static ItemStack createMockStack(ItemStack stack, BlockMarkerMove move) {
        // No need to create a mock stack if it already is one
        if (stack.getItem() instanceof RewindMockItem) {
            return stack;
        }

        ItemStack mockStack = MockItem.createMockStack(stack, JItemRegistry.REWIND_MOCK_ITEM);
        CompoundTag nbt = mockStack.getOrCreateTag();
        nbt.putUUID("RewindUuid", move.getUuid());
        nbt.putInt("RewindRun", move.getIteration().size());
        return mockStack;
    }

    public static ItemStack resolveMockStack(ItemStack stack) {
        if (!(stack.getItem() instanceof RewindMockItem)) {
            return stack;
        }
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains("RewindUuid") || !nbt.contains("RewindRun")) {
            return ItemStack.EMPTY;
        }
        UUID uuid = nbt.getUUID("RewindUuid");
        int run = nbt.getInt("RewindRun");
        Optional<BlockMarkerMove> move = BlockMarkerMoves.findFirst(m -> uuid.equals(m.getUuid()));
        // if in doubt, delete
        if (move.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // if the move is still running, return the stack
        if (move.get().getIteration().size() == run) {
            return stack;
        }
        // if the rewind was successful executed
        if (move.get().getIteration().get(run)) {
            return ItemStack.EMPTY;
        }
        // otherwise rewind didn't happen, make item real
        return MockItem.getMockedStack(stack);
    }

}
