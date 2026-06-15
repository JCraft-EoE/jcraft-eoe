package net.arna.jcraft.platform.forge;

import lombok.NonNull;
import net.arna.jcraft.client.rendering.api.MultiInstancePostProcessor;
import net.arna.jcraft.client.rendering.post.TimestopShaderFX;
import net.arna.jcraft.common.menu.DiscCaseMenu;
import net.arna.jcraft.forge.client.JShaderRegistry;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
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

    public static void callDiscCaseMenu(final ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Hey");
            }

            @Override
            public AbstractContainerMenu createMenu(final int i, final @NonNull Inventory inventory, final @NonNull Player player) {
                return new DiscCaseMenu(i, player);
            }
        });
    }
}
