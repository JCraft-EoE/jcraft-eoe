package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public abstract class MobEntityMixin {

    @WrapMethod(method = "getControllingPassenger")
    private LivingEntity jcraft$dontDisableAI(final Operation<LivingEntity> original) {
        if (JUtils.getStand((Mob)(Object)this) != null) {
            return null;
        }
        return original.call();
    }

}
