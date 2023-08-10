package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.NonNull;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.spec.SpecType;
import org.jetbrains.annotations.Nullable;

public interface SpecComponent extends Component, AutoSyncedComponent {
    SpecType getType();
    void setType(@NonNull SpecType type);

    @Nullable
    JCraftSpec getSpec();
}
