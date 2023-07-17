package net.arna.jcraft.mixin;

import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
    
    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamageable()Z", ordinal = 1))
    private boolean allowMasks(ItemStack stack) {
        return stack.getItem() == JObjectRegistry.CINDERELLA_MASK || stack.isDamageable();
    }
}
