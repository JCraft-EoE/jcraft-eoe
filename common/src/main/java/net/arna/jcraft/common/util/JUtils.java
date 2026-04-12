package net.arna.jcraft.common.util;

import com.google.common.base.MoreObjects;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.spec.JSpecHolder;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.ItemTossProjectile;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.entity.projectile.ScalpelProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.item.KnifeBundleItem;
import net.arna.jcraft.common.item.KnifeItem;
import net.arna.jcraft.common.item.ScalpelItem;
import net.arna.jcraft.common.network.s2c.JExplosionPacket;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.splatter.JSplatterManager;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class JUtils {
    public static final float DEG_TO_RAD = 0.017453292F;
    public static final float RAD_TO_DEG = 57.2957795131F;

    public static Vec3 randUnitVec(net.minecraft.util.RandomSource random) {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static Vec3 randUnitVec(Random random) {
        return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static void addVelocity(Entity entity, Vec3 vel) {
        GravityChangerAPI.addWorldVelocity(entity, vel.x, vel.y, vel.z);
        syncVelocityUpdate(entity);
    }

    public static void addVelocity(Entity entity, double x, double y, double z) {
        GravityChangerAPI.addWorldVelocity(entity, x, y, z);
        syncVelocityUpdate(entity);
    }

    public static void setVelocity(Entity entity, Vec3 vel) {
        entity.setDeltaMovement(vel.x, vel.y, vel.z);
        syncVelocityUpdate(entity);
    }

    public static void setVelocity(Entity entity, double x, double y, double z) {
        entity.setDeltaMovement(x, y, z);
        syncVelocityUpdate(entity);
    }

    public static void syncVelocityUpdate(Entity entity) {
        entity.hurtMarked = true;
        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    public static boolean canAct(LivingEntity living) {
        MobEffectInstance stun = living.getEffect(JStatusRegistry.DAZED.get());
        return stun == null || stun.getAmplifier() == 2;
    }

    public static boolean canJump(LivingEntity living) {
        if (!canAct(living)) return false;
        if (living.hasEffect(JStatusRegistry.KNOCKDOWN.get())) return false;

        StandEntity<?, ?> stand = JUtils.getStand(living);
        return (stand == null || !stand.isRemoteAndControllable());
    }

    public static void displayHitbox(Level world, Vec3 min, Vec3 max) {
        displayHitbox(world, new AABB(min, max));
    }

    public static void displayHitbox(Level world, AABB box) {
        displayHitboxes(world, Set.of(box));
    }

    public static void displayHitboxes(Level world, Collection<AABB> boxes) {
        if (world instanceof ServerLevel serverLevel) {
            final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeShort(1);
            buf.writeVarInt(boxes.size());

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;

            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            double maxZ = Double.MIN_VALUE;

            for (AABB box : boxes) {
                if (box.minX < minX) {
                    minX = box.minX;
                }
                if (box.minY < minY) {
                    minY = box.minY;
                }
                if (box.minZ < minZ) {
                    minZ = box.minZ;
                }

                if (box.maxX < maxX) {
                    maxX = box.maxX;
                }
                if (box.maxY < maxY) {
                    maxY = box.maxY;
                }
                if (box.maxZ < maxZ) {
                    maxZ = box.maxZ;
                }

                buf.writeDouble(box.minX);
                buf.writeDouble(box.minY);
                buf.writeDouble(box.minZ);

                buf.writeDouble(box.maxX);
                buf.writeDouble(box.maxY);
                buf.writeDouble(box.maxZ);
            }

            final AABB entireBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(48);

            ServerChannelFeedbackPacket.send(
                    serverLevel.getPlayers(p -> entireBox.contains(p.position())),
                    buf
            );
        } else {
            throw new IllegalArgumentException("JUtils.displayHitboxes() must be called serverside!");
        }
    }

    // Defaults to LivingEntity
    public static Set<LivingEntity> generateHitbox(Level world, Vec3 center, double hitboxSize, Set<Entity> except) {
        return generateHitbox(world, center, hitboxSize, e -> !except.contains(e));
    }

    public static Set<LivingEntity> generateHitbox(Level world, Vec3 center, double hitboxSize, Predicate<Entity> predicate) {
        double size = hitboxSize / 2;

        Vec3 v1 = center.subtract(size, size, size);
        Vec3 v2 = center.add(size, size, size);

        if (size > 0) {
            displayHitbox(world, v1, v2);
        }

        List<LivingEntity> hit = world.getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(predicate));
        Set<LivingEntity> toReturn = new HashSet<>(hit);
        for (LivingEntity l : hit)
        //JCraft.LOGGER.info("Stand: " + stand);
        {
            if (l instanceof StandEntity<?, ?> stand && stand.hasUser()) {
                toReturn.add(stand.getUserOrThrow());
            }
        }

        return toReturn;
    }

    public static Set<LivingEntity> generateHitboxNoDisplay(Level world, Vec3 center, double hitboxSize, Predicate<Entity> predicate) {
        double size = hitboxSize / 2;

        Vec3 v1 = center.subtract(size, size, size);
        Vec3 v2 = center.add(size, size, size);

        List<LivingEntity> hit = world.getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(predicate));
        Set<LivingEntity> toReturn = new HashSet<>(hit);
        for (LivingEntity l : hit)
        {
            if (l instanceof StandEntity<?, ?> stand && stand.hasUser()) {
                toReturn.add(stand.getUserOrThrow());
            }
        }

        return toReturn;
    }

    public static JSpec<?, ?> getSpec(LivingEntity livingEntity) {
        return JComponentPlatformUtils.getSpecData(livingEntity).getSpec();
    }

    public static void serverPlaySound(SoundEvent sound, ServerLevel serverWorld, Vec3 pos) {
        serverPlaySound(sound, serverWorld, pos, 32);
    }

    public static void serverPlaySound(SoundEvent sound, ServerLevel serverWorld, Vec3 pos, double radius) {
        around(serverWorld, pos, radius).forEach(
                serverPlayer -> serverPlayer.connection.send(
                        new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), SoundSource.PLAYERS, pos.x, pos.y, pos.z, 1, 1, 0)
                )
        );
    }

    public static BlockHitResult genericBlockRaycast(Level world, Entity entity, double range, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) {
        Vec3 eyePos = entity.position().add(GravityChangerAPI.getEyeOffset(entity));
        return world.clip(
                new ClipContext(
                        eyePos,
                        eyePos.add(entity.getLookAngle().scale(range)),
                        shapeType,
                        fluidHandling,
                        entity
                )
        );
    }

    public static HitResult raycastAll(Entity entity, Vec3 start, Vec3 end, ClipContext.Fluid fluidHandling) {
        return raycastAll(entity, start, end, fluidHandling, null);
    }

    /**
     * @return The closer {@link EntityHitResult} or {@link BlockHitResult}, defaulting to the BlockHitResult
     * if the distance is equal or no entity was hit.
     */
    public static HitResult raycastAll(Entity entity, Vec3 start, Vec3 end, ClipContext.Fluid fluidHandling, Predicate<Entity> entityPredicate) {
        Level world = entity.level();
        double rangeSquared = start.distanceToSqr(end);

        Predicate<Entity> combined = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> !e.isPassengerOfSameVehicle(entity));
        if (entityPredicate != null) {
            combined = combined.and(entityPredicate);
        }

        EntityHitResult eHit = ProjectileUtil.getEntityHitResult(entity, start, end,
                entity.getBoundingBox().inflate(rangeSquared), // Not technically necessary but doesn't matter
                combined,
                rangeSquared
        );
        boolean entityHit = eHit != null && eHit.getType() == HitResult.Type.ENTITY;
        HitResult bHit = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, fluidHandling, entity));

        Vec3 blockPos = bHit.getLocation();

        if (entityHit) {
            Vec3 entityPos = eHit.getLocation();
            if (blockPos.distanceToSqr(start) > entityPos.distanceToSqr(start)) {
                return eHit;
            } else {
                return bHit;
            }
        }

        return bHit;
    }

    public static Direction getLookDirection(Entity entity) {
        Vec3 rotVec = entity.getLookAngle();

        double x = rotVec.x;
        double y = rotVec.y;
        double z = rotVec.z;

        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        Direction direction = Direction.DOWN;
        if (absX > absY && absX > absZ) {
            direction = x > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY > absX && absY > absZ) {
            direction = y > 0 ? Direction.UP : Direction.DOWN;
        } else if (absZ > absX && absZ > absY) {
            direction = z > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        return direction;
    }

    /**
     * @return the stand user if the specified entity is a {@link StandEntity}
     */
    public static LivingEntity getUserIfStand(LivingEntity ent) {
        if (ent instanceof StandEntity<?, ?> stand && stand.hasUser()) {
            return stand.getUser();
        }
        return ent;
    }

    /**
     * @return the stand user if the specified entity is a {@link StandEntity}
     */
    public static Entity getUserIfStand(Entity ent) {
        if (ent instanceof StandEntity<?, ?> stand && stand.hasUser()) {
            return stand.getUser();
        }
        return ent;
    }

    public static void projectileDamageLogic(Projectile proj, Level world, Entity ent, Vec3 kb, int stunTicks, int stunType, boolean overrideStun,
                                             float damage, int blockstun, CommonHitPropertyComponent.HitAnimation hitAnimation) {
        projectileDamageLogic(proj, world, ent, kb, stunTicks, stunType, overrideStun, damage, blockstun, hitAnimation, false, false, true);
    }

    public static void projectileDamageLogic(Projectile proj, Level world, Entity ent, Vec3 kb, int stunTicks, int stunType, boolean overrideStun,
                                             float damage, int blockstun, CommonHitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        projectileDamageLogic(proj, world, ent, kb, stunTicks, stunType, overrideStun, damage, blockstun, hitAnimation, canBackstab, unblockable, true);
    }

    public static void projectileDamageLogic(Projectile proj, Level world, Entity ent, Vec3 kb, int stunTicks, int stunType, boolean overrideStun,
                                             float damage, int blockstun, CommonHitPropertyComponent.HitAnimation hitAnimation,
                                             boolean canBackstab, boolean unblockable, boolean cancelMoves) {
        final Entity owner = proj.getOwner();

        DamageSource source = (owner == null) ?
                JDamageSources.create(world, DamageTypes.GENERIC) :
                JDamageSources.create(world, DamageTypes.MOB_PROJECTILE, proj, owner);

        projectileDamageLogic(proj, world, ent, new AttackData(
                kb, stunTicks, stunType, overrideStun, damage, true,
                blockstun, source, owner, hitAnimation, null,
                canBackstab, unblockable, cancelMoves
        ));
    }

    public static void projectileDamageLogic(Projectile proj, Level world, Entity ent, AttackData attackData) {
        if (world.isClientSide) {
            return;
        }
        Objects.requireNonNull(proj, "Attempted to run ProjectileDamageLogic with invalid projectile in world " + world);

        if (ent instanceof LivingEntity living) {
            LivingEntity target = living;
            if (ent instanceof StandEntity<?, ?> stand) {
                target = stand.getUser();
            }

            damageLogic(world, target, attackData);
        }

        if (ent instanceof EndCrystal endCrystal) {
            endCrystal.hurt(attackData.source(), attackData.damage());
        }
    }

    public static float getDamageThroughArmor(final float damage, float totalArmor, float toughnessAttribute) {
        if (totalArmor > 20.0f) totalArmor = 20.0f;
        if (toughnessAttribute > 12.0f) toughnessAttribute = 12.0f;
        return CombatRules.getDamageAfterAbsorb(damage, totalArmor, toughnessAttribute);
    }

    //To check method ms usage, use spark[something]
    public static boolean isBlocking(LivingEntity entity) {
        if (entity instanceof StandEntity<?, ?> stand) {
            return stand.blocking;
        }
        StandEntity<?, ?> stand = JUtils.getStand(entity);
        return stand != null && stand.blocking;
    }

    public static void stopTick(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRotO = livingEntity.yBodyRot;
            livingEntity.yHeadRotO = livingEntity.yHeadRot;
            livingEntity.oAttackAnim = livingEntity.attackAnim;
            //TODO check if this moved or changed livingEntity.lastLimbDistance = livingEntity.limbDistance;
        }

        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();

        entity.xOld = entity.getX();
        entity.yOld = entity.getY();
        entity.zOld = entity.getZ();

        entity.xRotO = entity.getXRot();
        entity.yRotO = entity.getYRot();

        entity.walkDistO = entity.walkDist;
    }

    /**
     * @return the change in position for an entity between the current and last tick.
     */
    public static Vec3 deltaPos(@NonNull Entity ent) {
        return new Vec3(
                ent.getX() - ent.xo,
                ent.getY() - ent.yo,
                ent.getZ() - ent.zo
        );
    }

    public static List<BlockInfo> collectBlockInfo(Level world, BlockPos origin, int radius) {
        List<BlockInfo> infoList = new ArrayList<>();

        boolean[][] array = new boolean[radius * 2 + 1][radius * 2 + 1];

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (int y = originY + radius; y >= originY - radius; y--) {
            for (int x = originX - radius; x <= originX + radius; x++) {
                for (int z = originZ - radius; z <= originZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(x - originX, 2) + Math.pow(y - originY, 2) + Math.pow(z - originZ, 2));
                    if (!(distance <= radius)) {
                        continue;
                    }

                    double skipProbability = (distance / radius);
                    if (!(world.getRandom().nextDouble() > skipProbability / 2)) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    int x0 = x - originX + radius;
                    int z0 = z - originZ + radius;
                    if (!state.isFaceSturdy(world, pos, Direction.UP, SupportType.RIGID) || array[x0][z0]) {
                        continue;
                    }

                    array[x0][z0] = true;

                    BlockInfo info = new BlockInfo(state, pos);
                    infoList.add(info);
                }
            }
        }

        return infoList;
    }

    public static void explode(Level world, double x, double y, double z, float power, JExplosionModifier modifier) {
        explode(world, null, x, y, z, power, modifier);
    }

    public static void explode(Level world, @Nullable Entity entity, double x, double y, double z, float power, JExplosionModifier modifier) {
        if (modifier == null) {
            world.explode(entity, x, y, z, power, Level.ExplosionInteraction.MOB);
            return;
        }

        Explosion explosion = new Explosion(world, entity, x, y, z, power,
                MoreObjects.firstNonNull(modifier.getCreateFire(), false), modifier.getBlockInteraction());
        ((IJExplosion) explosion).jcraft$setModifier(modifier);
        explosion.explode();
        explosion.finalizeExplosion(true);

        if (world.isClientSide) {
            return;
        }
        for (ServerPlayer player : around((ServerLevel) world, new Vec3(x, y, z), 64)) {
            JExplosionPacket.send(player, x, y, z, power, explosion, modifier);
        }
    }

    /**
     * Supposed to be used in a stream.
     * Turns every object in the stream into a pair of its index in the stream and the object.
     *
     * @param <T> The type of the object
     * @return A function that turns every object into an enumerated pair.
     */
    public static <T> Function<T, IntObjectPair<T>> enumerate() {
        AtomicInteger index = new AtomicInteger();
        return t -> IntObjectPair.of(index.getAndIncrement(), t);
    }

    public static JSplatterManager getSplatterManager(Level world) {
        return ((IJSplatterManagerHolder) world).jcraft$getSplatterManager();
    }

    @Nullable
    public static StandEntity<?, ?> getStand(LivingEntity entity) {
        return entity == null ? null : entity instanceof StandEntity<?, ?> stand ? stand : JComponentPlatformUtils.getStandComponent(entity).getStand();
    }

    public static boolean isAffectedByTimeStop(Entity entity) {
        return JComponentPlatformUtils.getTimeStopData(entity)
                .map(data -> data.getTicks() > 0)
                .orElse(false);
    }

    public static boolean canDamage(DamageSource damageSource, Entity ent) {
        return ent != null && ent.isAlive() && ent.isAttackable() && !ent.isInvulnerableTo(damageSource) &&
                !(ent instanceof ArmorStand armorStand && armorStand.isMarker());
    }

    /**
     * Cancels the Spec and Stand moves for a specified {@link LivingEntity}
     *
     * @param livingEntity Entity to cancel the moves of
     */
    public static void cancelMoves(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec != null) {
                spec.cancelMove();
            }
        }

        StandEntity<?, ?> stand = JUtils.getStand(livingEntity);
        if (stand != null) {
            stand.cancelMove();
        }
    }

    /**
     * Converts a rotation vector to polar coordinates.
     *
     * @param rotationVector The rotation vector to convert
     * @return A Vec2f containing the theta and phi angles
     * @see Vec3#directionFromRotation(Vec2)
     */
    public static Vec2 rotationVectorToPolar(Vec3 rotationVector) {
        double x = rotationVector.x;
        double y = rotationVector.y;
        double z = rotationVector.z;

        // Calculate yaw (horizontal rotation)
        double yaw = Math.atan2(x, z) * (180 / Math.PI);

        // Calculate pitch (vertical rotation)
        double pitch = Math.atan2(rotationVector.horizontalDistance(), -y) * (180 / Math.PI);

        return new Vec2(90f - (float) pitch, (float) -yaw);
    }

    public static Mob mobCloneOf(Mob original) {
        EntityType<?> entityType = original.getType();
        Mob newMob = (Mob) entityType.create(original.level());

        if (newMob == null) {
            JCraft.LOGGER.error("Failed to create clone mob of type " + entityType + " in world " + original.level());
            return null;
        }

        // Copy properties
        newMob.setBaby(original.isBaby());
        if (original.hasCustomName()) {
            newMob.setCustomName(original.getCustomName());
            newMob.setCustomNameVisible(original.isCustomNameVisible());
        }

        newMob.tickCount = original.tickCount;

        // No duping
        newMob.setDropChance(EquipmentSlot.MAINHAND, 0);
        newMob.setDropChance(EquipmentSlot.OFFHAND, 0);

        newMob.setDropChance(EquipmentSlot.HEAD, 0);
        newMob.setDropChance(EquipmentSlot.CHEST, 0);
        newMob.setDropChance(EquipmentSlot.LEGS, 0);
        newMob.setDropChance(EquipmentSlot.FEET, 0);

        return newMob;
    }

    private static final Map<EntityType<?>, Float> uniqueBloodMults = Map.ofEntries(
            Map.entry(EntityType.ZOMBIE, 1.0f),
            Map.entry(EntityType.ZOMBIE_VILLAGER, 1.0f),
            Map.entry(EntityType.ZOMBIE_HORSE, 1.0f),

            Map.entry(EntityType.ZOGLIN, 0.5f),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, 0.5f),

            Map.entry(EntityType.HUSK, 0.1f),

            Map.entry(EntityType.VILLAGER, 1.5f),
            Map.entry(EntityType.PLAYER, 1.5f)
    );

    public static float getBloodMult(LivingEntity entity) {
        EntityType<?> type = entity.getType();

        if (type.is(JTagRegistry.BLOODLESS_ENTITIES)) {
            return 0;
        }

        if (type.is(EntityTypeTags.RAIDERS)) {
            return 1.5f;
        }

        if (type.is(EntityTypeTags.AXOLOTL_HUNT_TARGETS)) // Fishes
        {
            return 0.25f;
        }

        if (entity instanceof Animal) {
            return 0.5f;
        }

        if (uniqueBloodMults.containsKey(type)) {
            return uniqueBloodMults.get(type);
        }

        if (!entity.isInvertedHealAndHarm()) {
            return entity.getMaxHealth() / 20.0f;
        }

        return 0;
    }

    public static boolean canHoldMove(ServerPlayer player, MoveInputType type) {
        StandEntity<?, ?> stand = JUtils.getStand(player);
        JSpec<?, ?> spec;
        return stand != null && stand.canHoldMove(type) ||
                (spec = JUtils.getSpec(player)) != null && spec.canHoldMove(type) ||
                type.isHoldable(stand != null && stand.isStandby());
    }

    /**
     * Shoots a projectile without interference from GravityAPI.
     *
     * @param projectile
     * @param shooter    Entity this projectile inherits velocity from
     * @param pitch      in degrees
     * @param yaw        in degrees
     * @param roll       in degrees
     * @param speed      in meters per tick
     * @param divergence Spread, done via a {@link Vec3} of {@link net.minecraft.util.RandomSource#triangle(double, double)} calls
     */
    public static void shoot(@NonNull Projectile projectile, @Nullable Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float f = -Mth.sin(yaw * DEG_TO_RAD) * Mth.cos(pitch * DEG_TO_RAD);
        float g = -Mth.sin((pitch + roll) * DEG_TO_RAD);
        float h = Mth.cos(yaw * DEG_TO_RAD) * Mth.cos(pitch * DEG_TO_RAD);
        projectile.shoot(f, g, h, speed, divergence);
        if (shooter != null) {
            Vec3 vec3d = shooter.getDeltaMovement();
            projectile.setDeltaMovement(projectile.getDeltaMovement().add(vec3d.x, shooter.onGround() ? 0.0 : vec3d.y, vec3d.z));
        }
    }

    public static int PLAYER_ANIMATION_DIST = 256;

    /**
     * Plays an animation for the entity if their spec is currently not doing a move.
     * Higher priority than the dash animation.
     * @return Whether the animation was played.
     */
    public static boolean playAnimIfUnoccupied(LivingEntity living, String animation) {
        JSpec<?, ?> spec = JComponentPlatformUtils.getSpecData(living).getSpec();
        if (spec != null && spec.moveStun > 0) return false;
        playAnim(living, animation);
        return true;
    }

    public static void playAnim(LivingEntity living, String animation) {
        if (living instanceof ServerPlayer player) {
            around(player.serverLevel(), player.position(), PLAYER_ANIMATION_DIST).forEach(p -> PlayerAnimPacket.send(player, p, animation));
        } else if (living instanceof JSpecHolder specHolder) { specHolder.setAnimation(animation, 1.0f); }
    }

    public static Collection<ServerPlayer> around(ServerLevel world, Vec3 pos, double radius) {
        double radiusSq = radius * radius;

        List<ServerPlayer> list = new ArrayList<>();
        for (ServerPlayer p : world.players()) {
            if (p.distanceToSqr(pos) <= radiusSq) {
                list.add(p);
            }
        }
        return list;
    }

    public static Collection<ServerPlayer> all(MinecraftServer server) {
        Objects.requireNonNull(server, "The server cannot be null");

        // return an immutable collection to guard against accidental removals.
        if (server.getPlayerList() != null) {
            return Collections.unmodifiableCollection(server.getPlayerList().getPlayers());
        }

        return Collections.emptyList();
    }

    /**
     * @return All {@link ServerPlayer}s tracking the specified {@code entity}.
     * If the {@code entity} is a Player, there is <u>NO GUARANTEE</u> that the returned {@code Collection<ServerPlayer>} contains them.
     */
    public static @NonNull Collection<ServerPlayer> tracking(@NonNull final Entity entity) {
        if (entity.level().getChunkSource() instanceof ServerChunkCache serverChunkCache) { // Is serverside?
            final ChunkMap storage = serverChunkCache.chunkMap;
            final ChunkMap.TrackedEntity tracker = storage.entityMap.get(entity.getId());

            // return an immutable collection to guard against accidental removals.
            if (tracker != null) {
                return tracker.seenBy
                        .stream()
                        .map(ServerPlayerConnection::getPlayer)
                        .collect(Collectors.toUnmodifiableSet());
            }

            return Collections.emptySet();
        }

        throw new IllegalArgumentException("Only supported on server worlds!");
    }

    public static Vector2f getLookPY(Vec3 origin, Vec3 target) {
        final double d = target.x - origin.x, e = target.y - origin.y, f = target.z - origin.z;
        final double g = Math.sqrt(d * d + f * f);

        final float yaw = Mth.wrapDegrees((float) (Mth.atan2(-f, -d) * 57.2957763671875) - 90.0F); // deg; X, Z
        final float pitch = Mth.wrapDegrees((float) (Mth.atan2(-e, -g) * 57.2957763671875)); // deg; Y, len

        return new Vector2f(pitch, yaw);
    }

    public static Vec3 getLookVector(Vec3 origin, Vec3 target) {
        Vector2f pitchYaw = getLookPY(origin, target);
        final float pitch = pitchYaw.x, yaw = pitchYaw.y;

        return new Vec3(
                -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F),
                -Mth.sin((pitch) * 0.017453292F),
                Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F)
        );
    }

    public static boolean isFerrous(@Nullable Entity entity) {
        if (entity == null) return false;

        if (entity.getType().is(JTagRegistry.FERROUS_ENTITIES)) {
            return true;
        }

        final String stringName = entity.getName().toString().toLowerCase(Locale.ROOT);
        return stringName.contains("iron") || stringName.contains("ferro"); // Cross-mod compatibility :D
    }

    public static boolean shouldRenderStandsFor(Player player) {
        // Don't render stands if stand user sight is enabled and the player doesn't have a stand.
        if (JServerConfig.STAND_USER_SIGHT.getValue()) {
            StandType standType = JComponentPlatformUtils.getStandComponent(player).getType();
            return !StandTypeUtil.isNone(standType);
        }

        return true;
    }

    /**
     * Tosses or shoots the specified item.
     */
    public static void tossItem(final LivingEntity shooter, final Level level, final ItemStack itemStack, float velocity, boolean decrement) {
        if (level.isClientSide() || itemStack.isEmpty()) {
            return;
        }
        if (itemStack.getItem() instanceof final ArrowItem arrow) {
            final AbstractArrow arrowEntity = arrow.createArrow(level, itemStack, shooter);
            arrowEntity.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(arrowEntity);
        }
        else if (itemStack.getItem() instanceof SnowballItem) {
            final Snowball snowballEntity = new Snowball(level, shooter);
            snowballEntity.setItem(itemStack);
            snowballEntity.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(snowballEntity);
        }

        else if (itemStack.getItem() instanceof TridentItem) {
            final ThrownTrident thrownTrident = new ThrownTrident(level, shooter, itemStack);
            thrownTrident.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(thrownTrident);
        }
        else if (itemStack.getItem() instanceof ThrowablePotionItem) {
            final ThrownPotion thrownPotion = new ThrownPotion(level, shooter);
            thrownPotion.setItem(itemStack);
            thrownPotion.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(thrownPotion);
        }
        else if (itemStack.getItem() instanceof EggItem) {
            final ThrownEgg thrownEgg = new ThrownEgg(level, shooter);
            thrownEgg.setItem(itemStack);
            thrownEgg.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(thrownEgg);
        }
        else if (itemStack.getItem() instanceof ScalpelItem) {
            final ScalpelProjectile scalpelProjectile = new ScalpelProjectile(level, shooter);
            scalpelProjectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(scalpelProjectile);
        }
        else if (itemStack.getItem() instanceof KnifeBundleItem) {
            for (int i = 0; i < 9; i++) {
                KnifeProjectile knife = new KnifeProjectile(level, shooter);
                knife.setPos(knife.position().add(
                        level.random.triangle(0, 0.5),
                        level.random.triangle(0, 0.5),
                        level.random.triangle(0, 0.5)
                ));
                knife.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 5f);
                level.addFreshEntity(knife);
            }
        }
        else if (itemStack.getItem() instanceof KnifeItem) {
            final KnifeProjectile knifeProjectile = new KnifeProjectile(level, shooter);
            knifeProjectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(knifeProjectile);
        }
        else {
            final AbstractArrow projectile = new ItemTossProjectile(shooter, level, itemStack);
            projectile.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0f, velocity, 1f);
            level.addFreshEntity(projectile);
            shooter.playSound(JSoundRegistry.TOSS.get());
        }
        if (decrement) {
            itemStack.shrink(1);
        }
    }

    public static void tossItem(final Player player) {
        tossItem(player, player.level(), player.getItemInHand(InteractionHand.MAIN_HAND), 1f, !player.getAbilities().instabuild);
    }

    public static double nullSafeDistanceSqr(@Nullable Entity a, @Nullable Entity b) {
        if (a == null || b == null) return Double.POSITIVE_INFINITY;
        return a.distanceToSqr(b);
    }

    /**
     * Returns the minimum of any number of {@code double}s
     */
    public static double min(double... arr) {
        double min = Double.POSITIVE_INFINITY;
        for (double v : arr) {
            if (v < min) min = v;
        }
        return min;
    }

    /**
     * Selects a random element from the given varargs using the provided {@link RandomSource}.
     *
     * @throws IllegalArgumentException If no items are provided.
     */
    @SafeVarargs
    public static <T> @NonNull T chooseRandom(@NonNull RandomSource rng, T... items) {
        if (items == null || items.length == 0) throw new IllegalArgumentException("At least one item must be provided.");
        return items[rng.nextInt(items.length)];
    }

    public static boolean hasAdvancement(final ServerPlayer player, final ResourceLocation advancementLoc) {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        final Advancement advancement = server.getAdvancements().getAdvancement(advancementLoc);
        if (advancement == null) {
            return false;
        }
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static void maySendStandAboutInfo(final ServerPlayer player) {
        if (!hasAdvancement(player, JCraft.id("obtain_stand"))) {
            player.sendSystemMessage(Component.translatable("info.jcraft.first_stand"));
        }
    }

    public static void maySendSpecAboutInfo(final ServerPlayer player) {
        if (!hasAdvancement(player, JCraft.id("obtain_any_spec"))) {
            player.sendSystemMessage(Component.translatable("info.jcraft.first_spec"));
        }
    }
}
