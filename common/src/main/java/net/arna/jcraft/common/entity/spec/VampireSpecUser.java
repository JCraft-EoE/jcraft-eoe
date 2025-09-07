package net.arna.jcraft.common.entity.spec;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class VampireSpecUser extends SpecUserMob {
    public VampireSpecUser(Level level) {
        super(JEntityTypeRegistry.VAMPIRE_SPEC_USER.get(), level);
        setSpecType(JSpecTypeRegistry.VAMPIRE.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        goalSelector.addGoal(2, new RestrictSunGoal(this));
        goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));

        targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, true));
    }

    public static AttributeSupplier.Builder createUserAttributes() {
        return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4);
    }
}
