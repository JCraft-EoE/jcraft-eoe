package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.util.IJInputStateManagerHolder;
import net.arna.jcraft.common.util.InputStateManager;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerEntityMixin implements IJInputStateManagerHolder {

    private final @Unique InputStateManager jcraft$inputStateManager = new InputStateManager();

    @Override
    public InputStateManager jcraft$getJInputStateManager() {
        return jcraft$inputStateManager;
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void jcraft$copyInputStateManagerUponCopy(final ServerPlayer that, final boolean keepEverything, final CallbackInfo ci) {
        if (!keepEverything) {
            return;
        }
        final InputStateManager old = ((IJInputStateManagerHolder) that).jcraft$getJInputStateManager();
        jcraft$inputStateManager.copyFrom(old);
    }

    @WrapMethod(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;")
    private ItemEntity jcraft$dropItem(final ItemStack droppedItem, final boolean dropAround, final boolean includeThrowerName, final Operation<ItemEntity> original) {
        final ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
        if (JUtils.canAct(serverPlayer) || !serverPlayer.isAlive()) {
            original.call(droppedItem, dropAround, includeThrowerName);
        }
        return null;
    }

}
