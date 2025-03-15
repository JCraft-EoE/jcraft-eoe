package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.NameHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.logging.Level;

public enum BrainType implements NameHolder {

    // do NOT change the order in this enum, only append new entities AT THE END
    ZOMBIE(Zombie.class, EntityType.ZOMBIE),
    COW(Cow.class, EntityType.COW);

    Class<? extends LivingEntity> entityClass;

    EntityType<? extends LivingEntity> entityType;

    <T extends LivingEntity> BrainType(final Class<T> entityClass, final EntityType<T> entityType) {
        this.entityClass = entityClass;
        this.entityType = entityType;
    }

    public Brain<?> createBrain(final Level level) {
        try {
            return entityClass.getConstructor(EntityType.class, Level.class).newInstance(entityType, level).getBrain();
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            // TODO log error
        }
        return null;
    }

    @Override
    public Component getName() {
        return Component.translatable("entity.minecraft." + name().toLowerCase(Locale.ROOT));
    }
}
