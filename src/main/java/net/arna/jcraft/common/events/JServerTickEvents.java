package net.arna.jcraft.common.events;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedback;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JCraftUtils;
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
import net.minecraft.world.GameRules;
import net.minecraft.world.explosion.Explosion;

import java.util.*;

import static net.arna.jcraft.common.entity.StandEntity.Stun;
import static net.arna.jcraft.common.util.JCraftUtils.activeTimestops;

public class JServerTickEvents {
    public static void serverTick(MinecraftServer server) {
        // Player logic (cooldown handling and DamageTimer counting)
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (player == null) {
                continue;
            }
            if (player.isAlive()) {
                IEntityDataSaver user = (IEntityDataSaver) player;
                NbtCompound userData = user.getPersistentData();
                if (player.getAttacker() != null) {
                    userData.putInt("DamageTimer", 600);
                }

                // Damage timer
                if (userData.contains("DamageTimer")) {
                    userData.putInt("DamageTimer", userData.getInt("DamageTimer") - 1);
                }

                // Handle cooldowns
                int i = 0;
                for (String cooldownType : JCraft.cooldowns) {
                    i++;
                    if (!userData.contains(cooldownType)) {
                        userData.putInt(cooldownType, 0);
                    }

                    int reducedCd = userData.getInt(cooldownType) - 1;
                    userData.putInt(cooldownType, reducedCd);

                    if (reducedCd % 2 == 0 || reducedCd < 1) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeShort(3);
                        buf.writeInt(i);
                        buf.writeDouble(MathHelper.clamp(reducedCd / 20.0, 0.0, 10000.0));
                        ServerChannelFeedback.send(player, buf);
                    }
                }
            }
        }

        // Keeping track of dimhops
        Iterator<DimValues> iterator = JCraft.pastDimensions.iterator();
        ArrayList<DimValues> newPastDimensions = new ArrayList<>();

        while (iterator.hasNext()) {
            DimValues dimValues = iterator.next();
            Entity user = dimValues.user;
            if (user == null)
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

            Vec3d dimPos = dimValues.pos;
            if (user instanceof ServerPlayerEntity player) {
                player.teleport(original, dimPos.x, dimPos.y, dimPos.z, player.getYaw(), player.getPitch());
            } else {
                JCraft.teleportToWorld(user, original, dimPos.x, dimPos.y, dimPos.z);
            }
            JCraft.ClearPreloadedChunks(au); //this can probably be optimized
        }

        JCraft.pastDimensions = newPastDimensions;

        // Keeping track of timestops

        for (DimValues dimValues : activeTimestops) {
            if (dimValues.user instanceof StandEntity stand && dimValues.user.isAlive()) {
                if (stand.getTSTime() > 0) {
                    continue;
                }
            }

            activeTimestops.remove(dimValues);
            break;
        }

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
                player.removeStatusEffect(JStatusRegister.Dazed);
                Stun(player, 10, 1);
                Vec3d pPos = player.getEyePos();
                List<? extends Entity> toPush = JCraftUtils.GenerateHitbox(player.world, pPos, 4, Entity.class, filter);

                for (Entity ent : toPush) {
                    Vec3d awayVector = ent.getPos().subtract(pPos).normalize();
                    boolean pushAway = true;

                    // If the stand was hit, the attack will stop and the user will be hit remotely
                    if (ent instanceof StandEntity stand) {
                        if (stand.hasUser()) {
                            Stun(stand.getUser(), 10, 3);
                            stand.CancelAttack();
                        }
                    } else if (ent.getFirstPassenger() instanceof StandEntity stand) { // Stands should not have passengers
                        if (stand.blocking) {
                            pushAway = false;
                        } else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
                            //awayVector = awayVector.multiply(0.5);
                            Stun(living, 10, 3);
                            stand.CancelAttack();
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

        for (ServerWorld serverWorld : server.getWorlds()) {
            // Mob stand control logic
            List<MobEntity> mobEntities = (List<MobEntity>) serverWorld.getEntitiesByType(TypeFilter.instanceOf(MobEntity.class), EntityPredicates.VALID_ENTITY);

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
                                if (primeAdversary != null && primeAdversary.isAlive()) {
                                    stand.MobAI(mob, primeAdversary);
                                } else if (target != null && target.isAlive()) {
                                    stand.MobAI(mob, target);
                                } else if (biggestAttacker != null && biggestAttacker.isAlive()) {
                                    mob.setTarget(biggestAttacker);
                                }
                            } else if (mobData.contains("StandID")) {
                                JCraft.Summon(serverWorld, mob);
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
            List<ItemEntity> itemEntities = (List<ItemEntity>) serverWorld.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), EntityPredicates.VALID_ENTITY);

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
                        if (!item2.getStack().isOf(JObjectRegistry.FVREVOLVER)) {
                            continue;
                        }

                        Vec3d converge = item2.getPos().subtract(iPos);
                        Vec3d towardsVector = converge.normalize().multiply(0.25);
                        item.addVelocity(towardsVector.x, towardsVector.y, towardsVector.z);
                        item.velocityModified = true;

                        if (!item2.equals(item) && item2.distanceTo(item) < 1.0) {
                            Explosion explosion = serverWorld.createExplosion(null, iPos.x, iPos.y, iPos.z, 1f,
                                    serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? Explosion.DestructionType.BREAK : Explosion.DestructionType.NONE);
                            item.kill();
                            item2.kill();

                            List<LivingEntity> toDamage = serverWorld.getEntitiesByClass(LivingEntity.class,
                                    new Box(iPos.add(2, 2, 2), iPos.subtract(2, 2, 2))
                                    , EntityPredicates.VALID_ENTITY);

                            for (LivingEntity ent : toDamage) {
                                ent.damage(DamageSource.explosion(explosion), 10);
                                ent.addStatusEffect(new StatusEffectInstance(JStatusRegister.Knockdown, 30, 0));
                            }
                        }
                    }
                }
            }
        }
    }
}
