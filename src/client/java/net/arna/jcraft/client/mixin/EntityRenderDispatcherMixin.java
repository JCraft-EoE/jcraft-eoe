package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void shouldNotRenderIfRidingInvisibleClone(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Entity e = entity;
        do {
            if (e instanceof PlayerCloneEntity clone && !JCraftClient.shouldRenderClone(clone)) {
                cir.cancel();
                return;
            }

            e = e.getVehicle();
        } while (e != null);
    }
}
