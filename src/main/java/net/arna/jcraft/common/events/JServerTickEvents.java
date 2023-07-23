package net.arna.jcraft.common.events;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.JCraft.DashData;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.ITimeStop;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

import java.util.*;

import static net.arna.jcraft.common.entity.StandEntity.standUserAI;
import static net.arna.jcraft.common.entity.StandEntity.stun;
import static net.arna.jcraft.common.util.JUtils.activeTimestops;

public class JServerTickEvents {
    public static void serverTick(MinecraftServer server) {
        // Player logic (cooldown handling and DamageTimer counting)
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (player == null) continue;

            if (player.isAlive()) {
                NbtCompound userData = ((IEntityDataSaver) player).getPersistentData();
                if (player.getAttacker() != null)  userData.putInt("DamageTimer", 600);

                // Damage timer
                if (userData.contains("DamageTimer"))  userData.putInt("DamageTimer", userData.getInt("DamageTimer") - 1);

                // Handle cooldowns
                int i = 0;
                for (String cooldownType : JCraft.cooldowns) {
                    i++;
                    if (!userData.contains(cooldownType)) userData.putInt(cooldownType, 0);

                    int reducedCd = userData.getInt(cooldownType) - 1;
                    userData.putInt(cooldownType, reducedCd);

                    if (reducedCd % 2 == 0 || reducedCd < 1) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeShort(3);
                        buf.writeInt(i);
                        buf.writeDouble(MathHelper.clamp(reducedCd / 20.0, 0.0, 10000.0));
                        ServerChannelFeedbackPacket.send(player, buf);
                    }
                }
            }
        }

        // Dimensional hop handling
        Iterator<DimValues> iterator = JCraft.pastDimensions.iterator();
        ArrayList<DimValues> newPastDimensions = new ArrayList<>();

        while (iterator.hasNext()) {
            DimValues dimValues = iterator.next();
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
            if (user instanceof ServerPlayerEntity player) {
                player.teleport(original, dimPos.x, dimPos.y, dimPos.z, player.getYaw(), player.getPitch());
            } else {
                JCraft.teleportToWorld(user, original, dimPos.x, dimPos.y, dimPos.z);
            }
            JCraft.clearPreloadedChunks(au); //this can probably be optimized
        }

        JCraft.pastDimensions = newPastDimensions;

        // Timestop handling
        ArrayList<DimValues> newActiveTimestops = new ArrayList<>();

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

                for (Entity entity : toStop) {
                    if ( entity.hasVehicle() || entity == user || entity == ((IEntityDataSaver)user).getStand() || entity == user.getVehicle() ) continue;
                    ITimeStop ts = ((ITimeStop) entity);
                    ts.setTimeStopTicks(2);
                }

                newActiveTimestops.add(timestop);
            }
        }

        activeTimestops = newActiveTimestops;

        // Burst handling
        Iterator<Map.Entry<LivingEntity, Integer>> burstIter = JCraft.burstTimers.entrySet().iterator();
        HashMap<LivingEntity, Integer> newBurstTimers = new HashMap<>();

        while (burstIter.hasNext()) {
            Map.Entry<LivingEntity, Integer> burst = burstIter.next();
            LivingEntity player = burst.getKey();
            burst.setValue(burst.getValue() - 1);
            int newVal = burst.getValue();

            List<Entity> filter = new ArrayList<>();
            filter.add(player);
            if (player.hasPassengers()) {
                filter.addAll(player.getPassengerList());
            }

            if (newVal > 0) {
                newBurstTimers.put(player, newVal);
            } else {
                player.removeStatusEffect(JStatusRegister.DAZED);
                stun(player, 10, 1);
                Vec3d pPos = player.getEyePos();
                List<? extends Entity> toPush = JUtils.generateHitbox(player.world, pPos, 4, Entity.class, filter);

                for (Entity ent : toPush) {
                    Vec3d awayVector = ent.getPos().subtract(pPos).normalize();
                    boolean pushAway = true;

                    // If the stand was hit, the attack will stop and the user will be hit remotely
                    if (ent instanceof StandEntity stand) {
                        if (stand.hasUser()) {
                            stun(stand.getUser(), 10, 3);
                            stand.cancelAttack();
                        }
                    } else if (ent.getFirstPassenger() instanceof StandEntity stand) { // Stands should not have passengers
                        if (stand.blocking) {
                            pushAway = false;
                        } else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
                            //awayVector = awayVector.multiply(0.5);
                            stun(living, 10, 3);
                            stand.cancelAttack();
                        }
                    }

                    if (pushAway) {
                        ent.setVelocity(awayVector.x, awayVector.y / 5 + 0.4, awayVector.z);
                        ent.velocityModified = true;

                        if (ent instanceof ServerPlayerEntity serverPlayer) {
                            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
                        }
                    }
                }
            }
        }

        JCraft.burstTimers = newBurstTimers;

        // Dash handling
        ArrayList<DashData> newDashes = new ArrayList<>();

        for (DashData dash : JCraft.dashes) {
            dash.tickDash();
            if (dash.finished) continue;
            newDashes.add(dash);
        }

        JCraft.dashes = newDashes;

        for (ServerWorld serverWorld : server.getWorlds()) {
            List<? extends MobEntity> mobEntities = serverWorld.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), EntityPredicates.VALID_ENTITY);

            for (MobEntity mob : mobEntities) {
                IEntityDataSaver user = (IEntityDataSaver) mob;
                NbtCompound mobData = user.getPersistentData();

                if (mob.isAlive()) {
                    if (mobData != null) {
                        // Damage timer
                        if (mob.getAttacker() != null) {
                            mobData.putInt("DamageTimer", 600);
                        }
                        if (mobData.contains("DamageTimer")) {
                            mobData.putInt("DamageTimer", mobData.getInt("DamageTimer") - 1);
                        }

                        if (!mob.isAiDisabled()) {
                            // Target priority
                            if (mob.getFirstPassenger() instanceof StandEntity stand) {
                                LivingEntity biggestAttacker = mob.getDamageTracker().getBiggestAttacker();
                                LivingEntity primeAdversary = mob.getPrimeAdversary();
                                LivingEntity target = mob.getTarget();
                                if (primeAdversary != null && primeAdversary.isAlive() && stand.canTarget(primeAdversary)) {
                                    standUserAI(mob, primeAdversary, stand);
                                } else if (target != null && target.isAlive() && stand.canTarget(target)) {
                                    standUserAI(mob, target, stand);
                                } else if (biggestAttacker != null && biggestAttacker.isAlive() && stand.canTarget(biggestAttacker)) {
                                    mob.setTarget(biggestAttacker);
                                }
                            } else if (mobData.contains("StandID")) {
                                JCraft.summon(serverWorld, mob);
                            }

                            // Handle cooldowns
                            for (String cooldownType : JCraft.cooldowns) {
                                if (!mobData.contains(cooldownType)) {
                                    mobData.putInt(cooldownType, 0);
                                }

                                int reducedCd = mobData.getInt(cooldownType) - 1;
                                mobData.putInt(cooldownType, reducedCd);
                            }
                        }
                    }
                }
            }

            // Item attaction logic
            List<? extends ItemEntity> itemEntities = serverWorld.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), EntityPredicates.VALID_ENTITY);

            for (ItemEntity item : itemEntities) {
                if (item.getStack().isOf(JObjectRegistry.ANUBIS))
                    item.setPickupDelay(0);

                if (item.getStack().isOf(JObjectRegistry.FVREVOLVER)) {
                    if (item.age < 10)
                        item.setPickupDelay(100);
                    Vec3d iPos = item.getPos();

                    // Item attraction logic
                    List<ItemEntity> nearbyItems = serverWorld.getEntitiesByClass(ItemEntity.class,
                            new Box(iPos.add(16, 16, 16), iPos.subtract(16, 16, 16)),
                            EntityPredicates.VALID_ENTITY);

                    for (ItemEntity item2 : nearbyItems) {
                        if (!item2.getStack().isOf(JObjectRegistry.FVREVOLVER)) continue;
                        Vec3d converge = item2.getPos().subtract(iPos);
                        Vec3d towardsVector = converge.normalize().multiply(0.25);
                        item.addVelocity(towardsVector.x, towardsVector.y, towardsVector.z);
                        item.velocityModified = true;

                        if (!item2.equals(item) && item2.distanceTo(item) < 1.0) {
                            Explosion explosion = serverWorld.createExplosion(null, iPos.x, iPos.y, iPos.z, 1f,
                                    serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING) ? Explosion.DestructionType.BREAK : Explosion.DestructionType.NONE);
                            item.kill();
                            item2.kill();

                            List<LivingEntity> toDamage = serverWorld.getEntitiesByClass(LivingEntity.class,
                                    new Box(iPos.add(2, 2, 2), iPos.subtract(2, 2, 2))
                                    , EntityPredicates.VALID_ENTITY);

                            for (LivingEntity ent : toDamage) {
                                ent.damage(DamageSource.explosion(explosion), 10);
                                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, 30, 0));
                            }
                        }
                    }
                }
            }
        }
    }
}
