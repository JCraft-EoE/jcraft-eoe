package net.arna.jcraft.api;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.TrainingDummyEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.network.s2c.ComboCounterPacket;
import net.arna.jcraft.common.network.s2c.DamageNumberPacket;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin.LivingEntityInvoker;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mod.azure.azurelib.animation.dispatch.command.AzCommand.controllerBuilder;
import static net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation;

public interface Attacks {

    /**
     * Use instead of AzCommand.create() inside stand's State enums.
     */
    static AzCommand createAnimationCommand(
            String controllerName,
            String animationName,
            AzPlayBehavior playBehavior
    ) {
        return createAnimationCommand(controllerName, animationName, playBehavior, 0f, 1f, 0f, 0f, false);
    }

    static AzCommand createAnimationCommand(
            String controllerName,
            String animationName,
            AzPlayBehavior playBehavior,
            float startTickOffset,
            float animationSpeed,
            float freezeTickOffset,
            float repeatXTimes,
            boolean isReversing
    ) {
        return controllerBuilder()
                .cancel(controllerName)
                .playSequence(
                        controllerName,
                        sequenceBuilder -> sequenceBuilder.queue(
                                animationName,
                                props -> props.withPlayBehavior(playBehavior)
                        )
                )
                .setFreezeTickOffset(controllerName, freezeTickOffset)
                .setStartTickOffset(controllerName, startTickOffset)
                .setSpeed(controllerName, animationSpeed)
                .setRepeatAmount(controllerName, repeatXTimes)
                .setReverseAnimation(controllerName, isReversing)
                .build();
    }

    /**
     * (LEGACY) Highest level damage method, handles combo counting, DEFAULTS unblockable TO FALSE
     * Use {@link Attacks#damageLogic(Level, LivingEntity, AttackData)} instead.
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    @Deprecated(forRemoval = false, since = "0.17.3")
    static void damageLogic(Level world, LivingEntity ent, Vec3 kbVec, int stunTicks, int stunLevel,
                                   boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source,
                                   @Nullable Entity attacker, HitAnimation hitAnimation,
                                   boolean canBackstab, boolean unblockable) {
        if (world == null || world.isClientSide || ent == null || !ent.canBeSeenAsEnemy()) {
            return;
        }
        if (attacker instanceof ServerPlayer playerEntity) {
            comboCounterLogic(playerEntity, ent);
        }

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, null, canBackstab, unblockable, true);
    }

    /**
     * (LEGACY) Highest level damage method, handles combo counting, DEFAULTS attack TO NULL & unblockable TO FALSE.
     * Use {@link Attacks#damageLogic(Level, LivingEntity, AttackData)} instead.
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    @Deprecated(forRemoval = false, since = "0.17.3")
    static void damageLogic(Level world, LivingEntity ent, Vec3 kbVec, int stunTicks, int stunLevel,
                                   boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source,
                                   @Nullable Entity attacker, HitAnimation hitAnimation,
                                   boolean canBackstab) {
        if (world == null || world.isClientSide || ent == null || !ent.canBeSeenAsEnemy()) {
            return;
        }
        if (attacker instanceof ServerPlayer playerEntity) {
            comboCounterLogic(playerEntity, ent);
        }

        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, null, canBackstab, false, true);
    }

    /**
     * (LEGACY) Highest level damage method, handles combo counting, DEFAULTS attack TO NULL & canBackstab & unblockable TO FALSE
     * Use {@link Attacks#damageLogic(Level, LivingEntity, AttackData)} instead.
     *
     * @param world        world to process damage in
     * @param ent          victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     */
    @Deprecated(forRemoval = false, since = "0.17.3")
    static void damageLogic(Level world, LivingEntity ent, Vec3 kbVec, int stunTicks, int stunLevel,
                                   boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source,
                                   @Nullable Entity attacker, HitAnimation hitAnimation) {
        if (world == null || world.isClientSide || ent == null || !ent.canBeSeenAsEnemy()) {
            return;
        }
        if (attacker instanceof ServerPlayer playerEntity) {
            comboCounterLogic(playerEntity, ent);
        }
        baseDamageLogic(ent, kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun, source, attacker, hitAnimation, null, false, false, true);
    }

    static void damageLogic(Level world, LivingEntity victim, AttackData attackData) {
        if (world == null || world.isClientSide || victim == null || !victim.canBeSeenAsEnemy()) {
            return;
        }

        if (attackData.attacker() instanceof ServerPlayer playerEntity) {
            comboCounterLogic(playerEntity, victim);
        }

        baseDamageLogic(victim,
                attackData.kbVec(), attackData.stunTicks(), attackData.stunLevel(), attackData.overrideStun(),
                attackData.damage(), attackData.lift(), attackData.blockStun(), attackData.source(), attackData.attacker(),
                attackData.hitAnimation(), attackData.moveUsage(), attackData.canBackstab(), attackData.unblockable(), attackData.cancelMoves());
    }

    /**
     * Handles combo counting for specific player
     *
     * @param playerEntity attacker
     */
    static void comboCounterLogic(ServerPlayer playerEntity, @Nullable LivingEntity victim) {
        if (victim == null || victim instanceof IOwnable ownable && ownable.getMaster() == playerEntity) {
            return;
        }
        if (!JServerConfig.ENABLE_FRIENDLY_FIRE.getValue() && victim.isAlliedTo(playerEntity)) {
            return;
        }

        IComboCounter comboCounter = (IComboCounter) playerEntity;

        if (comboCounter.jcraft$getLastAttacked() != victim) {
            comboCounter.jcraft$setComboCount(1);
        } else {
            MobEffectInstance stun = comboCounter.jcraft$getLastAttacked().getEffect(JStatusRegistry.DAZED.get());
            if (stun != null && stun.getAmplifier() != 2) {
                comboCounter.jcraft$incrementComboCount();
            } else {
                comboCounter.jcraft$setComboCount(1);
            }

            ComboCounterPacket.send(playerEntity, comboCounter.jcraft$getComboCount(), ((IJCraftComboTracker) victim).jcraft$getDamageScaling());
        }

        comboCounter.jcraft$setLastAttacked(victim);
    }

    /**
     * Mid-level damage method, handles blocking, lifting, counters, velocity modification, and more.
     * Unpacks {@link AttackData} into its parameters.
     *
     * @param victim       victim
     * @param kbVec        knockback vector to apply
     * @param stunTicks    stun duration in ticks
     * @param overrideStun will the attack override all other types of stun?
     * @param damage       damage in half hearts
     * @param lift         will the attack lift the victim upon an aerial hit?
     * @param hitAnimation animation the opponent will do when they are hit
     */
    static void baseDamageLogic(LivingEntity victim, Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun,
                                float damage, boolean lift, int blockstun, DamageSource source, @Nullable Entity attacker,
                                HitAnimation hitAnimation, @Nullable MoveUsage moveUsage, boolean canBackstab, boolean unblockable,
                                boolean cancelAttack) {
        if (victim instanceof ICustomDamageHandler customDamageHandler) {
            if (!customDamageHandler.handleDamage(
                    kbVec, stunTicks, stunLevel, overrideStun, damage, lift, blockstun,
                    source, attacker, hitAnimation, moveUsage, canBackstab, unblockable)
            ) {
                return;
            }
        }

        if (victim != null && !JServerConfig.ENABLE_FRIENDLY_FIRE.getValue() && attacker != null && victim.isAlliedTo(attacker)) {
            return;
        }

        boolean hit = true;
        boolean tsHit = JUtils.isAffectedByTimeStop(victim);

        final StandEntity<?, ?> stand = JUtils.getStand(victim);
        if (stand != null) {
            // If a stand wants to block and can, but didn't get the chance to due to execution order, prompt blocking here.
            if (stand.wantToBlock && stand.canBlock()) {
                stand.tryBlock();
            }

            AbstractMove<?, ?> standAttack = stand.getCurrentMove();
            if (standAttack != null) {
                // Counter check
                if (!tsHit && standAttack.isCounter() && stand.getMoveStun() < standAttack.getWindupPoint()) {
                    //noinspection unchecked
                    ((AbstractCounterAttack<?, StandEntity<?, ?>>) standAttack).counter(stand, attacker, source);
                    victim.removeEffect(JStatusRegistry.DAZED.get());
                    return;
                }

                if (--stand.armorPoints < 0) {
                    if (cancelAttack) stand.cancelMove(true);
                } else {
                    JComponentPlatformUtils.getMiscData(victim).displayArmoredHit();
                }
            }

            if (stand.blocking && !stand.isRemote()) {
                boolean backstabbed = false;
                if (attacker != null) {
                    double delta = Math.abs((victim.yHeadRot + 90.0f) % 360.0f - (attacker.getYHeadRot() + 90.0f) % 360.0f);
                    if (canBackstab && (360.0 - delta % 360.0 < 45 || delta % 360.0 < 45) && victim.distanceToSqr(attacker.position()) >= 1.5625) { // Backstab logic
                        JCraft.createParticle((ServerLevel) attacker.level(), victim.getX(), attacker.getEyeY(), victim.getZ(), JParticleType.BACK_STAB);
                        stand.playSound(JSoundRegistry.BACKSTAB.get(), 1, 1);
                        stand.blocking = false;
                        overrideStun = true;
                        backstabbed = true;
                    }
                }

                if (!backstabbed && !unblockable) { // Didn't backstab, not unblockable
                    //JCraft.LOGGER.info("Enemy blocked attack, setting blockstun to: " + blockstun);
                    stand.setMoveStun(blockstun);
                    stand.setStandGauge(stand.getStandGauge() - 2 * damage);
                    stand.playSound(JSoundRegistry.STAND_BLOCK.get(), 1, 1);
                    hit = false;
                    overrideStun = false;
                } else {
                    stand.blocking = false;
                }
            }
        }

        if (victim == null) return;
        if (tsHit) {
            stunLevel = 3;
            if (stunTicks > 20) {
                stunTicks = 20;
            }
            lift = false;
        }

        // Stun application & overriding
        IJCraftComboTracker comboTracker = (IJCraftComboTracker) victim;

        /*
        if (JServerConfig.ENABLE_IPS.getValue()) {
            float scaling = comboTracker.jcraft$getDamageScaling();
            stunTicks *= (int) (scaling * 0.2 + 0.8);
        }
         */

        LivingEntity livingAttacker = null;
        if (attacker instanceof LivingEntity l) livingAttacker = l;

        if (hit) {
            comboTracker.jcraft$increaseHitCount();

            boolean allowFurtherStun = true;
            if (livingAttacker != null && moveUsage != null) allowFurtherStun = !comboTracker.jcraft$addMoveToCombo(livingAttacker, moveUsage);

            MobEffectInstance stun = victim.getEffect(JStatusRegistry.DAZED.get());
            if (stun != null) {
                if (overrideStun) {
                    victim.removeEffect(JStatusRegistry.DAZED.get());
                }
            }

            if (allowFurtherStun) {
                JCraft.stun(victim, stunTicks, stunLevel, attacker);
            }

            if (hitAnimation != null) {
                JComponentPlatformUtils.getHitProperties(victim).setHitAnimation(hitAnimation, stunTicks);
            }

            if (!tsHit) {
                victim.push(kbVec.x, kbVec.y, kbVec.z);
            }
        }

        // Interrupting spec moves
        JSpec<?, ?> spec = JUtils.getSpec(victim);
        if (spec != null && spec.curMove != null) {
            if (--spec.armorPoints < 0) {
                if (cancelAttack) spec.cancelMove(true);
            } else {
                JComponentPlatformUtils.getMiscData(victim).displayArmoredHit();
            }
        }

        // Aerial hits keep the victim up
        if (lift) {
            Vec3 vel = victim.getDeltaMovement();
            double finalY = vel.y;

            if (!victim.onGround()) {
                finalY = Mth.clamp(vel.y / 2, 0.085, 0.25);
            }

            GravityChangerAPI.setWorldVelocity(victim,
                    new Vec3(
                            Mth.clamp(vel.x, -1, 1),
                            Mth.clamp(finalY, -0.25, 0.25),
                            Mth.clamp(vel.z, -1, 1)
                    ));
        }

        damage(attacker, damage, source, victim);

        if (livingAttacker != null) {
            final JSpec<?, ?> userSpec = JUtils.getSpec(livingAttacker);
            if (userSpec != null) {
                final SpecType userSpecType = userSpec.getType();
                // combo acknowledger
                if (userSpecType == JSpecTypeRegistry.HAMON.get() && livingAttacker instanceof ServerPlayer player) {
                    final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(player);
                    if (victim.getUUID().equals(hamon.getLastSendoAired()) && victim.getUUID().equals(hamon.getLastStomped()) &&
                            // make sure stomp was used after sendo air
                            hamon.getLastStompedTick() < hamon.getLastSendoAiredTick() &&
                            // but not too long ago (30 seconds)
                            hamon.getLastSendoAiredTick() - hamon.getLastStompedTick() <= 600 &&
                            // check that stomp was last used move TODO do something less hacky than "was it used within the last five seconds"
                            hamon.getLastStompedTick() <= 100
                    ) {
                        JAdvancementTriggerRegistry.HAMON6.trigger(player);
                    }
                }
            }
        }

        if ( (victim.isDeadOrDying() || victim.getHealth() <= 0f) && livingAttacker != null) {
            final StandEntity<?, ?> standAttacker = JUtils.getStand(livingAttacker);
            if (standAttacker != null) {
                standAttacker.freshKill(victim);
            }
            if (stand != null && stand.hasUser() && // if killed entity was a using a stand
                    (standAttacker != null ? standAttacker.getUser() : livingAttacker) instanceof final Player player && !player.level().isClientSide()) {
                player.awardStat(JStatRegistry.STAND_USERS_KILLED.get());
            }
            final JSpec<?,?> userSpec = JUtils.getSpec(livingAttacker);
            if (userSpec != null) {
                final SpecType userSpecType = userSpec.getType();
                // combo acknowledger
                if (userSpecType == JSpecTypeRegistry.HAMON.get() && livingAttacker instanceof ServerPlayer player) {
                    final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(player);
                    if (victim.getUUID().equals(hamon.getLastZoomPunched()) && victim.getUUID().equals(hamon.getLastSendoed()) &&
                            // make sure sendo was used after zoom punch
                            hamon.getLastSendoedTick() < hamon.getLastZoomPunchedTick() &&
                            // but not too long ago (30 seconds)
                            hamon.getLastZoomPunchedTick() - hamon.getLastSendoedTick() <= 600 &&
                            // check that sendo was last used move TODO do something less hacky than "was it used within the last five seconds"
                            hamon.getLastSendoedTick() <= 100
                    ) {
                        JAdvancementTriggerRegistry.HAMON4.trigger(player);
                    }
                }
            }
        }

        if (tsHit) {
            JComponentPlatformUtils.getTimeStopData(victim).ifPresent(ts -> ts.addTotalVelocity(kbVec));
        } else {
            JUtils.syncVelocityUpdate(victim);
        }
    }

    /**
     * Basic damage method, you likely want to use baseDamageLogic or damageLogic instead
     *
     * @param damage       damage in half hearts
     * @param damageSource source of damage
     * @param ent          entity to harm
     */
    static void damage(final @Nullable Entity attacker, float damage, final DamageSource damageSource, final LivingEntity ent) {
        if (!JUtils.canDamage(damageSource, ent)) {
            return;
        }

        float scaling = ((IJCraftComboTracker) ent).jcraft$getDamageScaling();

        //JCraft.LOGGER.info("Damaging entity: " + ent + " with damage: " + damage + " and scaling: " + scaling);
        damage *= scaling;

        //tryApplyHitstop(attacker, ent, damage);

        if (JServerConfig.HEALTH_TO_DAMAGE_SCALING.getValue()) {
            float healthRatio = ent.getMaxHealth() / 20.0f;
            float damageAdjustment = healthRatio - 1.0f;

            if (damageAdjustment > 0.0f) {
                damage *= (1.0f + damageAdjustment / 5.0f);
            }
        }

        float armor = ent.getArmorValue();
        float toughness = (float) ent.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

        // raw damage goes into statistics
        if (JUtils.getUserIfStand(attacker) instanceof final Player player) {
            player.awardStat(JStatRegistry.RAW_DAMAGE.get(), (int)damage);
        }

        // Players are hit as if they have unenchanted netherite
        if (ent instanceof Player) {
            armor = 20.0f;
            toughness = 12.0f;
        } else if ( // Standless non-players have received damage multiplied
                !(ent instanceof StandEntity<?,?>)
                        && JUtils.getStand(ent) == null
        ) {
            damage *= JServerConfig.VS_STANDLESS_DAMAGE_MULTIPLIER.getValue();
        }

        // All stands ignore 10% of armor & armor toughness
        // Armor and toughness considerations are also capped in here at netherite levels
        damage = JUtils.getDamageThroughArmor(damage, armor * 0.9f, toughness * 0.9f);
        damage = ((LivingEntityInvoker) ent).invokeModifyAppliedDamage(damageSource, damage);

        // Functionally a callback invocation; we can't copy the nuances of all damage effects, so we do this instead.
        // ent.hurt(damageSource, Float.MIN_NORMAL);

        // Apply absorption
        applyAbsorptionAndStats(damage, damageSource, ent);
    }

    /*
    static void tryApplyHitstop(@Nullable Entity attacker, LivingEntity victim, float damage) {
        if (damage < 3.0f) return;

        final int hitstop = (int) (damage / 2.5f);

        if (JServerConfig.ENABLE_HITSTOP.getValue()) {
            if (attacker instanceof LivingEntity livingAttacker) {
                JComponentPlatformUtils.getTimeStopData(attacker).ifPresent(data -> data.setTicks(hitstop));

                StandEntity<?, ?> attackerStand = JUtils.getStand(livingAttacker);
                if (attackerStand != null) {
                    JComponentPlatformUtils.getTimeStopData(attackerStand).ifPresent(data -> data.setTicks(hitstop));
                }
            }

            JComponentPlatformUtils.getTimeStopData(victim).ifPresent(data -> data.setTicks(hitstop));

            StandEntity<?, ?> victimStand = JUtils.getStand(victim);
            if (victimStand != null) {
                JComponentPlatformUtils.getTimeStopData(victimStand).ifPresent(data -> data.setTicks(hitstop));
            }
        }
    }
     */

    /**
     * Basic damage method, ignores potion effects and enchantments, accounts for armor and damage scaling
     *
     * @param damage       damage in half hearts
     * @param damageSource source of damage
     * @param ent          entity to harm
     */
    static void trueDamage(float damage, DamageSource damageSource, LivingEntity ent) {
        if (ent == null || ent.isRemoved() || ent.isDeadOrDying()) {
            return;
        }

        //tryApplyHitstop(damageSource.getEntity(), ent, damage);

        float scaling = ((IJCraftComboTracker) ent).jcraft$getDamageScaling();
        //JCraft.LOGGER.info("True damaging entity: " + ent + " with damage: " + damage + " and scaling: " + scaling);
        damage *= scaling;

        // Send damage number packet for training dummies
        if (ent instanceof TrainingDummyEntity && !ent.level().isClientSide() && damage > 0) {
            sendDamageNumberPacketForTrueDamage((TrainingDummyEntity) ent, damage);
        }

        // All stands ignore 10% of armor & armor toughness
        damage = JUtils.getDamageThroughArmor(damage, (float) ent.getArmorValue() * 0.9f, (float) ent.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * 0.9f);

        // Apply absorption
        applyAbsorptionAndStats(damage, damageSource, ent);
    }

    static void sendDamageNumberPacketForTrueDamage(TrainingDummyEntity entity, float damage) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 pos = entity.position();
        final int radius = Math.max(0, JServerConfig.DUMMY_DAMAGE_INDICATOR_RANGE.getValue());
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(pos.add(radius, radius, radius), pos.subtract(radius, radius, radius)));

        if (!nearbyPlayers.isEmpty()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            new DamageNumberPacket(entity.getId(), damage).write(buf);

            NetworkManager.sendToPlayers(nearbyPlayers, JPacketRegistry.S2C_DAMAGE_NUMBER, buf);
        }
    }

    static void applyAbsorptionAndStats(float damage, final DamageSource damageSource, final LivingEntity ent) {
        float f = damage;
        damage = Math.max(damage - ent.getAbsorptionAmount(), 0.0F);
        ent.setAbsorptionAmount(ent.getAbsorptionAmount() - (f - damage));

        if (damage <= 0) {
            return;
        }

        final float h = ent.getHealth();
        final LivingEntityInvoker invoker = (LivingEntityInvoker) ent;

        // Statistics
        final Level world = ent.level();
        if (ent instanceof Player) {
            NetworkManager.sendToPlayers(JUtils.tracking(ent), JPacketRegistry.S2C_STAND_HURT, new FriendlyByteBuf(Unpooled.buffer()).writeVarInt(ent.getId()));
        } else {
            world.broadcastEntityEvent(ent, (byte) 2);
        }

        ent.level().broadcastDamageEvent(ent, damageSource);
        invoker.callPlayHurtSound(damageSource);
        invoker.setLastDamageTaken(damage);
        invoker.setLastDamageSource(damageSource);
        invoker.setLastDamageTime(world.getGameTime());

        ent.invulnerableTime = 20;
        ent.hurtDuration = ent.hurtTime = 10;

        ent.setHealth(h - damage);
        ent.getCombatTracker().recordDamage(damageSource, damage);
        ent.gameEvent(GameEvent.ENTITY_DAMAGE);

        if (damageSource.getEntity() instanceof LivingEntity livingAttacker) {
            ent.setLastHurtByMob(livingAttacker);
            if (livingAttacker instanceof Player player) {
                invoker.setLastHurtByPlayerTime(100);
                ent.setLastHurtByPlayer(player);
            }
            else if (livingAttacker instanceof StandEntity<?,?> stand && stand.hasUser() && stand.getUser() instanceof Player player) {
                invoker.setLastHurtByPlayerTime(100);
                ent.setLastHurtByPlayer(player);
            }
        }

        if (ent.isDeadOrDying()) {
            ent.die(damageSource);
        }
    }

    static boolean prototypeMatch(AbstractMove<?, ?> a, AbstractMove<?, ?> b) {
        if (a.getClass() != b.getClass()) return false;
        if (a.getMoveClass() != b.getMoveClass()) return false;
        if (a.isAerialVariant() != b.isAerialVariant()) return false;
        if (a.isCrouchingVariant() != b.isCrouchingVariant()) return false;
        if (a.getCooldown() != b.getCooldown()) return false;
        if (a.getDuration() != b.getDuration()) return false;
        if (a.getWindup() != b.getWindup()) return false;

        if (a instanceof AbstractSimpleAttack<?, ?> aa) {
            if (b instanceof AbstractSimpleAttack<?, ?> ab) {
                if (aa.getStun() != ab.getStun()) return false;
                if (aa.getOffset() != ab.getOffset()) return false;
                if (aa.getKnockback() != ab.getKnockback()) return false;
            } else {
                return false;
            }
        }

        return true;
    }
}
