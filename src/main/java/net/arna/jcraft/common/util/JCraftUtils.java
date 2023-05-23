package net.arna.jcraft.common.util;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedback;
import net.arna.jcraft.common.entity.CreamEntity;
import net.arna.jcraft.common.entity.D4CEntity;
import net.arna.jcraft.common.entity.KingCrimsonEntity;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.spec.Brawler;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

import java.util.ArrayList;
import java.util.List;

import static net.arna.jcraft.common.entity.StandEntity.DamageLogic;

public final class JCraftUtils {
    public static List<DimValues> activeTimestops = new ArrayList<>();

    // Specify what type the hitbox searches for
    public static List<? extends Entity> GenerateHitbox(World world, Vec3d center, double hitboxSize, Class<? extends Entity> entityClass, List<Entity> except) {
        double size = hitboxSize / 2;

        Vec3d v1 = center.subtract(size, size, size);
        Vec3d v2 = center.add(size, size, size);

        if (world.getGameRules().getBoolean(JCraft.SHOW_HITBOXES) && v1 != v2 && size > 0) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(1);
            buf.writeDouble(v1.x);
            buf.writeDouble(v2.x);
            buf.writeDouble(v1.y);
            buf.writeDouble(v2.y);
            buf.writeDouble(v1.z);
            buf.writeDouble(v2.z);
            for (PlayerEntity player : world.getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                    ServerChannelFeedback.send(serverPlayerEntity, buf);
                }

            }
        }

        List<? extends Entity> hit = world.getEntitiesByClass(entityClass, new Box(v1, v2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        ArrayList<Entity> toReturn = new ArrayList<>(List.copyOf(hit));
        for (Entity e : hit) {
            //JCraft.LOGGER.info(e);
            if (except.contains(e)) {
                toReturn.remove(e);
                continue;
            }
            if (e instanceof StandEntity stand) {
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
    public static List<LivingEntity> GenerateHitbox(World world, Vec3d center, double hitboxSize, List<Entity> except) {
        double size = hitboxSize / 2;

        Vec3d v1 = center.subtract(size, size, size);
        Vec3d v2 = center.add(size, size, size);

        if (world.getGameRules().getBoolean(JCraft.SHOW_HITBOXES) && v1 != v2 && size > 0) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(1);
            buf.writeDouble(v1.x);
            buf.writeDouble(v2.x);
            buf.writeDouble(v1.y);
            buf.writeDouble(v2.y);
            buf.writeDouble(v1.z);
            buf.writeDouble(v2.z);
            for (PlayerEntity player : world.getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                    ServerChannelFeedback.send(serverPlayerEntity, buf);
                }
            }
        }

        List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
        ArrayList<LivingEntity> toReturn = new ArrayList<>(List.copyOf(hit));
        for (LivingEntity l : hit) {
            if (except != null && except.contains(l)) {
                //JCraft.LOGGER.info("Removing: " + l);
                toReturn.remove(l);
                continue;
            }
            if (l instanceof StandEntity stand) {
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
        JCraftSpec spec = null;

        if (playerNbt.getInt("SpecID") == 1) {
            spec = new Brawler();
        }
        if (spec != null) {
            spec.player = player;
        }

        playerSpec.setSpec(spec);
    }

    public static JCraftSpec getSpec(PlayerEntity player) {
        ISpec playerSpec = (ISpec) player;

        // Autogenerate spec data when necessary
        NbtCompound playerNbt = ((IEntityDataSaver) player).getPersistentData();
        if (playerNbt.contains("SpecID")) {
            if (playerSpec.getSpec() == null) {
                assignSpec(player, playerNbt, playerSpec);
            }
        } else {
            playerNbt.putInt("SpecID", player.world.getGameRules().getInt(JCraft.DEFAULT_SPEC));
            return getSpec(player);
        }

        return playerSpec.getSpec();
    }

    public static void ProjectileDamageLogic(ProjectileEntity proj, World world, Entity ent, Vec3d kb, int stunT, int stunType, boolean overrideStun, float damage) {
        Entity owner = proj.getOwner();
        DamageSource source = DamageSource.thrownProjectile(proj, owner);

        if (ent instanceof LivingEntity living) {
            LivingEntity target = living;
            if (ent instanceof StandEntity stand && !stand.blocking)
                target = stand.getUser();
            DamageLogic(world, target, kb, stunT, stunType, overrideStun, damage, false, source, owner);
        }

        if (ent instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(source, damage);
    }

    //To check method ms usage, use spark[something]
    public static boolean isBlocking(LivingEntity entity) {
        if (entity.getFirstPassenger() instanceof StandEntity stand) {
            return stand.blocking;
        }
        return false;
    }

    public static boolean shouldRender(LivingEntity entity) {
        Entity passenger = entity.getFirstPassenger();
        if (passenger instanceof KingCrimsonEntity kc && kc.getTETime() > 0)
            return false;
        if (passenger instanceof D4CEntity d4c && d4c.getState() == 11)
            return false;
        return !(passenger instanceof CreamEntity cream) || !cream.getHalfBall();
    }

    public static boolean isTimestopped(Entity entity) {
        return ((ITimeStop) entity).getTimeStopTicks() > 0;
    }

    public static boolean isStoppingTime(Entity entity) {
        for (DimValues d :
                activeTimestops) {
            if (d.user == entity) {
                return true;
            }
        }
        return false;
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
        for (DimValues timeStop : activeTimestops) {
            if (timeStop != null) {
                if (timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536) {
                    return true;
                }
            }
        }

        return false;
    }

    public static int getTicksIfInTSRange(BlockPos pos) {
        for (DimValues timeStop : activeTimestops) {
            if (timeStop != null) {
                if (timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536) {
                    return ((StandEntity) timeStop.user).getTSTime();
                }
            }
        }

        return 0;
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel model, StandEntity entity, LivingEntity player) {
        animateGenericHumanoid(model, entity, player, false, false);
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel model, StandEntity entity, LivingEntity player, boolean flipBody, boolean flipHead) {
        animateGenericHumanoid(model, entity, player, flipBody, flipHead, 0, 0);
    }

    public static void animateGenericHumanoid(AnimatedTickingGeoModel model, StandEntity entity, LivingEntity player, boolean flipBody, boolean flipHead, float tPO, float hPO) {
        float overVel = 0;
        float velInfluence = 90f;

        if (entity.getMoveStun() < 1) {
            Vec3d playerVel = player.getVelocity();
            overVel = MathHelper.clamp((float) playerVel.horizontalLength() - 0.05f, -1f, 1f);

            if (playerVel.normalize().add(entity.getRotationVector()).horizontalLengthSquared() < playerVel.normalize().horizontalLengthSquared()) {
                velInfluence *= -1;
            }

            IBone torso = model.getAnimationProcessor().getBone("torso");
            if (torso != null) {
                float pitch = (180f + overVel * velInfluence) * 3.1415f / 180f;
                if (flipBody) {
                    pitch += 3.1415f;
                    pitch = -pitch;
                }
                torso.setRotationX(pitch + tPO);
            }
        }

        if (entity.getState() == 3 || entity.getState() < 2) { // if in/going to idle, or blocking
            IBone head = model.getAnimationProcessor().getBone("head");
            if (head != null) {
                float headPitch = (player.getPitch() - overVel * velInfluence) * 3.1415f / 180f;
                if (flipHead) {
                    headPitch = -headPitch;
                }
                head.setRotationX(headPitch + hPO);
            }
        }
    }
}
