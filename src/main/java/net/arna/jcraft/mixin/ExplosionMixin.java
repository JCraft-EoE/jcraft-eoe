package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.IJExplosion;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class ExplosionMixin implements IJExplosion {
    @Shadow @Final
    private boolean createFire;
    @Shadow @Final
    private Explosion.DestructionType destructionType;
    @Shadow @Final private World world;
    private @Unique JExplosionModifier modifier;

    // Interface implementation
    @Override
    public void jcraft$setModifier(JExplosionModifier modifier) {
        this.modifier = modifier;
    }

    // Functionality
    @Redirect(method = "affectWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion;destructionType:Lnet/minecraft/world/explosion/Explosion$DestructionType;"), require = 2)
    private Explosion.DestructionType overrideDestructionType(Explosion thiz) {
        return modifier == null || modifier.getDestructionType() == null ? destructionType : modifier.getDestructionType();
    }

    @Redirect(method = "affectWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion;createFire:Z"))
    private boolean overrideCreateFire(Explosion thiz) {
        return modifier == null || modifier.getCreateFire() == null ? createFire : modifier.getCreateFire();
    }

    @ModifyVariable(method = "affectWorld", at = @At("HEAD"), argsOnly = true)
    private boolean overrideParticlesArgument(boolean particles) {
        return particles || modifier != null && modifier.getParticle() != null;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), require = 2)
    private ParticleEffect overrideParticleEffect(ParticleEffect particle) {
        return modifier == null || modifier.getParticle() == null ? particle : modifier.getParticle();
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            require = 2, index = 4)
    private double overrideParticleVelocityX(double x) {
        return modifier == null || modifier.getParticleVelocity() == null ? x : modifier.getParticleVelocity().x;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            require = 2, index = 5)
    private double overrideParticleVelocityY(double y) {
        return modifier == null || modifier.getParticleVelocity() == null ? y : modifier.getParticleVelocity().y;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            require = 2, index = 6)
    private double overrideParticleVelocityZ(double z) {
        return modifier == null || modifier.getParticleVelocity() == null ? z : modifier.getParticleVelocity().z;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private SoundEvent overrideSound(SoundEvent sound) {
        return modifier == null || modifier.getSound() == null ? sound : modifier.getSound();
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private SoundCategory overrideSoundCategory(SoundCategory category) {
        return modifier == null || modifier.getSoundCategory() == null ? category : modifier.getSoundCategory();
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 5)
    private float overrideVolume(float volume) {
        return modifier == null || modifier.getVolumeGetter() == null ? volume : modifier.getVolumeGetter().apply(world.random);
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 6)
    private float overridePitch(float pitch) {
        return modifier == null || modifier.getPitchGetter() == null ? pitch : modifier.getPitchGetter().apply(world.random);
    }
}
