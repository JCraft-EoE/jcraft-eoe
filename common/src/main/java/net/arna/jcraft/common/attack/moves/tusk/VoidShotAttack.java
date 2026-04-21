package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class VoidShotAttack extends AbstractMove<VoidShotAttack, TuskAct3Entity> {
    public static final int VOID_DURATION = 28;

    private float exitDamage = 0f;
    private float exitBlastRadius = 0f;

    public VoidShotAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public VoidShotAttack withExitDamage(float damage, float blastRadius) {
        this.exitDamage = damage;
        this.exitBlastRadius = blastRadius;
        return this;
    }

    public float getExitDamage() { return exitDamage; }
    public float getExitBlastRadius() { return exitBlastRadius; }

    @Override
    public @NotNull MoveType<VoidShotAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        attacker.playSound(JSoundRegistry.TUSK_HEAVY_SHOT.get(), 1.0f, 0.8f);
        NailProjectile nail = NailProjectile.wormholeFromTuskAct3(attacker);
        if (nail == null) return Set.of();

        nail.setNoGravity(true);
        nail.setPos(user.position().add(0, user.getBbHeight() * 0.55, 0));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 2.125F, 0.0F);
        attacker.level().addFreshEntity(nail);

        attacker.enterVoid(nail);
        return Set.of();
    }

    @Override
    protected @NonNull VoidShotAttack getThis() {
        return this;
    }

    @Override
    public @NonNull VoidShotAttack copy() {
        VoidShotAttack copy = new VoidShotAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance());
        copy.exitDamage = this.exitDamage;
        copy.exitBlastRadius = this.exitBlastRadius;
        return copyExtras(copy);
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
