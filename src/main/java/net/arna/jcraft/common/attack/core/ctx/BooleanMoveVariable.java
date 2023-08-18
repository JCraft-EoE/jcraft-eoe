package net.arna.jcraft.common.attack.core.ctx;

import lombok.Getter;

public class BooleanMoveVariable extends MoveVariable<Boolean> {
    public BooleanMoveVariable() {
        super(boolean.class);
    }

    @Override
    MoveContext.Entry<Boolean> createEntry() {
        return super.createEntry();
    }

    @Getter
    public static class BooleanEntry extends MoveContext.Entry<Boolean> {
        private boolean booleanValue;

        public BooleanEntry() {
            super(boolean.class);
        }

        public void setValue(boolean value) {
            super.setValue(booleanValue = value);
        }
    }
}
