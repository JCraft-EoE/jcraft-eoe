package net.arna.jcraft.common.component.player;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.NonNull;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import org.jetbrains.annotations.Nullable;

public interface PhComponent extends Component, AutoSyncedComponent {
    int getLevel();
    void increaseLevel();
    void resetLevel();
}
