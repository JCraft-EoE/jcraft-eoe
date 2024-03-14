package net.arna.jcraft.common.util;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.network.s2c.JExplosionPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.splatter.JSplatterManager;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;

public final class JUtils {
    public static final float RAD_TO_DEG = 0.017453292F;

    public static Vec3d randUnitVec(net.minecraft.util.math.random.Random random) {
        return new Vec3d(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static Vec3d randUnitVec(java.util.Random random) {
        return new Vec3d(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
    }

    public static void addVelocity(Entity entity, Vec3d vel) {
        GravityChangerAPI.addWorldVelocity(entity, vel.x, vel.y, vel.z);
        syncVelocityUpdate(entity);
    }
    public static void addVelocity(Entity entity, double x, double y, double z) {
        GravityChangerAPI.addWorldVelocity(entity, x, y, z);
        syncVelocityUpdate(entity);
    }
    public static void setVelocity(Entity entity, Vec3d vel) {
        entity.setVelocity(vel.x, vel.y, vel.z);
        syncVelocityUpdate(entity);
    }
    public static void setVelocity(Entity entity, double x, double y, double z) {
        entity.setVelocity(x, y, z);
        syncVelocityUpdate(entity);
    }

    public static void syncVelocityUpdate(Entity entity) {
        entity.velocityModified = true;
        if (entity instanceof ServerPlayerEntity serverPlayer)
            serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
    }

    public static boolean canAct(LivingEntity living) {
        StatusEffectInstance stun = living.getStatusEffect(JStatusRegistry.DAZED);
        return stun == null || stun.getAmplifier() == 2;
    }

    public static void displayHitbox(World world, Vec3d min, Vec3d max) {
        displayHitbox(world, new Box(min, max));
    }

    public static void displayHitbox(World world, Box box) {
        displayHitboxes(world, Set.of(box));
    }

    public static void displayHitboxes(World world, Collection<Box> boxes) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(1);
        buf.writeVarInt(boxes.size());

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Box box : boxes) {
            if (box.minX < minX) minX = box.minX;
            if (box.minY < minY) minY = box.minY;
            if (box.minZ < minZ) minZ = box.minZ;

            if (box.maxX < maxX) maxX = box.maxX;
            if (box.maxY < maxY) maxY = box.maxY;
            if (box.maxZ < maxZ) maxZ = box.maxZ;

            buf.writeDouble(box.minX);
            buf.writeDouble(box.minY);
            buf.writeDouble(box.minZ);

            buf.writeDouble(box.maxX);
            buf.writeDouble(box.maxY);
            buf.writeDouble(box.maxZ);
        }

        Box entireBox = new Box(minX, minY, minZ, maxX, maxY, maxZ).expand(48);
        world.getPlayers().stream()
                .filter(p -> p instanceof ServerPlayerEntity)
                .map(p -> (ServerPlayerEntity) p)
                .filter(p -> entireBox.contains(p.getPos()))
                .forEach(p -> ServerChannelFeedbackPacket.send(p, buf));
    }

    // Defaults to LivingEntity
    public static Set<LivingEntity> generateHitbox(World world, Vec3d center, double hitboxSize, Set<Entity> except) {
        return generateHitbox(world, center, hitboxSize, e -> !except.contains(e));
    }

    public static Set<LivingEntity> generateHitbox(World world, Vec3d center, double hitboxSize, Predicate<Entity> predicate) {
        double size = hitboxSize / 2;

        Vec3d v1 = center.subtract(size, size, size);
        Vec3d v2 = center.add(size, size, size);

        if (size > 0) displayHitbox(world, v1, v2);

        List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(predicate));
        Set<LivingEntity> toReturn = new HashSet<>(hit);
        for (LivingEntity l : hit)
            //JCraft.LOGGER.info("Stand: " + stand);
            if (l instanceof StandEntity<?, ?> stand && stand.hasUser())
                toReturn.add(stand.getUserOrThrow());

        return toReturn;
    }

    public static JSpec<?,?> getSpec(PlayerEntity player) {
        return JComponents.getSpecData(player).getSpec();
    }

    public static void serverPlaySound(SoundEvent sound, ServerWorld serverWorld, Vec3d pos) {
        serverPlaySound(sound, serverWorld, pos, 32);
    }

    public static void serverPlaySound(SoundEvent sound, ServerWorld serverWorld, Vec3d pos, double radius) {
        PlayerLookup.around(serverWorld, pos, radius).forEach(
                serverPlayer -> serverPlayer.networkHandler.sendPacket(
                        new PlaySoundS2CPacket(sound, SoundCategory.PLAYERS, pos.x, pos.y, pos.z, 1, 1, 0)
                )
        );
    }

    public static BlockHitResult genericBlockRaycast(World world, Entity entity, double range, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        Vec3d eyePos = RotationUtil.vecPlayerToWorld(entity.getEyePos(), GravityChangerAPI.getGravityDirection(entity));
        return world.raycast(
                new RaycastContext(
                        eyePos,
                        eyePos.add(entity.getRotationVector().multiply(range)),
                        shapeType,
                        fluidHandling,
                        entity
                )
        );
    }

    public static Vec3d raycastAll(Entity entity, Vec3d start, Vec3d end, RaycastContext.FluidHandling fluidHandling) {
        World world = entity.getWorld();
        double rangeSquared = start.squaredDistanceTo(end);

        EntityHitResult eHit = ProjectileUtil.raycast(entity, start, end,
                entity.getBoundingBox().expand(rangeSquared), // Not technically necessary but doesn't matter
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR,
                rangeSquared
        );
        boolean entityHit = eHit != null && eHit.getType() == HitResult.Type.ENTITY;
        HitResult bHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, fluidHandling, entity));

        Vec3d blockPos = bHit.getPos();

        if (entityHit && !eHit.getEntity().isConnectedThroughVehicle(entity)) {
            Vec3d entityPos = eHit.getPos();
            if (blockPos.squaredDistanceTo(start) > entityPos.squaredDistanceTo(start))
                return entityPos;
            else
                return blockPos;
        }

        return blockPos;
    }

    public static Direction getLookDirection(Entity entity) {
        Vec3d rotVec = entity.getRotationVector();

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
        if (ent instanceof StandEntity<?, ?> stand && stand.hasUser())
            return stand.getUser();
        return ent;
    }

    public static void projectileDamageLogic(ProjectileEntity proj, World world, Entity ent, Vec3d kb, int stunT, int stunType, boolean overrideStun, float damage, int blockstun, HitPropertyComponent.HitAnimation hitAnimation) {
        projectileDamageLogic(proj, world, ent, kb, stunT, stunType, overrideStun, damage, blockstun, hitAnimation, false, false);
    }

    public static void projectileDamageLogic(ProjectileEntity proj, World world, Entity ent, Vec3d kb, int stunT, int stunType, boolean overrideStun, float damage, int blockstun, HitPropertyComponent.HitAnimation hitAnimation, boolean unblockable, boolean canBackstab) {
        if (world.isClient) return;
        Objects.requireNonNull(proj, "Attempted to run ProjectileDamageLogic with invalid projectile in world " + world);
        Entity owner = proj.getOwner();
        DamageSource source;
        if (owner == null)
            source = DamageSource.GENERIC;
        else source = DamageSource.thrownProjectile(proj, owner);

        if (ent instanceof LivingEntity living) {
            LivingEntity target = living;
            if (ent instanceof StandEntity<?, ?> stand)
                target = stand.getUser();
            damageLogic(world, target, kb, stunT, stunType, overrideStun, damage, false, blockstun, source, owner, hitAnimation, canBackstab, unblockable);
        }

        if (ent instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(source, damage);
    }

    //To check method ms usage, use spark[something]
    public static boolean isBlocking(LivingEntity entity) {
        if (entity instanceof StandEntity<?, ?> stand) return stand.blocking;
        StandEntity<?, ?> stand = JUtils.getStand(entity);
        return stand != null && stand.blocking;
    }

    public static void stopTick(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.prevBodyYaw = livingEntity.bodyYaw;
            livingEntity.prevHeadYaw = livingEntity.headYaw;
            livingEntity.lastHandSwingProgress = livingEntity.handSwingProgress;
            livingEntity.lastLimbDistance = livingEntity.limbDistance;
        }

        entity.prevX = entity.getX();
        entity.prevY = entity.getY();
        entity.prevZ = entity.getZ();

        entity.lastRenderX = entity.getX();
        entity.lastRenderY = entity.getY();
        entity.lastRenderZ = entity.getZ();

        entity.prevPitch = entity.getPitch();
        entity.prevYaw = entity.getYaw();

        entity.prevHorizontalSpeed = entity.horizontalSpeed;
    }

    public static Vec3d deltaPos(@NotNull Entity ent) {
        return new Vec3d(
                ent.getX() - ent.prevX,
                ent.getY() - ent.prevY,
                ent.getZ() - ent.prevZ
        );
    }

    public static List<BlockInfo> collectBlockInfo(World world, BlockPos origin, int radius) {
        List<BlockInfo> infoList = new ArrayList<>();

        boolean[][] array = new boolean[radius * 2 + 1][radius * 2 + 1];

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (int y = originY + radius; y >= originY - radius; y--) {
            for (int x = originX - radius; x <= originX + radius; x++) {
                for (int z = originZ - radius; z <= originZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(x - originX, 2) + Math.pow(y - originY, 2) + Math.pow(z - originZ, 2));
                    if (!(distance <= radius)) continue;

                    double skipProbability = (distance / radius);
                    if (!(world.getRandom().nextDouble() > skipProbability / 2)) continue;

                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    int x0 = x - originX + radius;
                    int z0 = z - originZ + radius;
                    if (!state.isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID) || array[x0][z0])
                        continue;

                    array[x0][z0] = true;

                    BlockInfo info = new BlockInfo(state, pos);
                    infoList.add(info);
                }
            }
        }

        return infoList;
    }

    public static void explode(World world, double x, double y, double z, float power, JExplosionModifier modifier) {
        explode(world, null, x, y, z, power, modifier);
    }

    public static void explode(World world, @Nullable Entity entity, double x, double y, double z, float power, JExplosionModifier modifier) {
        if (modifier == null) {
            world.createExplosion(entity, x, y, z, power, Explosion.DestructionType.DESTROY);
            return;
        }

        Explosion explosion = new Explosion(world, entity, x, y, z, power);
        ((IJExplosion) explosion).jcraft$setModifier(modifier);
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);

        if (world.isClient) return;
        for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, new Vec3d(x, y, z), 64))
            JExplosionPacket.send(player, x, y, z, power, explosion, modifier);
    }

    /**
     * Supposed to be used in a stream.
     * Turns every object in the stream into a pair of its index in the stream and the object.
     * @return A function that turns every object into an enumerated pair.
     * @param <T> The type of the object
     */
    public static <T> Function<T, IntObjectPair<T>> enumerate() {
        AtomicInteger index = new AtomicInteger();
        return t -> IntObjectPair.of(index.getAndIncrement(), t);
    }

    public static JSplatterManager getSplatterManager(World world) {
        return ((IJSplatterManagerHolder) world).jcraft$getSplatterManager();
    }

    @Nullable
    public static StandEntity<?, ?> getStand(LivingEntity entity) {
        return entity == null ? null : entity instanceof StandEntity<?, ?> stand ? stand : JComponents.getStandData(entity).getStand();
    }

    public static boolean isAffectedByTimeStop(Entity entity) {
        return JComponents.getTimeStopData(entity).getTicks() > 0;
    }

    public static boolean canDamage(DamageSource damageSource, Entity ent) {
        return ent != null && ent.isAlive() && ent.isAttackable() && !ent.isInvulnerableTo(damageSource) &&
                !(ent instanceof ArmorStandEntity armorStand && armorStand.isMarker());
    }

    /**
     * Cancels the Spec and Stand moves for a specified {@link LivingEntity}
     * @param livingEntity Entity to cancel the moves of
     */
    public static void cancelMoves(LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity player) {
            JSpec<?, ?> spec = JUtils.getSpec(player);
            if (spec != null) spec.cancelMove();
        }

        StandEntity<?, ?> stand = JUtils.getStand(livingEntity);
        if (stand != null) stand.cancelMove();
    }

    /**
     * Converts a rotation vector to polar coordinates.
     *
     * @param rotationVector The rotation vector to convert
     * @return A Vec2f containing the theta and phi angles
     * @see Vec3d#fromPolar(Vec2f)
     */
    public static Vec2f rotationVectorToPolar(Vec3d rotationVector) {
        double x = rotationVector.x;
        double y = rotationVector.y;
        double z = rotationVector.z;

        // Calculate yaw (horizontal rotation)
        double yaw = Math.atan2(x, z) * (180 / Math.PI);

        // Calculate pitch (vertical rotation)
        double pitch = Math.atan2(rotationVector.horizontalLength(), -y) * (180 / Math.PI);

        return new Vec2f(90f - (float) pitch, (float) -yaw);
    }

    public static MobEntity mobCloneOf(MobEntity original) {
        EntityType<?> entityType = original.getType();
        MobEntity newMob = (MobEntity) entityType.create(original.getWorld());

        if (newMob == null) {
            JCraft.LOGGER.error("Failed to create clone mob of type " + entityType + " in world " + original.getWorld());
            return null;
        }

        // Copy properties
        newMob.setBaby(original.isBaby());
        if (original.hasCustomName()) {
            newMob.setCustomName(original.getCustomName());
            newMob.setCustomNameVisible(original.isCustomNameVisible());
        }

        newMob.age = original.age;

        // No duping
        newMob.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
        newMob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0);

        newMob.setEquipmentDropChance(EquipmentSlot.HEAD, 0);
        newMob.setEquipmentDropChance(EquipmentSlot.CHEST, 0);
        newMob.setEquipmentDropChance(EquipmentSlot.LEGS, 0);
        newMob.setEquipmentDropChance(EquipmentSlot.FEET, 0);

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

        if (type.isIn(EntityTypeTags.RAIDERS))
            return 1.5f;

        if (type.isIn(EntityTypeTags.SKELETONS))
            return 0;

        if (type.isIn(EntityTypeTags.AXOLOTL_HUNT_TARGETS)) // Fishes
            return 0.25f;

        if (uniqueBloodMults.containsKey(type))
            return uniqueBloodMults.get(type);

        return 0;
    }
}
