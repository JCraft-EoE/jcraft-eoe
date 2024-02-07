package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Set;

public class GiveStandAttack extends AbstractSimpleAttack<GiveStandAttack, WhiteSnakeEntity> {
    public GiveStandAttack(int cooldown, int windup, int duration, float moveDistance, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, 0, stun, hitboxSize, knockback, offset);
    }

    @Override
    public boolean canBeInitiated(WhiteSnakeEntity attacker) {
        if (!attacker.hasUser())
            return false;
        return super.canBeInitiated(attacker) && attacker.getUserOrThrow().getOffHandStack().getItem() == JObjectRegistry.STAND_DISC;
    }

    @Override
    public void onInitiate(WhiteSnakeEntity attacker) {
        attacker.equipStack(EquipmentSlot.OFFHAND, attacker.getUserOrThrow().getOffHandStack());
        attacker.getUserOrThrow().equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        super.onInitiate(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(WhiteSnakeEntity attacker, LivingEntity user, MoveContext ctx) {
        ItemStack itemStack = attacker.getOffHandStack();

        super.perform(attacker, user, ctx).stream().findFirst().ifPresent(
                (target) -> {
                    StandType itemStand = null;
                    int itemSkin = 0;

                    NbtCompound data = itemStack.getOrCreateNbt();
                    StandComponent standData = JComponents.getStandData(target);

                    StandType targetStand = standData.getType();
                    if (targetStand != null) return; // Can't overwrite other's stands
                    if (data.contains("StandID", NbtElement.INT_TYPE)) itemStand = StandType.fromId(data.getInt("StandID"));
                    if (itemStand == null) return;
                    if (data.contains("Skin", NbtElement.INT_TYPE)) itemSkin = data.getInt("Skin");

                    standData.setTypeAndSkin(itemStand, itemSkin);
                    data.putInt("StandID", 0);
                    data.putInt("Skin", 0);

                    StandEntity<?, ?> stand = standData.getStand();
                    if (stand != null) stand.discard();
                    JCraft.summon(target.getWorld(), target);
                }
        );

        attacker.getUserOrThrow().equipStack(EquipmentSlot.OFFHAND, itemStack);
        attacker.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        return Set.of();
    }

    @Override
    protected @NonNull GiveStandAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GiveStandAttack copy() {
        return copyExtras(new GiveStandAttack(
                getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun(), getHitboxSize(), getKnockback(), getOffset()
        ));
    }
}
