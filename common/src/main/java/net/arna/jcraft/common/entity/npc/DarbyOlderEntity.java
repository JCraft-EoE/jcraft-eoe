package net.arna.jcraft.common.entity.npc;

import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.common.tickable.JEnemies;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class DarbyOlderEntity extends PathfinderMob {

    public DarbyOlderEntity(Level world) {
        super(JEntityTypeRegistry.DARBY_OLDER.get(), world);
        final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(this);
        standData.setType(JStandTypeRegistry.OSIRIS.get());
        standData.setSkin(0);

        if (world.isClientSide()) return;
        JEnemies.add(this);
    }

    public static AttributeSupplier.Builder createDarbyOlderAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.5);
    }
}
