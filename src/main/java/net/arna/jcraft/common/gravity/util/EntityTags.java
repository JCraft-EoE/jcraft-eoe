package net.arna.jcraft.common.gravity.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class EntityTags {
    public static final TagKey<EntityType<?>> FORBIDDEN_ENTITIES = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier("gravitychanger", "forbidden_entities"));
    public static final TagKey<EntityType<?>> FORBIDDEN_ENTITY_RENDERING = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier("gravitychanger", "forbidden_entity_rendering"));

    public static boolean canChangeGravity(Entity entity) {
        return !entity.getType().getRegistryEntry().isIn(FORBIDDEN_ENTITIES);
    }
}
