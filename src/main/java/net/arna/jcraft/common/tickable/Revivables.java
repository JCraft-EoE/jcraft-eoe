package net.arna.jcraft.common.tickable;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Revivables {
    public static class ReviveData {
        @NonNull @Getter
        final EntityType<?> type;
        @NonNull @Getter
        final Vec3d pos;
        @NonNull final RegistryKey<World> worldKey;
        public int timer = 20 * 60; // 1min

        private ReviveData(@NotNull EntityType<?> type, @NotNull Vec3d pos, @NotNull RegistryKey<World> worldKey) {
            this.type = type;
            this.pos = pos;
            this.worldKey = worldKey;
        }
    }

    protected static final List<ReviveData> revivables = new ArrayList<>();

    public static void addRevivable(EntityType<?> type, Vec3d pos, RegistryKey<World> worldKey) {
        revivables.add(new ReviveData(type, pos, worldKey));
    }

    public static void removeRevivable(ReviveData reviveData) {
        revivables.remove(reviveData);
    }

    public static void revive(@NonNull MinecraftServer server, @NonNull ReviveData revivable) {
        ServerWorld serverWorld = server.getWorld(revivable.worldKey);
        if (serverWorld == null) {
            JCraft.LOGGER.fatal("Tried to revive entity from invalid ServerWorld!");
            return;
        }
        Entity entity = revivable.type.create(serverWorld);
        if (entity == null) {
            JCraft.LOGGER.warn("Failed to create entity from EntityType: " + revivable.type);
            return;
        }
        entity.setPosition(revivable.pos);
        serverWorld.spawnEntity(entity);
        removeRevivable(revivable);
    }

    public static boolean removeRevivableAt(Vec3d pos) {
        for (ReviveData revivable : revivables) {
            if (revivable.pos == pos)
                return revivables.remove(revivable);
        }
        return false;
    }


    public static List<ReviveData> getAround(Vec3d pos, double distance) {
        List<ReviveData> out = new ArrayList<>();
        for (ReviveData revivable : revivables) {
            if (revivable.pos.squaredDistanceTo(pos) <= distance*distance)
                out.add(revivable);
        }
        return out;
    }

    public static void tick(MinecraftServer server) {
        List<ReviveData> newRevivables = new ArrayList<>();
        for (ReviveData revivable : revivables) {
            if (--revivable.timer > 1)
                newRevivables.add(revivable);
        }
        revivables.clear();
        revivables.addAll(newRevivables);
    }
}
