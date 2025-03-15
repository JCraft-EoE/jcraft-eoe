package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.NameHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public enum BrainType implements NameHolder {

    // do NOT change the order in this enum, only append new entities AT THE END
    ZOMBIE(Zombie.class, EntityType.ZOMBIE),
    COW(Cow.class, EntityType.COW);

    Class<? extends Mob> entityClass;

    EntityType<? extends Mob> entityType;

    <T extends Mob> BrainType(final Class<T> entityClass, final EntityType<T> entityType) {
        this.entityClass = entityClass;
        this.entityType = entityType;
    }

    public Behavior createBehavior(final Level level) {
        try {
            final Mob mob = entityClass.getConstructor(EntityType.class, Level.class).newInstance(entityType, level);
            return new Behavior(mob.getBrain(), mob.goalSelector, mob.targetSelector);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            // TODO log error
        }
        return null;
    }

    public static BrainType find(final Mob mob) {
        if (mob instanceof Zombie) {
            return ZOMBIE;
        }
        else if (mob instanceof Cow) {
            return COW;
        }
        return null;
    }

    @Override
    public Component getName() {
        return Component.translatable("entity.minecraft." + name().toLowerCase(Locale.ROOT));
    }
}
