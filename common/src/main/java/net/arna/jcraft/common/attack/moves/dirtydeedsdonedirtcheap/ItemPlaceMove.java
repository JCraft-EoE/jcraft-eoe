package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.item.AuMockItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class ItemPlaceMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<ItemPlaceMove<A>, A> {
    private static final List<ItemStack> placeableStacks = List.of(
            Items.STICK.getDefaultInstance(),
            Items.COBBLESTONE.getDefaultInstance(),
            Items.DEAD_BUSH.getDefaultInstance(),
            Items.APPLE.getDefaultInstance(),
            Items.OAK_SAPLING.getDefaultInstance()
    );
    private boolean placingFirstStack = true;
    private ItemStack placing;

    public ItemPlaceMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull MoveType<ItemPlaceMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        if (placingFirstStack) {
            placing = placeableStacks.get(baseEntity.getRandom().nextInt(placeableStacks.size()));
        }

        baseEntity.setItemSlot(EquipmentSlot.OFFHAND, placing.copy());
        placingFirstStack = !placingFirstStack;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final ItemStack offHandStack = baseEntity.getOffhandItem();

        final ItemEntity item = new ItemEntity(baseEntity.level(), baseEntity.getX(), baseEntity.getY() + 0.2, baseEntity.getZ(),
                AuMockItem.createMockStack(placing), 0, 0, 0);
        item.setPickUpDelay(200);
        baseEntity.level().addFreshEntity(item);

        // Remove item from D4C's hand
        offHandStack.shrink(1);

        return Set.of();
    }

    @Override
    protected @NonNull ItemPlaceMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ItemPlaceMove<A> copy() {
        return copyExtras(new ItemPlaceMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static final class Type extends AbstractMove.Type<ItemPlaceMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<ItemPlaceMove<?>>, ItemPlaceMove<?>> buildCodec(final RecordCodecBuilder.Instance<ItemPlaceMove<?>> instance) {
            return baseDefault(instance, ItemPlaceMove::new);
        }
    }
}
