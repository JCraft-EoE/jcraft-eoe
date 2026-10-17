package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.registry.JDimensionRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeItemMixin {

    @WrapMethod(method = "useOn")
    private InteractionResult jcraft$noAuEndPortal(final UseOnContext context, final Operation<InteractionResult> original) {
        if (context != null && context.getLevel().dimension() == JDimensionRegistry.AU_DIMENSION_KEY) {
            return InteractionResult.PASS;
        }
        return original.call(context);
    }

}
