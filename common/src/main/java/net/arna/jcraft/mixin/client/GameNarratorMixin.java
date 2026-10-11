package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.client.JClientConfig;
import net.minecraft.client.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameNarrator.class, priority = 2000)
public abstract class GameNarratorMixin {

    @WrapMethod(method = "isActive")
    public boolean jcraft$disableNarrator(final Operation<Boolean> original) {
        final JClientConfig config = JClientConfig.getInstance();
        if (config != null && config.isDisableNarrator()) {
            return false;
        }
        return original.call();
    }

}
