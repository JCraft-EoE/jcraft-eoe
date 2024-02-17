package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.item.FVRevolverItem;
import net.arna.jcraft.common.util.DimensionData;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class RevolverFire {
    protected static final List<DimensionData> toFire = new ArrayList<>();
    public static void enqueue(DimensionData dimensionData) { toFire.add(dimensionData); }
    public static void remove(DimensionData dimensionData) { toFire.remove(dimensionData); }
    public static void tick(MinecraftServer server) {
        List<DimensionData> newToFire = new ArrayList<>();

        for (DimensionData toFireData : toFire) {
            LivingEntity user = toFireData.user;
            if (user != null && user.isAlive()) {
                if (toFireData.timer-- > 0)
                    newToFire.add(toFireData);
                else {
                    ServerWorld world = server.getWorld(toFireData.worldKey);
                    if (world == null) {
                        JCraft.LOGGER.fatal("World that toFireData belongs to no longer exists! Key: " + toFireData.worldKey + " user: " + user);
                        continue;
                    }

                    ItemStack main = user.getMainHandStack();
                    if (main.getItem() == JObjectRegistry.FV_REVOLVER)
                        FVRevolverItem.fire(main, world, user);
                }
            }
        }

        toFire.clear();
        toFire.addAll(newToFire);
    }
}
