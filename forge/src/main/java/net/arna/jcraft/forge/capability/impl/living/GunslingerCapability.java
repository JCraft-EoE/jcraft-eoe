package net.arna.jcraft.forge.capability.impl.living;

import net.arna.jcraft.common.component.impl.living.CommonGunslingerComponentImpl;
import net.arna.jcraft.forge.capability.api.JCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class GunslingerCapability extends CommonGunslingerComponentImpl implements JCapability {

    public static Capability<GunslingerCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public GunslingerCapability(LivingEntity living) {
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        writeToNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readFromNbt(tag);
    }

    public static GunslingerCapability getCapability(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(new GunslingerCapability(entity));
    }

}
