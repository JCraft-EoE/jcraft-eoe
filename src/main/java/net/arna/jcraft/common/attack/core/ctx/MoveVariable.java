package net.arna.jcraft.common.attack.core.ctx;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor()
public class MoveVariable<T> {
    public final Class<T> type;

    @SuppressWarnings("UnstableApiUsage")
    public MoveVariable(TypeToken<T> typeToken) {
        this((Class<T>) typeToken.getRawType());
    }

    MoveContext.Entry<T> createEntry() {
        return new MoveContext.Entry<>(type);
    }
}
