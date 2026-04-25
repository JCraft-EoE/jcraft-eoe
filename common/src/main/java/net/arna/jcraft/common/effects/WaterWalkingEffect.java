package net.arna.jcraft.common.effects;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class WaterWalkingEffect extends AbstractFluidWalkingEffect {

    public WaterWalkingEffect() {
        super(0x5bc0dc);
    }

    @Override
    public boolean supports(final Fluid fluid) {
        return Fluids.WATER.isSame(fluid);
    }

}
