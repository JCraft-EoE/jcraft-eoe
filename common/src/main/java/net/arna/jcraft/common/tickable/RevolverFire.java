package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.FVRevolverItem;
import net.arna.jcraft.common.item.Peacemaker;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true, since = "0.17.3")
public class RevolverFire {
    protected static final List<DimensionData> toFire = new ArrayList<>();


    public static void enqueue(DimensionData dimensionData) {
        toFire.add(dimensionData);
    }

    public static void remove(DimensionData dimensionData) {
        toFire.remove(dimensionData);
    }

    public static void tick(MinecraftServer server) {
        List<DimensionData> newToFire = new ArrayList<>();

        for (DimensionData toFireData : toFire) {
            final LivingEntity user = toFireData.getUser();
            if (user != null && user.isAlive()) {
                if (toFireData.getTimer() > 0) {
                    toFireData.decreaseTimer();
                    newToFire.add(toFireData);
                } else {
                    ServerLevel world = server.getLevel(toFireData.getWorldKey());
                    if (world == null) {
                        JCraft.LOGGER.warn("World that toFireData belongs to no longer exists! Key: " + toFireData.getWorldKey() + " user: " + user);
                        continue;
                    }

                    ItemStack main = user.getMainHandItem();
                    if (main.getItem() == JItemRegistry.FV_REVOLVER.get()) {
                        FVRevolverItem.fire(main, world, user);
                    } else if (main.getItem() == JItemRegistry.PEACEMAKER.get()) {
                        Peacemaker.shoot(main, world, user);
                    }
                }
            }
        }

        toFire.clear();
        toFire.addAll(newToFire);
    }
}
