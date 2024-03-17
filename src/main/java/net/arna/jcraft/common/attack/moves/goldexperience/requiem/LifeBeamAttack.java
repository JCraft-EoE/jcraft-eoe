package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

@Getter
public class LifeBeamAttack extends AbstractMove<LifeBeamAttack, GEREntity> {
    public static final IntMoveVariable CHARGE_TIME = new IntMoveVariable();

    public LifeBeamAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GEREntity attacker, LivingEntity user, MoveContext ctx) {
        GERScorpionEntity scorpion = new GERScorpionEntity(JEntityTypeRegistry.GER_SCORPION, attacker.getWorld());
        if (ctx.getInt(CHARGE_TIME) >= 18) scorpion.charge();
        scorpion.setInitialVel(user.getRotationVector().multiply(2));
        Vec3d ePos = attacker.getEyePos();
        scorpion.refreshPositionAndAngles(ePos.x, ePos.y, ePos.z, -user.getYaw() - 90f, attacker.getPitch());
        scorpion.setMaster(user);
        attacker.getWorld().spawnEntity(scorpion);

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CHARGE_TIME);
    }

    @Override
    protected @NonNull LifeBeamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull LifeBeamAttack copy() {
        return copyExtras(new LifeBeamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
