package net.arna.jcraft.common.util;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.explosion.Explosion;

import java.util.function.BiConsumer;
import java.util.function.Function;

@With
@Data
@Builder(builderClassName = "Builder")
public class JExplosionModifier {
    private final Boolean createFire; // Has to be nullable to indicate no change.
    private final Explosion.DestructionType destructionType;
    private final DefaultParticleType particle;
    private final Vec3d particleVelocity;
    private final SoundEvent sound;
    private final SoundCategory soundCategory;
    private final Function<Random, Float> volumeGetter, pitchGetter;

    public void write(PacketByteBuf buf, Random random) {
        write(createFire, buf, PacketByteBuf::writeBoolean);
        write(destructionType, buf, (b, dt) -> b.writeVarInt(dt.ordinal()));
        write(particle, buf, (b, p) -> b.writeRegistryKey(Registries.PARTICLE_TYPE.getKey(p).orElseThrow()));
        write(particleVelocity, buf, (b, v) -> {
            b.writeDouble(v.x);
            b.writeDouble(v.y);
            b.writeDouble(v.z);
        });
        write(sound, buf, (b, s) -> b.writeRegistryKey(Registries.SOUND_EVENT.getKey(sound).orElseThrow()));
        write(soundCategory, buf, (b, c) -> b.writeVarInt(c.ordinal()));
        write(volumeGetter, buf, (b, v) -> b.writeFloat(v.apply(random)));
        write(pitchGetter, buf, (b, p) -> b.writeFloat(p.apply(random)));
    }

    private <T> void write(T t, PacketByteBuf buf, BiConsumer<PacketByteBuf, T> writer) {
        boolean nonNull = t != null;
        buf.writeBoolean(nonNull);
        if (nonNull) writer.accept(buf, t);
    }

    public static JExplosionModifier read(PacketByteBuf buf) {
        Builder builder = JExplosionModifier.builder()
                .createFire(read(buf, PacketByteBuf::readBoolean))
                .destructionType(read(buf, b -> Explosion.DestructionType.values()[b.readVarInt()]))
                .particle(read(buf, b -> (DefaultParticleType) Registries.PARTICLE_TYPE.get(b.readRegistryKey(RegistryKeys.PARTICLE_TYPE))))
                .particleVelocity(read(buf, b -> new Vec3d(b.readDouble(), b.readDouble(), b.readDouble())))
                .sound(read(buf, b -> Registries.SOUND_EVENT.get(b.readRegistryKey(RegistryKeys.SOUND_EVENT))))
                .soundCategory(read(buf, b -> SoundCategory.values()[b.readVarInt()]));
        if (buf.readBoolean()) builder.volume(buf.readFloat());
        if (buf.readBoolean()) builder.pitch(buf.readFloat());

        return builder.build();
    }

    private static <T> T read(PacketByteBuf buf, Function<PacketByteBuf, T> reader) {
        boolean nonNull = buf.readBoolean();
        return nonNull ? reader.apply(buf) : null;
    }

    public static class Builder {

        public Builder volume(float volume) {
            volumeGetter(random -> volume);
            return this;
        }

        public Builder pitch(float pitch) {
            pitchGetter(random -> pitch);
            return this;
        }
    }
}
