package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class FlamethrowerAttack<A extends IAttacker<? extends A, ?>> extends AbstractBarrageAttack<FlamethrowerAttack<A>, A> {

    private final RecipeManager.CachedCheck<Container, CampfireCookingRecipe> recipeCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public FlamethrowerAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                              final float damage, final int stun, final float hitboxSize, final float knockback,
                              final float offset, final int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NonNull MoveType<FlamethrowerAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        for (LivingEntity target : targets) {
            if (!target.isOnFire()) {
                target.setSecondsOnFire(getInterval());
            }
        }
        return targets;
    }

    @Override
    protected void performHook(final A attacker, final Set<LivingEntity> targets,
                               final Set<AABB> boxes, final DamageSource damageSource,
                               final Vec3 forwardPos, final Vec3 rotationVector) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Level level = baseEntity.level();
        if (level.isClientSide()) {
            return;
        }

        final JSplatterManager splatterManager = JSplatterManager.get(baseEntity.level());
        final Set<ItemEntity> itemEntities = new HashSet<>();
        for (final AABB box : boxes) {
            splatterManager.getIntersections(box, s -> s instanceof GasolineSplatter)
                    .forEach(s -> ((GasolineSplatter) s).lightOnFire());
            itemEntities.addAll(level.getEntitiesOfClass(ItemEntity.class, box));
        }
        for (final ItemEntity itemEntity : itemEntities) {
            final SimpleContainer container = new SimpleContainer(itemEntity.getItem());
            recipeCheck.getRecipeFor(container, level).ifPresent(recipe -> {
                final ItemStack result = recipe.getResultItem(level.registryAccess());
                final int amount = itemEntity.getItem().getCount();
                for (int i = 0; i < amount; i++) {
                    level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result.copy()));
                }
                itemEntity.discard();
            });
        }
    }

    @Override
    protected @NonNull FlamethrowerAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull FlamethrowerAttack<A> copy() {
        return copyExtras(new FlamethrowerAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<FlamethrowerAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlamethrowerAttack<?>>, FlamethrowerAttack<?>> buildCodec(RecordCodecBuilder.Instance<FlamethrowerAttack<?>> instance) {
            return barrageDefault(instance, FlamethrowerAttack::new);
        }
    }
}