package net.arna.jcraft.common.events;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.JCraft.DashData;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.arna.jcraft.common.entity.stand.StandEntity.standUserAI;
import static net.arna.jcraft.common.entity.stand.StandEntity.stun;
import static net.arna.jcraft.common.util.JUtils.activeTimestops;

public class JServerTickEvents {
    public static void serverTick(MinecraftServer server) {
        // Player logic (cooldown handling and DamageTimer counting)
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (player == null || !player.isAlive()) continue;

            if (player.getAttacker() != null) JComponents.getMiscData(player).startDamageTimer();
        }

        // Dimensional hop handling
        List<DimValues> newPastDimensions = new ArrayList<>();

        for (DimValues dimValues : JCraft.pastDimensions) {
            Entity user = dimValues.user;
            if (user == null || !user.isAlive() || user.isRemoved())
                continue;

            ServerWorld au = (ServerWorld) user.getWorld();
            ServerWorld original = server.getWorld(dimValues.worldKey);
            if (au == original)
                continue;

            dimValues.timer--;
            if (dimValues.timer > 1) {
                newPastDimensions.add(dimValues);
                continue;
            }

            Vec3d dimPos = user.getPos(); //dimValues.pos;
            if (user instanceof ServerPlayerEntity player)
                player.teleport(original, dimPos.x, dimPos.y, dimPos.z, player.getYaw(), player.getPitch());
            else JCraft.teleportToWorld(user, original, dimPos.x, dimPos.y, dimPos.z);

            if (newPastDimensions.isEmpty()) // Nobody left in AU
                JCraft.clearPreloadedChunks(au);
        }

        JCraft.pastDimensions.clear();
        JCraft.pastDimensions.addAll(newPastDimensions);

        // Timestop handling
        List<DimValues> newActiveTimestops = new ArrayList<>();

        for (DimValues timestop : activeTimestops) {
            Entity user = timestop.user;
            //JCraft.LOGGER.info("SERVER: Ticking timestop " + timestop + " with user " + user + " and duration " + timestop.timer);

            if (user != null && user.isAlive() && timestop.timer-- > 0) {
                ServerWorld world = server.getWorld(timestop.worldKey);
                if (world == null) {
                    JCraft.LOGGER.fatal("World that timestop belongs to no longer exists! Key: " + timestop.worldKey + " Timestopper: " + user);
                    continue;
                }

                Vec3d pos = timestop.pos;

                List<? extends Entity> toStop = world.getEntitiesByClass(Entity.class,
                        new Box(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

                for (Entity entity : toStop)
                    if (!entity.hasVehicle() && entity != user && (!(entity instanceof LivingEntity living) || entity != JUtils.getStand(living)) &&
                            entity != user.getVehicle())
                        JComponents.getTimeStopData(entity).setTicks(2);

                newActiveTimestops.add(timestop);
            }
        }

        activeTimestops.clear();
        activeTimestops.addAll(newActiveTimestops);

        // Burst handling
        Map<LivingEntity, Integer> newBurstTimers = new HashMap<>();

        for (Map.Entry<LivingEntity, Integer> burst : JCraft.burstTimers.entrySet()) {
            LivingEntity player = burst.getKey();
            burst.setValue(burst.getValue() - 1);
            int newVal = burst.getValue();

            List<Entity> filter = new ArrayList<>();
            filter.add(player);
            if (player.hasPassengers()) filter.addAll(player.getPassengerList());

            if (newVal > 0) {
                newBurstTimers.put(player, newVal);
                continue;
            }

            player.removeStatusEffect(JStatusRegistry.DAZED);
            stun(player, 10, 1);
            Vec3d pPos = player.getEyePos();
            List<? extends Entity> toPush = JUtils.generateHitbox(player.world, pPos, 4, Entity.class, filter);

            for (Entity ent : toPush) {
                Vec3d awayVector = ent.getPos().subtract(pPos).normalize();
                boolean pushAway = true;

                // If the stand was hit, the attack will stop and the user will be hit remotely
                if (ent instanceof StandEntity<?, ?> stand) {
                    if (stand.hasUser()) {
                        stun(stand.getUser(), 10, 3);
                        stand.cancelAttack();
                    }
                } else if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand) { // Stands should not have passengers
                    if (stand.blocking) pushAway = false;
                    else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
                        //awayVector = awayVector.multiply(0.5);
                        stun(living, 10, 3);
                        stand.cancelAttack();
                    }
                }

                if (!pushAway) continue;
                ent.setVelocity(awayVector.x, awayVector.y / 5 + 0.4, awayVector.z);
                ent.velocityModified = true;

                if (ent instanceof ServerPlayerEntity serverPlayer)
                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
            }
        }

        JCraft.burstTimers.clear();
        JCraft.burstTimers.putAll(newBurstTimers);

        // Dash handling
        List<DashData> newDashes = new ArrayList<>();

        for (DashData dash : JCraft.dashes) {
            dash.tickDash();
            if (dash.finished) continue;
            newDashes.add(dash);
        }

        JCraft.dashes.clear();
        JCraft.dashes.addAll(newDashes);

        for (ServerWorld serverWorld : server.getWorlds()) {
            List<? extends MobEntity> mobEntities = serverWorld.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), EntityPredicates.VALID_ENTITY);

            for (MobEntity mob : mobEntities) {
                if (!mob.isAlive()) continue;

                // Damage timer
                if (mob.getAttacker() != null) JComponents.getMiscData(mob).startDamageTimer();

                if (mob.isAiDisabled()) continue;

                // Target priority
                if (mob.getFirstPassenger() instanceof StandEntity<?, ?> stand) {
                    LivingEntity biggestAttacker = mob.getDamageTracker().getBiggestAttacker();
                    LivingEntity primeAdversary = mob.getPrimeAdversary();
                    LivingEntity target = mob.getTarget();
                    if (primeAdversary != null && primeAdversary.isAlive() && stand.canTarget(primeAdversary))
                        standUserAI(mob, primeAdversary, stand);
                    else if (target != null && target.isAlive() && stand.canTarget(target))
                        standUserAI(mob, target, stand);
                    else if (biggestAttacker != null && biggestAttacker.isAlive() && stand.canTarget(biggestAttacker))
                        mob.setTarget(biggestAttacker);
                } else if (JComponents.getStandData(mob).getType() != null) JCraft.summon(serverWorld, mob);
            }
        }

        // Handle items of interest
        HashMap<Entity, EntityInterest> entitiesOfInterest = JCraft.getEntitiesOfInterest();
        HashMap<Entity, EntityInterest> newItemsOfInterest = new HashMap<>();

        for (Map.Entry<Entity, EntityInterest> entityAndInterest : entitiesOfInterest.entrySet()) {
            Entity entity = entityAndInterest.getKey();
            if (entity == null || !entity.isAlive()) continue;
            EntityInterest interest = entityAndInterest.getValue();
            ServerWorld serverWorld = (ServerWorld) entity.getWorld();
            boolean saveForNextIteration = true;

            switch (interest.getType()) {
                default -> saveForNextIteration = false;
                case BLOCK_ATTRACTION -> {
                    BlockPos attractionBlockPos = interest.getAttractionBlockPos();
                    if (entity.squaredDistanceTo(attractionBlockPos.getX(), attractionBlockPos.getY(), attractionBlockPos.getZ()) < 4) {
                        boolean griefing = serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING);
                        dimensionalExplosion(serverWorld, griefing, entity);
                        if (griefing) serverWorld.setBlockState(attractionBlockPos, Blocks.AIR.getDefaultState());
                    } else {
                        BlockPos delta = attractionBlockPos.subtract(entity.getBlockPos());
                        Vec3d towardsVel = new Vec3d(delta.getX(), delta.getY(), delta.getZ()).normalize();
                        entity.addVelocity(towardsVel.x, towardsVel.y, towardsVel.z);
                        entity.velocityModified = true;
                    }
                }
                case ITEM_ATTRACTION -> {
                    if (!(entity instanceof ItemEntity item)) continue;
                    for (Map.Entry<Entity, EntityInterest> entityAndInterest2 : entitiesOfInterest.entrySet()) {
                        Entity entity2 = entityAndInterest2.getKey();
                        if (entity2 instanceof ItemEntity item2) {
                            if (
                                    entityAndInterest2.getValue().getType() == EntityInterest.ItemInterestType.ITEM_ATTRACTION &&
                                    item2 != entity &&
                                    item2.getWorld() == serverWorld &&
                                    item2.getStack().getItem() == item.getStack().getItem() &&
                                    item2.squaredDistanceTo(entity) <= 256
                            ) {
                                Vec3d converge = item2.getPos().subtract(entity.getPos());
                                Vec3d towardsVector = converge.normalize().multiply(0.25);
                                entity.addVelocity(towardsVector.x, towardsVector.y, towardsVector.z);
                                entity.velocityModified = true;

                                if (item2.distanceTo(entity) <= 1.0) {
                                    dimensionalExplosion(serverWorld, serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING), entity, item2);
                                    saveForNextIteration = false;
                                }
                            }
                        }
                    }
                }
            }

            if (saveForNextIteration) newItemsOfInterest.put(entity, interest);
        }

        entitiesOfInterest.clear();
        entitiesOfInterest.putAll(newItemsOfInterest);
    }

    private static void dimensionalExplosion(ServerWorld serverWorld, boolean griefing, Entity one) {
        dimensionalExplosion(serverWorld, griefing, one, null);
    }

    private static void dimensionalExplosion(ServerWorld serverWorld, boolean griefing, Entity one, @Nullable Entity other) {
        Vec3d midPos = one.getPos();
        if (other != null) {
            midPos = midPos.add(other.getPos()).multiply(0.5);
            other.discard();
        }

        one.discard();

        Explosion explosion = serverWorld.createExplosion(null, midPos.x, midPos.y, midPos.z, 1f,
                griefing ? Explosion.DestructionType.BREAK : Explosion.DestructionType.NONE);

        List<LivingEntity> toDamage = serverWorld.getEntitiesByClass(LivingEntity.class,
                new Box(midPos.add(1.5, 1.5, 1.5), midPos.subtract(1.5, 1.5, 1.5)),
                EntityPredicates.VALID_ENTITY);

        for (LivingEntity ent : toDamage) {
            ent.damage(explosion.getDamageSource(), 7);
            StandEntity.stun(ent, 10, 3);
            ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0));
        }
    }
}
