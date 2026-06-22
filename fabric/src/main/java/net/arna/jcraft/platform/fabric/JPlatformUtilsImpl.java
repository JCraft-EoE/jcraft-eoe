package net.arna.jcraft.platform.fabric;

import lombok.NonNull;
import net.arna.jcraft.client.rendering.api.MultiInstancePostProcessor;
import net.arna.jcraft.client.rendering.post.TimestopShaderFX;
import net.arna.jcraft.common.menu.MainMenu;
import net.arna.jcraft.fabric.client.JShaderRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class JPlatformUtilsImpl {

    public static MultiInstancePostProcessor<TimestopShaderFX> getZaWarudo(){
        return JShaderRegistry.ZA_WARUDO;
    }

    public static ShaderInstance getTest() {
        return JShaderRegistry.TEST.getInstance().get();
    }

    public static ShaderInstance getRred() {
        return JShaderRegistry.RREDE.getInstance().get();
    }

    public static void callMainMenu(final ServerPlayer player) {
        var factory = new ExtendedScreenHandlerFactory() {

            @Override
            public AbstractContainerMenu createMenu(final int i, final @NonNull Inventory inventory, final @NonNull Player player) {
                return new MainMenu(i, player);
            }

            @Override
            @NonNull
            public Component getDisplayName() {
                return Component.literal("Hey");
            }

            @Override
            public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {

            }
        };
        player.openMenu(factory);
    }

}
