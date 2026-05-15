package net.arna.jcraft.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreathParticle extends RisingParticle {

    public BreathParticle(ClientLevel clientLevel, double x, double y, double z, double vx, double vy, double vz,
                          final SpriteSet spriteSet, final Options options) {
        super(clientLevel, x, y, z, vx, vy, vz);
        quadSize = options.scale();
        pickSprite(spriteSet);
    }

    @Override
    protected int getLightColor(float tint) {
        return 255;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return JParticleTextureSheet.OVERLAP_SHEET;
    }

    public static class Factory implements ParticleProvider<Options> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(final @NotNull BreathParticle.Options options, final @NotNull ClientLevel world,
                                       final double x, final double y, final double z, final double velocityX,
                                       final double velocityY, final double velocityZ) {
            return new BreathParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteSet, options);
        }
    }

    public static class Type extends ParticleType<Options> {
        public static Type INSTANCE = new Type();

        private Type() {
            super(true, Options.Deserializer.INSTANCE);
        }

        @Override
        public @NotNull Codec<Options> codec() {
            return Options.CODEC;
        }
    }

    public record Options(float scale) implements ParticleOptions {
            public static Codec<Options> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(Codec.FLOAT.fieldOf("scale").forGetter(Options::scale))
                            .apply(instance, Options::new));

        @Override
        public @NotNull ParticleType<?> getType() {
            return Type.INSTANCE;
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeFloat(scale);
        }

        @Override
        public @NotNull String writeToString() {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " " + scale;
        }

        @SuppressWarnings("deprecation") // No clue why it's deprecated, this class is required.
        public static class Deserializer implements ParticleOptions.Deserializer<Options> {
            public static final Deserializer INSTANCE = new Deserializer();

            private Deserializer() {}

            @Override
            public @NotNull BreathParticle.Options fromCommand(@NotNull ParticleType<Options> particleType,
                                                               @NotNull StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float scale = reader.readFloat();
                return new Options(scale);
            }

            @Override
            public @NotNull BreathParticle.Options fromNetwork(@NotNull ParticleType<Options> particleType,
                                                               @NotNull FriendlyByteBuf buffer) {
                float scale = buffer.readFloat();
                return new Options(scale);
            }
        }
    }
}
