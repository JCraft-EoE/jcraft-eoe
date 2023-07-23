package net.arna.jcraft.common.util;
import net.minecraft.entity.LivingEntity;

public interface IOwnable {
    LivingEntity getMaster();
    void setMaster(LivingEntity m);
}
