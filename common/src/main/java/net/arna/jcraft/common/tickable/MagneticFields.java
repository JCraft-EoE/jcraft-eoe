package net.arna.jcraft.common.tickable;

import lombok.NonNull;
import net.arna.jcraft.common.entity.projectile.RazorProjectile;
import net.arna.jcraft.common.network.s2c.MagneticFieldParticlePacket;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MagneticFields {
    public static class MagneticField {
        public static final int TICKS_TO_LIVE = 60 * 20;

        public int time = TICKS_TO_LIVE;
        public Vec3 pos;
        private final double baseStrength, additiveStrength;
        private final ServerLevel level;
        private final Entity owner;

        MagneticField(ServerLevel level, Entity owner, double baseStrength, double peakStrength, Vec3 pos) {
            this.level = level;
            this.owner = owner;
            this.baseStrength = baseStrength;
            this.additiveStrength = peakStrength - baseStrength;
            this.pos = pos;
        }

        public double getStrength() {
            return baseStrength + additiveStrength * time / (double) TICKS_TO_LIVE;
        }

        private void tick() {
            for (Entity entity : level.getAllEntities()) {
                if (!JUtils.isFerrous(entity)) continue;
                if (entity == owner) continue;

                // Inverse cube root falloff when inside range
                final double strength = getStrength();
                final double distanceSqr = entity.distanceToSqr(pos);
                double attraction = (strength - Math.cbrt(distanceSqr)) / 35.0;

                if (entity instanceof RazorProjectile) attraction /= 2.0;
                // Hard linear cutoff outside range
                if (distanceSqr > strength * strength) attraction -= Math.sqrt(distanceSqr) / strength;

                if (attraction <= 0.0) continue;

                JUtils.addVelocity(entity, pos.subtract(entity.position()).normalize().scale(attraction));
            }

            time--;
        }
    }

    private static final List<MagneticField> fields = new ArrayList<>();

    public static void tick() {
        for (MagneticField field : fields) {
            field.tick();
            if (field.time % 2 == 0) continue;
            if (field.owner instanceof ServerPlayer serverPlayer) {
                MagneticFieldParticlePacket.send(serverPlayer, field.getStrength(), field.pos);
            }
        }
        fields.removeIf(field -> field.time <= 0);
    }

    public static void forAllOfOwner(@NonNull final Entity owner, Consumer<MagneticField> consumer) {
        for (MagneticField field : fields) {
            if (field.owner != owner) continue;
            if (field.level != owner.level()) continue;
            consumer.accept(field);
        }
    }

    public static void forAllOfOwner(@NonNull final Entity owner, Function<MagneticField, Boolean> consumer) {
        for (MagneticField field : fields) {
            if (field.owner != owner) continue;
            if (field.level != owner.level()) continue;
            if (!consumer.apply(field)) break;
        }
    }

    public static @Nullable MagneticField nearestOfOwnerTo(@NonNull final Entity owner, @NonNull final Vec3 pos) {
        if (fields.isEmpty()) return null;
        return fields.stream()
                .filter(field -> field.owner == owner)
                .min(
                        (a, b) -> {
                            final double aDist = pos.distanceToSqr(a.pos);
                            final double bDist = pos.distanceToSqr(b.pos);
                            return Double.compare(aDist, bDist);
                        }
                ).orElse(null);
    }

    public static @Nullable MagneticField nearestTo(@NonNull final Vec3 pos) {
        if (fields.isEmpty()) return null;
        return fields.stream()
                .min(
                        (a, b) -> {
                            final double aDist = pos.distanceToSqr(a.pos);
                            final double bDist = pos.distanceToSqr(b.pos);
                            return Double.compare(aDist, bDist);
                        }
                ).get();
    }

    public static MagneticField createField(final ServerLevel level, final Entity owner, final Vec3 pos) {
        return createField(level, owner, pos, 5.0f, 10.0f);
    }

    public static MagneticField createField(final ServerLevel level, final Entity owner, final Vec3 pos, final float baseStrength, final float peakStrength) {
        MagneticField field = new MagneticField(level, owner, baseStrength, peakStrength, pos);
        fields.add(field);
        return field;
    }
}
