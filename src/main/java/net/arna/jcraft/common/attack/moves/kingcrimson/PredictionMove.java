package net.arna.jcraft.common.attack.moves.kingcrimson;

import com.google.common.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class PredictionMove extends AbstractMove<PredictionMove, KingCrimsonEntity> {
    public static final MoveVariable<Map<Entity, Vec3d>> PREDICTION_INFO = new MoveVariable<>(new TypeToken<>() {});

    public PredictionMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void onInitiate(KingCrimsonEntity attacker) {
        super.onInitiate(attacker);

        attacker.getMoveContext().get(PREDICTION_INFO).clear();

        // Send epitaph state start
        if (attacker.getUser() instanceof ServerPlayerEntity player)
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE,
                    new PacketByteBuf(Unpooled.buffer().writeBoolean(true)));
    }

    @Override
    public void tick(KingCrimsonEntity attacker) {
        super.tick(attacker);

        if (attacker.getMoveStun() == getWindupPoint())
            beginPrediction(attacker); // Clientside prediction, serverside is in specialAttack()

        if (attacker.age % 2 == 0) {
            tickPredictions(attacker);
            if (attacker.hasUser()) attacker.getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                    10, 2, true, false));
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KingCrimsonEntity attacker, LivingEntity user, MoveContext ctx) {
        return Set.of();
    }

    public void beginPrediction(KingCrimsonEntity attacker) {
        if (!(attacker.getUser() instanceof ServerPlayerEntity player)) return;

        Map<Entity, Vec3d> predictionInfo = attacker.getMoveContext().get(PREDICTION_INFO);
        for (Entity entity : getEntitiesToCatch(attacker.getWorld(), attacker, player))
            predictionInfo.put(entity, entity.getPos());

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(true);
        buf.writeVarInt(getWindupPoint());
        ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, buf);
    }

    public static void finishPrediction(KingCrimsonEntity attacker) {
        Map<Entity, Vec3d> predictionInfo = attacker.getMoveContext().get(PREDICTION_INFO);
        for (Map.Entry<Entity, Vec3d> prediction : predictionInfo.entrySet()) {
            Entity entity = prediction.getKey();
            if (entity == null) continue;

            Vec3d pos = prediction.getValue();
            entity.teleport(pos.x, pos.y, pos.z);
        }

        cancelPrediction(attacker, predictionInfo);
        attacker.cancelMove();
    }

    public static void cancelPrediction(KingCrimsonEntity attacker) {
        Map<Entity, Vec3d> predictionInfo = attacker.getMoveContext().get(PREDICTION_INFO);
        cancelPrediction(attacker, predictionInfo);
    }

    public static void cancelPrediction(KingCrimsonEntity attacker, Map<Entity, Vec3d> predictionInfo) {
        if (attacker.getUser() instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_EPITAPH_STATE, new PacketByteBuf(Unpooled.buffer().writeBoolean(false)));
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_TIME_ERASE_PREDICTION_STATE, new PacketByteBuf(Unpooled.buffer().writeBoolean(false)));
        }

        predictionInfo.clear();
    }

    public void tickPredictions(KingCrimsonEntity attacker) {
        Map<Entity, Vec3d> predictionInfo = attacker.getMoveContext().get(PREDICTION_INFO);
        Map<Entity, Vec3d> predictions = new HashMap<>(predictionInfo);
        updatePredictions(predictions.entrySet(), attacker.getMoveStun());
        predictionInfo.clear();
        predictionInfo.putAll(predictions);
    }

    public static List<Entity> getEntitiesToCatch(World world, StandEntity<?, ?> stand, PlayerEntity player) {
        if (world == null || stand == null) return List.of();

        return world.getEntitiesByClass(Entity.class, stand.getBoundingBox().expand(64),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != player));
    }

    public static void updatePredictions(Set<Map.Entry<Entity, Vec3d>> predictionsSet, int ticksLeft) {
        Map<Entity, Map.Entry<Entity, Vec3d>> predictions = predictionsSet.stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e));
        Set<Entity> updated = new HashSet<>();

        for (Map.Entry<Entity, Map.Entry<Entity, Vec3d>> prediction : predictions.entrySet())
            updatePrediction(predictions, prediction.getValue(), updated, ticksLeft);
    }

    private static void updatePrediction(Map<Entity, Map.Entry<Entity, Vec3d>> predictions, Map.Entry<Entity, Vec3d> prediction,
                                         Set<Entity> updated, int ticksLeft) {
        Entity entity = prediction.getKey();
        if (updated.contains(entity)) return;

        updated.add(entity);
        if (entity == null || !entity.isAlive()) return;

        World world = entity.getWorld();

        Vec3d currentPos = entity.getPos().add(0, 0.1, 0);
        Vec3d futurePos = currentPos;
        boolean changed = false;

        Vec3i gravity = GravityChangerAPI.getGravityDirection(entity).getVector();
        Vec3d drop = new Vec3d(gravity.getX(), gravity.getY(), gravity.getZ()).multiply(9.81 / 400 * ticksLeft * ticksLeft);

        // If in air and not in a liquid, account for drop
        if (!entity.isOnGround() && !entity.isSubmergedInWater() && !entity.isInLava()) {
            //JCraft.LOGGER.info("Target is in air");
            futurePos = futurePos.add(drop);
            changed = true;
        }

        // If moving faster than 0.01 m/s, account for distance traveled
        Vec3d velocity = entity.getVelocity();
        if (entity instanceof PlayerEntity player) // EXTREMELY cursed implementation of player velocity because NOTHING ELSE WORKS
            velocity = JComponents.MISC.get(player).getDesiredVelocity();
        //JCraft.LOGGER.info("Target is moving at a velocity of: " + velocity);
        if (velocity.lengthSquared() > 0.0001) {
            Vec3d velocityComp = new Vec3d(velocity.x * ticksLeft, Math.max(0, velocity.y * ticksLeft), velocity.z * ticksLeft);
            //JCraft.LOGGER.info("Modified velocity: " + velocityComp);
            futurePos = futurePos.add(velocityComp);
            changed = true;
        }

        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (!predictions.containsKey(vehicle)) return;

            // Ensure vehicle is updated.
            Map.Entry<Entity, Vec3d> vehiclePrediction = predictions.get(vehicle);
            updatePrediction(predictions, vehiclePrediction, updated, ticksLeft);
            // Account for change in position of vehicle.
            futurePos = futurePos.add(vehiclePrediction.getValue().subtract(vehiclePrediction.getKey().getPos()));
        }

        // Collision check between current and extrapolated future position
        if (!changed) return;

        //JCraft.LOGGER.info("Predicted position changed, time left: " + timeLeft);
        BlockHitResult hitResult = world.raycast(new RaycastContext(currentPos, futurePos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, entity));
        prediction.setValue(hitResult.getPos());
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(PREDICTION_INFO, new WeakHashMap<>());
    }

    @Override
    protected @NonNull PredictionMove getThis() {
        return this;
    }

    @Override
    public @NonNull PredictionMove copy() {
        return copyExtras(new PredictionMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
