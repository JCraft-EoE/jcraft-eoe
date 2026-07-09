package net.arna.jcraft.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MoshParticle extends TextureSheetParticle {
    private static final int VARIANTS = 5, FRAMES = 4, TOTAL_SPRITES = VARIANTS * FRAMES;
    protected final SpriteSet spriteProvider;
    private final Entity parent;
    private final int variant;

    MoshParticle(final ClientLevel level, final double x, final double y, final double z,
                 final SpriteSet spriteProvider, final Options options) {
        super(level, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;
        this.parent = level.getEntity(options.entityId());
        variant = random.nextInt(VARIANTS);
        this.alpha = 1.00f;
        this.quadSize = 0.25f + random.nextFloat() * 0.25f;
        this.lifetime = 10 + random.nextInt(8);

        final Vector3f color = options.color();
        final float brightness = 0.65f + random.nextFloat() * 0.35f;
        final float maxComp = Math.max(color.x(), Math.max(color.y(), color.z()));
        if (maxComp > 0.01f) {
            final float scale = brightness / maxComp;
            this.setColor(color.x() * scale, color.y() * scale, color.z() * scale);
        } else {
            this.setColor(brightness, brightness, brightness);
        }

        this.xd = 0;
        this.yd = 0.005;
        this.zd = 0;
        this.gravity = 0;
        updateSprite();
    }

    private void tryMatchParent() {
        if (parent != null) {
            Vec3 deltaPos = JUtils.deltaPos(parent);
            setParticleSpeed(deltaPos.x, deltaPos.y + 0.005, deltaPos.z);
        }
    }

    @Override
    public void tick() {
        if (parent == null || parent.isRemoved()) {
            remove();
            return;
        }

        updateSprite();
        tryMatchParent();
        super.tick();
    }

    private void updateSprite() {
        if (age == lifetime) return;

        // Doing a bit of a cheat here: passing sprite count - 1 as the lifetime
        // makes the age parameter a sprite index.
        // This way, we can achieve 5 animated variants with one particle type.

        int offset = variant * FRAMES;
        int frame = age * FRAMES / lifetime;

        setSprite(spriteProvider.get(frame + offset, TOTAL_SPRITES - 1));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<Options> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(final @NotNull Options options, final @NotNull ClientLevel world,
                                       final double x, final double y, final double z,
                                       final double vx, final double vy, final double vz) {
            var out = new MoshParticle(world, x, y, z, this.spriteProvider, options);
            out.tryMatchParent();
            return out;
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

    public record Options(int entityId, Vector3f color) implements ParticleOptions {
        public static Codec<Options> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("entity_id").forGetter(Options::entityId),
                ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(Options::color)
        ).apply(instance, Options::new));

        @Override
        public @NotNull ParticleType<?> getType() {
            return Type.INSTANCE;
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf buf) {
            buf.writeFloat(entityId);
        }

        @Override
        public @NotNull String writeToString() {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(getType()) + " " + entityId + " " + color.x() + " " + color.y() + " " + color.z();
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
                reader.expect(' ');
                float r = reader.readFloat();
                reader.expect(' ');
                float g = reader.readFloat();
                reader.expect(' ');
                float b = reader.readFloat();
                Vector3f color = new Vector3f(r, g, b);

                return new Options(entityId, color);
            }

            @Override
            public @NotNull Options fromNetwork(@NotNull ParticleType<Options> particleType,
                                                @NotNull FriendlyByteBuf buffer) {
                int entityId = buffer.readInt();
                Vector3f color = buffer.readVector3f();
                return new Options(entityId, color);
            }
        }
    }
}