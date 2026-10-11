package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.arna.jcraft.api.registry.JServerReloadListenerRegistry;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ReloadableServerResources.class, priority = 100)
public abstract class ReloadableServerResourcesMixin {

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    private List<PreparableReloadListener> addListeners(final List<PreparableReloadListener> original) {
        List<PreparableReloadListener> listeners = new ArrayList<>(original);
        JServerReloadListenerRegistry.register(listeners::add);
        return listeners;
    }

}
