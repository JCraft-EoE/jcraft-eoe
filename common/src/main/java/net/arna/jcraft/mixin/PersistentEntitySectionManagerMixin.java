package net.arna.jcraft.mixin;

import net.arna.jcraft.common.events.JEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PersistentEntitySectionManager.class, priority = Integer.MAX_VALUE)
public abstract class PersistentEntitySectionManagerMixin<T extends EntityAccess> {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void addEntity(T entityAccess, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && entityAccess instanceof Entity entity) {
            JEntityEvents.POST_ADD.invoker().add(entity, worldGenSpawned);
        }
    }

}
