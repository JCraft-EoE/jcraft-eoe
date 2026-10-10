package net.arna.jcraft.common.item;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.platform.JPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DiscCaseItem extends Item {

    @Getter
    private final int size;

    public DiscCaseItem(final @NonNull Properties properties, final int size) {
        super(properties.stacksTo(1));
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative!");
        }
        this.size = size;
    }

    @NonNull
    @Override
    public InteractionResultHolder<ItemStack> use(final @NonNull Level level, final @NonNull Player player, final @NonNull InteractionHand usedHand) {
        final ItemStack item = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            return InteractionResultHolder.fail(item);
        }
        JPlatformUtils.callDiscCaseMenu((ServerPlayer)player);
        return InteractionResultHolder.success(item);
    }

}
