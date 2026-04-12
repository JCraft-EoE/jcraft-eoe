package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import org.jetbrains.annotations.NotNull;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class DrillShotChargeMove extends AbstractHoldableMove<DrillShotChargeMove, TuskAct2Entity> {
    public DrillShotChargeMove(int cooldown, int windup, int duration, float moveDistance, int minimumCharge) {
        super(cooldown, windup, duration, moveDistance, minimumCharge);
    }

    @Override
    public void onInitiate(TuskAct2Entity attacker) {
        super.onInitiate(attacker);
        attacker.setDrillChargeTime(0);
    }

    @Override
    public void activeTick(TuskAct2Entity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);
        attacker.setDrillChargeTime(attacker.getDrillChargeTime() + 1);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct2Entity attacker, LivingEntity user) {
        return Set.of();
    }

    @Override
    public @NotNull MoveType<DrillShotChargeMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull DrillShotChargeMove getThis() {
        return this;
    }

    @Override
    public @NonNull DrillShotChargeMove copy() {
        return copyExtras(new DrillShotChargeMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getMinimumCharge()));
    }

    public static class Type extends AbstractHoldableMove.Type<DrillShotChargeMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<DrillShotChargeMove>, DrillShotChargeMove> buildCodec(RecordCodecBuilder.Instance<DrillShotChargeMove> instance) {
            return holdableDefault(instance, DrillShotChargeMove::new);
        }
    }
}
