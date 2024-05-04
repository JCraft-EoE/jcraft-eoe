package net.arna.jcraft.common.screenhandler;

import net.arna.jcraft.registry.JScreenHandlerTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

public class MenuScreenHandler extends ScreenHandler {



    public MenuScreenHandler(int syncId, PacketByteBuf buf) {
        super(JScreenHandlerTypeRegistry.MENU_SCREEN_HANDLER, syncId);

    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
