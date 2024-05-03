package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.VampireComponent;
import net.arna.jcraft.common.config.ConfigOption;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.events.JServerEvents;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.network.c2s.ConfigUpdatePacket;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.tickable.Revivables;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;

import static net.arna.jcraft.registry.JObjectRegistry.COFFIN_BLOCK;

public interface JEventsRegistry {
    static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {
                    boolean toLaunch = false;
                    Entity attacker = source.getAttacker();
                    StatusEffectInstance stun = entity.getStatusEffect(JStatusRegistry.DAZED);

                    if (stun != null && stun.getAmplifier() != 2) {
                        // Only apply stun nerfs if hit with a weapon or a projectile
                        if (attacker instanceof LivingEntity living) {
                            boolean hasWeapon = source.isOf(DamageTypes.MOB_PROJECTILE);
                            if (!hasWeapon)
                                hasWeapon = !living.getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty();
                            toLaunch = hasWeapon;
                        }

                        if (source.isOf(DamageTypes.EXPLOSION)) {
                            toLaunch = true;
                        }

                        if (toLaunch) {
                            int duration = stun.getDuration() / 3;

                            entity.removeStatusEffect(JStatusRegistry.DAZED);
                            StandEntity.stun(entity, duration, 3);

                            Vec3i upVec = GravityChangerAPI.getGravityDirection(entity).getVector();
                            Vec3d upVecD = new Vec3d(-upVec.getX() / 3.0, -upVec.getY() / 3.0, -upVec.getZ() / 3.0);

                            Vec3d sourcePos = source.getPosition();
                            if (sourcePos == null) { // RNG Launch upwards
                                sourcePos = new Vec3d(
                                    entity.getRandom().nextGaussian(),
                                    entity.getRandom().nextGaussian(),
                                    entity.getRandom().nextGaussian())
                                        .add(entity.getPos())
                                        .subtract(upVecD);
                            }

                            Vec3d knockback = entity.getPos().subtract(sourcePos).normalize().add(upVecD);
                            GravityChangerAPI.setWorldVelocity(entity, knockback);
                            entity.velocityModified = true;
                        }
                    }
                    return true;
                }
        );

        ServerEntityEvents.ENTITY_LOAD.register(JServerEvents::entityLoad);

        ServerLivingEntityEvents.AFTER_DEATH.register((living, source) -> {
            if (living instanceof ServerPlayerEntity serverPlayer) {
                GameRules gameRules = living.getWorld().getGameRules();
                if (!gameRules.getBoolean(JCraft.KEEP_STAND))
                    JComponents.getStandData(living).setTypeAndSkin(StandType.NONE, 0);
                if (!gameRules.getBoolean(JCraft.KEEP_SPEC))
                    JComponents.getSpecData(serverPlayer).setType(SpecType.NONE);

                if (source.getAttacker() instanceof LivingEntity killer) {
                    JComponents.getCooldowns(killer).clear(CooldownType.COMBO_BREAKER);

                    boolean killVampirism = JServerConfig.KILL_VAMPIRISM.getValue();
                    if (killer instanceof ServerPlayerEntity killerPlayer) {
                        if (killVampirism) {
                            killerPlayer.getHungerManager().add(20, 20f);
                            VampireComponent vampireComponent = JComponents.getVampirism(killerPlayer);
                            if (vampireComponent.isVampire())
                                vampireComponent.setBlood(20.0f);
                        }
                    }
                    if (killVampirism)
                        killer.setHealth(killer.getMaxHealth());
                }
            }

            Revivables.addRevivable(living.getType(), living.getPos(), living.getWorld().getRegistryKey());
        });

        ServerTickEvents.END_SERVER_TICK.register(JServerEvents::serverTick);

        

        EntitySleepEvents.ALLOW_SLEEP_TIME.register(
                (player, sleepingPos, vanillaResult) -> {
                    if (player.getWorld() instanceof ServerWorld serverWorld)
                        if (serverWorld.getBlockState(sleepingPos).isOf(COFFIN_BLOCK))
                            return serverWorld.isDay() ? ActionResult.SUCCESS : ActionResult.FAIL;
                    return ActionResult.PASS;
                }
        );

        EntitySleepEvents.ALLOW_BED.register(
                (entity, sleepingPos, state, vanillaResult) -> {
                    if (state.isOf(COFFIN_BLOCK))
                        if (entity instanceof ServerPlayerEntity serverPlayer)
                            return serverPlayer.canResetTimeBySleeping() ? ActionResult.FAIL : ActionResult.SUCCESS;
                    return ActionResult.PASS;
                }
        );

        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register(
                (entity, sleepingPos, sleepingDirection) -> {
                    BlockState state = entity.getWorld().getBlockState(sleepingPos);
                    if (state.isOf(COFFIN_BLOCK))
                        return state.get(CoffinBlock.FACING);
                    return sleepingDirection;
                }
        );

        EntitySleepEvents.STOP_SLEEPING.register(
                (entity, sleepingPos) -> {
                    if (entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.canResetTimeBySleeping() && serverPlayer.getWorld() instanceof ServerWorld serverWorld) {
                        BlockState state = serverWorld.getBlockState(sleepingPos);
                        if (state.isOf(COFFIN_BLOCK)) {
                            if (serverWorld.sleepManager.canSkipNight(serverWorld.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE))
                                    && serverWorld.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
                                serverWorld.setTimeOfDay(13000);
                            serverWorld.setBlockState(sleepingPos, state.with(CoffinBlock.OCCUPIED, false));
                        }
                    }
                }
        );

        // Disable item/block usage while stunned
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!JUtils.canAct(player))
                return TypedActionResult.fail(stack);
            return TypedActionResult.pass(stack);
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!JUtils.canAct(player))
                return ActionResult.FAIL;

            // Remote players do stuff with their stand, not themselves
            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand != null && stand.isRemoteAndControllable())
                return ActionResult.FAIL;

            return ActionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!JUtils.canAct(player))
                return ActionResult.FAIL;

            // Remote players do stuff with their stand, not themselves
            StandEntity<?, ?> stand = JUtils.getStand(player);
            if (stand != null && stand.isRemoteAndControllable())
                return ActionResult.FAIL;

            return ActionResult.PASS;
        });


        // Send initial values of server config options to the player.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ConfigUpdatePacket.sendOptionsToClient(handler.getPlayer(), ConfigOption.getImmutableOptions().values()));

        ServerLifecycleEvents.SERVER_STARTING.register(JServerConfig::load);
        ServerLifecycleEvents.SERVER_STARTED.register(JServerEvents::finishLoading);
    }
}
