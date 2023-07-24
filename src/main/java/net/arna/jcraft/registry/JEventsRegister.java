package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.events.JPlayerEntityEvents;
import net.arna.jcraft.common.events.JServerTickEvents;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public interface JEventsRegister {
    static void registerEvents() {
        ServerEntityEvents.ENTITY_LOAD.register(
                (entity, world) -> {
                    // If an item was spawned in the AU
                    if (world.getRegistryKey().equals(JDimensionRegister.AU_DIMENSION_KEY) && entity instanceof ItemEntity item) {
                        // And it isn't a mock item, and it wasn't thrown out by a player
                        if (item.getThrower() != null) return;
                        // Convert it to a mock item (incompatible and useless)
                        item.setStack(
                                MockItem.createMockStack(item.getStack())
                        );
                    }
                }
        );

        ServerLivingEntityEvents.AFTER_DEATH.register(
                (living, source) -> {
                    if (living instanceof ServerPlayerEntity player) {
                        NbtCompound playerData = ((IEntityDataSaver) player).getPersistentData();
                        // Reset cooldowns upon death
                        for (String cooldownType : JCraft.cooldowns) playerData.putInt(cooldownType, 0);

                        if (source.getAttacker() instanceof LivingEntity killer) {
                            NbtCompound killerData = ((IEntityDataSaver) killer).getPersistentData();
                            killerData.putInt(JCraft.comboBreakerCD, 0);
                        }
                    }
                }
        );

        ServerPlayerEvents.COPY_FROM.register(new JPlayerEntityEvents());

        ServerTickEvents.END_SERVER_TICK.register(JServerTickEvents::serverTick);
    }
}