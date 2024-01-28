package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.IAttacker;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Getter
public class HealMove<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<HealMove<A>, A> {
    private final float health;
    private final HealTarget target;
    private final Consumer<LivingEntity> consumer;

    public HealMove(int cooldown, int windup, int duration, float moveDistance, float hitboxSize, float offset,
                    float health, HealTarget target) {
        this(cooldown, windup, duration, moveDistance, hitboxSize, offset, health, target, e -> {});
    }

    public HealMove(int cooldown, int windup, int duration, float moveDistance, float hitboxSize, float offset,
                    float health, HealTarget target, Consumer<LivingEntity> consumer) {
        super(cooldown, windup, duration, moveDistance, 0f, 0, hitboxSize, 0f, offset);
        this.health = health;
        this.target = target;
        this.consumer = consumer;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = target.pickTargets(super.perform(attacker, user, ctx), user);
        targets.forEach(e -> {
            e.heal(health);
            consumer.accept(e);
        });

        if (target == HealTarget.TARGETS && attacker.getUserOrThrow().isSneaking()) {
            ServerWorld world = (ServerWorld) user.getWorld();
            BlockHitResult hitResult = JUtils.genericBlockRaycast(world, user, 2, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);

                boolean fertilized = false;

                if (blockState.getBlock() instanceof Fertilizable fertilizable) {
                    if (fertilizable.isFertilizable(world, blockPos, blockState, false))
                        if (fertilizable.canGrow(world, world.random, blockPos, blockState)) {
                            for (int i = 0; i < 5; i++)
                                fertilizable.grow(world, world.random, blockPos, blockState);
                            fertilized = true;
                        }
                }

                if (!fertilized)
                    BoneMealItem.useOnGround(new ItemStack(Items.AIR), world, blockPos, Direction.DOWN);

                world.syncWorldEvent(1505, blockPos, 0); // Display
            }
        }

        return targets;
    }

    @Override
    protected @NonNull HealMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull HealMove<A> copy() {
        return copyExtras(new HealMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getHitboxSize(), getOffset(),
                health, target, consumer));
    }

    public enum HealTarget {
        TARGETS((targets, user) -> targets),
        USER((targets, user) -> Set.of(user));

        private final BiFunction<Set<LivingEntity>, LivingEntity, Set<LivingEntity>> targetPicker;

        HealTarget(BiFunction<Set<LivingEntity>, LivingEntity, Set<LivingEntity>> targetPicker) {
            this.targetPicker = targetPicker;
        }

        public Set<LivingEntity> pickTargets(Set<LivingEntity> targets, LivingEntity user) {
            return targetPicker.apply(targets, user);
        }
    }
}
