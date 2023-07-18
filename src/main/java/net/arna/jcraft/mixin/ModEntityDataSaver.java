package net.arna.jcraft.mixin;

import lombok.Getter;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ModEntityDataSaver implements IEntityDataSaver {
    // Server data tracking
    @Getter
    private @Unique boolean thin = false;
    private @Unique NbtCompound persistentData; // JCraft NBT data implementation

    public void markThin() {
        this.thin = true;
    }

    // Stand tracking
    @Nullable private StandEntity stand;

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

    @Override
    public void copyFrom(IEntityDataSaver dataSaver) {
        this.persistentData = dataSaver.getPersistentData().copy();
        if (dataSaver.isThin()) markThin();
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

    @Shadow @Nullable public abstract Entity getFirstPassenger();
}
