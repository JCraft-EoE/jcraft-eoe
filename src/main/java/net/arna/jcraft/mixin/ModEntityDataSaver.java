package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ModEntityDataSaver implements IEntityDataSaver {
    @Shadow @Nullable public abstract Entity getFirstPassenger();

    @Nullable private StandEntity stand;
    private NbtCompound persistentData;

    public void setStand(@Nullable StandEntity standEntity) {
        this.stand = standEntity;
    }

    public StandEntity getStand() {
        if (this.stand == null || !this.stand.isAlive() || this.stand.isRemoved()) {
            this.stand = null;
            if (this.getFirstPassenger() instanceof StandEntity standEntity)
                this.stand = standEntity;
        }
        return this.stand;
    }

    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) this.persistentData = new NbtCompound();
        return persistentData;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (persistentData != null) {
            nbt.put("JCraftData", persistentData);
        }
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("JCraftData", 10)) {
            persistentData = nbt.getCompound("JCraftData");
        }
    }
}