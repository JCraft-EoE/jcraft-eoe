package net.arna.jcraft.mixin;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private @Unique boolean hadStand = false;

    @Inject(method = "moveToWorld", at = @At("HEAD"))
    private void saveStandStateBeforeWorldMove(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        hadStand = JUtils.getStand((ServerPlayerEntity) (Object) this) != null;
    }

    // Inject at the end of the if-block
    @Inject(method = "moveToWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;syncedFoodLevel:I", shift = At.Shift.AFTER))
    private void resummonStandAfterWorldMove(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (!hadStand) return;
        StandEntity<?, ?> stand = JCraft.summon(destination, (ServerPlayerEntity) (Object) this);
        if (stand != null) stand.setPlaySummonSound(false);
    }

    @Inject(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;" +
            "removePlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity$RemovalReason;)V"))
    private void doNotPlayDesummonSoundWhenMovingWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        StandEntity<?, ?> stand = JUtils.getStand((ServerPlayerEntity) (Object) this);
        if (stand == null) return;

        stand.setPlayDesummonSound(false);
    }
}
