package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class SandCloneMove extends AbstractMove<SandCloneMove, TheFoolEntity> {
    public static final MoveVariable<MobEntity> SAND_CLONE = new MoveVariable<>(MobEntity.class);

    public SandCloneMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Vec3d pos = user.getEyePos();

        // Display sand effect
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeShort(11);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(2);

        for (ServerPlayerEntity sendPlayer : PlayerLookup.world((ServerWorld) attacker.getWorld())) {
            ServerChannelFeedbackPacket.send(sendPlayer, buf);
            if (sendPlayer == user) continue;
            if (sendPlayer.isInRange(user, 4)) // Blind players caught in the cloud
                sendPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));
        }

        if (user.isSneaking()) {
            for (int i = 0; i < 32; i++) {
                double y = 0.4;
                double h = i * 3.1415 / 8;
                double hDiv = 5;
                if (i >= 16) {
                    y = 0.8;
                    hDiv = 9.5;
                }
                TheFoolEntity.createFoolishSand(attacker.getWorld(), attacker.getBlockPos(),
                        new Vec3d(Math.sin(h) / hDiv, y, Math.cos(h) / hDiv));
            }
            return Set.of();
        }

        // Summon clone
        if (user instanceof ServerPlayerEntity player) {
            PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(attacker.getWorld());
            playerCloneEntity.copyPositionAndRotation(player);
            playerCloneEntity.setMaster(player);
            playerCloneEntity.markSand();
            playerCloneEntity.disableDrops();

            playerCloneEntity.equipStack(EquipmentSlot.HEAD, user.getEquippedStack(EquipmentSlot.HEAD).copy());
            playerCloneEntity.equipStack(EquipmentSlot.CHEST, user.getEquippedStack(EquipmentSlot.CHEST).copy());
            playerCloneEntity.equipStack(EquipmentSlot.LEGS, user.getEquippedStack(EquipmentSlot.LEGS).copy());
            playerCloneEntity.equipStack(EquipmentSlot.FEET, user.getEquippedStack(EquipmentSlot.FEET).copy());

            setSandClone(ctx, playerCloneEntity);
        } else if (user instanceof MobEntity mob) {
            MobEntity newMob = JUtils.mobCloneOf(mob);
            setSandClone(ctx, newMob);
        }

        attacker.getWorld().spawnEntity(ctx.get(SAND_CLONE));
        return Set.of();
    }

    public void tickClone(TheFoolEntity attacker) {
        MobEntity sandClone = attacker.getMoveContext().get(SAND_CLONE);
        if (sandClone != null && sandClone.age > 200)
            setSandClone(attacker.getMoveContext(), null);
    }

    public void discardClone(TheFoolEntity attacker) {
        MobEntity sandClone = attacker.getMoveContext().get(SAND_CLONE);
        if (sandClone != null) sandClone.discard();
    }

    private void setSandClone(MoveContext ctx, MobEntity clone) {
        MobEntity sandClone = ctx.get(SAND_CLONE);
        if (sandClone != null) sandClone.kill();
        ctx.set(SAND_CLONE, clone);
        if (clone == null) return;
        JComponents.getStandData(clone).setType(StandType.NONE);
        applySandCloneModifiers(clone);
    }

    public static void applySandCloneModifiers(LivingEntity entity) {
        if (entity == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to invalid entity!");
            return;
        }
        EntityAttributeInstance maxHealthAttribute = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            JCraft.LOGGER.error("Tried to apply sand clone attribute modifiers to entity with no max health attribute!");
            return;
        }

        maxHealthAttribute.addPersistentModifier(
                new EntityAttributeModifier("Sand Clone Max Health Modifier", -1.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
        );
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(SAND_CLONE);
    }

    @Override
    protected @NonNull SandCloneMove getThis() {
        return this;
    }

    @Override
    public @NonNull SandCloneMove copy() {
        return copyExtras(new SandCloneMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
