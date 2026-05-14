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
                          final SpriteSet spriteProvider) {
        super(clientLevel, x, y, z, vx, vy, vz);
        pickSprite(spriteProvider);
    }

    private BreathParticle applyOptions(BreathParticleOptions options) {
        quadSize = options.scale();
        return this;
    }

    @Override
    protected int getLightColor(float tint) {
        return 255;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return JParticleTextureSheet.OVERLAP_SHEET;
    }

    public static class Factory implements ParticleProvider<BreathParticleOptions> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(final @NotNull BreathParticleOptions parameters, final @NotNull ClientLevel world,
                                       final double x, final double y, final double z, final double velocityX,
                                       final double velocityY, final double velocityZ) {
            return new BreathParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider)
                    .applyOptions(parameters);
        }
    }

    public static class BreathParticleType extends ParticleType<BreathParticleOptions> {
        public static BreathParticleType INSTANCE = new BreathParticleType();

        private BreathParticleType() {
            super(true, BreathParticleOptions.Deserializer.INSTANCE);
        }

        @Override
        public @NotNull Codec<BreathParticleOptions> codec() {
            return BreathParticleOptions.CODEC;
        }
    }

    public record BreathParticleOptions(float scale) implements ParticleOptions {
            public static Codec<BreathParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(Codec.FLOAT.fieldOf("scale").forGetter(BreathParticleOptions::scale))
                            .apply(instance, BreathParticleOptions::new));

        @Override
        public @NotNull ParticleType<?> getType() {
            return BreathParticleType.INSTANCE;
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeFloat(scale);
        }

        @Override
        public @NotNull String writeToString() {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " ";
        }

        @SuppressWarnings("deprecation") // No clue why it's deprecated, this class is required.
        public static class Deserializer implements ParticleOptions.Deserializer<BreathParticleOptions> {
            public static final Deserializer INSTANCE = new Deserializer();

            private Deserializer() {}

            @Override
            public @NotNull BreathParticleOptions fromCommand(@NotNull ParticleType<BreathParticleOptions> particleType,
                                                              @NotNull StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float scale = reader.readFloat();
                return new BreathParticleOptions(scale);
            }

            @Override
            public @NotNull BreathParticleOptions fromNetwork(@NotNull ParticleType<BreathParticleOptions> particleType,
                                                              @NotNull FriendlyByteBuf buffer) {
                float scale = buffer.readFloat();
                return new BreathParticleOptions(scale);
            }
        }
    }
}
