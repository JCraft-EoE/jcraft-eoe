package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.events.EntityTickEvent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.EntityAddon;
import net.arna.jcraft.mixin_logic.EntityMixinLogic;
import net.arna.jcraft.mixin_logic.Jangler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class)
public abstract class EntityMixin implements EntityAddon, Jangler {
    @Shadow
    public int tickCount;
    private @Unique boolean fromSpawner = false;
    private @Unique int lastJangle = 0;

    /**
     * Stand positioning mixin function
     *
     * @param passenger stand entity
     */
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$updatePassengerPosition(Entity passenger, Entity.MoveFunction positionUpdater, CallbackInfo info) {
        EntityMixinLogic.jcraft$updatePassengerPosition((Entity)(Object)this, passenger, positionUpdater, info);
    }

    /**
     * Disables sprinting particles during time erase
     */
    @SuppressWarnings("ConstantValue")
    @Inject(method = "canSpawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void jcraft$shouldSpawnSprintingParticles(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity living && JUtils.getStand(living) instanceof KingCrimsonEntity kc && kc.getTETime() > 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        EntityTickEvent.ENTITY_PRE.invoker().tick((Entity) (Object) this);
    }

    @Override
    public boolean jcraft$setFromSpawner() {
        return fromSpawner = true;
    }

    @Override
    public boolean jcraft$isFromSpawner() {
        return fromSpawner;
    }

    @Override
    public int jcraft$getLastJangleAge() {
        return lastJangle;
    }

    @Override
    public void jcraft$markJangle() {
        lastJangle = tickCount;
    }
}
