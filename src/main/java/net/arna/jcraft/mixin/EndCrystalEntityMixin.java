package net.arna.jcraft.mixin;

import net.arna.jcraft.common.config.JServerConfig;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EndCrystalEntity.class)
public class EndCrystalEntityMixin {
    @ModifyArg(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"), index = 4)
    private float modifyExplosionPower(float d) {
        if (JServerConfig.REDUCE_DEADLY_EXPLOSIONS.getValue())
            return 1.5f;
        return 6.0f;
    }
}