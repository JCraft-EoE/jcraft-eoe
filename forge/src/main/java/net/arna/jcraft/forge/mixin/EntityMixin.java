package net.arna.jcraft.forge.mixin;

import net.arna.jcraft.mixin_logic.EntityMixinLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class)
public abstract class EntityMixin {

    @Inject(method = "lambda$changeDimension$16", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;restoreFrom(Lnet/minecraft/world/entity/Entity;)V"))
    private void doNotPlayDesummonSoundWhenMovingWorld(ServerLevel arg, PortalInfo portalinfo, Boolean spawnPortal, CallbackInfoReturnable<Entity> cir) {
        EntityMixinLogic.jcraft$doNotPlayDesummonSoundWhenMovingWorld((Entity) (Object) this);
    }
}
