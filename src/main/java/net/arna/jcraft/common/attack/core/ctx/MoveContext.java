package net.arna.jcraft.common.attack.core.ctx;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MoveContext {
    private final Map<MoveVariable<?>, Entry<?>> entries = new HashMap<>();

    public <T> T get(MoveVariable<T> variable) {
        Entry<?> entry = getEntry(variable);
        return (T) entry.getValue();
    }

    public int getInt(IntMoveVariable variable) {
        IntMoveVariable.IntEntry entry = (IntMoveVariable.IntEntry) getEntry(variable);
        return entry.getIntValue();
    }

    public float getFloat(FloatMoveVariable variable) {
        FloatMoveVariable.FloatEntry entry = (FloatMoveVariable.FloatEntry) getEntry(variable);
        return entry.getFloatValue();
    }

    public boolean getBoolean(BooleanMoveVariable variable) {
        BooleanMoveVariable.BooleanEntry entry = (BooleanMoveVariable.BooleanEntry) getEntry(variable);
        return entry.getBooleanValue();
    }

    public <T> void set(MoveVariable<T> variable, T value) {
        getEntry(variable).setValue(value);
    }

    public void setInt(IntMoveVariable variable, int value) {
        IntMoveVariable.IntEntry entry = (IntMoveVariable.IntEntry) getEntry(variable);
        entry.setValue(value);
    }

    public void setFloat(FloatMoveVariable variable, float value) {
        FloatMoveVariable.FloatEntry entry = (FloatMoveVariable.FloatEntry) getEntry(variable);
        entry.setValue(value);
    }

    public void setBoolean(BooleanMoveVariable variable, boolean value) {
        BooleanMoveVariable.BooleanEntry entry = (BooleanMoveVariable.BooleanEntry) getEntry(variable);
        entry.setValue(value);
    }

    public void incrementInt(IntMoveVariable variable, int increment) {
        IntMoveVariable.IntEntry entry = (IntMoveVariable.IntEntry) getEntry(variable);
        entry.setValue(entry.getIntValue() + increment);
    }

    @NotNull
    private <T> Entry<T> getEntry(MoveVariable<T> variable) {
        Entry<?> entry = entries.get(variable);
        if (entry == null) throw new IllegalArgumentException("No entry for the given variable could be found. " +
                "Has it been registered?");

        return (Entry<T>) entry;
    }

    public <T> void register(@NonNull MoveVariable<T> variable) {
        entries.put(variable, variable.createEntry());
    }

    public <T> void register(@NonNull MoveVariable<T> variable, T initialValue) {
        register(variable);
        set(variable, initialValue);
    }

    public void register(@NonNull IntMoveVariable variable, int initialValue) {
        register(variable);
        setInt(variable, initialValue);
    }

    public void register(@NonNull FloatMoveVariable variable, float initialValue) {
        register(variable);
        setFloat(variable, initialValue);
    }

    public void register(@NonNull BooleanMoveVariable variable, boolean initialValue) {
        register(variable);
        setBoolean(variable, initialValue);
    }

    @Getter
    @RequiredArgsConstructor
    static class Entry<T> {
        private final Class<T> type;
        @Setter
        private T value;
    }
}
