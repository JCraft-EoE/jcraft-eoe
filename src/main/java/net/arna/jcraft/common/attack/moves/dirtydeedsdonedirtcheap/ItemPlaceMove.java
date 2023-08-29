package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.BooleanMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.item.MockItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Set;

public class ItemPlaceMove extends AbstractMove<ItemPlaceMove, D4CEntity> {
    public static final BooleanMoveVariable PLACING_FIRST_STACK = new BooleanMoveVariable();
    public static final MoveVariable<ItemStack> PLACING = new MoveVariable<>(ItemStack.class);
    private static final List<ItemStack> placeableStacks = List.of(
            Items.STICK.getDefaultStack(),
            Items.COBBLESTONE.getDefaultStack(),
            Items.DEAD_BUSH.getDefaultStack(),
            Items.APPLE.getDefaultStack(),
            Items.OAK_SAPLING.getDefaultStack()
    );

    public ItemPlaceMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void onInitiate(D4CEntity attacker) {
        super.onInitiate(attacker);

        MoveContext ctx = attacker.getMoveContext();
        boolean placingFirstStack = ctx.getBoolean(PLACING_FIRST_STACK);
        if (placingFirstStack) ctx.set(PLACING, placeableStacks.get(attacker.getRandom().nextInt(placeableStacks.size())));

        attacker.equipStack(EquipmentSlot.OFFHAND, ctx.get(PLACING).copy());
        ctx.setBoolean(PLACING_FIRST_STACK, !placingFirstStack);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        ItemStack offHandStack = attacker.getOffHandStack();
        ItemEntity item = new ItemEntity(attacker.getWorld(), attacker.getX(), attacker.getY() + 0.2, attacker.getZ(),
                MockItem.createMockStack(ctx.get(PLACING)), 0, 0, 0);
        item.setPickupDelay(200);
        attacker.getWorld().spawnEntity(item);
        offHandStack.decrement(1);

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(PLACING_FIRST_STACK, true);
        ctx.register(PLACING);
    }

    @Override
    protected @NonNull ItemPlaceMove getThis() {
        return this;
    }

    @Override
    public @NonNull ItemPlaceMove copy() {
        return copyExtras(new ItemPlaceMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
