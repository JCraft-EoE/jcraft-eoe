package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public final class UpdraftAttack extends AbstractMove<UpdraftAttack, SpeedKingEntity> {
    private static final Map<String, UpdraftPad> ACTIVE_PADS = new HashMap<>();

    public UpdraftAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<UpdraftAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        Vec3 userPos = user.position();
        BlockPos padPos = new BlockPos((int)userPos.x, (int)userPos.y - 1, (int)userPos.z);

        // Create updraft pad key
        String padKey = attacker.level().dimension().location() + "_" +
                padPos.getX() + "_" + padPos.getY() + "_" + padPos.getZ();

        // Create the invisible updraft pad
        ACTIVE_PADS.put(padKey, new UpdraftPad(
                attacker.level(),
                padPos,
                attacker.level().getGameTime() + 60, // 3 seconds hardcoded
                user
        ));

        return Set.of();
    }

    public static void tickUpdraftPads(Level level) {
        ACTIVE_PADS.entrySet().removeIf(entry -> {
            UpdraftPad pad = entry.getValue();
            long currentTime = level.getGameTime();
            long startTime = pad.finalLaunchTime - pad.windDuration;

            // Check if entities are on the pad
            Vec3 padCenter = Vec3.atCenterOf(pad.pos.above());
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(padCenter.add(-1, 0, -1), padCenter.add(1, 2, 1)))) {

                if (currentTime >= pad.finalLaunchTime) {
                    // Final launch with damage
                    entity.push(0, 6.0, 0); // 6 blocks up
                    entity.hurt(entity.damageSources().magic(), 1.0f); // 1 heart damage
                    entity.hurtMarked = true;
                } else if (currentTime >= startTime) {
                    // Continuous wind effect - 4 blocks up over 3 seconds
                    entity.push(0, 4.0 / pad.windDuration, 0);
                    entity.hurt(entity.damageSources().magic(), 0.5f); // Slight chip damage
                    entity.hurtMarked = true;
                }
            }

            // Create wind particles
            if (level instanceof ServerLevel serverLevel && currentTime >= startTime) {
                for (int i = 0; i < 5; i++) {
                    double x = padCenter.x + (level.random.nextDouble() - 0.5) * 2;
                    double z = padCenter.z + (level.random.nextDouble() - 0.5) * 2;
                    double y = padCenter.y;

                    serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 1, 0, 0.1);
                }
            }

            // Remove pad after final launch
            return currentTime > pad.finalLaunchTime;
        });
    }

    @Override
    protected @NonNull UpdraftAttack getThis() {
        return this;
    }

    @Override
    public @NonNull UpdraftAttack copy() {
        return copyExtras(new UpdraftAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class UpdraftPad {
        public final Level level;
        public final BlockPos pos;
        public final long finalLaunchTime;
        public final int windDuration;
        public final LivingEntity owner;

        public UpdraftPad(Level level, BlockPos pos, long finalLaunchTime, LivingEntity owner) {
            this.level = level;
            this.pos = pos;
            this.finalLaunchTime = finalLaunchTime;
            this.windDuration = 60; // 3 seconds
            this.owner = owner;
        }
    }

    public static class Type extends AbstractMove.Type<UpdraftAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<UpdraftAttack>, UpdraftAttack> buildCodec(RecordCodecBuilder.Instance<UpdraftAttack> instance) {
            return baseDefault(instance, UpdraftAttack::new);
        }
    }
}