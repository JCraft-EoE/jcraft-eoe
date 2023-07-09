package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Shadow
    protected ClientWorld world;

    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    void jcraft$tickParticle(Particle particle, CallbackInfo info) {
        ParticleAccessor particleAccessor = (ParticleAccessor) particle;
        if (
                JClientUtils.isInTSRange(
                new Vec3d( particleAccessor.getX(), particleAccessor.getY(), particleAccessor.getZ() )
                )
        ) {
            particleAccessor.setPrevX(particleAccessor.getX());
            particleAccessor.setPrevY(particleAccessor.getY());
            particleAccessor.setPrevZ(particleAccessor.getZ());
            info.cancel();
        }
    }
}
