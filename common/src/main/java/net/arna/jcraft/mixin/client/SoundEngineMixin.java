package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    // Makes the user of a stand hear the sounds their stand hears if it's remote.
    // It does so by manipulating the sound's position to be the same distance on each axis
    // the sound is from the player, if the sound is closer to the stand than it is to the player.

    // Same expression in two different methods
    @ModifyExpressionValue(method = {"play", "tickNonPaused"}, at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 overrideSoundPositionOnPlay(Vec3 ogSoundPos) {
        // Check if the camera entity has a stand and whether it's remote.
        Minecraft client = Minecraft.getInstance();
        Entity cam = client.getCameraEntity();
        StandEntity<?, ?> camStand = cam instanceof LivingEntity le ? JUtils.getStand(le) : null;

        // Camera entity has no stand, it's not remote, or it can't hear, don't modify sound pos.
        if (camStand == null || !camStand.isRemote() || camStand.getType().is(JTagRegistry.CANT_HEAR))
            return ogSoundPos;

        // If the sound is closer to the stand than it is to the player,
        // we need to modify the sound position such that dx, dy and dz between the player and the sound
        // becomes the same as between the stand and the sound.

        double camDist = cam.position().distanceToSqr(ogSoundPos);
        double standDist = camStand.position().distanceToSqr(ogSoundPos);

        // Camera is closer to sound than stand, use original position.
        if (camDist <= standDist) return ogSoundPos;

        // Stand is closer to sound than camera, use stand position.
        Vec3 delta = ogSoundPos.subtract(camStand.position());
        return cam.position().add(delta);
    }
}
