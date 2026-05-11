package net.arna.jcraft.api.attack.moves;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractTossMove<T extends AbstractTossMove<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {

    @Getter
    private final float velocityMultiplier;

    @Getter
    private final float spreadMultiplier;

    public AbstractTossMove(int cooldown, int windup, int duration, float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    public AbstractTossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        this(cooldown, windup, duration, moveDistance, velocityMultiplier, 1f);
    }

    /**
     * @param velocityMultiplier what to multiply the charge time with to get the velocity
     * @param spreadMultiplier what to multiply the inaccuracy with
     */
    public AbstractTossMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier, float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
        this.spreadMultiplier = spreadMultiplier;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        final LivingEntity base = attacker.getBaseEntity();
        if (base.level().isClientSide()) return Set.of();
        final ItemStack itemStack = base.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) return Set.of();
        final Entity thrown = JUtils.tossItem(base, base.level(), itemStack, getChargeTime() * velocityMultiplier, spreadMultiplier, true, getSpawnPos(base));
        onTossed(attacker, user, thrown);
        return Set.of();
    }

    /**
     * Returns the world-space position from which the item should be spawned.
     * Defaults to the right-hand position derived from the D4C model's bipedHandRight bone:
     * 0.75 blocks up, 0.3125 blocks to the right, and 0.0625 blocks forward — all gravity-aware.
     */
    protected @NonNull Vec3 getSpawnPos(@NonNull LivingEntity base) {
        final Vec3 up = RotationUtil.vecPlayerToWorld(0, 12.0 / 16.0, 0, GravityChangerAPI.getGravityDirection(base));
        final Vec3 look = base.getLookAngle();
        final Vec3 right = look.cross(up.normalize());
        return base.position()
                .add(up)
                .add(right.scale(5.0 / 16.0))
                .add(look.scale(1.0 / 16.0));
    }

    protected void onTossed(A attacker, LivingEntity user, @Nullable Entity thrown) {}

    @Override
    public void onCancel(A attacker) {
        final LivingEntity base = attacker.getBaseEntity();
        Containers.dropItemStack(base.level(), base.getX(), base.getY(), base.getZ(),
                base.getItemInHand(InteractionHand.MAIN_HAND));
    }

    public abstract static class Type<T extends AbstractTossMove<? extends T, ?>> extends AbstractMove.Type<T> {
        protected RecordCodecBuilder<T, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(AbstractTossMove::getVelocityMultiplier);
        }

        protected RecordCodecBuilder<T, Float> spreadMultiplier() {
            return Codec.FLOAT.fieldOf("spreadMultiplier").forGetter(AbstractTossMove::getSpreadMultiplier);
        }
    }
}
