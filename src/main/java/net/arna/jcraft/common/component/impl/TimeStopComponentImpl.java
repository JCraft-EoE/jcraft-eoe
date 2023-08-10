package net.arna.jcraft.common.component.impl;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.TimeStopComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.arna.jcraft.common.util.JUtils.stopTick;

@Getter
public class TimeStopComponentImpl implements TimeStopComponent {
    private final Entity entity;
    private int ticks;

    public TimeStopComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setTicks(int ticks) {
        this.ticks = ticks;
        JComponents.TIME_STOP.sync(entity);
    }

    @Override
    public void tick(CallbackInfo ci) {
        if (ticks <= 0) return;

        stopTick(entity);
        for (Entity passenger : entity.getPassengerList()) stopTick(passenger);
        ticks--;
        ci.cancel();
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {

    }
}
