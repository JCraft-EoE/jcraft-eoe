package net.arna.jcraft.common.attack.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.core.MoveAction;
import net.arna.jcraft.api.attack.core.MoveActionType;
import net.arna.jcraft.api.attack.core.RunMoment;
import net.arna.jcraft.common.util.JCodecUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.function.Supplier;

@Getter
public class PlaySoundAction extends MoveAction<PlaySoundAction, IAttacker<?, ?>> {
    private final Supplier<SoundEvent> sound;
    private final float minVol, maxVol, minPitch, maxPitch;
    private final boolean bind;

    private PlaySoundAction(final Supplier<SoundEvent> sound, final float minVol, final float maxVol,
                            final float minPitch, final float maxPitch, final boolean onImpact) {
        this.sound = sound;
        this.minVol = minVol;
        this.maxVol = maxVol;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.bind = !onImpact;

        if (onImpact)
            setRunMoment(RunMoment.ON_HIT);
    }

    public static PlaySoundAction playSound(SoundEvent sound) {
        return playSound(sound, 1.0F, 1.0F);
    }

    public static PlaySoundAction playSound(SoundEvent sound, float volume, float pitch) {
        return playSound(sound, volume, volume, pitch, pitch);
    }

    public static PlaySoundAction playSound(SoundEvent sound, float volMin, float volMax, float pitchMin, float pitchMax) {
        return new PlaySoundAction(() -> sound, volMin, volMax, pitchMin, pitchMax, false);
    }

    public static PlaySoundAction playSound(RegistrySupplier<SoundEvent> sound) {
        return playSound(sound, 1.0F, 1.0F);
    }

    public static PlaySoundAction playSound(RegistrySupplier<SoundEvent> sound, float volume, float pitch) {
        return playSound(sound, volume, volume, pitch, pitch);
    }

    public static PlaySoundAction playSound(RegistrySupplier<SoundEvent> sound, float volMin, float volMax, float pitchMin, float pitchMax) {
        return new PlaySoundAction(sound, volMin, volMax, pitchMin, pitchMax, false);
    }

    public static PlaySoundAction playImpactSound(SoundEvent sound) {
        return playImpactSound(sound, 1.0F, 1.0F);
    }

    public static PlaySoundAction playImpactSound(SoundEvent sound, float volume, float pitch) {
        return playImpactSound(sound, volume, volume, pitch, pitch);
    }

    public static PlaySoundAction playImpactSound(SoundEvent sound, float volMin, float volMax, float pitchMin, float pitchMax) {
        return new PlaySoundAction(() -> sound, volMin, volMax, pitchMin, pitchMax, true);
    }

    public static PlaySoundAction playImpactSound(RegistrySupplier<SoundEvent> sound) {
        return playImpactSound(sound, 1.0F, 1.0F);
    }

    public static PlaySoundAction playImpactSound(RegistrySupplier<SoundEvent> sound, float volume, float pitch) {
        return playImpactSound(sound, volume, volume, pitch, pitch);
    }

    public static PlaySoundAction playImpactSound(RegistrySupplier<SoundEvent> sound, float volMin, float volMax, float pitchMin, float pitchMax) {
        return new PlaySoundAction(sound, volMin, volMax, pitchMin, pitchMax, true);
    }

    /**
     * Only play the sound if an impact was landed. (I.e., some target was hit)
     * @return The action
     */
    public PlaySoundAction onImpact() {
        return new PlaySoundAction(sound, minVol, maxVol, minPitch, maxPitch, true);
    }

    private static float randomize(RandomSource random, float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    @Override
    public void perform(IAttacker<?, ?> attacker, LivingEntity user, Set<LivingEntity> targets) {
        float volume = randomize(attacker.getBaseEntity().getRandom(), minVol, maxVol);
        float pitch = randomize(attacker.getBaseEntity().getRandom(), minPitch, maxPitch);
        attacker.playAttackerSound(sound.get(), volume, pitch, bind);
    }

    @Override
    public @NonNull MoveActionType<PlaySoundAction> getType() {
        return Type.INSTANCE;
    }

    public static class Type extends MoveActionType<PlaySoundAction> {
        public static final Type INSTANCE = new Type();

        @Override
        public Codec<PlaySoundAction> getCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    runMoment(),
                    JCodecUtils.SOUND_EVENT_SUPPLIER_CODEC.fieldOf("sound").forGetter(PlaySoundAction::getSound),
                    Codec.FLOAT.optionalFieldOf("min_vol", 1f).forGetter(PlaySoundAction::getMinVol),
                    Codec.FLOAT.optionalFieldOf("max_vol", 1f).forGetter(PlaySoundAction::getMaxVol),
                    Codec.FLOAT.optionalFieldOf("min_pitch", 1f).forGetter(PlaySoundAction::getMinPitch),
                    Codec.FLOAT.optionalFieldOf("max_pitch", 1f).forGetter(PlaySoundAction::getMaxPitch)
            ).apply(instance, apply((sound, minVol, maxVol, minPitch, maxPitch) ->
                    new PlaySoundAction(sound, minVol, maxVol, minPitch, maxPitch, false))));
        }
    }
}
