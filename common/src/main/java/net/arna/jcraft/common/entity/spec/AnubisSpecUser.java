package net.arna.jcraft.common.entity.spec;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AnubisSpecUser extends SpecUserMob {
    public AnubisSpecUser(Level level) {
        super(JEntityTypeRegistry.ANUBIS_SPEC_USER.get(), level);
        setSpecType(JSpecTypeRegistry.ANUBIS.get());

        setItemInHand(InteractionHand.MAIN_HAND, JItemRegistry.ANUBIS.get().getDefaultInstance());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, true));
    }

    public static AttributeSupplier.Builder createUserAttributes() {
        return createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4);
    }
}
