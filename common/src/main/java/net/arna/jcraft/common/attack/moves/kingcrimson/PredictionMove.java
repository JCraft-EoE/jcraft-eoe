package net.arna.jcraft.common.attack.moves.kingcrimson;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonCooldownsComponent;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class PredictionMove extends AbstractMove<PredictionMove, KingCrimsonEntity> {
    private final Map<Entity, Vec3> predictionInfo = new WeakHashMap<>();

    public PredictionMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<PredictionMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean canBeQueued(KingCrimsonEntity attacker) {
        return false;
    }

    @Override
    public void onInitiate(final KingCrimsonEntity attacker) {
        super.onInitiate(attacker);

        predictionInfo.clear();

        // Send epitaph state start
        if (attacker.getUser() instanceof ServerPlayer player) {
            NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_EPITAPH_STATE,
                    new FriendlyByteBuf(Unpooled.buffer().writeBoolean(true)));
        }
    }

    @Override
    public void activeTick(final KingCrimsonEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);

        if (moveStun == getWindupPoint()) {
            beginPrediction(attacker); // Clientside prediction, serverside is in specialAttack()
        }

        if (attacker.tickCount % 2 != 0) return;

        tickPredictions(attacker);
        if (attacker.hasUser()) {
            attacker.getUserOrThrow().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    10, 2, true, false));
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final KingCrimsonEntity attacker, final LivingEntity user) {
        return Set.of();
    }

    @Override
    public boolean onInitMove(KingCrimsonEntity attacker, MoveClass moveClass) {
        if (moveClass != MoveClass.ULTIMATE || !attacker.hasUser()) return false;

        LivingEntity user = attacker.getUserOrThrow();
        final CommonCooldownsComponent cooldowns = JComponentPlatformUtils.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) <= 0) {
            cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 20);
            finishPrediction(attacker);
        }

        return true;
    }

    public void beginPrediction(final KingCrimsonEntity attacker) {
        if (!(attacker.getUser() instanceof ServerPlayer player)) {
            return;
        }

        for (final Entity entity : getEntitiesToCatch(attacker.level(), attacker, player)) {
            predictionInfo.put(entity, entity.position());
        }

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        buf.writeVarInt(getWindupPoint());
        NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, buf);
    }

    public void finishPrediction(final KingCrimsonEntity attacker) {
        for (Map.Entry<Entity, Vec3> prediction : predictionInfo.entrySet()) {
            final Entity entity = prediction.getKey();
            if (entity == null) {
                continue;
            }

            final Vec3 pos = prediction.getValue();
            entity.teleportToWithTicket(pos.x, pos.y, pos.z);
        }

        cancelPrediction(attacker, predictionInfo);
        attacker.cancelMove();
    }

    public void cancelPrediction(final KingCrimsonEntity attacker) {
        cancelPrediction(attacker, predictionInfo);
    }

    public static void cancelPrediction(final KingCrimsonEntity attacker, final Map<Entity, Vec3> predictionInfo) {
        if (attacker.getUser() instanceof ServerPlayer player) {
            NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_EPITAPH_STATE, new FriendlyByteBuf(Unpooled.buffer().writeBoolean(false)));
            NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, new FriendlyByteBuf(Unpooled.buffer().writeBoolean(false)));
        }

        predictionInfo.clear();
    }

    public void tickPredictions(final KingCrimsonEntity attacker) {
        final Map<Entity, Vec3> predictions = new HashMap<>(predictionInfo);
        updatePredictions(predictions, attacker.getMoveStun());
        predictionInfo.clear();
        predictionInfo.putAll(predictions);
    }

    public static List<Entity> getEntitiesToCatch(final Level world, final StandEntity<?, ?> stand, final Player player) {
        if (world == null || stand == null) {
            return Collections.emptyList();
        }

        return world.getEntitiesOfClass(Entity.class, stand.getBoundingBox().inflate(64),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != player && !e.getType().is(JTagRegistry.UNAFFECTED_BY_EPITAPH)));
    }

    public static void updatePredictions(final Map<Entity, Vec3> predictions, final int ticksLeft) {
        final Set<Entity> updated = new HashSet<>();
        for (Map.Entry<Entity, Vec3> prediction : predictions.entrySet()) {
            updatePrediction(predictions, prediction, updated, ticksLeft);
        }
    }

    //todo: fixme
    private static void updatePrediction(final Map<Entity, Vec3> predictions, final Map.Entry<Entity, Vec3> prediction,
                                         final Set<Entity> updated, final int ticksLeft) {
        final Entity entity = prediction.getKey();
        if (updated.contains(entity)) {
            return;
        }

        updated.add(entity);
        if (entity == null || !entity.isAlive()) {
            return;
        }

        final Level world = entity.level();

        final Vec3 currentPos = entity.position().add(0, 0.1, 0);
        Vec3 futurePos = currentPos;
        boolean changed = false;

        final Vec3i gravity = GravityChangerAPI.getGravityDirection(entity).getNormal();
        final Vec3 drop = new Vec3(gravity.getX(), gravity.getY(), gravity.getZ()).scale(9.81 / 400 * ticksLeft * ticksLeft);

        // If in air and not in a liquid, account for drop
        if (!entity.onGround() && !entity.isUnderWater() && !entity.isInLava()) {
            //JCraft.LOGGER.info("Target is in air");
            futurePos = futurePos.add(drop);
            changed = true;
        }

        // If moving faster than 0.01 m/s, account for distance traveled
        Vec3 velocity = entity.getDeltaMovement();
        if (entity instanceof Player player) // EXTREMELY cursed implementation of player velocity because NOTHING ELSE WORKS
        {
            velocity = JComponentPlatformUtils.getMiscData(player).getDesiredVelocity();
        }
        //JCraft.LOGGER.info("Target is moving at a velocity of: " + velocity);
        if (velocity.lengthSqr() > 0.0001) {
            Vec3 velocityComp = new Vec3(velocity.x * ticksLeft, Math.max(0, velocity.y * ticksLeft), velocity.z * ticksLeft);
            //JCraft.LOGGER.info("Modified velocity: " + velocityComp);
            futurePos = futurePos.add(velocityComp);
            changed = true;
        }

        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (!predictions.containsKey(vehicle)) {
                return;
            }

            // Ensure vehicle is updated.
            Map.Entry<Entity, Vec3> vehiclePrediction = new AbstractMap.SimpleEntry<>(vehicle, predictions.get(vehicle));
            updatePrediction(predictions, vehiclePrediction, updated, ticksLeft);
            // Account for change in position of vehicle.
            futurePos = futurePos.add(vehiclePrediction.getValue().subtract(vehiclePrediction.getKey().position()));
        }

        // Collision check between current and extrapolated future position
        if (!changed) {
            return;
        }

        //JCraft.LOGGER.info("Predicted position changed, time left: " + timeLeft);
        BlockHitResult hitResult = world.clip(new ClipContext(currentPos, futurePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, entity));
        prediction.setValue(hitResult.getLocation());
    }

    @Override
    protected @NonNull PredictionMove getThis() {
        return this;
    }

    @Override
    public @NonNull PredictionMove copy() {
        return copyExtras(new PredictionMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<PredictionMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PredictionMove>, PredictionMove> buildCodec(RecordCodecBuilder.Instance<PredictionMove> instance) {
            return baseDefault(instance, PredictionMove::new);
        }
    }
}
