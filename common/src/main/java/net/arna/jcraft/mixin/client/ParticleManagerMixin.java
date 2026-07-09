package net.arna.jcraft.mixin.client;

import com.google.common.collect.ImmutableList;
import net.arna.jcraft.client.particle.JParticleTextureSheet;
import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin {
    @Shadow
    @Mutable
    @Final
    private static List<ParticleRenderType> RENDER_ORDER;
    @Shadow
    protected ClientLevel level;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addSheets(CallbackInfo ci) {
        List<ParticleRenderType> sheets = new ArrayList<>(RENDER_ORDER); // Create mutable copy
        sheets.addAll(JParticleTextureSheet.J_SHEETS);
        RENDER_ORDER = ImmutableList.copyOf(sheets);
    }

    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    void jcraft$tickParticle(Particle particle, CallbackInfo info) {
        ParticleAccessor particleAccessor = (ParticleAccessor) particle;

        Vec3 particlePos = new Vec3(particleAccessor.getX(), particleAccessor.getY(), particleAccessor.getZ());
        if (JClientUtils.isInTSRange(particlePos)) {
            particleAccessor.setPrevX(particleAccessor.getX());
            particleAccessor.setPrevY(particleAccessor.getY());
            particleAccessor.setPrevZ(particleAccessor.getZ());
            info.cancel();
        }
    }
}
