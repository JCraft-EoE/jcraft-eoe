package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.tickable.Timestops;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Set;

@Getter
public class TimeSkipMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<TimeSkipMove<A>, A> {
    private final double distance;

    public TimeSkipMove(int cooldown, double distance) {
        super(cooldown, 0, 0, 0);
        this.distance = distance;
        mobilityType = MobilityType.TELEPORT;
    }

    @Override
    public boolean canBeInitiated(A attacker) {
        if (Timestops.getTimestop(attacker.getUser()) != null)
            return false;
        return super.canBeInitiated(attacker);
    }

    @Override
    public void onInitiate(A attacker) {
        // Don't play the sounds
        getInitActions().forEach(action -> action.perform(attacker, attacker.getUser(), attacker.getMoveContext()));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        doTimeSkip(attacker, user, distance, getSounds());

        return Set.of();
    }

    public static void doTimeSkip(IAttacker<?, ?> attacker, LivingEntity user, double distance, List<SoundEvent> sounds) {
        boolean hasVehicle = user.hasVehicle();

        if (hasVehicle)
            distance /= 3;

        Vec3d eyePos = user.getEyePos();
        HitResult hitResult = attacker.getEntityWorld().raycast(
                new RaycastContext(
                        eyePos,
                        eyePos.add(user.getRotationVector().multiply(distance)),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, user));
        Vec3d tpPos = hitResult.getPos();

        // 3s minimum ult cooldown
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.STAND_ULTIMATE) < 60)
            cooldowns.setCooldown(CooldownType.STAND_ULTIMATE, 60);

        if (hasVehicle) user.getRootVehicle().setPosition(tpPos.x, tpPos.y, tpPos.z);
        else user.teleport(tpPos.x, tpPos.y, tpPos.z);

        for (SoundEvent sound : sounds)
            attacker.getEntityWorld().playSound(null, tpPos.x, tpPos.y, tpPos.z, sound, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    protected @NonNull TimeSkipMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimeSkipMove<A> copy() {
        return copyExtras(new TimeSkipMove<>(getCooldown(), distance));
    }
}
