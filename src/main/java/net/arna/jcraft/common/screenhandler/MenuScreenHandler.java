package net.arna.jcraft.common.screenhandler;

import lombok.Getter;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.registry.JScreenHandlerTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class MenuScreenHandler extends ScreenHandler {

    @Getter
    protected final Text standName;

    public MenuScreenHandler(int syncId, PacketByteBuf buf) {
        super(JScreenHandlerTypeRegistry.MENU_SCREEN_HANDLER, syncId);
        if (buf != null && buf.readBoolean()) { // has stand id
            final int id = buf.readInt();
            standName = StandType.fromId(id).getNameText();
        }
        else {
            standName = Text.literal("No stand active");
        }
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
