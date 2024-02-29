package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

public class CloneSpawnMove extends AbstractMove<CloneSpawnMove, D4CEntity> {
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

    public static final MoveVariable<CloneType> CLONE_TYPE = new MoveVariable<>(CloneType.class);

    public CloneSpawnMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public void onInitiate(D4CEntity attacker) {
        super.onInitiate(attacker);
        attacker.getMoveContext().set(CLONE_TYPE, CloneType.SWORD);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        ItemStack weapon = ctx.get(CLONE_TYPE).weapon.getDefaultStack();
        if (weapon.isDamageable())
            weapon.setDamage(weapon.getMaxDamage());

        if (user instanceof ServerPlayerEntity playerEntity) {
            PlayerCloneEntity clone = new PlayerCloneEntity(attacker.getWorld());
            clone.copyPositionAndRotation(playerEntity);
            clone.setMaster(playerEntity);
            clone.disableDrops();

            attacker.getWorld().spawnEntity(clone);
            clone.equipStack(EquipmentSlot.MAINHAND, weapon);
            JComponents.getStandData(clone).setType(StandType.NONE);
        } else if (user instanceof MobEntity mob) { //Code sourced from MobEntity.class convertTo()
            EntityType<?> entityType = mob.getType();
            MobEntity newMob = (MobEntity) entityType.create(attacker.getWorld());

            if (newMob == null) {
                JCraft.LOGGER.error("Failed to create D4C clone mob of type " + entityType + " in world " + attacker.getWorld());
                return Set.of();
            }

            newMob.copyPositionAndRotation(mob);
            newMob.setBaby(mob.isBaby());

            if (mob.hasCustomName()) {
                newMob.setCustomName(mob.getCustomName());
                newMob.setCustomNameVisible(mob.isCustomNameVisible());
            }

            newMob.age = mob.age;

            attacker.getWorld().spawnEntity(newMob);
            newMob.equipStack(EquipmentSlot.MAINHAND, weapon);
            JComponents.getStandData(newMob).setType(StandType.NONE);
        }

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CLONE_TYPE, CloneType.SWORD);
    }

    @Override
    protected @NonNull CloneSpawnMove getThis() {
        return this;
    }

    @Override
    public @NonNull CloneSpawnMove copy() {
        return copyExtras(new CloneSpawnMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
