package net.arna.jcraft.mixin.client;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the (protected) {@code state()} of a composite render type so the afterimage fade can recover the part's
 * texture. The enclosing {@code CompositeRenderType} is package-private, hence the string target.
 */
@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface RenderTypeStateAccessor {
    @Invoker("state")
    RenderType.CompositeState jcraft$callState();
}
