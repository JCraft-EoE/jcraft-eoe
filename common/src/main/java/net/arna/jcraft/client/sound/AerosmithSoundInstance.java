package net.arna.jcraft.client.sound;

import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class AerosmithSoundInstance extends AbstractTickableSoundInstance {
    public static final float FLYBY_DISTANCE = 10f;
    private final AerosmithEntity aerosmith;
    private final boolean isFlyby;

    public AerosmithSoundInstance(AerosmithEntity aerosmith) {
        this(aerosmith, false);
    }

    private AerosmithSoundInstance(AerosmithEntity aerosmith, boolean isFlyby) {
        super(isFlyby ? JSoundRegistry.AS_DIST.get() : JSoundRegistry.AS_IDLE.get(), getSoundSourceFor(aerosmith), aerosmith.getRandom());
        this.aerosmith = aerosmith;
        this.isFlyby = isFlyby;
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
        if (isStopped()) return;

        update();

        if (aerosmith.isRemoved() || !aerosmith.isAlive() || aerosmith.getUser() == null) {
            stop();
            return;
        }

        // Swap to flyby if we're remote and close enough.
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || volume == 0f) return;

        if ((player.distanceToSqr(aerosmith) < FLYBY_DISTANCE * FLYBY_DISTANCE) != isFlyby) {
            // Swapping from flyby sound to idle sound or vice versa.
            stop();
            client.getSoundManager().queueTickingSound(new AerosmithSoundInstance(aerosmith, !isFlyby));
            volume = 0f;
        }
    }

    private void update() {
        x = aerosmith.getX();
        y = aerosmith.getY();
        z = aerosmith.getZ();
    }
}
