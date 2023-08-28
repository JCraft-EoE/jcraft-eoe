package net.arna.jcraft.common.attack.core.ctx;

import lombok.Getter;

public class FloatMoveVariable extends MoveVariable<Float> {
    public FloatMoveVariable() {
        super(float.class);
    }

    @Override
    MoveContext.Entry<Float> createEntry() {
        return new FloatEntry();
    }

    @Getter
    public static class FloatEntry extends MoveContext.Entry<Float> {
        private float floatValue;

        public FloatEntry() {
            super(float.class);
        }

        public void setValue(float value) {
            floatValue = value;
        }

        @Override
        public void setValue(Float value) {
            setValue(value.floatValue());
        }

        @Override
        public Float getValue() {
            throw new UnsupportedOperationException("Use MoveContext#getFloat(FloatMoveVariable) to get float values.");
        }
    }
}
