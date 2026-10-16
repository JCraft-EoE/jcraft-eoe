package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class GoldenRatioPacket {

    public static FriendlyByteBuf write(float score) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeFloat(score);
        return buf;
    }

    public static void handle(final FriendlyByteBuf friendlyByteBuf, final NetworkManager.PacketContext context) {
        final Player player = context.getPlayer();
        final float score = friendlyByteBuf.readFloat();
        final CommonSpinComponent spin = JComponentPlatformUtils.getSpin(player);
        if (score <= 35f) {
            spin.goldenRatioA(3);
        }
        else if (score <= 38f) {
            spin.goldenRatioB(2);
        }
        else if (score <= 45f) {
            spin.goldenRatioC(1);
        }
    }

}
