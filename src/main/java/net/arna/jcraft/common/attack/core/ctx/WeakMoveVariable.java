package net.arna.jcraft.common.attack.core.ctx;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Weakly references its value, so it may be discarded by the GC.
 * Should be used for anything referenced by Minecraft (such as worlds or entities).
 */
public class WeakMoveVariable<T> extends MoveVariable<T> {
    public WeakMoveVariable(Class<T> type) {
        super(type);
    }

    @Override
    MoveContext.Entry<T> createEntry() {
        return super.createEntry();
    }

    private static class WeakEntry<T> extends MoveContext.Entry<T> {
        private @Nullable WeakReference<T> value;

        public WeakEntry(Class<T> type) {
            super(type);
        }

        @Override
        public T getValue() {
            return value == null ? null : value.get();
        }

        @Override
        public void setValue(T value) {
            this.value = value == null ? null : new WeakReference<>(value);
        }
    }
}
