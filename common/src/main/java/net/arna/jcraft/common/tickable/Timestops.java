package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * For clientside timestops, see {@link net.arna.jcraft.client.util.JClientUtils#activeTimestops}
 */
public class Timestops {
    protected static final List<DimensionData> TIMESTOPS = new ArrayList<>();

    public static void enqueue(final DimensionData dimensionData) {
        TIMESTOPS.add(dimensionData);
    }

    public static void remove(final DimensionData dimensionData) {
        TIMESTOPS.remove(dimensionData);
    }

    /**
     * Common-side. Thus cannot access serverside data.
     */
    public static final Predicate<Entity> TIMESTOP_PREDICATE = entity -> {
        if (entity instanceof Player player) {
            if (entity.isSpectator()) {
                return false;
            }
            if (player.isCreative()) {
                return false;
            }
        }
        if (entity instanceof final LivingEntity living) {
            StandEntity<?, ?> stand = null;
            if (living instanceof final StandEntity<?,?> livingStand) {
                stand = livingStand;
            } else {
                CommonStandComponent standComponent = JComponentPlatformUtils.getStandComponent(living);
                if (standComponent.getStand() != null) {
                    stand = standComponent.getStand();
                }
            }

            if (stand != null) {
                if (stand instanceof final KingCrimsonEntity kingCrimson) {
                    if (kingCrimson.getTETime() > 0) {
                        return false;
                    }
                }
                if (stand instanceof final GEREntity requiem) {
                    if (requiem.getState() == GEREntity.State.COUNTER) {
                        return false;
                    }
                }
            }
        }
        return true;
    };

    public static void tick(final MinecraftServer server) {
        final List<DimensionData> newActiveTimestops = new ArrayList<>();

        for (final DimensionData timestop : TIMESTOPS) {
            final Entity user = timestop.getUser();
            //JCraft.LOGGER.info("SERVER: Ticking timestop " + timestop + " with user " + user + " and duration " + timestop.timer);

            final int timer = timestop.getTimer();
            if (user != null && user.isAlive() && timer > 0) {
                timestop.decreaseTimer();
                final ServerLevel world = server.getLevel(timestop.getWorldKey());
                if (world == null) {
                    JCraft.LOGGER.warn("World that timestop belongs to no longer exists! Key: " + timestop.getWorldKey() + " Timestopper: " + user);
                    continue;
                }

                final Vec3 pos = timestop.getPos();

                final List<? extends Entity> toStop = world.getEntitiesOfClass(Entity.class,
                        new AABB(pos.add(96.0, 96.0, 96.0), pos.subtract(96.0, 96.0, 96.0)), TIMESTOP_PREDICATE);

                for (final Entity entity : toStop) {
                    if (
                            !entity.isPassenger()
                            && entity != user
                            && !user.isPassengerOfSameVehicle(entity)
                    ) {
                        if (JComponentPlatformUtils.getTimeStopData(entity).isPresent()) {
                            JComponentPlatformUtils.getTimeStopData(entity).get().setTicks(2);
                        }
                    }
                }

                newActiveTimestops.add(timestop);
            }
        }

        TIMESTOPS.clear();
        TIMESTOPS.addAll(newActiveTimestops);
    }

    public static boolean isInTSRange(Vec3 pos) {
        for (DimensionData timeStop : TIMESTOPS) {
            if (timeStop != null) {
                if (timeStop.getPos().distanceToSqr(pos.x(), pos.y(), pos.z()) <= 65536) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isInTSRange(BlockPos pos) {
        for (DimensionData timeStop : TIMESTOPS) {
            if (timeStop != null && timeStop.getPos().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 65536) {
                return true;
            }
        }

        return false;
    }

    public static int getTicksIfInTSRange(BlockPos pos) {
        for (DimensionData timeStop : TIMESTOPS) {
            if (timeStop != null && timeStop.getUser().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 65536) {
                return timeStop.getTimer();
            }
        }

        return 0;
    }

    public static @Nullable DimensionData getTimestop(Entity entity) {
        for (DimensionData data : TIMESTOPS) {
            if (data.getUser() == entity) {
                return data;
            }
        }
        return null;
    }
}
