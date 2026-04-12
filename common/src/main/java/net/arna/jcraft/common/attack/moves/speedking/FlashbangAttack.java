package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public final class FlashbangAttack extends AbstractMove<FlashbangAttack, SpeedKingEntity> {
    private static final Map<String, FlashbangTimer> ACTIVE_TIMERS = new HashMap<>();

    public FlashbangAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<FlashbangAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        Vec3 userPos = user.position();
        Vec3 lookDirection = user.getLookAngle();
        Vec3 targetPos = userPos.add(lookDirection.scale(2.0));

        BlockPos bombPos = new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z);
        BlockState state = attacker.level().getBlockState(bombPos);

        // If the target position is not air, try placing on the ground
        if (!state.isAir()) {
            bombPos = bombPos.below();
            state = attacker.level().getBlockState(bombPos);
            if (!state.isAir()) {
                // Try above if below is also not air
                bombPos = bombPos.above(2);
            }
        }

        // Create timer key
        String timerKey = attacker.level().dimension().location() + "_" +
                bombPos.getX() + "_" + bombPos.getY() + "_" + bombPos.getZ();

        // Plant the timer bomb - uses the move's duration
        ACTIVE_TIMERS.put(timerKey, new FlashbangTimer(
                attacker.level(),
                bombPos,
                attacker.level().getGameTime() + getDuration(),
                user
        ));

        return Set.of();
    }

    public static void tickTimers(Level level) {
        ACTIVE_TIMERS.entrySet().removeIf(entry -> {
            FlashbangTimer timer = entry.getValue();
            long currentTime = level.getGameTime();

            if (currentTime >= timer.explodeTime) {
                // Explode
                explodeFlashbang(timer.level, timer.pos, timer.owner);
                return true; // Remove from map
            }

            return false;
        });
    }

    private static void explodeFlashbang(Level level, BlockPos pos, LivingEntity owner) {
        Vec3 explosionPos = Vec3.atCenterOf(pos);
        int radius = 4; // 4 block radius for flashbang effect

        // Find all living entities in the area
        List<LivingEntity> entitiesInArea = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(explosionPos.add(-radius, -radius, -radius), explosionPos.add(radius, radius, radius)),
                entity -> entity != owner);

        for (LivingEntity entity : entitiesInArea) {
            // Apply flashbang effects: blindness and nausea
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true)); // 5 seconds
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true)); // 4 seconds nausea
        }

        // Create explosion visual/sound effect but no damage
        if (!level.isClientSide) {
            level.explode(null, explosionPos.x, explosionPos.y, explosionPos.z, 0.5f, false,
                    Level.ExplosionInteraction.NONE);
        }
    }

    @Override
    protected @NonNull FlashbangAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FlashbangAttack copy() {
        return copyExtras(new FlashbangAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class FlashbangTimer {
        public final Level level;
        public final BlockPos pos;
        public final long explodeTime;
        public final LivingEntity owner;

        public FlashbangTimer(Level level, BlockPos pos, long explodeTime, LivingEntity owner) {
            this.level = level;
            this.pos = pos;
            this.explodeTime = explodeTime;
            this.owner = owner;
        }
    }

    public static class Type extends AbstractMove.Type<FlashbangAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlashbangAttack>, FlashbangAttack> buildCodec(RecordCodecBuilder.Instance<FlashbangAttack> instance) {
            return baseDefault(instance, FlashbangAttack::new);
        }
    }
}