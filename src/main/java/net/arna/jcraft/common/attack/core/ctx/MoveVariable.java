package net.arna.jcraft.common.attack.core.ctx;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor()
public class MoveVariable<T> {
    public final Class<T> type;

    MoveContext.Entry<T> createEntry() {
        return new MoveContext.Entry<>(type);
    }
}
