package net.arna.jcraft.api.attack.moves;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.arna.jcraft.common.item.KnifeBundleItem;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class AbstractSpinMove<T extends AbstractSpinMove<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractMove<T, A> {

    @Getter
    private final float velocityMultiplier;

    @Getter
    private final float spreadMultiplier;

    public AbstractSpinMove(int cooldown, int windup, int duration, float moveDistance) {
        this(cooldown, windup, duration, moveDistance, 1 / 40f);
    }

    public AbstractSpinMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier) {
        this(cooldown, windup, duration, moveDistance, velocityMultiplier, 1f);
    }

    /**
     * @param velocityMultiplier what to multiply the charge time with to get the velocity
     * @param spreadMultiplier what to multiply the inaccuracy with
     */
    public AbstractSpinMove(int cooldown, int windup, int duration, float moveDistance, float velocityMultiplier, float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance);
        this.velocityMultiplier = velocityMultiplier;
        this.spreadMultiplier = spreadMultiplier;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        if (user.level().isClientSide()) {
            return Set.of();
        }
        final CommonSpinComponent spin = JComponentPlatformUtils.getSpin(user);
        final Byte slot = spin.getSlot();
        if (slot == null || (slot > 1 && !(user instanceof Player))) {
            return Set.of();
        }
        final ItemStack itemStack = slot != 0 ?
                (user instanceof final Player player ? player.getInventory().getItem(slot) : user.getMainHandItem()) :
                user.getOffhandItem();
        if (itemStack.isEmpty()) {
            return Set.of();
        }
        if (itemStack.getItem() instanceof KnifeBundleItem) {
            handleKnifeBundle(user, user.level(), itemStack, getChargeTime() * velocityMultiplier, spreadMultiplier, true, getSpawnPos(user));
        }
        else {
            final Entity thrown = JUtils.spinItem(user, user.level(), itemStack, getChargeTime() * velocityMultiplier, spreadMultiplier, true, getSpawnPos(user));
            onTossed(attacker, user, thrown);
        }
        spin.endSpinning();
        return Set.of();
    }

    /**
     * Returns the world-space position from which the item should be spawned.
     */
    protected @NonNull Vec3 getSpawnPos(@NonNull LivingEntity base) {
        return base.position().add(JUtils.getEyePos(base));
    }

    protected void handleKnifeBundle(final LivingEntity living, final Level level, final ItemStack itemStack, final float velocity, final float spreadMultiplier, final boolean decrement, final @NonNull Vec3 spawnPos) {
        // TODO do something with this later
        // JUtils.spinItem(living, level, itemStack, velocity, spreadMultiplier, decrement, spawnPos);
    }

    protected void onTossed(A attacker, LivingEntity user, @Nullable Entity thrown) {}

    @Override
    public void onCancel(A attacker) {
        if (attacker != null && attacker.getBaseEntity() != null) {
            JComponentPlatformUtils.getSpin(attacker.getBaseEntity()).cancelSpinning();
        }
    }

    public abstract static class Type<T extends AbstractSpinMove<? extends T, ?>> extends AbstractMove.Type<T> {
        protected RecordCodecBuilder<T, Float> velocityMultiplier() {
            return Codec.FLOAT.fieldOf("velocityMultiplier").forGetter(AbstractSpinMove::getVelocityMultiplier);
        }

        protected RecordCodecBuilder<T, Float> spreadMultiplier() {
            return Codec.FLOAT.fieldOf("spreadMultiplier").forGetter(AbstractSpinMove::getSpreadMultiplier);
        }
    }
}
