package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FoodData.class)
public abstract class HungerManagerMixin {
    @Unique
    private CommonVampireComponent jcraft$vampireComponent;
    @Unique
    private boolean jcraft$isVampire;

    @WrapMethod(method = "getFoodLevel")
    int jcraft$getBloodLevel(final Operation<Integer> original) {
        if (this.jcraft$isVampire) {
            return (int)Math.floor(jcraft$vampireComponent.getBlood());
        }
        return original.call();
    }

    @WrapMethod(method = "getSaturationLevel")
    float jcraft$getSaturationLevel(final Operation<Float> original) {
        if (this.jcraft$isVampire) {
            return 0f;
        }
        return original.call();
    }

    @WrapMethod(method = "tick")
    void jcraft$updateVampirism(final Player player, final Operation<Void> original) {
        this.jcraft$vampireComponent = JComponentPlatformUtils.getVampirism(player);
        this.jcraft$isVampire = jcraft$vampireComponent.isVampire();
        if (!jcraft$isVampire) {
            original.call(player);
        }
    }

}
