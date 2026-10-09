package net.arna.jcraft.fabric.common.component.impl.living;

import lombok.NonNull;
import net.arna.jcraft.common.component.impl.living.CommonSpinComponentImpl;
import net.arna.jcraft.fabric.common.component.JComponents;
import net.arna.jcraft.fabric.common.component.living.SpinComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class SpinComponentImpl extends CommonSpinComponentImpl implements SpinComponent {

    public SpinComponentImpl(final @NonNull LivingEntity entity) {
        super(entity);
    }

    @Override
    public void sync(final Entity entity) {
        JComponents.SPIN.sync(entity);
    }

}
