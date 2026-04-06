package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Tusk Act 3 - Voidshot (Special 3)
 *
 * The user fires a shot at their own head, then briefly enters their own wormhole.
 * While inside the void the user is invulnerable (up to 20 ticks = 1 second).
 * Punishable on windup and recovery.
 * Using any other move while in the void forces them out immediately.
 */
public final class VoidShotAttack extends AbstractMove<VoidShotAttack, TuskAct3Entity> {
    public static final int VOID_DURATION = 20;

    public VoidShotAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull MoveType<VoidShotAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        // Grant the stand invulnerability for VOID_DURATION ticks
        // Tracked via TuskAct3Entity.voidTicks field
        attacker.enterVoid();
        return Set.of();
    }

    @Override
    protected @NonNull VoidShotAttack getThis() {
        return this;
    }

    @Override
    public @NonNull VoidShotAttack copy() {
        return copyExtras(new VoidShotAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<VoidShotAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<VoidShotAttack>, VoidShotAttack>
        buildCodec(RecordCodecBuilder.Instance<VoidShotAttack> instance) {
            return baseDefault(instance, VoidShotAttack::new);
        }
    }
}
