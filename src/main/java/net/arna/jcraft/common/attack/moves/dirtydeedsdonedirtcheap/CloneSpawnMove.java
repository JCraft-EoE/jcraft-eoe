package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;

public class CloneSpawnMove extends AbstractMove<CloneSpawnMove, D4CEntity> {
    public CloneSpawnMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        ItemStack weapon = new ItemStack(Items.IRON_SWORD);
        weapon.setDamage(249);

        if (user instanceof ServerPlayerEntity playerEntity) {
            PlayerCloneEntity clone = new PlayerCloneEntity(attacker.getWorld());
            clone.copyPositionAndRotation(playerEntity);
            clone.setMaster(playerEntity);

            attacker.getWorld().spawnEntity(clone);
            clone.equipStack(EquipmentSlot.MAINHAND, weapon);
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
        }

        return Set.of();
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
