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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lock-on indicator particle. Behaves like {@link BreathParticle} in that it renders to the
 * {@link JParticleTextureSheet#OVERLAP_SHEET} so it draws on top of everything, but instead of rising
 * it stays bound to an entity, rendering on top of it.
 */
public class LockOnParticle extends TextureSheetParticle {
    private final SpriteSet spriteProvider;
    private final Entity parent;

    LockOnParticle(final ClientLevel level, final double x, final double y, final double z,
                   final SpriteSet spriteProvider, final Options options) {
        super(level, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;
        this.parent = level.getEntity(options.entityId());
        this.alpha = 1f;
        this.quadSize = 1f;
        this.lifetime = 9;
        this.gravity = 0;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        followParent();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setSpriteFromAge(spriteProvider);
    }

    private void followParent() {
        if (parent == null) return;
        // Remember the previous position so rendering interpolates smoothly while the entity moves.
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setPos(parent.getX(), parent.getY(), parent.getZ());
    }

    @Override
    public void tick() {
        if (parent == null || parent.isRemoved()) {
            remove();
            return;
        }

        if (this.age++ >= this.lifetime) {
            remove();
            return;
        }

        followParent();
        this.setSpriteFromAge(spriteProvider);
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
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(final @NotNull Options options, final @NotNull ClientLevel world,
                                       final double x, final double y, final double z,
                                       final double vx, final double vy, final double vz) {
            return new LockOnParticle(world, x, y, z, this.spriteProvider, options);
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

    public record Options(int entityId) implements ParticleOptions {
        public static Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("entity_id").forGetter(Options::entityId)
        ).apply(instance, Options::new));

        @Override
        public @NotNull ParticleType<?> getType() {
            return Type.INSTANCE;
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeVarInt(entityId);
        }

        @Override
        public @NotNull String writeToString() {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " " + entityId;
        }

        @SuppressWarnings("deprecation") // No clue why it's deprecated, this class is required.
        public static class Deserializer implements ParticleOptions.Deserializer<Options> {
            public static final Deserializer INSTANCE = new Deserializer();

            private Deserializer() {}

            @Override
            public @NotNull Options fromCommand(@NotNull ParticleType<Options> particleType,
                                                @NotNull StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                int entityId = reader.readInt();
                return new Options(entityId);
            }

            @Override
            public @NotNull Options fromNetwork(@NotNull ParticleType<Options> particleType,
                                                @NotNull FriendlyByteBuf buffer) {
                int entityId = buffer.readVarInt();
                return new Options(entityId);
            }
        }
    }
}
