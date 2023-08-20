package net.arna.jcraft.common.attack.core.ctx;

import lombok.Getter;

public class IntMoveVariable extends MoveVariable<Integer> {
    public IntMoveVariable() {
        super(int.class);
    }

    @Override
    MoveContext.Entry<Integer> createEntry() {
        return new IntEntry();
    }

    @Getter
    static class IntEntry extends MoveContext.Entry<Integer> {
        private int intValue;

        public IntEntry() {
            super(int.class);
        }

        public void setValue(int value) {
            super.setValue(this.intValue = value);
        }

        @Override
        public void setValue(Integer value) {
            setValue(value.intValue());
        }

        @Override
        public Integer getValue() {
            throw new UnsupportedOperationException("Use MoveContext#getInt(IntMoveVariable) to get int values.");
        }
    }
}
