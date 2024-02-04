package net.arna.jcraft.common.component.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TimeStopComponent extends Component, AutoSyncedComponent {
    int getTicks();
    void setTicks(int ticks);
    void addTotalVelocity(Vec3d vel);
    void tick(CallbackInfo ci);
}
