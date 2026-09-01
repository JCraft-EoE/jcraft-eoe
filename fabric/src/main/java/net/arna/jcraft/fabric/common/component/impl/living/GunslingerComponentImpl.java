package net.arna.jcraft.fabric.common.component.impl.living;

import net.arna.jcraft.common.component.impl.living.CommonGunslingerComponentImpl;
import net.arna.jcraft.fabric.common.component.JComponents;
import net.arna.jcraft.fabric.common.component.living.GunslingerComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class GunslingerComponentImpl extends CommonGunslingerComponentImpl implements GunslingerComponent {

    public GunslingerComponentImpl(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void sync(Entity entity) {
        JComponents.GUNSLINGER.sync(entity);
    }

}
