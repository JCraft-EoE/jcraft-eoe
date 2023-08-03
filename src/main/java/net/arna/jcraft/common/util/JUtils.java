package net.arna.jcraft.common.util;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.CreamEntity;
import net.arna.jcraft.common.entity.D4CEntity;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.network.s2c.JExplosionPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.arna.jcraft.common.splatter.JSplatterManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public final class JUtils {
    public static List<DimValues> activeTimestops = new ArrayList<>();

    public static Vec3d adjustForGravity(Vec3d vec, Direction gravDir) {
        switch (gravDir) {
            case UP -> {
                return new Vec3d(vec.x, -vec.y, vec.z);
            }
            case NORTH -> {
                return new Vec3d(vec.x, vec.y, vec.z);
            }
            case SOUTH -> {
                return new Vec3d(vec.x, vec.y, vec.z);
            }
            case WEST -> {
                return new Vec3d(vec.x, vec.y, vec.z);
            }
            case EAST -> {
                return new Vec3d(vec.x, vec.y, vec.z);
            }
            default -> {
                return vec;
            }
        }
    }

    public static void displayHitbox(World world, Vec3d v1, Vec3d v2) {
        if (v1.equals(v2)) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(1);
        buf.writeDouble(v1.x);
        buf.writeDouble(v1.y);
        buf.writeDouble(v1.z);

        buf.writeDouble(v2.x);
        buf.writeDouble(v2.y);
        buf.writeDouble(v2.z);

        Vec3d center = new Box(v1, v2).getCenter();
        world.getPlayers().stream()
                .filter(p -> p instanceof ServerPlayerEntity)
                .map(p -> (ServerPlayerEntity) p)
                .filter(p -> p.getPos().squaredDistanceTo(center) < 48 * 48)
                .forEach(p -> ServerChannelFeedbackPacket.send(p, buf));
    }

    // Specify what type the hitbox searches for
    public static List<? extends Entity> generateHitbox(World world, Vec3d center, double hitboxSize, Class<? extends Entity> entityClass, List<Entity> except) {
        double size = hitboxSize / 2;

        Vec3d v1 = center.subtract(size, size, size);
        Vec3d v2 = center.add(size, size, size);

        if (size > 0) displayHitbox(world, v1, v2);

        List<? extends Entity> hit = world.getEntitiesByClass(entityClass, new Box(v1, v2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        ArrayList<Entity> toReturn = new ArrayList<>(List.copyOf(hit));
        for (Entity e : hit) {
            //JCraft.LOGGER.info(e);
            if (except.contains(e)) {
                toReturn.remove(e);
                continue;
            }
            if (e instanceof StandEntity<?, ?> stand) {
                if (stand.hasUser()) {
                    LivingEntity user = stand.getUser();
                    if (!hit.contains(user))
                        toReturn.add(user);
                }
            }
        }

        return toReturn;
    }

    // Defaults to LivingEntity
    public static List<LivingEntity> generateHitbox(World world, Vec3d center, double hitboxSize, List<Entity> except) {
        double size = hitboxSize / 2;

        Vec3d v1 = center.subtract(size, size, size);
        Vec3d v2 = center.add(size, size, size);

        if (size > 0) displayHitbox(world, v1, v2);

        List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        ArrayList<LivingEntity> toReturn = new ArrayList<>(List.copyOf(hit));
        for (LivingEntity l : hit) {
            if (except != null && except.contains(l)) {
                //JCraft.LOGGER.info("Removing: " + l);
                toReturn.remove(l);
                continue;
            }
            if (l instanceof StandEntity<?, ?> stand) {
                //JCraft.LOGGER.info("Stand: " + stand);
                if (stand.hasUser()) {
                    LivingEntity user = stand.getUser();
                    if (!hit.contains(user))
                        toReturn.add(user);
                }
            }
        }

        return toReturn;
    }

    public static void assignSpec(PlayerEntity player, NbtCompound playerNbt, ISpec playerSpec) {
        JCraftSpec spec = SpecType.fromId(playerNbt.getInt("SpecID"));
        if (spec != null)
            spec.player = player;
        playerSpec.setSpec(spec);
    }

    public static JCraftSpec getSpec(PlayerEntity player) {
        ISpec playerSpec = (ISpec) player;

        // Autogenerate spec data when necessary
        NbtCompound playerNbt = ((IEntityDataSaver) player).getPersistentData();
        if (playerNbt.contains("SpecID")) {
            if (playerSpec.getSpec() == null)
                assignSpec(player, playerNbt, playerSpec);
        } else {
            playerNbt.putInt("SpecID", player.world.getGameRules().getInt(JCraft.DEFAULT_SPEC));
            return getSpec(player);
        }

        return playerSpec.getSpec();
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

        if (entityHit) {
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

    /**
     * @param data NBT data of the entity in question
     * @return whether an entity is a stand user based on its NBT data
     */
    public static boolean isStandUser(NbtCompound data) {
        if (data.contains("StandID"))
            return data.getInt("StandID") != 0;
        return false;
    }

    public static void projectileDamageLogic(ProjectileEntity proj, World world, Entity ent, Vec3d kb, int stunT, int stunType, boolean overrideStun, float damage, int blockstun) {
        if (world.isClient) return;
        Objects.requireNonNull(proj, "Attempted to run ProjectileDamageLogic with invalid projectile in world " + world);
        Entity owner = proj.getOwner();
        DamageSource source;
        if (owner == null)
            source = DamageSource.GENERIC;
        else
            source = DamageSource.thrownProjectile(proj, owner);

        if (ent instanceof LivingEntity living) {
            LivingEntity target = living;
            if (ent instanceof StandEntity<?, ?> stand)
                target = stand.getUser();
            damageLogic(world, target, kb, stunT, stunType, overrideStun, damage, false, blockstun, source, owner);
        }

        if (ent instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(source, damage);
    }

    //To check method ms usage, use spark[something]
    public static boolean isBlocking(LivingEntity entity) {
        if (entity instanceof StandEntity<?, ?> stand) return stand.blocking;
        if (entity.getFirstPassenger() instanceof StandEntity<?, ?> stand) return stand.blocking;
        return false;
    }

    public static boolean shouldForceRender(Entity entity) {
        if (entity instanceof D4CEntity d4c && d4c.getState() == D4CEntity.State.FLAG)
            return true;
        return entity instanceof CreamEntity cream && cream.isHalfBall();
    }

    public static boolean shouldNotRender(Entity entity) {
        Entity passenger = entity.getFirstPassenger();
        return passenger instanceof KingCrimsonEntity kc && kc.getTETime() > 0 ||
                passenger instanceof D4CEntity d4c && d4c.getState() == D4CEntity.State.FLAG ||
                passenger instanceof CreamEntity cream && cream.isHalfBall();
    }

    public static boolean isTimestopped(Entity entity) {
        return ((ITimeStop) entity).getTimeStopTicks() > 0;
    }

    public static @Nullable DimValues getTimestop(Entity entity) {
        for (DimValues d : activeTimestops)
            if (d.user == entity) return d;
        return null;
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

    public static boolean isInTSRange(Vec3d pos) {
        for (DimValues timeStop : activeTimestops) {
            if (timeStop != null) {
                if (timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInTSRange(BlockPos pos) {
        for (DimValues timeStop : activeTimestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return true;

        return false;
    }

    public static int getTicksIfInTSRange(BlockPos pos) {
        for (DimValues timeStop : activeTimestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                    return timeStop.timer;

        return 0;
    }

    public static Vec3d deltaPos(Entity ent) {
        return new Vec3d(
                ent.getX() - ent.prevX,
                ent.getY() - ent.prevY,
                ent.getZ() - ent.prevZ
        );
    }

    public static List<BlockInfo> collectBlockInfo(World world, BlockPos origin, int radius) {
        List<BlockInfo> infoList = new ArrayList<>();

        int[][] array = new int[radius * 2 + 1][radius * 2 + 1];

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
                    if (!state.isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID) || array[x0][z0] != 0)
                        continue;

                    array[x0][z0] = 1;

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
}
