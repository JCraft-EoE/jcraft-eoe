package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Tusk Act 3 - Vortex Hole (Special 2)
 *
 * Fires a hole that moves in the user's look direction.
 * While traveling, it pulls all nearby entities (including the user) toward it.
 * Similar to KQBTD Bubble's movement model.
 */
public final class VortexAttack extends AbstractMove<VortexAttack, TuskAct3Entity> {

    public VortexAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<VortexAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile nail = NailProjectile.vortexFromTuskAct3(attacker);
        if (nail == null) return Set.of();

        Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
        nail.setPos(attacker.position().add(heightOffset));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.2F, 0.2F);

        attacker.level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull VortexAttack getThis() {
        return this;
    }

    @Override
    public @NonNull VortexAttack copy() {
        return copyExtras(new VortexAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<VortexAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<VortexAttack>, VortexAttack>
        buildCodec(RecordCodecBuilder.Instance<VortexAttack> instance) {
            return baseDefault(instance, VortexAttack::new);
        }
    }
}
