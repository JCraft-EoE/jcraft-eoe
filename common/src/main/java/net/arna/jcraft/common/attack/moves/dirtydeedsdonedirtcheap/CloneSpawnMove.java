package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CloneSpawnMove extends AbstractMove<CloneSpawnMove, D4CEntity> {
    private CloneType cloneType = CloneType.SWORD;

    public CloneSpawnMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<CloneSpawnMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(D4CEntity attacker) {
        super.onInitiate(attacker);
        cloneType = CloneType.SWORD;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final D4CEntity attacker, final LivingEntity user) {
        final ItemStack weapon = cloneType.weapon.getDefaultInstance();
        if (weapon.isDamageableItem()) {
            weapon.setDamageValue(weapon.getMaxDamage());
        }

        if (user instanceof final ServerPlayer playerEntity) {
            final PlayerCloneEntity clone = new PlayerCloneEntity(attacker.level());
            clone.copyPosition(playerEntity);
            clone.setMaster(playerEntity);
            clone.disableDrops();
            clone.disableExperience();

            attacker.level().addFreshEntity(clone);
            clone.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            JComponentPlatformUtils.getStandComponent(clone).setType(JStandTypeRegistry.NONE.get());
        } else if (user instanceof final Mob mob) { //Code sourced from MobEntity.class convertTo()
            final EntityType<?> entityType = mob.getType();
            final Mob newMob = (Mob) entityType.create(attacker.level());

            if (newMob == null) {
                JCraft.LOGGER.error("Failed to create D4C clone mob of type {} in world {}", entityType, attacker.level());
                return Set.of();
            }

            newMob.copyPosition(mob);
            newMob.setBaby(mob.isBaby());

            if (mob.hasCustomName()) {
                newMob.setCustomName(mob.getCustomName());
                newMob.setCustomNameVisible(mob.isCustomNameVisible());
            }

            newMob.tickCount = mob.tickCount;

            attacker.level().addFreshEntity(newMob);
            newMob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            JComponentPlatformUtils.getStandComponent(newMob).setType(JStandTypeRegistry.NONE.get());
        }

        return Set.of();
    }

    @Override
    public boolean onInitMove(D4CEntity attacker, MoveClass moveClass) {
        switch (moveClass) {
            case SPECIAL1 -> cloneType = CloneType.AXE;
            case SPECIAL2 -> cloneType = CloneType.BOW;
            case SPECIAL3 -> cloneType = CloneType.EMPTY;
            default -> {
                return false;
            }
        }

        return true;
    }

    @Override
    protected @NonNull CloneSpawnMove getThis() {
        return this;
    }

    @Override
    public @NonNull CloneSpawnMove copy() {
        return copyExtras(new CloneSpawnMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<CloneSpawnMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<CloneSpawnMove>, CloneSpawnMove> buildCodec(RecordCodecBuilder.Instance<CloneSpawnMove> instance) {
            return baseDefault(instance, CloneSpawnMove::new);
        }
    }

    public enum CloneType {
        SWORD(Items.IRON_SWORD),
        AXE(Items.WOODEN_AXE),
        BOW(Items.BOW),
        EMPTY(Items.AIR);

        public final Item weapon;

        CloneType(Item weapon) {
            this.weapon = weapon;
        }
    }
}
