package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.IJExplosion;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

@Mixin(Explosion.class)
public class ExplosionMixin implements IJExplosion {
    @Shadow @Final @Mutable
    private boolean createFire;
    @Shadow @Final @Mutable
    private Explosion.DestructionType destructionType;
    @Shadow @Final private World world;
    private @Unique ParticleEffect particle;
    private @Unique SoundEvent sound;
    private @Unique SoundCategory soundCategory;
    private @Unique Function<Random, Float> volumeGetter;
    private @Unique Function<Random, Float> pitchGetter;

    // Interface implementation
    @Override
    public void jcraft$setCreateFire(boolean createFire) {
        this.createFire = createFire;
    }

    @Override
    public boolean jcraft$isCreateFire() {
        return createFire;
    }

    @Override
    public void jcraft$setDestructionType(Explosion.DestructionType destructionType) {
        this.destructionType = destructionType;
    }

    @Override
    public Explosion.DestructionType jcraft$getDestructionType() {
        return destructionType;
    }

    @Override
    public void jcraft$setParticle(ParticleEffect particle) {
        this.particle = particle;
    }

    @Override
    public ParticleEffect jcraft$getParticle() {
        return particle;
    }

    @Override
    public void jcraft$setSound(SoundEvent sound) {
        this.sound = sound;
    }

    @Override
    public SoundEvent jcraft$getSound() {
        return sound;
    }

    @Override
    public void jcraft$setSoundCategory(SoundCategory category) {
        soundCategory = category;
    }

    @Override
    public SoundCategory jcraft$getSoundCategory() {
        return soundCategory;
    }

    @Override
    public void jcraft$setVolume(Function<Random, Float> volumeGetter) {
        this.volumeGetter = volumeGetter;
    }

    @Override
    public void jcraft$setPitch(Function<Random, Float> pitchGetter) {
        this.pitchGetter = pitchGetter;
    }

    // Functionality
    @ModifyVariable(method = "affectWorld", at = @At("HEAD"), argsOnly = true)
    private boolean overrideParticlesArgument(boolean particles) {
        return particles || particle != null;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), require = 2)
    private ParticleEffect overrideParticleEffect(ParticleEffect particle) {
        return this.particle == null ? particle : this.particle;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private SoundEvent overrideSound(SoundEvent sound) {
        return this.sound == null ? sound : this.sound;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"))
    private SoundCategory overrideSoundCategory(SoundCategory category) {
        return soundCategory == null ? category : soundCategory;
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 5)
    private float overrideVolume(float volume) {
        return volumeGetter == null ? volume : volumeGetter.apply(world.random);
    }

    @ModifyArg(method = "affectWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 6)
    private float overridePitch(float pitch) {
        return pitchGetter == null ? pitch : pitchGetter.apply(world.random);
    }
}
