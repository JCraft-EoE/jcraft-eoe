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

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Tusk Act 3's wormhole attack.
 * First press: Fires a slow homing nail that deals no damage
 * Second press: Teleports user to nail location
 */
public final class WormholeAttack extends AbstractMove<WormholeAttack, TuskAct3Entity> {
    private WeakReference<NailProjectile> wormholeNail;
    private boolean nailActive = false;

    public WormholeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<WormholeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(final TuskAct3Entity attacker) {
        // Check if wormhole nail is still alive
        final NailProjectile nail = wormholeNail == null ? null : wormholeNail.get();
        if (nail != null && nail.isAlive()) {
            // Update nail to curve towards user's aim (handled in NailProjectile.tick())
            nailActive = true;
        } else {
            nailActive = false;
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        final NailProjectile existingNail = wormholeNail == null ? null : wormholeNail.get();

        if (existingNail != null && existingNail.isAlive() && nailActive) {
            // Second press: Teleport to nail
            Vec3 nailPos = existingNail.position();
            user.teleportTo(nailPos.x, nailPos.y, nailPos.z);

            // Discard the nail
            existingNail.discard();
            wormholeNail = null;
            nailActive = false;
        } else {
            // First press: Fire wormhole nail
            NailProjectile nail = NailProjectile.wormholeFromTuskAct3(attacker);
            if (nail == null) return Set.of();

            // Position nail at stand height
            Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
            nail.setPos(attacker.position().add(heightOffset));

            // Shoot slowly
            nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 0.5F, 0.0F);

            attacker.level().addFreshEntity(nail);
            wormholeNail = new WeakReference<>(nail);
            nailActive = true;
        }

        return Set.of();
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