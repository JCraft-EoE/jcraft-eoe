package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JRecipeRegistry;
import net.arna.jcraft.common.recipe.CrazyDiamondRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public final class RestorationAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<RestorationAttack<A>, A> {
    /**
     * Creates a new simple attack with a single hitbox.
     *
     * @param cooldown     The cooldown for this attack in ticks.
     * @param windup       The windup of this attack in ticks. How long until the blow is landed.
     * @param duration     The duration after which a new attack can be initiated in ticks.
     * @param moveDistance The distance at which the hitbox is placed.
     * @param damage       The damage this attack deals.
     * @param hitboxSize   The size of the hitbox in blocks.
     * @param knockback    The strength of the knock-back.
     * @param offset       The amount the hitbox is offset by.
     */
    public RestorationAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                             final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    /**
     * For light attacks
     *
     * @param windup       The windup of this attack in ticks. How long until the blow is landed.
     * @param duration     The duration after which a new attack can be initiated in ticks.
     * @param moveDistance The distance at which the hitbox is placed.
     * @param damage       The damage this attack deals.
     * @param offset       The amount the hitbox is offset by.
     */
    public static <A extends IAttacker<? extends A, ?>> RestorationAttack<A> lightAttack(final int windup, final int duration,
                                                                                         final float moveDistance, final float damage, final int stun,
                                                                                         final float knockback, final float offset) {
        return new RestorationAttack<A>(duration + stun, windup, duration, moveDistance, damage, stun, 1.5f, knockback, offset).noLoopPrevention();
    }

    @Override
    public @NotNull MoveType<RestorationAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected void performHook(final A attacker, final Set<LivingEntity> targets, final Set<AABB> boxes, final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        if (!attacker.hasUser()) {
            return;
        }
        final LivingEntity user = attacker.getUserOrThrow();
        final Level level = user.level();
        for (final AABB box : boxes) {
            final Vec3 center = box.getCenter();
            final BlockPos pos = BlockPos.containing(center.x(), center.y(), center.z());
            final Block oldBlock = level.getBlockState(pos).getBlock();
            final Container blockContainer = new SimpleContainer(new ItemStack(oldBlock));
            final Optional<CrazyDiamondRecipe> recipe = level.getRecipeManager().getRecipeFor(JRecipeRegistry.CD_TYPE.get(), blockContainer, level);
            if (recipe.isEmpty()) {
                continue;
            }
            final ItemStack result = recipe.get().assemble(blockContainer, level.registryAccess());
            if (!(result.getItem() instanceof final BlockItem blockItem)) {
                continue;
            }
            level.setBlockAndUpdate(pos, blockItem.getBlock().defaultBlockState());
        }
    }

    @Override
    protected @NonNull RestorationAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull RestorationAttack<A> copy() {
        return copyExtras(new RestorationAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<RestorationAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<RestorationAttack<?>>, RestorationAttack<?>> buildCodec(RecordCodecBuilder.Instance<RestorationAttack<?>> instance) {
            return attackDefault(instance, RestorationAttack::new);
        }
    }
}
