package net.arna.jcraft.common.attack.core.ctx;

import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MoveVariable<T> {
    private final Class<T> type;

    public MoveVariable(TypeToken<T> typeToken) {
        // For parameterized types, this simply returns a Class object of the raw type.
        // This does not matter, however, as at compile-time, Java thinks it returns
        // a class of a parameterized type, which is good enough for what we are trying to achieve.
        // I.e. if T is List<Entity>, this returns Class<List> and not Class<List<Entity>>, but at
        // compile-time, Java thinks it does return Class<List<Entity>>, so that's good enough.
        this((Class<T>) typeToken.getRawType());
    }

    MoveContext.Entry<T> createEntry() {
        return new MoveContext.Entry<>(type);
    }
}
