package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Redirect(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasPassengers()Z"))
    private boolean ignoreStandsWhenEnteringPortal(Entity entity) {
        return entity.getPassengerList().stream().anyMatch(e -> !(e instanceof StandEntity<?,?>));
    }
}
