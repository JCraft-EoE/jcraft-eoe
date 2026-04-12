package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.item.AuMockItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class ItemPlaceMove extends AbstractMove<ItemPlaceMove, D4CEntity> {
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
    public @NotNull MoveType<ItemPlaceMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(final D4CEntity attacker) {
        super.onInitiate(attacker);

        if (placingFirstStack) {
            placing = placeableStacks.get(attacker.getRandom().nextInt(placeableStacks.size()));
        }

        attacker.setItemSlot(EquipmentSlot.OFFHAND, placing.copy());
        placingFirstStack = !placingFirstStack;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final D4CEntity attacker, final LivingEntity user) {
        final ItemStack offHandStack = attacker.getOffhandItem();

        final ItemEntity item = new ItemEntity(attacker.level(), attacker.getX(), attacker.getY() + 0.2, attacker.getZ(),
                AuMockItem.createMockStack(placing), 0, 0, 0);
        item.setPickUpDelay(200);
        attacker.level().addFreshEntity(item);

        // Remove item from D4C's hand
        offHandStack.shrink(1);

        return Set.of();
    }

    @Override
    protected @NonNull ItemPlaceMove getThis() {
        return this;
    }

    @Override
    public @NonNull ItemPlaceMove copy() {
        return copyExtras(new ItemPlaceMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static final class Type extends AbstractMove.Type<ItemPlaceMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<ItemPlaceMove>, ItemPlaceMove> buildCodec(final RecordCodecBuilder.Instance<ItemPlaceMove> instance) {
            return baseDefault(instance, ItemPlaceMove::new);
        }
    }
}
