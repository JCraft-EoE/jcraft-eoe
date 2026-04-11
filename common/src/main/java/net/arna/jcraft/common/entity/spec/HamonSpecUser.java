package net.arna.jcraft.common.entity.spec;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class HamonSpecUser extends SpecUserMob {
    public HamonSpecUser(Level level) {
        super(JEntityTypeRegistry.HAMON_SPEC_USER.get(), level);
        setSpecType(JSpecTypeRegistry.HAMON.get());
    }

    protected HamonSpecUser(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        setSpecType(JSpecTypeRegistry.HAMON.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // Hamon attacks vampires
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(this, VampireSpecUser.class, true)
        );

        // Hamon attacks Enemy mobs (not Creepers)
        targetSelector.addGoal(
                3,
                new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, livingEntity -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper))
        );
    }

    public static AttributeSupplier.Builder createUserAttributes() {
        return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4);
    }
}
