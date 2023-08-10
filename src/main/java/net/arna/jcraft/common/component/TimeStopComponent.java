package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface TimeStopComponent extends Component, AutoSyncedComponent {
    int getTicks();
    void setTicks(int ticks);

    void tick(CallbackInfo ci);
}
