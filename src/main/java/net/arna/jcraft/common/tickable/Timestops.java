package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Timestops {
    protected static final List<DimensionData> timestops = new ArrayList<>();
    public static void enqueue(DimensionData dimensionData) { timestops.add(dimensionData); }
    public static void remove(DimensionData dimensionData) { timestops.remove(dimensionData); }
    public static void tick(MinecraftServer server) {
        List<DimensionData> newActiveTimestops = new ArrayList<>();

        for (DimensionData timestop : timestops) {
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

        timestops.clear();
        timestops.addAll(newActiveTimestops);
    }

    public static boolean isInTSRange(Vec3d pos) {
        for (DimensionData timeStop : timestops)
            if (timeStop != null)
                if (timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                    return true;

        return false;
    }

    public static boolean isInTSRange(BlockPos pos) {
        for (DimensionData timeStop : timestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return true;

        return false;
    }

    public static int getTicksIfInTSRange(BlockPos pos) {
        for (DimensionData timeStop : timestops)
            if (timeStop != null && timeStop.pos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) <= 65536)
                return timeStop.timer;

        return 0;
    }

    public static @Nullable DimensionData getTimestop(Entity entity) {
        for (DimensionData d : timestops)
            if (d.user == entity) return d;
        return null;
    }
}
