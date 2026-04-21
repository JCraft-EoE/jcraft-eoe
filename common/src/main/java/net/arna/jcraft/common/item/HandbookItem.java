package net.arna.jcraft.common.item;

import net.arna.jcraft.platform.JPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HandbookItem extends Item {

    public HandbookItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand usedHand) {
        final ItemStack item = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            return InteractionResultHolder.fail(item);
        }
        JPlatformUtils.callMainMenu((ServerPlayer)player);
        return InteractionResultHolder.success(item);
    }
}
