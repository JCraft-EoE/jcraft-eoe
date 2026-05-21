package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.arna.jcraft.common.effects.FlammableEffect;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.events.EntityTickEvent;
import net.arna.jcraft.common.events.JEntityEvents;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.mixin_logic.EntityAddon;
import net.arna.jcraft.mixin_logic.EntityMixinLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
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

    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
    private void jcraft$preventExtinguish(int ticks, CallbackInfo ci) {
        if (ticks <= 0 && (Object) this instanceof LivingEntity living && FlammableEffect.isFlammable(living) && living.isOnFire()) {
            ci.cancel();
        }
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
        if (CreamEntity.isCreaming((Entity) (Object) this))
            cir.setReturnValue(true);
    }

    @SuppressWarnings("ConstantValue")
    @ModifyExpressionValue(method = "isInvulnerableTo", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;invulnerable:Z", opcode = Opcodes.GETFIELD))
    private boolean invulnerableIfCreaming(boolean original) {
        return original || CreamEntity.isCreaming((Entity) (Object) this);
    }

    @Inject(method = "checkInsideBlocks", at = @At("HEAD"), cancellable = true)
    private void jcraft$ignoreBlockPushingIfCreaming(CallbackInfo ci) {
        if (CreamEntity.isCreaming((Entity) (Object) this))
            ci.cancel();
    }

    @Inject(method = "setRemoved", at = @At("RETURN"))
    private void jcraft$fireRemovedEvent(Entity.RemovalReason removalReason, CallbackInfo ci) {
        JEntityEvents.REMOVE.invoker().remove((Entity) (Object) this, removalReason);
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
