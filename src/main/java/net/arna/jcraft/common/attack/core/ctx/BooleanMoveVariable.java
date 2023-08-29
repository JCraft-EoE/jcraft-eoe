package net.arna.jcraft.common.attack.core.ctx;

public class BooleanMoveVariable extends MoveVariable<Boolean> {
    public BooleanMoveVariable() {
        super(boolean.class);
    }

    @Override
    MoveContext.Entry<Boolean> createEntry() {
        return new BooleanEntry();
    }

    public static class BooleanEntry extends MoveContext.Entry<Boolean> {
        private boolean booleanValue;

        public BooleanEntry() {
            super(boolean.class);
        }

        public boolean getBooleanValue() {
            return booleanValue;
        }

        public void setValue(boolean value) {
            super.setValue(booleanValue = value);
        }

        @Override
        public void setValue(Boolean value) {
            setValue(value.booleanValue());
        }

        @Override
        public Boolean getValue() {
            throw new UnsupportedOperationException("Use MoveContext#getBoolean(BooleanMoveVariable) to get boolean values.");
        }
    }
}
