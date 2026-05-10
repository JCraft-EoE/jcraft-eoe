package net.arna.jcraft.client.sound;

import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class AerosmithSoundInstance extends AbstractTickableSoundInstance {
    private final AerosmithEntity aerosmith;

    public AerosmithSoundInstance(SoundEvent soundEvent, AerosmithEntity aerosmith) {
        super(soundEvent, getSoundSourceFor(aerosmith), aerosmith.getRandom());
        this.aerosmith = aerosmith;
        looping = true;
        delay = 0;

        update();
    }

    private static SoundSource getSoundSourceFor(AerosmithEntity aerosmith) {
        LivingEntity user = aerosmith.getUser();
        return user instanceof Player ? SoundSource.PLAYERS :
                user instanceof Mob ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    @Override
    public void tick() {
        update();

        if (aerosmith.isRemoved() || !aerosmith.isAlive() || aerosmith.getUser() == null)
            stop();
    }

    private void update() {
        volume = aerosmith.isRemote() ? 0.2f : 1.0f;
        x = aerosmith.getX();
        y = aerosmith.getY();
        z = aerosmith.getZ();
    }
}
