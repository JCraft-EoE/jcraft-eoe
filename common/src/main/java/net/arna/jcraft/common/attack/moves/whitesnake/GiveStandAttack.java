package net.arna.jcraft.common.attack.moves.whitesnake;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class GiveStandAttack extends AbstractSimpleAttack<GiveStandAttack, WhiteSnakeEntity> {
    public GiveStandAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, 0, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<GiveStandAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean conditionsMet(final WhiteSnakeEntity attacker) {
        if (!attacker.hasUser()) {
            return false;
        }
        return super.conditionsMet(attacker) && attacker.getUserOrThrow().getOffhandItem().getItem() == JItemRegistry.STAND_DISC.get();
    }

    @Override
    public void onInitiate(final WhiteSnakeEntity attacker) {
        attacker.setItemSlot(EquipmentSlot.OFFHAND, attacker.getUserOrThrow().getOffhandItem());
        attacker.getUserOrThrow().setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        super.onInitiate(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WhiteSnakeEntity attacker, final LivingEntity user) {
        final ItemStack itemStack = attacker.getOffhandItem();
        final boolean[] consumed = {false};

        super.perform(attacker, user).stream().findFirst().ifPresent((target) -> {
            if (target.getType().is(JTagRegistry.CAN_NEVER_HAVE_STAND) || !itemStack.is(JItemRegistry.STAND_DISC.get())) {
                return;
            }

            StandType itemStand;
            int itemSkin = 0;

            CompoundTag data = itemStack.getOrCreateTag();
            CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(target);

            if (standData.getType() != null) {
                return; // Can't overwrite other's stands
            }

            itemStand = StandDiscItem.getStandType(itemStack);
            if (itemStand == null) {
                return;
            }

            if (data.contains("Skin", Tag.TAG_INT)) {
                itemSkin = data.getInt("Skin");
            }

            standData.setTypeAndSkin(itemStand, itemSkin);

            StandEntity<?, ?> stand = standData.getStand();
            if (stand != null) {
                stand.discard();
            }
            JCraft.summon(target.level(), target);
            consumed[0] = true;
        });

        attacker.getUserOrThrow().setItemSlot(EquipmentSlot.OFFHAND, consumed[0] ? ItemStack.EMPTY : itemStack);
        attacker.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
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

    public static class Type extends AbstractSimpleAttack.Type<GiveStandAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<GiveStandAttack>, GiveStandAttack> buildCodec(RecordCodecBuilder.Instance<GiveStandAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), stun(),
                    hitboxSize(), knockback(), offset()).apply(instance, applyAttackExtras(GiveStandAttack::new));
        }
    }
}
