package net.arna.jcraft.common.attack.moves.shared;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Set;

public class TimeSkipMove<A extends IAttacker<?, ?>> extends AbstractMove<TimeSkipMove<A>, A> {
    private final double distance;

    public TimeSkipMove(int cooldown, double distance) {
        super(cooldown, 0, 0, 0);
        this.distance = distance;
        mobilityType = MobilityType.TELEPORT;
    }

    @Override
    public boolean onInitialize(A attacker) {
        return canBeInitiated(attacker); // Don't play the sounds.
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        boolean hasVehicle = user.hasVehicle();
        double distance = this.distance;

        if (hasVehicle)
            distance /= 3;

        Vec3d eyePos = user.getEyePos();
        HitResult hitResult = attacker.getWorld().raycast(
                new RaycastContext(
                        eyePos,
                        eyePos.add(user.getRotationVector().multiply(distance)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, user));
        Vec3d telePos = hitResult.getPos();

        // 3s minimum ult cooldown
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) < 60)
            cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 60);

        if (hasVehicle) user.getRootVehicle().setPosition(telePos.x, telePos.y, telePos.z);
        else user.teleport(telePos.x, telePos.y, telePos.z);

        for (SoundEvent sound : getSounds())
            attacker.getWorld().playSound(null, telePos.x, telePos.y, telePos.z, sound, SoundCategory.PLAYERS, 1f, 1f);

        return Set.of();
    }

    @Override
    protected @NonNull TimeSkipMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimeSkipMove<A> copy() {
        return null;
    }
}
