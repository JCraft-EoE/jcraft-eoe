package net.arna.jcraft.common.entity.spec;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

public class BrawlerSpecUser extends SpecUserMob {
    public BrawlerSpecUser(Level level) {
        super(JEntityTypeRegistry.BRAWLER_SPEC_USER.get(), level);
        setSpecType(JSpecTypeRegistry.BRAWLER.get());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createUserAttributes() {
        return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4);
    }
}
