package net.arna.jcraft.common.util;

import lombok.Builder;
import lombok.Data;
import lombok.With;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@With
@Data
@Builder(builderClassName = "Builder")
public class JExplosionModifier {
    private final Predicate<Entity> hurtFilter;
    private final Boolean createFire; // Has to be nullable to indicate no change.
    private final Explosion.BlockInteraction blockInteraction;
    private final SimpleParticleType particle;
    private final Vec3 particleVelocity;
    private final SoundEvent sound;
    private final SoundSource soundCategory;
    private final Function<RandomSource, Float> volumeGetter, pitchGetter;

    public void write(FriendlyByteBuf buf, RandomSource random) {
        write(blockInteraction, buf, (b, dt) -> b.writeVarInt(dt.ordinal()));
        write(particle, buf, (b, p) -> b.writeResourceKey(BuiltInRegistries.PARTICLE_TYPE.getResourceKey(p).orElseThrow()));
        write(particleVelocity, buf, (b, v) -> {
            b.writeDouble(v.x);
            b.writeDouble(v.y);
            b.writeDouble(v.z);
        });
        write(sound, buf, (b, s) -> b.writeResourceKey(BuiltInRegistries.SOUND_EVENT.getResourceKey(sound).orElseThrow()));
        write(soundCategory, buf, (b, c) -> b.writeVarInt(c.ordinal()));
        write(volumeGetter, buf, (b, v) -> b.writeFloat(v.apply(random)));
        write(pitchGetter, buf, (b, p) -> b.writeFloat(p.apply(random)));
    }

    private <T> void write(T t, FriendlyByteBuf buf, BiConsumer<FriendlyByteBuf, T> writer) {
        boolean nonNull = t != null;
        buf.writeBoolean(nonNull);
        if (nonNull) {
            writer.accept(buf, t);
        }
    }

    public static JExplosionModifier read(FriendlyByteBuf buf) {
        JExplosionModifier.Builder builder = JExplosionModifier.builder()
                .blockInteraction(read(buf, b -> Explosion.BlockInteraction.values()[b.readVarInt()]))
                .particle(read(buf, b -> (SimpleParticleType) BuiltInRegistries.PARTICLE_TYPE.get(b.readResourceKey(Registries.PARTICLE_TYPE))))
                .particleVelocity(read(buf, b -> new Vec3(b.readDouble(), b.readDouble(), b.readDouble())))
                .sound(read(buf, b -> BuiltInRegistries.SOUND_EVENT.get(b.readResourceKey(Registries.SOUND_EVENT))))
                .soundCategory(read(buf, b -> SoundSource.values()[b.readVarInt()]));
        if (buf.readBoolean()) {
            builder.volume(buf.readFloat());
        }
        if (buf.readBoolean()) {
            builder.pitch(buf.readFloat());
        }

        return builder.build();
    }

    private static <T> T read(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
        boolean nonNull = buf.readBoolean();
        return nonNull ? reader.apply(buf) : null;
    }

    public static class Builder {

        public JExplosionModifier.Builder noDamage() {
            return hurtFilter(e -> false);
        }

        public JExplosionModifier.Builder volume(float volume) {
            return volumeGetter(random -> volume);
        }

        public JExplosionModifier.Builder pitch(float pitch) {
            return pitchGetter(random -> pitch);
        }
    }
}
