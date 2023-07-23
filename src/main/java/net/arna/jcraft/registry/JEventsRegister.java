package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.events.JPlayerEntityEvents;
import net.arna.jcraft.common.events.JServerTickEvents;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public interface JEventsRegister {
    static void registerEvents() {
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