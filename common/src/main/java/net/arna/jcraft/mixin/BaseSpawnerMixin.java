package net.arna.jcraft.mixin;

import net.arna.jcraft.mixin_logic.EntityAddon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {

    @Inject(method = "method_18085", at = @At("HEAD"))
    private static void jcraft$setMobFromSpawner(final double d, final double e, final double f, final Entity entity, final CallbackInfoReturnable<Entity> cir) {
        ((EntityAddon) entity).jcraft$setFromSpawner();
    }
}
