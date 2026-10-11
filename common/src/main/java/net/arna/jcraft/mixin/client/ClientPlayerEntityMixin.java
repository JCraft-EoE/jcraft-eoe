package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    @WrapMethod(method = "drop")
    private boolean jcraft$dropSelectedItem(final boolean fullStack, final Operation<Boolean> original) {
        if (JUtils.canAct((LocalPlayer)(Object)this)) {
            return original.call(fullStack);
        }
        return false;
    }

}
