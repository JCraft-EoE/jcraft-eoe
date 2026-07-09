package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.DashData;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class StandBlockPacket {
    private static final Set<ServerPlayer> blocking = Collections.newSetFromMap(new WeakHashMap<>());

    public static FriendlyByteBuf write(boolean isBlocking) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(isBlocking);
        return buf;
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        MinecraftServer server = context.getPlayer().getServer();

        boolean blockDown = buf.readBoolean();
        server.execute(() -> {
            if (blockDown) {
                blocking.add(player);
            } else {
                blocking.remove(player);
            }

            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand == null) {
                return;
            }

            boolean blocking = stand.wantToBlock;
            if (!blocking && blockDown) {
                if (allowBlockingWith(player.getMainHandItem()) && allowBlockingWith(player.getOffhandItem())) {
                    stand.wantToBlock = true;
                    if (stand.canAttack() && !DashData.isDashing(player)) {
                        stand.tryBlock();
                    }
                }
            } else if (blocking && !blockDown) {
                stand.wantToBlock = false;
            }
        });
    }

    private static boolean allowBlockingWith(ItemStack itemStack) {
        if (itemStack.is(JItemRegistry.ANUBIS.get()) || itemStack.is(JItemRegistry.ANUBIS_SHEATHED.get()) || itemStack.getItem() instanceof ShieldItem) {
            return true;
        }
        return itemStack.getUseAnimation() == UseAnim.NONE;
    }

    public static boolean isBlocking(ServerPlayer player) {
        return blocking.contains(player);
    }
}
