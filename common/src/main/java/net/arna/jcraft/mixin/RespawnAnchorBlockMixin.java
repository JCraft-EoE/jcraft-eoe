package net.arna.jcraft.mixin;

import net.arna.jcraft.common.config.JServerConfig;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin {

    @ModifyArg(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"
            )
    )
    private float modifyExplosionPower(float d) {
        if (JServerConfig.REDUCE_DEADLY_EXPLOSIONS.getValue()) {
            return d * 0.3f;
        }
        return d;
    }

}
