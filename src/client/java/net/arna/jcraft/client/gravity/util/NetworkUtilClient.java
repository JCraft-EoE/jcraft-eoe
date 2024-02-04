package net.arna.jcraft.client.gravity.util;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.component.entity.GravityComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class NetworkUtilClient {

    public static Optional<GravityComponent> getGravityComponent(MinecraftClient client, int entityId) {
        if (client.world == null) return Optional.empty();
        Entity entity = client.world.getEntityById(entityId);
        if (entity == null) return Optional.empty();
        GravityComponent gc = GravityChangerAPI.getGravityComponent(entity);
        if (gc == null) return Optional.empty();
        return Optional.of(gc);
    }
}
