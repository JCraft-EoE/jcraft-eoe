package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.events.EntityTickEvent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.EntityAddon;
import net.arna.jcraft.mixin_logic.EntityMixinLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class)
public abstract class EntityMixin implements EntityAddon {

    @Unique
    private boolean fromSpawner = false;

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

    @Inject(method = "isInvulnerable", at = @At("HEAD"), cancellable = true)
    private void invulnerableIfCreaming(CallbackInfoReturnable<Boolean> cir) {
        if (jcraft$isCreaming())
            cir.setReturnValue(true);
    }

    @ModifyExpressionValue(method = "isInvulnerableTo", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;invulnerable:Z"))
    private boolean invulnerableIfCreaming(boolean original) {
        return original || jcraft$isCreaming();
    }

    private @Unique boolean jcraft$isCreaming() {
        // Mark user invulnerable if they're using Cream and are voiding.
        Entity thiz = (Entity) (Object) this;
        return thiz instanceof LivingEntity le &&
                JUtils.getStand(le) instanceof CreamEntity cream &&
                cream.getVoidTime() > 0;
    }

    @Override
    public boolean jcraft$setFromSpawner() {
        return fromSpawner = true;
    }

    @Override
    public boolean jcraft$isFromSpawner() {
        return fromSpawner;
    }
}
