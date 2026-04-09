package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Tusk Act 3 - Wormhole (Ultimate)
 * First press: Fires a slow homing nail.
 * Second press (while nail is alive): Teleports to the nail. Deals 6 void damage in 2-block radius.
 * Like KQBTD's Bites the Dust — initMove routes to teleport when nail is active.
 */
public final class WormholeAttack extends AbstractMove<WormholeAttack, TuskAct3Entity> {
    private WeakReference<NailProjectile> wormholeNail;
    private boolean nailActive = false;

    public WormholeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
        manualCooldown = true;
    }

    @Override
    public @NotNull MoveType<WormholeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    public boolean isNailActive() {
        return nailActive;
    }

    /**
     * Immediately teleports to the nail — bypasses ALL cooldown/movestun checks.
     * Called directly from onUserMoveInput (before canAttack check), like BTD detonation.
     */
    public boolean doTeleportNow(TuskAct3Entity attacker, LivingEntity user) {
        final NailProjectile nail = wormholeNail == null ? null : wormholeNail.get();
        if (nail == null || !nail.isAlive()) return false;
        doTeleport(attacker, user, nail);
        return true;
    }

    @Override
    public void tick(final TuskAct3Entity attacker) {
        final NailProjectile nail = wormholeNail == null ? null : wormholeNail.get();
        nailActive = nail != null && nail.isAlive();
        if (!nailActive) {
            wormholeNail = null;
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        fireNail(attacker, user);
        return Set.of();
    }

    private void doTeleport(TuskAct3Entity attacker, LivingEntity user, NailProjectile nail) {
        Vec3 nailPos = nail.position();
        user.teleportTo(nailPos.x, nailPos.y, nailPos.z);

        // Hit feedback
        attacker.playSound(JSoundRegistry.IMPACT_1.get(), 1.0f, 0.8f);

        // Void damage in 2-block radius
        AABB blastBox = new AABB(nailPos.x - 2, nailPos.y - 2, nailPos.z - 2,
                nailPos.x + 2, nailPos.y + 2, nailPos.z + 2);
        attacker.level().getEntitiesOfClass(LivingEntity.class, blastBox,
                e -> e != user && e.isAlive() && !e.isSpectator())
                .forEach(e -> Attacks.trueDamage(6, JDamageSources.stand(attacker), e));

        nail.discard();
        wormholeNail = null;
        nailActive = false;
    }

    private void fireNail(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile nail = NailProjectile.wormholeFromTuskAct3(attacker);
        if (nail == null) return;

        JComponentPlatformUtils.getCooldowns(user).setCooldown(CooldownType.STAND_ULTIMATE, getCooldown());

        Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
        nail.setPos(user.position().add(heightOffset));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.5F, 0.0F);

        attacker.level().addFreshEntity(nail);
        wormholeNail = new WeakReference<>(nail);
        nailActive = true;
    }

    @Override
    protected @NonNull WormholeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull WormholeAttack copy() {
        return copyExtras(new WormholeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<WormholeAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<WormholeAttack>, WormholeAttack>
        buildCodec(RecordCodecBuilder.Instance<WormholeAttack> instance) {
            return baseDefault(instance, WormholeAttack::new);
        }
    }
}
